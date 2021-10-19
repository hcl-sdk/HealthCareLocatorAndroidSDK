package com.healthcarelocator.fragments.map

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import base.fragments.IFragment
import com.google.gson.Gson
import com.healthcarelocator.R
import com.healthcarelocator.adapter.search.SearchAdapter
import com.healthcarelocator.custom.CenterLayoutManager
import com.healthcarelocator.extensions.*
import com.healthcarelocator.model.activity.ActivityObject
import com.healthcarelocator.model.config.HealthCareLocatorCustomObject
import com.healthcarelocator.model.map.HCLPlace
import com.healthcarelocator.state.HealthCareLocatorSDK
import kotlinx.android.synthetic.main.fragment_map_result.*
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent


class HCLMapResultFragment : IFragment(), View.OnClickListener, MapListener {

    companion object {
        fun newInstance(speciality: String = "", activities: ArrayList<ActivityObject>) = HCLMapResultFragment().apply {
            this.speciality = speciality
            this.activities = activities
        }
    }

    private var healthCareLocatorCustomObject: HealthCareLocatorCustomObject = HealthCareLocatorSDK.getInstance().getConfiguration()
    private val mapFragmentTag: String = StarterMapFragment::class.java.name
    private val mapFragment by lazy {
        MapFragment.newInstance(healthCareLocatorCustomObject, activities, 0f, true)
    }
    private var isRelaunch = false
    private var activities: ArrayList<ActivityObject> = arrayListOf()
    private var speciality: String = ""
    private val searchAdapter by lazy { SearchAdapter(getScreenWidth(), speciality)}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fm = this@HCLMapResultFragment.childFragmentManager
        if (fm.findFragmentByTag(mapFragmentTag) == null && savedInstanceState == null) {
            fm.beginTransaction().add(R.id.mapContainer, mapFragment, mapFragmentTag)
                    .commit()
        }
        mapFragment.onMapListener = this
        isRelaunch = getAbsFragment()?.getRelaunchState() ?: false
        rvLocations.apply {
            layoutManager = CenterLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = searchAdapter
            showDistance()
            searchAdapter.setData(activities)
        }
        rvLocations.postDelay({ rv ->
            getRunningMapFragment()?.drawMarkerOnMap(activities, false,
                    getAbsFragment()?.isNearMe() ?: false)
            getRunningMapFragment()?.apply {
                onMarkerSelectionChanged = { ids ->
                    val selectedPosition = activities.getIndexes {
                        ids.indexOf(it.id) >= 0
                    }
                    if (selectedPosition.isNotEmpty()) {
                        rv?.execute { it.smoothScrollToPosition(selectedPosition.first()) }
                        searchAdapter.setSelectedPosition(selectedPosition)
                        setHighLight(selectedPosition)
                    }
                }
                onMarkerSelection = { position ->
                    val selectedPosition = activities.getIndexes {
                        (it.workplace?.address?.location?.getLocationByString() ?: "") == position
                    }
                    if (selectedPosition.isNotEmpty()) {
                        rv.smoothScrollToPosition(selectedPosition.first())
                        searchAdapter.setSelectedPosition(selectedPosition)
                        setHighLight(selectedPosition)
                    }
                }
            }
        }, 1000L)
        searchAdapter.onHCPCardClickedListener = { oneKeyLocation ->
            if (parentFragment is FullMapFragment) (parentFragment as FullMapFragment).navigateToHCPProfile(oneKeyLocation)
            else if (parentFragment is HCLNearMeFragment) (parentFragment as HCLNearMeFragment).navigateToHCPProfile(oneKeyLocation)
        }
        showRelaunch(isRelaunch)
        btnRelaunch.setOnClickListener(this)
        btnCurrentLocation.setOnClickListener(this)
        btnRelaunch.setRippleBackground(healthCareLocatorCustomObject.colorSecondary.getColor(), 50f)
        btnCurrentLocation.setIconFromDrawableId(healthCareLocatorCustomObject.iconMapGeoLoc)
        healthCareLocatorCustomObject.also {
            btnCurrentLocation.setColorFilter(if (it.darkMode) Color.WHITE else ContextCompat.getColor(context!!, R.color.colorOneKeyText))
            btnCurrentLocation.background = ContextCompat.getDrawable(context!!,
                    if (it.darkMode) R.drawable.bg_black_circle_border else R.drawable.bg_white_circle_border)
        }
    }

    private fun setHighLight(lisSelection: ArrayList<Int>) {
        val listSelection = arrayListOf<String>()
        for (i in lisSelection.indices) {
            listSelection.add(activities[lisSelection[i]].id)
        }
        sharedPreferences.edit {
            putBoolean(isLocationSelection, true)
            putString(locationSelection, Gson().toJson(listSelection))
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnCurrentLocation -> {
                showLoading(true)
                getRunningMapFragment()?.moveToCurrentLocation() { lat, lng ->
                    getAbsFragment()?.also {
                        it.setNearMeState(true)
                        it.forceSearch(HCLPlace(context!!, lat, lng), -1.0)
                    }
                }
            }
            R.id.btnRelaunch -> {
                animateRelaunch(true)
                getRunningMapFragment()?.getCenter() { lat, lng, distance ->
                    getAbsFragment()?.also {
                        it.setNearMeState(false)
                        it.reverseGeoCoding(HCLPlace(context!!, lat, lng), distance)
                    }
                }
            }
        }
    }

    override fun onScroll(event: ScrollEvent?): Boolean {
        if (event != null && event.x != 0 && event.y != 0) {
            requestRelaunch()
        }
        return true
    }

    override fun onZoom(event: ZoomEvent?): Boolean {
        return true
    }

    private fun getRunningMapFragment(): MapFragment? {
        return if (isAdded) childFragmentManager.fragments.getFragmentBy {
            it::class.java.simpleName == MapFragment::class.java.simpleName
        } as? MapFragment
        else null

    }

    override fun onDestroyView() {
        searchAdapter.reset()
        searchAdapter.notifyDataSetChanged()
        super.onDestroyView()
    }

    fun requestRelaunch() {
        isRelaunch = true
        getAbsFragment()?.setRelaunchState(true)
        showRelaunch(isRelaunch)
    }

    fun updateActivities(activities: ArrayList<ActivityObject>) {
        showDistance()
        showLoading(false)
        animateRelaunch(false)
        this.activities = activities
        searchAdapter.setData(activities)
        getRunningMapFragment()?.let {
            it.drawMarkerOnMap(activities, false, getAbsFragment()?.isNearMe() ?: false)
        }
    }

    private fun showLoading(state: Boolean) {
        loadingCurrentLocation.visibility = state.getVisibility()
    }

    private fun animateRelaunch(state: Boolean) {
        if (state) {
            showRelaunch(true)
            btnRelaunch.isEnabled = false
            context?.also {
                ivRelaunch.startAnimation(AnimationUtils.loadAnimation(it,
                        R.anim.onekey_sdk_rotate).apply { fillAfter = true })
            }
        } else {
            btnRelaunch.isEnabled = true
            ivRelaunch.clearAnimation()
            ivRelaunch.animate().cancel()
            getAbsFragment()?.setRelaunchState(false)
            showRelaunch(state)
        }
    }

    private fun showRelaunch(state: Boolean) {
        btnRelaunch.visibility = state.getVisibility()
    }

    private fun showDistance() {
        searchAdapter.isPlaceAvailable = getAbsFragment()?.getPlaceDetail()?.placeId?.isNotNullAndEmpty()
                ?: false
    }

    private fun getAbsFragment(): AbsMapFragment<*, *>? = parentFragment as? AbsMapFragment<*, *>
}