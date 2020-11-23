package com.ekino.onekeysdk.fragments.map

import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import base.fragments.AppFragment
import com.ekino.onekeysdk.R
import com.ekino.onekeysdk.adapter.search.SearchAdapter
import com.ekino.onekeysdk.custom.LinearLayoutManagerWithSmoothScroller
import com.ekino.onekeysdk.extensions.ThemeExtension
import com.ekino.onekeysdk.extensions.getColor
import com.ekino.onekeysdk.extensions.setRippleBackground
import com.ekino.onekeysdk.extensions.setRippleCircleBackground
import com.ekino.onekeysdk.model.OneKeyLocation
import com.ekino.onekeysdk.model.config.OneKeyViewCustomObject
import com.ekino.onekeysdk.model.map.OneKeyPlace
import com.ekino.onekeysdk.viewmodel.map.FullMapViewModel
import kotlinx.android.synthetic.main.fragment_full_map.*

class FullMapFragment :
        AppFragment<FullMapFragment, FullMapViewModel>(R.layout.fragment_full_map),
        View.OnClickListener {
    companion object {
        fun newInstance(oneKeyViewCustomObject: OneKeyViewCustomObject, speciality: String,
                        place: OneKeyPlace?, locations: ArrayList<OneKeyLocation>) =
                FullMapFragment().apply {
                    this.oneKeyViewCustomObject = oneKeyViewCustomObject
                    this.speciality = speciality
                    this.place = place
                    this.locations = locations
                }
    }

    private var oneKeyViewCustomObject: OneKeyViewCustomObject = ThemeExtension.getInstance().getThemeConfiguration()
    private var speciality: String = ""
    private var place: OneKeyPlace? = null
    private var locations: ArrayList<OneKeyLocation> = arrayListOf()

    private val mapFragmentTag: String = StarterMapFragment::class.java.name
    private val mapFragment by lazy { MapFragment.newInstance(oneKeyViewCustomObject, locations) }
    private val searchAdapter by lazy { SearchAdapter() }

    override val viewModel: FullMapViewModel = FullMapViewModel()

    override fun initView(view: View) {
        btnBack.setOnClickListener(this)
        initHeader()
        viewModel.apply {
            requestPermissions(this@FullMapFragment)
            permissionRequested.observe(this@FullMapFragment, Observer { granted ->
                if (!granted) return@Observer
                val fm = this@FullMapFragment.childFragmentManager
                if (fm.findFragmentByTag(mapFragmentTag) == null) {
                    fm.beginTransaction().add(R.id.mapContainer, mapFragment, mapFragmentTag)
                            .commit()
                }
            })
        }

        rvLocations.apply {
            layoutManager = LinearLayoutManagerWithSmoothScroller(
                    context,
                    LinearLayoutManager.HORIZONTAL,
                    false
            )
            adapter = searchAdapter
            searchAdapter.setData(locations)
        }
        mapFragment.onMarkerSelectionChanged = { id ->
            val selectedPosition = locations.indexOfFirst { it.id == id }
            if (selectedPosition >= 0)
                rvLocations.smoothScrollToPosition(selectedPosition)
        }
        btnCurrentLocation.setOnClickListener { }
    }

    override val onPassingEventListener: (data: Any) -> Unit = {
        super.onPassingEventListener
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnBack -> {
                activity?.onBackPressed()
            }
        }
    }

    private fun initHeader() {
        tvSpeciality.text = speciality
        tvAddress.text = place?.displayName ?: ""
        val result = "${locations.size}"
        tvResult.text = SpannableStringBuilder(result).apply {
            setSpan(ForegroundColorSpan(oneKeyViewCustomObject.primaryColor.getColor()),
                    0, result.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        mapViewMode.setRippleBackground(oneKeyViewCustomObject.primaryColor.getColor(), 50f)
        ivSort.setRippleCircleBackground(oneKeyViewCustomObject.secondaryColor.getColor(), 255)
    }
}