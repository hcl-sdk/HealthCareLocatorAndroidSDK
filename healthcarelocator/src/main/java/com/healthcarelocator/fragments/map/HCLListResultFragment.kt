package com.healthcarelocator.fragments.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import base.fragments.IFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.healthcarelocator.R
import com.healthcarelocator.adapter.search.SearchAdapter
import com.healthcarelocator.extensions.*
import com.healthcarelocator.model.activity.ActivityObject
import com.healthcarelocator.model.config.HealthCareLocatorCustomObject
import com.healthcarelocator.state.HealthCareLocatorSDK
import kotlinx.android.synthetic.main.fragment_one_key_list_result.*

class HCLListResultFragment : IFragment() {
    companion object {
        fun newInstance(speciality: String = "", activities: ArrayList<ActivityObject>) = HCLListResultFragment().apply {
            this.speciality = speciality
            this.activities = activities
        }
    }

    private var healthCareLocatorCustomObject: HealthCareLocatorCustomObject = HealthCareLocatorSDK.getInstance().getConfiguration()
    private var activities: ArrayList<ActivityObject> = arrayListOf()
    private var speciality: String = ""
    private val searchAdapter by lazy { SearchAdapter(-1, speciality) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_one_key_list_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listContainer.setBackgroundColor(healthCareLocatorCustomObject.colorListBackground.getColor())
        rvResult.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = searchAdapter
            showDistance()
            searchAdapter.setData(activities)
        }
        getHighLight()
        searchAdapter.onHCPCardClickedListener = { oneKeyLocation ->
            if (parentFragment is FullMapFragment) (parentFragment as FullMapFragment).navigateToHCPProfile(oneKeyLocation)
            else if (parentFragment is HCLNearMeFragment) (parentFragment as HCLNearMeFragment).navigateToHCPProfile(oneKeyLocation)
        }
    }

    private fun getHighLight() {
        rvResult.postDelay({ rv ->
            getAbsFragment()?.apply {
                val json = sharedPreferences.getString(locationSelection, "")
                val selectedPosition = Gson().fromJson<ArrayList<String>>(json, object : TypeToken<ArrayList<String?>?>() {}.type)
                val listSelect = arrayListOf<Int>()
                val listActivitiesID = arrayListOf<String>()
                for (i in activities.indices) {
                    listActivitiesID.add(activities[i].id)
                }
                if (selectedPosition.isNotNullable() && selectedPosition.isNotEmpty() && sharedPreferences.getBoolean(isLocationSelection, false) && json != "") {
                    for (i in selectedPosition.indices) {
                        listSelect.add(listActivitiesID.indexOf(selectedPosition[i]))
                    }
                    rv?.execute { it.smoothScrollToPosition(listSelect.first()) }
                    searchAdapter.setSelectedPosition(listSelect)
                }
            }
        }, 1000L)
    }

    fun updateActivities(activities: ArrayList<ActivityObject>) {
        showDistance()
        this.activities = activities
        searchAdapter.setData(activities)
    }

    private fun showDistance(){
        searchAdapter.isPlaceAvailable = getAbsFragment()?.getPlaceDetail()?.placeId?.isNotNullAndEmpty()?:false
    }

    private fun getAbsFragment(): AbsMapFragment<*, *>? = parentFragment as? AbsMapFragment<*, *>
}