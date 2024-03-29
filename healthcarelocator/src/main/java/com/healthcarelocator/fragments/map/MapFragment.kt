package com.healthcarelocator.fragments.map

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View.OnGenericMotionListener
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import base.fragments.IFragment
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.healthcarelocator.R
import com.healthcarelocator.custom.map.HCLMapView
import com.healthcarelocator.extensions.*
import com.healthcarelocator.model.activity.ActivityObject
import com.healthcarelocator.model.activity.AddressObject
import com.healthcarelocator.model.config.HealthCareLocatorCustomObject
import com.healthcarelocator.model.map.CurrentPositionMarker
import com.healthcarelocator.model.map.HCLMarker
import com.healthcarelocator.service.location.LocationClient
import com.healthcarelocator.state.HealthCareLocatorSDK
import com.healthcarelocator.utils.HCLConstant
import com.healthcarelocator.viewmodel.map.HCLMapViewModel
import org.osmdroid.events.MapListener
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.TilesOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider

class MapFragment : IFragment(), IMyLocationConsumer, Marker.OnMarkerClickListener,
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnCameraMoveStartedListener {

    companion object {
        fun newInstance(
                healthCareLocatorCustomObject: HealthCareLocatorCustomObject,
                activities: ArrayList<ActivityObject>, modifyZoomLevel: Float = 0f,
                boundingBox: Boolean = false
        ) =
                MapFragment().apply {
                    this.healthCareLocatorCustomObject = healthCareLocatorCustomObject
                    this.activities = activities
                    this.modifyZoomLevel = modifyZoomLevel
                    this.boundingBox = boundingBox
                }
    }

    private val viewModel by lazy { HCLMapViewModel() }
    private var healthCareLocatorCustomObject: HealthCareLocatorCustomObject =
            HealthCareLocatorSDK.getInstance().getConfiguration()
    private var activities: ArrayList<ActivityObject> = arrayListOf()
    private var modifyZoomLevel: Float = 0f
    private var boundingBox: Boolean = false
    var onMapListener: MapListener? = null

    var onMarkerSelectionChanged: (ids: ArrayList<String>) -> Unit = {}
    var onMarkerSelection: (position: String) -> Unit = {}
    private var lastItemSelected: com.google.android.gms.maps.model.Marker? = null

    // ===========================================================
    // Constants
    // ===========================================================
    private val PREFS_NAME = "org.andnav.osm.prefs"
    private val PREFS_TILE_SOURCE = "tilesource"
    private val PREFS_LATITUDE_STRING = "latitudeString"
    private val PREFS_LONGITUDE_STRING = "longitudeString"
    private val PREFS_ORIENTATION = "orientation"
    private val PREFS_ZOOM_LEVEL_DOUBLE = "zoomLevelDouble"

    // ===========================================================
    // Fields
    // ===========================================================
    private var mPrefs: SharedPreferences? = null
    private var mMapView: HCLMapView? = null
    private val oneKeyMarkers by lazy { arrayListOf<HCLMarker>() }
    private var lastCurrentLocation: Location? = null
    private var mRotationGestureOverlay: RotationGestureOverlay? = null
    private var mCopyrightOverlay: CopyrightOverlay? = null
    private lateinit var selectedIcon: Drawable
    private var locationProvider: GpsMyLocationProvider? = null
    private var groupMap = hashMapOf<String, ArrayList<ActivityObject>>()

    /**
     * Variables for google map
     */
    private var selectedMarkerBitMap: BitmapDescriptor? = null
    private var markerBitMap: BitmapDescriptor? = null
    private var googleMap: GoogleMap? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        if (healthCareLocatorCustomObject.mapService == MapService.OSM) {
            mMapView = HCLMapView(inflater.context)
            mMapView!!.setDestroyMode(false)
            mMapView!!.tag = "mapView" // needed for OpenStreetMapViewTest
            mMapView!!.setOnGenericMotionListener(OnGenericMotionListener { v, event ->
                if (0 != event.source and InputDevice.SOURCE_CLASS_POINTER) {
                    when (event.action) {
                        MotionEvent.ACTION_SCROLL -> {
                            if (event.getAxisValue(MotionEvent.AXIS_VSCROLL) < 0.0f) mMapView!!.controller.zoomOut() else {
                                //this part just centers the map on the current mouse location before the zoom action occurs
                                val iGeoPoint =
                                        mMapView!!.projection.fromPixels(
                                                event.x.toInt(),
                                                event.y.toInt()
                                        )
                                mMapView!!.controller.animateTo(iGeoPoint)
                                mMapView!!.controller.zoomIn()
                            }
                            return@OnGenericMotionListener true
                        }
                    }
                }
                false
            })
            return mMapView
        } else {
            return inflater.inflate(R.layout.fragment_one_key_google_map, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.bindView(this)
        if (healthCareLocatorCustomObject.mapService == MapService.OSM) {
            if (savedInstanceState != null) {
                boundingBox = savedInstanceState.getBoolean("boundingBox", false)
                val list =
                        savedInstanceState.getParcelableArrayList<ActivityObject>(HCLConstant.locations)
                if (!list.isNullOrEmpty())
                    activities = list
            }
            drawMarkerOnMap(activities)
        } else if (healthCareLocatorCustomObject.mapService == MapService.GOOGLE_MAP) {
            activity?.isGooglePlayServiceAvailable({
                (childFragmentManager.findFragmentById(R.id.googleMapView)
                        as? SupportMapFragment)?.getMapAsync(this)
            }, { })
        }

//        mMapView?.overlays?.addAll(oneKeyMarkers)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(HCLConstant.locations, activities)
        outState.putBoolean("boundingBox", boundingBox)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val context: Context? = this.activity
        locationProvider = GpsMyLocationProvider(context)
        locationProvider!!.startLocationProvider(this)
        if (healthCareLocatorCustomObject.mapService == MapService.OSM) {
            onMapListener?.let { mMapView?.addMapListener(it) }
            mPrefs = context!!.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            updateCurrentLocationOSM()
            mRotationGestureOverlay = RotationGestureOverlay(mMapView)
            mRotationGestureOverlay!!.isEnabled = false
            mMapView!!.overlays.add(mRotationGestureOverlay)
            mMapView!!.mapOrientation = 0f
            mMapView!!.setMultiTouchControls(true)
            mMapView!!.isTilesScaledToDpi = true
            if (healthCareLocatorCustomObject.darkModeForMap)
                mMapView!!.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
        }
    }

    override fun onResume() {
        super.onResume()
        if (healthCareLocatorCustomObject.mapService == MapService.OSM) {
            try {
                mMapView!!.setTileSource(TileSourceFactory.WIKIMEDIA)
            } catch (e: IllegalArgumentException) {
                mMapView!!.setTileSource(TileSourceFactory.WIKIMEDIA)
            }
            mMapView?.onResume()
        }
    }

    override fun onPause() {
        if (healthCareLocatorCustomObject.mapService == MapService.OSM) {
            mPrefs?.edit {
                putFloat(PREFS_ORIENTATION, mMapView!!.mapOrientation)
                putString(PREFS_LATITUDE_STRING, "${mMapView!!.mapCenter.latitude}")
                putString(PREFS_LONGITUDE_STRING, "${mMapView!!.mapCenter.longitude}")
                if (modifyZoomLevel == 0f)
                    putFloat(PREFS_ZOOM_LEVEL_DOUBLE, mMapView!!.zoomLevelDouble.toFloat())
            }
            mMapView?.onPause()
        }
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.unbindView()
        mMapView?.onDetach()
        locationProvider?.stopLocationProvider()
    }

    override fun onLocationChanged(location: Location?, source: IMyLocationProvider?) {
        this.lastCurrentLocation = location
    }

    override fun onMarkerClick(marker: Marker?, mapView: org.osmdroid.views.MapView?): Boolean {
        if (mapView == null || mapView.overlays.isNullOrEmpty())
            return true
        marker?.let {
            validateMarker(marker)
            if (marker is HCLMarker && marker.groupSize > 1) {
                val obj = groupMap[marker.getLocationInString()]
                if (obj.isNullable())
                    onMarkerSelectionChanged(arrayListOf(marker.id))
                else {
                    val ids = arrayListOf<String>()
                    obj!!.forEach {
                        ids.add(it.id)
                    }
                    onMarkerSelectionChanged(ids)
                }
            } else {
                onMarkerSelectionChanged(arrayListOf(marker.id))
            }
        }
        return true
    }

    fun updateCurrentLocationOSM() {
        val client = LocationClient(context!!)
        client.requestLastLocation().registerDataCallBack({ currentLocation ->
            client.removeLocationUpdate()
            client.releaseApiClient()
            mMapView?.apply {
                val filtered = overlays?.filterIsInstance<CurrentPositionMarker>()
                        ?: listOf()
                overlays.removeAll(filtered)
                try {
                    overlays?.add(CurrentPositionMarker(mMapView).apply {
                        setInfoWindow(null)
                        position = GeoPoint(currentLocation.latitude, currentLocation.longitude)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        icon = ContextCompat.getDrawable(context!!, R.drawable.ic_current_location)
                    })
                } catch (e: Exception) {

                }
            }
        }, {}, {})
    }

    fun drawMarkerOnMap(
            activities: ArrayList<ActivityObject>, moveCamera: Boolean = false,
            isNearMe: Boolean = false
    ) {
        if (healthCareLocatorCustomObject.mapService == MapService.OSM) {
            mMapView?.apply {
                val clustersFiltered = overlays?.filterIsInstance<HCLMarker>()
                        ?: listOf()
                overlays.removeAll(clustersFiltered)
                oneKeyMarkers.clear()
                if (activities.isEmpty()) return
                selectedIcon = context!!.getDrawableFilledIcon(
                        R.drawable.ic_location_on_white_36dp,
                        healthCareLocatorCustomObject.colorMarkerSelected.getColor()
                )!!
                viewModel.groupLocations(activities) { map ->
                    if (map.size == 0) return@groupLocations
                    groupMap = map
                    map.forEach { obj ->
                        val list = obj.value
                        list.forEach {
                            val marker = HCLMarker(mMapView).apply {
                                id = it.id
                                setOnMarkerClickListener(this@MapFragment)
                                val location = it.workplace?.address?.location?.getGeoPoint()
                                        ?: GeoPoint(0.0, 0.0)
                                groupSize = list.size
                                position = location
                                setAnchor(Marker.ANCHOR_CENTER, 1f)
                                icon = context!!.getDrawableFilledIcon(
                                        R.drawable.baseline_location_on_black_36dp,
                                        healthCareLocatorCustomObject.colorMarker.getColor(), groupSize
                                )
                                title = it.workplace?.address?.getAddress() ?: ""
                            }
                            oneKeyMarkers.add(marker)
                            mMapView?.overlays?.add(marker)
                        }
                    }
                    if (moveCamera && activities.size == 1) {
                        val position = activities[0].workplace?.address?.location?.getGeoPoint()
                                ?: GeoPoint(0.0, 0.0)
                        controller.setCenter(position)
                        controller.animateTo(position, 15.0, 0)
                    }
                    if (oneKeyMarkers.isEmpty()) return@groupLocations
                    if (!isNearMe)
                        viewModel.getOSMBoundLevel(this, oneKeyMarkers)
                    else viewModel.getOSMBoundNearMeLevel(
                            parentFragment!!.parentFragment!!::class.java.simpleName,
                            context, this, oneKeyMarkers
                    )
                }
            }
        } else {
            this.activities = activities
            if (googleMap == null) return
            lastItemSelected = null
            googleMap!!.clear()
            val boundBuilder = LatLngBounds.builder()
            val markers = arrayListOf<HCLMarker>()
            viewModel.groupLocations(activities) { map ->
                if (map.size == 0) return@groupLocations
                map.forEach { obj ->
                    val location = obj.key.getLatLng().run { LatLng(this[0], this[1]) }
                    boundBuilder.include(location)
                    val marker = googleMap!!.addMarker(
                            MarkerOptions().icon(markerBitMap)
                                    .position(location)
                    )
                    marker.tag = obj.key
                    markers.add(HCLMarker(MapView(context)).apply {
                        position = GeoPoint(location.latitude, location.longitude)
                    })
                }
                if (markers.isEmpty()) return@groupLocations
                if (!isNearMe)
                    viewModel.getGoogleMapBoundLevel(googleMap!!, markers)
                else viewModel.getGoogleMapBoundNearMeLevel(context!!, googleMap!!, markers)
            }
//            activities.forEach { activity ->
//                val location = activity.workplace?.address?.location?.getLatLng()
//                    ?: LatLng(0.0, 0.0)
//                boundBuilder.include(location)
//                val marker = googleMap!!.addMarker(
//                    MarkerOptions().icon(markerBitMap)
//                        .position(location)
//                )
//                marker.tag = activity.id
//                markers.add(OneKeyMarker(MapView(context)).apply {
//                    position = GeoPoint(location.latitude, location.longitude)
//                })
//            }
//            if (markers.isEmpty()) return
//            if (!isNearMe)
//                viewModel.getGoogleMapBoundLevel(googleMap!!, markers)
//            else viewModel.getGoogleMapBoundNearMeLevel(context!!, googleMap!!, markers)
//            boundLocations(boundBuilder)
        }

    }

    fun drawAddressOnMap(listOfAddress: ArrayList<AddressObject>, moveCamera: Boolean = false) {

        if (healthCareLocatorCustomObject.mapService == MapService.OSM) {
            mMapView?.apply {
                val clustersFiltered = overlays?.filterIsInstance<HCLMarker>()
                        ?: listOf()
                overlays.removeAll(clustersFiltered)
                oneKeyMarkers.clear()
                if (listOfAddress.isEmpty()) return
                selectedIcon = context!!.getDrawableFilledIcon(
                        R.drawable.ic_location_on_white_36dp,
                        healthCareLocatorCustomObject.colorMarkerSelected.getColor()
                )!!
                listOfAddress.forEach { address ->
                    val marker = HCLMarker(mMapView).apply {
                        id = address.activityId
                        setOnMarkerClickListener(this@MapFragment)
                        val location = address.location?.getGeoPoint()
                                ?: GeoPoint(0.0, 0.0)
                        position = location
                        setAnchor(Marker.ANCHOR_CENTER, 1f)
                        icon = context!!.getDrawableFilledIcon(
                                R.drawable.baseline_location_on_black_36dp,
                                healthCareLocatorCustomObject.colorMarker.getColor()
                        )
                        title = address.getAddress() ?: ""
                    }
                    mMapView?.overlays?.add(marker)
                    oneKeyMarkers.add(marker)
                }
                if (moveCamera && listOfAddress.size == 1) {
                    val position = listOfAddress[0].location?.getGeoPoint()
                            ?: GeoPoint(0.0, 0.0)
                    controller.setCenter(position)
                    controller.animateTo(position, 15.0, 0)
                }
            }
        } else if (healthCareLocatorCustomObject.mapService == MapService.GOOGLE_MAP) {
            googleMap?.also { googleMap ->
                googleMap.clear()
                var location: LatLng = LatLng(0.0, 0.0)
                listOfAddress.forEach { address ->
                    location = address.location?.getLatLng() ?: LatLng(0.0, 0.0)
                    val marker = googleMap.addMarker(
                            MarkerOptions().icon(markerBitMap)
                                    .position(location)
                    )
                    marker.tag = address.activityId
                }
                animateCameraGoogleMap(CameraUpdateFactory.newLatLngZoom(location, 13f))
            }
        }
    }

    fun moveToPosition(position: GeoPoint) {
        mMapView?.apply {
            controller.setCenter(position)
            controller.animateTo(position, 13.0, 2000)
        }
    }

    private fun animateCameraGoogleMap(update: CameraUpdate) {
        googleMap?.animateCamera(update)
    }

    private fun validateMarker(marker: Marker) {
        marker.id
        if (mMapView == null) return
        mMapView?.controller?.apply {
            setCenter(marker.position)
            animateTo(marker.position, mMapView!!.zoomLevelDouble.toDouble(), 2000)
        }
        oneKeyMarkers.filter { oneKeyMarker -> oneKeyMarker.selected }
                .mapIndexed { _, oneKeyMarker ->
                    val lastIndexOfOverLay = mMapView!!.overlays.indexOf(oneKeyMarker)
                    oneKeyMarker.icon = context!!.getDrawableFilledIcon(
                            R.drawable.baseline_location_on_black_36dp,
                            healthCareLocatorCustomObject.colorMarker.getColor(), oneKeyMarker.groupSize
                    )
                    oneKeyMarker.selected = false
                    if (lastIndexOfOverLay >= 0) {
                        mMapView!!.overlays[lastIndexOfOverLay] = oneKeyMarker
                    }
                }
        if (mMapView?.overlays.isNullOrEmpty()) return

        val indexOfOverLay = mMapView!!.overlays.indexOf(marker)
        val index = oneKeyMarkers.indexOf(marker)
        if (indexOfOverLay in 0 until mMapView!!.overlays.size) {
            if (index >= 0) {
                (marker as? HCLMarker)?.apply {
                    marker.icon = context!!.getDrawableFilledIcon(
                            R.drawable.ic_location_on_white_36dp,
                            healthCareLocatorCustomObject.colorMarkerSelected.getColor(),
                            marker.groupSize
                    )
                    selected = true
                    oneKeyMarkers[index] = this
                }
                mMapView?.overlays?.removeAt(indexOfOverLay)
                mMapView?.overlays?.add(oneKeyMarkers[index])
            }
        }
    }

    fun getCenter(callback: (lat: Double, lng: Double, distance: Double) -> Unit) {
        if (healthCareLocatorCustomObject.mapService == MapService.OSM) {
            val center = mMapView?.mapCenter
            val boundingBox = mMapView?.boundingBox
            val distance = boundingBox?.run {
                val d = getDistanceFromBoundingBox(latNorth, lonEast, latSouth, lonWest)
                return@run if (d < 2000.0) 2000.0 else d
            } ?: 2000.0
            callback(center?.latitude ?: 0.0, center?.longitude ?: 0.0, distance)
        } else if (healthCareLocatorCustomObject.mapService == MapService.GOOGLE_MAP && googleMap.isNotNullable()) {
            val center = googleMap!!.cameraPosition?.target
            val boundingBox: LatLngBounds = googleMap!!.projection.visibleRegion.latLngBounds
            val d = getDistanceFromTwoCoordinates(boundingBox.northeast, boundingBox.southwest)
            val distance = if (d < 2000.0) 2000.0 else d
            callback(center?.latitude ?: 0.0, center?.longitude ?: 0.0, distance)
        }
    }

    fun moveToCurrentLocation(
            forcedZoom: Boolean = false,
            callback: (lat: Double, lng: Double) -> Unit = { _, _ -> }
    ) {
        locationProvider?.lastKnownLocation?.also { location ->
            if (healthCareLocatorCustomObject.mapService == MapService.OSM) {
                updateCurrentLocationOSM()
                mMapView?.apply {
                    val position = GeoPoint(location.latitude, location.longitude)
                    callback(location.latitude, location.longitude)
                    controller.setCenter(position)
                    controller.animateTo(position, if (forcedZoom) 15.0 else zoomLevelDouble, 2000)
                }
            } else {
                callback(location.latitude, location.longitude)
                moveToPosition(location.getLatLng())
            }
        }
    }

    fun getLastLocation(): GeoPoint? =
            locationProvider?.lastKnownLocation?.run { GeoPoint(latitude, longitude) }

    /**
     * ================================
     * Functions handle Google map view
     * ================================
     */
    @Throws(SecurityException::class)
    override fun onMapReady(p0: GoogleMap?) {
        if (p0.isNullable()) return
        this.googleMap = p0
        lastItemSelected = null
        googleMap?.apply {
            if (healthCareLocatorCustomObject.darkModeForMap)
                setMapStyle(MapStyleOptions.loadRawResourceStyle(context!!, R.raw.google_map_night))
            isMyLocationEnabled = true
            uiSettings.isMyLocationButtonEnabled = false
            selectedMarkerBitMap = context!!.getDrawableFilledIcon(
                    healthCareLocatorCustomObject.markerIcon,
                    healthCareLocatorCustomObject.colorMarkerSelected.getColor()
            ).getBitmapDescriptor()
            markerBitMap = context!!.getDrawableFilledIcon(
                    healthCareLocatorCustomObject.markerIcon,
                    healthCareLocatorCustomObject.colorMarker.getColor()
            ).getBitmapDescriptor()
            setOnMarkerClickListener(this@MapFragment)
            setOnCameraMoveStartedListener(this@MapFragment)
            drawMarkerOnMap(activities)
        }
    }

    override fun onMarkerClick(p0: com.google.android.gms.maps.model.Marker?): Boolean {
        Log.d("onMarkerClick", "lastItemSelected: ${lastItemSelected?.position?.getString()}")
        Log.d("onMarkerClick", "Marker: ${p0?.position?.getString()}")
        p0?.also { marker ->
            if (lastItemSelected != null) {
                lastItemSelected!!.setIcon(markerBitMap)
            }
            lastItemSelected = marker
            lastItemSelected?.setIcon(selectedMarkerBitMap)
            onMarkerSelection((marker.tag as? String) ?: "")
        }
        return false
    }

    private fun moveToPosition(latLng: LatLng) {
        googleMap?.apply {
            animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, cameraPosition.zoom))
        }
    }

    override fun onCameraMoveStarted(p0: Int) {
        if (p0 == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            (parentFragment as? HCLMapResultFragment)?.requestRelaunch()
        }
    }
}