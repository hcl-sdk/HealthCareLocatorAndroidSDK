package com.healthcarelocator.fragments.search

import android.content.Context
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import base.extensions.addFragment
import base.extensions.pushFragment
import base.fragments.AppFragment
import com.healthcarelocator.R
import com.healthcarelocator.adapter.search.HCLPlaceAdapter
import com.healthcarelocator.adapter.search.IndividualAdapter
import com.healthcarelocator.dialog.LoadingDialog
import com.healthcarelocator.extensions.*
import com.healthcarelocator.fragments.map.FullMapFragment
import com.healthcarelocator.fragments.map.HCLNearMeFragment
import com.healthcarelocator.fragments.profile.HCLProfileFragment
import com.healthcarelocator.model.HealthCareLocatorSpecialityObject
import com.healthcarelocator.model.SearchObject
import com.healthcarelocator.model.config.HealthCareLocatorCustomObject
import com.healthcarelocator.model.map.HCLPlace
import com.healthcarelocator.state.HealthCareLocatorSDK
import com.healthcarelocator.utils.KeyboardUtils
import com.healthcarelocator.viewmodel.search.SearchViewModel
import com.iqvia.onekey.GetIndividualByNameQuery
import kotlinx.android.synthetic.main.fragment_search.*
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider


class SearchFragment : AppFragment<SearchFragment, SearchViewModel>(R.layout.fragment_search),
        View.OnClickListener, HCLPlaceAdapter.OnHCLPlaceClickedListener, IMyLocationConsumer,
        View.OnFocusChangeListener, IndividualAdapter.OnIndividualClickedListener {

    companion object {
        fun newInstance(
                healthCareLocatorCustomObject: HealthCareLocatorCustomObject,
                isUseNearMe: Boolean = false,
                currentLocation: Location? = null
        ) =
                SearchFragment().apply {
                    this.healthCareLocatorCustomObject = healthCareLocatorCustomObject
                    this.currentLocation = currentLocation
                    useNearMe = isUseNearMe
                }

        private var useNearMe: Boolean = false
    }

    private var healthCareLocatorCustomObject: HealthCareLocatorCustomObject =
            HealthCareLocatorSDK.getInstance().getConfiguration()
    private val placeAdapter by lazy { HCLPlaceAdapter(healthCareLocatorCustomObject, this) }
    private val individualAdapter by lazy { IndividualAdapter() }
    private var selectedPlace: HCLPlace? = null
    private var locationProvider: GpsMyLocationProvider? = null
    private var currentLocation: Location? = null
    private var selectedSpeciality: HealthCareLocatorSpecialityObject? = null
    private var selectedName: HealthCareLocatorSpecialityObject? = null
    private var isExpand = false
    private var loadingDialog: LoadingDialog? = null
    var onItemClicked = false

    override val viewModel: SearchViewModel = SearchViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            org.osmdroid.config.Configuration.getInstance().load(
                    context, context!!.getSharedPreferences("OneKeySDK", Context.MODE_PRIVATE)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        KeyboardUtils.setUpHideSoftKeyboard(activity, container)
        if (savedInstanceState != null) {
            selectedSpeciality = savedInstanceState.getParcelable("selectedSpeciality")
            selectedPlace = savedInstanceState.getParcelable("selectedPlace")
            isExpand = savedInstanceState.getBoolean("expand", false)
            if (isExpand) selectionWrapper.expand(true)
            else selectionWrapper.collapse(true)
        }
        viewModel.requestPermission(this)
        viewModel.permissionGranted.observe(this, Observer {
            if (it) {
                if (locationProvider == null) {
                    locationProvider = GpsMyLocationProvider(context)
                }
                locationProvider?.startLocationProvider(this)
            }
        })

        if (currentLocation != null && useNearMe) {
            setNearMeText()
        }

        healthCareLocatorCustomObject?.also {
            val darkMode = it.darkMode
            val primaryColor = it.colorPrimary.getColor()
            btnSearch.setRippleBackground(primaryColor, 20f)
            edtName.textSize = it.fontSearchInput.size.toFloat()
            edtSpecialty.textSize = it.fontSearchInput.size.toFloat()
            edtWhere.textSize = it.fontSearchInput.size.toFloat()
            ivNearMe.setColorFilter(primaryColor)
            ivLocationSelected.setColorFilter(primaryColor)
            tvLocationSelected.setTextColor(primaryColor)
            ivNearMe.setRippleCircleBackground(primaryColor, 26)
            ivLocationSelected.setRippleCircleBackground(primaryColor, 26)
            selectionLine.setBackgroundColor(primaryColor)
            ivNameClear.setIconFromDrawableId(it.iconCross, true, it.colorGrey.getColor())
            ivSpecialityClear.setIconFromDrawableId(it.iconCross, true, it.colorGrey.getColor())
            ivAddressClear.setIconFromDrawableId(it.iconCross, true, it.colorGrey.getColor())
            btnSearch.setIconFromDrawableId(it.searchIcon, true, Color.WHITE)
            ivNearMe.setIconFromDrawableId(it.iconGeoLoc, true, it.colorPrimary.getColor())
            ivLocationSelected.setIconFromDrawableId(
                    it.iconMarkerMin,
                    true,
                    it.colorPrimary.getColor()
            )
            nameChildWrapper.setBackgroundWithCorner(
                    if (darkMode) it.darkModeLightColor.getColor()
                    else Color.WHITE, it.colorCardBorder.getColor(), 10f, 0)
            specialityChildWrapper.setBackgroundWithCorner(
                    if (darkMode) it.darkModeLightColor.getColor()
                    else Color.WHITE, it.colorCardBorder.getColor(), 10f, 0)
            addressChildWrapper.setBackgroundWithCorner(
                    if (darkMode) it.darkModeLightColor.getColor()
                    else Color.WHITE, it.colorCardBorder.getColor(), 10f, 0)
            btnBack.setColorFilter(if (darkMode) Color.WHITE else Color.BLACK)
        }
        btnBack.setOnClickListener(this)
        ivNameClear.setOnClickListener(this)
        ivSpecialityClear.setOnClickListener(this)
        ivAddressClear.setOnClickListener(this)
        btnSearch.setOnClickListener(this)
        nearMeWrapper.setOnClickListener(this)

        viewModel.apply {
            onAddressChanged(edtWhere)
            onSpecialityChanged(this@SearchFragment, edtSpecialty)
            onNameChanged(this@SearchFragment, edtName)
            places.observe(this@SearchFragment, Observer {
                placeAdapter.setData(it)
            })
            nameEvent.observe(this@SearchFragment, Observer {
                setNameClearState(it)
                setError(nameWrapper)
                if (edtName.hasFocus())
                    rvSpeciality.visibility = it.getVisibility()
            })
            specialityEvent.observe(this@SearchFragment, Observer {
                setSpecialityClearState(it)
                setError(specialityWrapper)
                if (edtSpecialty.hasFocus())
                    rvSpeciality.visibility = it.getVisibility()
            })
            addressEvent.observe(this@SearchFragment, Observer {
                setAddressClearState(it)
                setError(addressWrapper)
                rvSpeciality.visibility = View.GONE
                if (edtWhere.text.toString() == "Near me") {
                    isExpand = false
                    selectionWrapper.collapse(true)
                }
            })
        }
        rvAddress.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = placeAdapter
        }
        edtName.onFocusChangeListener = this
        edtSpecialty.onFocusChangeListener = this
        edtWhere.onFocusChangeListener = this
        edtName.requestFocus()
        initName()
        initIndividual()
        initAddress()
        KeyboardUtils.showSoftKeyboard(activity)
        if (HealthCareLocatorSDK.getInstance().getConfiguration().mapService == MapService.GOOGLE_MAP) {
            loadingDialog = LoadingDialog(context!!)
            loadingDialog?.initLayout()
        }
    }

    override fun onResume() {
        super.onResume()
        FullMapFragment.clear()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("expand", isExpand)
        outState.putParcelable("selectedSpeciality", selectedSpeciality)
        outState.putParcelable("selectedPlace", selectedPlace)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        locationProvider?.stopLocationProvider()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnBack -> activity?.onBackPressed()
            R.id.ivNameClear -> {
                edtName.setText("")
                edtName.requestFocus()
                setNameClearState(false)
                selectedName = null
                viewBlockedNameEditable.visibility = View.GONE
            }
            R.id.ivSpecialityClear -> {
                edtSpecialty.setText("")
                setSpecialityClearState(false)
                selectedSpeciality = null
                viewBlockedSpecialtyEditable.visibility = View.GONE
            }
            R.id.ivAddressClear -> {
                edtWhere.setText("")
                locationSelectedWrapper.visibility = View.GONE
                setAddressClearState(false)
                selectedPlace = null
            }
            R.id.btnSearch -> {
                if (selectedSpeciality == null && edtName.text.toString().isEmpty() && edtSpecialty.text.toString().isEmpty() && selectedPlace?.placeId != "near_me") {
                    setError(
                            nameWrapper,
                            ContextCompat.getColor(context!!, R.color.colorOneKeyRed)
                    )
                    setError(
                            specialityWrapper,
                            ContextCompat.getColor(context!!, R.color.colorOneKeyRed)
                    )
                    return
                } else if (edtSpecialty.text.toString().isNotEmpty() || edtName.text.toString().isNotEmpty()) {
                    if (selectedSpeciality == null && edtSpecialty.text.toString().isNotEmpty() && viewModel.getSpecialtySearch().isNotNullable()) {
                        selectedSpeciality = viewModel.getSpecialtySearch()
                        edtSpecialty.setText(selectedSpeciality.toString())
                    }

                    if (edtName.text.toString().isNotEmpty()) {
                        viewBlockedNameEditable.visibility = View.VISIBLE
                    }
                }

                if (selectedPlace == null && selectedPlace?.placeId.isNullOrEmpty() && edtWhere.text.toString().isNotEmpty() && viewModel.getAddressSearch().isNullable()) {
                    setError(addressWrapper,
                            ContextCompat.getColor(context!!, R.color.colorOneKeyRed))
                    return
                } else if (selectedPlace == null && edtWhere.text.toString().isNotEmpty() && viewModel.getAddressSearch().isNotNullable()) {
                    selectedPlace = viewModel.getAddressSearch()
                    edtWhere.setText(selectedPlace!!.displayName)
                }
                setFocusable(false)
                val resultSearch = if (edtName.text.toString().isNotEmpty() && edtSpecialty.text.toString().isEmpty()) {
                    edtName.text.toString()
                } else if (edtName.text.toString().isEmpty() && edtSpecialty.text.toString().isNotEmpty()) {
                    edtSpecialty.text.toString()
                } else {
                    edtName.text.toString() + ", " + edtSpecialty.text.toString()
                }
                healthCareLocatorCustomObject?.also {
                    onItemClicked = true
                    context?.getSharedPreferences("OneKeySDK", Context.MODE_PRIVATE)?.apply {
                        viewModel.storeSearch(this, SearchObject(selectedSpeciality
                                ?: HealthCareLocatorSpecialityObject(longLbl = resultSearch),
                                selectedPlace ?: HCLPlace().apply {
                                    displayName = edtWhere.text.toString()
                                }))
                    }
                    if (selectedPlace?.placeId == "near_me" && edtSpecialty.text.toString()
                                    .isEmpty() && edtName.text.toString().isEmpty() && currentLocation != null) {
                        (activity as? AppCompatActivity)?.addFragment(R.id.fragmentContainer,
                                HCLNearMeFragment.newInstance(healthCareLocatorCustomObject, "", null,
                                        HCLPlace(placeId = "near_me", latitude = "${currentLocation!!.latitude}",
                                                longitude = "${currentLocation!!.longitude}",
                                                displayName = getString(R.string.hcl_near_me)),
                                        arrayListOf(), currentLocation), true)
                    } else {
                        (activity as? AppCompatActivity)?.pushFragment(R.id.fragmentContainer,
                                FullMapFragment.newInstance(it, resultSearch,
                                        selectedSpeciality, selectedPlace ?: HCLPlace().apply {
                                    displayName = getString(R.string.hcl_anywhere)
                                }), true)
                    }
                }
            }
            R.id.nearMeWrapper -> {
                setNearMeText()
            }
        }
    }

    override fun onPlaceClickedListener(place: HCLPlace) {
        edtWhere.setText(place.displayName)
        locationSelectedWrapper.visibility = View.VISIBLE
        tvLocationSelected.text = place.displayName
        this.selectedPlace = place
        if (HealthCareLocatorSDK.getInstance().getConfiguration().mapService == MapService.GOOGLE_MAP) {
            getLoadingDialog()?.show()
            viewModel.getGooglePlaceDetail(this, selectedPlace!!)
        }
    }

    private fun initName() {
        rvSpeciality.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = individualAdapter
        }
        individualAdapter.setData(viewModel.individuals.value ?: arrayListOf())
        individualAdapter.onIndividualClickedListener = this
        viewModel.nameState.observe(this, Observer {
            showNameProgressBar(it)
        })
        viewModel.individuals.observe(this, Observer {
            individualAdapter.setKeyword(edtName.text.toString())
            individualAdapter.setData(it)
        })
    }

    private fun initIndividual() {
        rvSpeciality.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = individualAdapter
        }
        individualAdapter.setData(viewModel.individuals.value ?: arrayListOf())
        individualAdapter.onIndividualClickedListener = this
        viewModel.specialtyState.observe(this, Observer {
            showSpecialtyProgressBar(it)
        })
        viewModel.individuals.observe(this, Observer {
            individualAdapter.setKeyword(edtSpecialty.text.toString())
            individualAdapter.setData(it)
        })
    }

    private fun initAddress() {
        viewModel.addressState.observe(this, Observer {
            showAddressLoading(it)
        })
    }

    private fun setNearMeText() {
        currentLocation?.apply {
            selectedPlace = HCLPlace(
                    placeId = "near_me", latitude = "$latitude",
                    longitude = "$longitude", displayName = getString(R.string.hcl_near_me)
            )
            edtWhere.setText(selectedPlace?.displayName ?: "")
        }
    }

    private fun setNameClearState(state: Boolean) {
        ivNameClear.visibility = state.getVisibility()
    }

    private fun setSpecialityClearState(state: Boolean) {
        ivSpecialityClear.visibility = state.getVisibility()
    }

    private fun setAddressClearState(state: Boolean) {
        ivAddressClear.visibility = state.getVisibility()
    }

    private fun setError(
            view: View,
            strokeColor: Int = healthCareLocatorCustomObject.colorCardBorder.getColor()
    ) {
        view.setBackgroundWithCorner(Color.WHITE, strokeColor, 12f, 3)
    }

    override fun onLocationChanged(location: Location?, source: IMyLocationProvider?) {
        currentLocation = location?.getCurrentLocation(currentLocation)
        if (currentLocation != null && ((edtWhere.hasFocus()) ||
                        (edtSpecialty.hasFocus() && edtSpecialty.text.toString().isEmpty()))
                && edtWhere.text.toString() != "Near me"
        ) {
            isExpand = true
            selectionWrapper.expand(true)
        } else {
            isExpand = false
            selectionWrapper.collapse(true)
        }
    }

    override fun onIndividualClickedListener(data: HealthCareLocatorSpecialityObject) {
        selectedSpeciality = data
        onItemClicked = true
        viewBlockedSpecialtyEditable.visibility = View.VISIBLE
        edtSpecialty.setText(data.longLbl)
        edtWhere.requestFocus()
    }

    override fun onHCPClickedListener(data: GetIndividualByNameQuery.Individual) {
        onItemClicked = true
        (activity as? AppCompatActivity)?.addFragment(
                R.id.fragmentContainer,
                HCLProfileFragment.newInstance(
                        healthCareLocatorCustomObject,
                        null,
                        data.mainActivity().id()
                ), true
        )
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (v?.id == edtSpecialty.id && edtSpecialty.text.toString().isNotEmpty()) {
            rvSpeciality.visibility = hasFocus.getVisibility()
        } else {
            rvSpeciality.visibility = View.GONE
        }

        if (v?.id == edtName.id && edtName.text.toString().isNotEmpty()) {
            rvSpeciality.visibility = hasFocus.getVisibility()
        } else {
            rvSpeciality.visibility = View.GONE
        }

        if (v?.id == edtWhere.id) {
            rvSpeciality.visibility = View.GONE
        }
    }

    private fun showNameProgressBar(state: Boolean) {
        nameBar.visibility = state.getVisibility()
        setNameClearState(!state)
    }

    private fun showSpecialtyProgressBar(state: Boolean) {
        specialtyBar.visibility = state.getVisibility()
        setSpecialityClearState(!state)
    }

    private fun showAddressLoading(state: Boolean) {
        addressLoading.visibility = state.getVisibility()
        setAddressClearState(!state)
    }

    fun clearIndividualData() {
        individualAdapter.clear()
    }

    fun setFocusable(isFocusable: Boolean) {
        edtName.isFocusableInTouchMode = isFocusable
        edtName.isFocusable = isFocusable
        edtSpecialty.isFocusableInTouchMode = isFocusable
        edtSpecialty.isFocusable = isFocusable
        edtWhere.isFocusableInTouchMode = isFocusable
        edtWhere.isFocusable = isFocusable
        if (isFocusable) {
            edtWhere.requestFocus()
        }
    }

    fun showMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun getLoadingDialog(): LoadingDialog? {
        if (loadingDialog == null)
            loadingDialog = LoadingDialog(context!!)
        return loadingDialog
    }
}