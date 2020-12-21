package com.ekino.onekeysdk.fragments

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import base.extensions.addFragment
import base.fragments.AppFragment
import com.ekino.onekeysdk.R
import com.ekino.onekeysdk.adapter.home.LastConsultedAdapter
import com.ekino.onekeysdk.adapter.home.LastSearchAdapter
import com.ekino.onekeysdk.extensions.*
import com.ekino.onekeysdk.fragments.map.FullMapFragment
import com.ekino.onekeysdk.fragments.map.MapFragment
import com.ekino.onekeysdk.fragments.map.NearMeFragment
import com.ekino.onekeysdk.fragments.map.StarterMapFragment
import com.ekino.onekeysdk.fragments.profile.OneKeyProfileFragment
import com.ekino.onekeysdk.fragments.search.SearchFragment
import com.ekino.onekeysdk.model.OneKeyLocation
import com.ekino.onekeysdk.model.config.OneKeyViewCustomObject
import com.ekino.onekeysdk.model.map.OneKeyPlace
import com.ekino.onekeysdk.viewmodel.home.OneKeyHomFullViewModel
import kotlinx.android.synthetic.main.fragment_one_key_home_full.*
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider

class OneKeyHomeFullFragment : AppFragment<OneKeyHomeFullFragment,
        OneKeyHomFullViewModel>(R.layout.fragment_one_key_home_full), View.OnClickListener, IMyLocationConsumer {
    companion object {
        fun newInstance(oneKeyViewCustomObject: OneKeyViewCustomObject = OneKeyViewCustomObject.Builder().build()) =
                OneKeyHomeFullFragment().apply {
                    this.oneKeyViewCustomObject = oneKeyViewCustomObject
                }
    }

    private val locations by lazy { getDummyHCP() }
    private var locationProvider: GpsMyLocationProvider? = null
    private var currentLocation: Location? = null

    private var searchTag = 0
    private var consultedTag = 0

    private val mapFragmentTag: String = StarterMapFragment::class.java.name
    private val mapFragment by lazy { MapFragment.newInstance(oneKeyViewCustomObject, arrayListOf()) }

    private var oneKeyViewCustomObject: OneKeyViewCustomObject = ThemeExtension.getInstance().getThemeConfiguration()
    private val lastSearchAdapter by lazy { LastSearchAdapter(oneKeyViewCustomObject) }
    private val lastConsultedAdapter by lazy { LastConsultedAdapter(oneKeyViewCustomObject) }

    override val viewModel = OneKeyHomFullViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            org.osmdroid.config.Configuration.getInstance().load(
                    context, context!!.getSharedPreferences("OneKeySDK", Context.MODE_PRIVATE))
        } catch (e: Exception) {
            //
        }
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        var lastSearches = ArrayList(locations.take(3))
        var lastConsulted = ArrayList(locations.take(3))
        if (savedInstanceState != null) {
            searchTag = savedInstanceState.getInt("lastSearchTag", 0)
            consultedTag = savedInstanceState.getInt("lastConsultedTag", 0)
            lastSearches = savedInstanceState.getParcelableArrayList("lastSearches")
                    ?: ArrayList()
            lastConsulted = savedInstanceState.getParcelableArrayList("lastConsulted")
                    ?: ArrayList()
        }
        val fm = this@OneKeyHomeFullFragment.childFragmentManager
        if (fm.findFragmentByTag(mapFragmentTag) == null && savedInstanceState == null) {
            fm.beginTransaction().add(R.id.nearMeMap, mapFragment, mapFragmentTag)
                    .commit()
        }
        viewModel.apply {
            requestPermissions(this@OneKeyHomeFullFragment)
            permissionGranted.observe(this@OneKeyHomeFullFragment, Observer { granted ->
                if (granted) {
                    if (locationProvider == null) {
                        locationProvider = GpsMyLocationProvider(context)
                    }
                    locationProvider?.startLocationProvider(this@OneKeyHomeFullFragment)
                }
            })
            activities.observe(this@OneKeyHomeFullFragment, Observer { list ->
                getRunningMapFragment()?.drawMarkerOnMap(list, true)
            })
            loading.observe(this@OneKeyHomeFullFragment, Observer { state ->
                showNearMeLoading(state)
            })
        }

        viewMoreSearches.text = getViewTagText(searchTag)
        viewMoreConsulted.text = getViewTagText(consultedTag)

        oneKeyViewCustomObject.also {
            ivSearch.setRippleBackground(it.colorPrimary.getColor(), 15f)
            viewMoreSearches.setTextColor(it.colorPrimary.getColor())
            viewMoreConsulted.setTextColor(it.colorPrimary.getColor())
            edtSearch.textSize = it.fontSearchInput.size.toFloat()
        }

        newSearchWrapper.setOnClickListener(this)
        viewMoreSearches.setOnClickListener(this)
        viewMoreConsulted.setOnClickListener(this)
        mapOverlay.setOnClickListener(this)
        initLastSearch(lastSearches, lastConsulted)

        context?.getSharedPreferences("OneKeySDK", Context.MODE_PRIVATE)?.also { pref ->
            viewModel.apply {
                getConsultedProfiles(pref)
                consultedProfiles.observe(this@OneKeyHomeFullFragment, Observer {
                    checkViewMoreConsulted(it.size)
                    lastConsultedAdapter.setData(it.take(if (consultedTag == 0) 3 else 10).toArrayList())
                })
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList("lastSearches", lastSearchAdapter.getData())
        outState.putParcelableArrayList("lastConsulted", lastSearchAdapter.getData())
        outState.putInt("lastSearchTag", searchTag)
        outState.putInt("lastConsultedTag", consultedTag)
    }

    override fun onResume() {
        super.onResume()
        FullMapFragment.clear()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.viewMoreSearches -> {
                if (searchTag == 0) {
                    lastSearchAdapter.addList(lastSearchAdapter.itemCount, ArrayList(locations.takeLast(7)))
                    searchTag = 1
                    viewMoreSearches.text = getViewTagText(1)
                } else {
                    lastSearchAdapter.removeRange(3, lastSearchAdapter.itemCount)
                    searchTag = 0
                    viewMoreSearches.text = getViewTagText(0)
                }
            }
            R.id.viewMoreConsulted -> {
                if (consultedTag == 0) {
                    val list = viewModel.consultedProfiles.value ?: arrayListOf()
                    lastConsultedAdapter.addList(lastConsultedAdapter.itemCount,
                            ArrayList(if (list.size >= 10) list.takeLast(7) else list.takeLast(list.size - 3)))
                    consultedTag = 1
                    viewMoreConsulted.text = getViewTagText(1)
                } else {
                    lastConsultedAdapter.removeRange(3, lastConsultedAdapter.itemCount)
                    consultedTag = 0
                    viewMoreConsulted.text = getViewTagText(0)
                }
            }
            R.id.newSearchWrapper -> startNewSearch()
            R.id.mapOverlay -> {
                currentLocation?.also {
                    (activity as? AppCompatActivity)?.addFragment(R.id.fragmentContainer,
                            NearMeFragment.newInstance(oneKeyViewCustomObject, "", null,
                                    OneKeyPlace(placeId = "near_me", latitude = "${it.latitude}",
                                            longitude = "${it.longitude}", displayName = "Near me"),
                                    oneKeyViewCustomObject.favoriteIds, currentLocation), true)
                }
            }
        }
    }

    private fun initLastSearch(lastSearches: ArrayList<OneKeyLocation>, lastConsulted: ArrayList<OneKeyLocation>) {
        rvLastSearch.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = lastSearchAdapter
            lastSearchAdapter.setData(lastSearches)
        }
        lastSearchAdapter.onItemRemovedListener = {
            if (lastSearchAdapter.getData().isEmpty())
                lastSearchWrapper.visibility = View.GONE
        }
        lastSearchAdapter.onItemClickedListener = { location ->
//            if (location.isHCP)
//                oneKeyViewCustomObject.also {
//                    (activity as? AppCompatActivity)?.addFragment(R.id.fragmentContainer,
//                            OneKeyProfileFragment.newInstance(it, location), true)
//                }
        }
        lastConsultedAdapter.onItemRemovedListener = { data, position ->
            context?.getSharedPreferences("OneKeySDK", Context.MODE_PRIVATE)?.apply {
                viewModel.removeConsultedProfile(this, data)
            }
            val list = viewModel.consultedProfiles.value ?: arrayListOf()
            val indexed = list.indexOfFirst { it.id == data.id }
            if (indexed >= 0) list.removeAt(indexed)
            checkViewMoreConsulted(list.size)
            if (lastConsultedAdapter.getData().isEmpty())
                lastConsultedWrapper.visibility = View.GONE
            else {
                lastConsultedAdapter.setData(list.take(if (consultedTag == 0) 3 else 10).toArrayList())
            }
        }
        lastConsultedAdapter.onItemClickedListener = { obj ->
            oneKeyViewCustomObject.also {
                (activity as? AppCompatActivity)?.addFragment(R.id.fragmentContainer,
                        OneKeyProfileFragment.newInstance(it, null, obj.id), true)
            }
        }
        rvLastConsulted.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = lastConsultedAdapter
        }
    }


    private fun startNewSearch() {
        oneKeyViewCustomObject.also {
            (activity as? AppCompatActivity)?.addFragment(R.id.fragmentContainer,
                    SearchFragment.newInstance(it), true)
        }
    }

    private fun showNearMeLoading(state: Boolean) {
        nearMeLoading.visibility = state.getVisibility()
    }

    private fun getRunningMapFragment(): MapFragment? = childFragmentManager.fragments.firstOrNull {
        it::class.java.name == MapFragment::class.java.name
    } as? MapFragment

    private fun getViewTagText(tag: Int): String = if (tag == 0) "View more" else "View less"
    private fun checkViewMoreConsulted(size: Int) {
        if (size <= 3) viewMoreConsulted.visibility = View.GONE
    }

    override fun onLocationChanged(location: Location?, source: IMyLocationProvider?) {
        currentLocation = location?.getCurrentLocation(currentLocation)
        currentLocation?.also {
            viewModel.getNearMeHCP(it) {
                if (isAdded)
                    getRunningMapFragment()?.moveToPosition(GeoPoint(it.latitude, it.longitude))
            }
        }
    }
}