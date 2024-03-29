package com.healthcarelocator.fragments.map

import android.content.Context
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.forEach
import androidx.lifecycle.Observer
import base.extensions.pushFragment
import base.extensions.runOnUiThread
import base.fragments.FragmentState
import base.fragments.IFragment
import base.fragments.IFragmentState
import com.healthcarelocator.R
import com.healthcarelocator.custom.text.HCLTextView
import com.healthcarelocator.extensions.*
import com.healthcarelocator.fragments.profile.HCLProfileFragment
import com.healthcarelocator.fragments.search.SearchFragment
import com.healthcarelocator.model.HealthCareLocatorSpecialityObject
import com.healthcarelocator.model.activity.ActivityObject
import com.healthcarelocator.model.config.HealthCareLocatorCustomObject
import com.healthcarelocator.model.map.HCLPlace
import com.healthcarelocator.state.HealthCareLocatorSDK
import com.healthcarelocator.utils.HCLConstant
import com.healthcarelocator.utils.HCLLog
import com.healthcarelocator.utils.KeyboardUtils
import com.healthcarelocator.viewmodel.map.NearMeViewModel
import kotlinx.android.synthetic.main.fragment_full_map.*
import kotlinx.android.synthetic.main.fragment_full_map.edtSearch
import kotlinx.android.synthetic.main.fragment_full_map.ivSearch
import kotlinx.android.synthetic.main.fragment_full_map.newSearchWrapper
import kotlinx.android.synthetic.main.fragment_one_key_home_main.*

class HCLNearMeFragment :
        AbsMapFragment<HCLNearMeFragment, NearMeViewModel>(R.layout.fragment_full_map),
        View.OnClickListener {
    companion object {
        fun newInstance(
                healthCareLocatorCustomObject: HealthCareLocatorCustomObject,
                c: String,
                s: HealthCareLocatorSpecialityObject?,
                p: HCLPlace?,
                listIds: ArrayList<String> = arrayListOf(),
                cLocation: Location? = null
        ) =
                HCLNearMeFragment().apply {
                    this.healthCareLocatorCustomObject = healthCareLocatorCustomObject
                    speciality = s
                    criteria = c
                    specialities = listIds
                    place = p
                    currentLocation = cLocation
                    if (p?.placeId == "near_me") {
                        activeScreen = 1
                        isNearMe = true
                    }
                }

        private var navigateToProfile = false
        private var currentLocation: Location? = null
        private var specialities = arrayListOf<String>()

        fun clear() {
            navigateToProfile = false
            currentLocation = null
        }
    }

    private var healthCareLocatorCustomObject: HealthCareLocatorCustomObject =
            HealthCareLocatorSDK.getInstance().getConfiguration()
    private val fragmentState: IFragmentState by lazy {
        FragmentState(
                childFragmentManager,
                R.id.resultContainer
        )
    }
    private var resultFragments: ArrayList<IFragment> = arrayListOf()
    private var activities = arrayListOf<ActivityObject>()
    private var sorting: Int = 0
    private var place: HCLPlace? = null
    private var criteria: String = ""
    private var activeScreen = 0
    private var isRelaunch = false
    private var isNearMe = false
    private var speciality: HealthCareLocatorSpecialityObject? = null
    private var specialityName: String = ""
    override val viewModel: NearMeViewModel = NearMeViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        KeyboardUtils.setUpHideSoftKeyboard(activity, container)
        if (savedInstanceState != null) {
            isRelaunch = savedInstanceState.getBoolean("isRelaunch", false)
            speciality = savedInstanceState.getParcelable(HCLConstant.speciality)
            specialities = savedInstanceState.getStringArrayList("specialities") ?: arrayListOf()
            criteria = savedInstanceState.getString(criteria, "")
            place = savedInstanceState.getParcelable(HCLConstant.place)
            navigateToProfile = savedInstanceState.getBoolean(HCLConstant.navigateToProfile)
            activities = savedInstanceState.getParcelableArrayList("activities") ?: arrayListOf()
            sorting = savedInstanceState.getInt("sorting", 0)
            activeScreen = savedInstanceState.getInt("activeScreen", 0)
        }
        if (!navigateToProfile)
            childFragmentManager.fragments.filter {
                it::class.java.name == HCLMapResultFragment::class.java.name
                        || it::class.java.name == HCLListResultFragment::class.java.name
            }.map { childFragmentManager.beginTransaction().remove(it).commit() }
        else navigateToProfile = false

        val isSpeciality = specialities.isNotEmpty()
        labelWrapper.visibility = (isSpeciality).getVisibility()
        newSearchWrapper.visibility = (!isSpeciality).getVisibility()
        healthCareLocatorCustomObject.apply {
            newSearchWrapper.setBackgroundWithCorner(
                    if (darkMode) darkModeColor.getColor() else Color.WHITE,
                    colorCardBorder.getColor(), 12f, 3)
            edtSearch.setBackgroundWithCorner(if (darkMode) darkModeColor.getColor() else Color.WHITE,
                    colorCardBorder.getColor(), 12f, 3)
            edtSearch.setHintTextColor(if (darkMode) Color.parseColor("#55ffffff") else Color.parseColor("#55000000"))
            ivSearch.setIconFromDrawableId(searchIcon, true,
                    if (darkMode) darkModeColor.getColor() else Color.WHITE)
            ivSearch.setRippleBackground(colorPrimary.getColor(), 15f)
            sortWrapper.setBackgroundWithCorner(Color.WHITE, colorCardBorder.getColor(), 50f, 3)
            btnBack.setColorFilter(if (darkMode) Color.WHITE else Color.BLACK)
            ivSearchIcon.background = ContextCompat.getDrawable(context!!,
                    if (darkMode) R.drawable.bg_black_circle_border else R.drawable.bg_gray_cirle)
            loadingWrapper.setBackgroundColor(if (darkMode) darkModeColor.getColor() else Color.WHITE)
        }

        initHeader()
        btnBack.setOnClickListener(this)

        viewModel.apply {
            requestPermissions(this@HCLNearMeFragment)
            if (healthCareLocatorCustomObject.specialities.isNotEmpty() && specialityName.isEmpty()) {
                getSpecialityNameByCode(healthCareLocatorCustomObject.specialities.first())
            }
            specialityLabel.observe(this@HCLNearMeFragment, Observer {
                specialityName = it
                setSpecialityName()
            })
            permissionRequested.observe(this@HCLNearMeFragment, Observer { granted ->
                if (!granted) {
                    showLoading(false)
                    return@Observer
                }
                if (this@HCLNearMeFragment.activities.isEmpty())
                    getActivities(
                            context!!, criteria, if (speciality.isNotNullable())
                        arrayListOf(speciality!!.id) else specialities, place
                    ) { location ->
                        currentLocation = location
                    }
                else {
                    setModeButtons(activeScreen)
                    showLoading(false)
                    initTabs()
                    setResult()
                }
                loading.observe(this@HCLNearMeFragment, Observer {
                    showLoading(it)
                })
                activities.observe(this@HCLNearMeFragment, Observer {
                    if (it.isEmpty()) {
                        showNoResult()
                    }
                    this@HCLNearMeFragment.activities = it
                    setModeButtons(activeScreen)
                    initTabs()
                    setResult()
                })
            })
        }
        listViewMode.setOnClickListener(this)
        mapViewMode.setOnClickListener(this)
        newSearchWrapper.setOnClickListener(this)
        context?.getSharedPreferences("OneKeySDK", Context.MODE_PRIVATE)?.edit {
            putBoolean(isLocationSelection, false)
            putString(locationSelection, "")
        }
    }

    private fun initTabs() {
        viewModel.sortActivities(ArrayList(activities), sorting) {
            resultFragments = arrayListOf(
                    HCLListResultFragment.newInstance(if (speciality.isNotNullable()) {
                        speciality!!.longLbl
                    } else {""}, ArrayList(it)),
                    HCLMapResultFragment.newInstance(if (speciality.isNotNullable()) {
                        speciality!!.longLbl
                    } else {""}, ArrayList(it))
            )
            fragmentState.apply {
                enableAnim(false)
                setStacksRootFragment(resultFragments)
                if (resultFragments.isNotEmpty() && activeScreen < resultFragments.size)
                    showStack(activeScreen)
            }
        }
    }

    override val onPassingEventListener: (data: Any) -> Unit = {
        super.onPassingEventListener
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (!isVisible) return
        outState.putParcelable(HCLConstant.place, place)
        outState.putParcelable(HCLConstant.speciality, speciality)
        outState.putStringArrayList("specialities", specialities)
        outState.putString("criteria", criteria)
        outState.putBoolean(HCLConstant.navigateToProfile, navigateToProfile)
        outState.putParcelableArrayList("activities", activities)
        outState.putInt("sorting", sorting)
        outState.putInt("activeScreen", activeScreen)
        outState.putBoolean("isRelaunch", isRelaunch)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnBack -> activity?.onBackPressed()
            R.id.listViewMode -> {
                setModeButtons(0)
                activeScreen = 0
                fragmentState.showStack(0)
            }
            R.id.mapViewMode -> {
                setModeButtons(1)
                activeScreen = 1
                fragmentState.showStack(1)
            }
            R.id.ivSort -> {
                navigateToProfile = true
                (activity as? AppCompatActivity)?.pushFragment(
                        R.id.fragmentContainer,
                        HCLSortFragment.newInstance(healthCareLocatorCustomObject, sorting), true
                )
            }
            R.id.newSearchWrapper -> {
                (activity as? AppCompatActivity)?.pushFragment(
                        R.id.fragmentContainer,
                        SearchFragment.newInstance(
                                healthCareLocatorCustomObject,
                                true, currentLocation
                        ), true
                )
            }
        }
    }

    private fun setSpecialityName() {
        val sharedPreferences = context!!.getSharedPreferences("SampleOneKeySDK", Context.MODE_PRIVATE)
        val isSearchCardiology = sharedPreferences.getBoolean("Pref.isSearchCardiology", false)
        val specialtyLabel = sharedPreferences.getString("Pref.specialtyLabel", "")
        if (isSearchCardiology) {
            if (specialtyLabel.isNotNullAndEmpty()) {
                tvSpeciality.text = specialtyLabel
            } else {
                tvSpeciality.text = "CARDIOLOGY"
            }
            sharedPreferences.edit {
                putBoolean("Pref.isSearchCardiology", false)
            }
        } else if (healthCareLocatorCustomObject.specialities.isNotEmpty()) {
            tvSpeciality.text = specialtyLabel ?: specialityName
        }
    }

    private fun initHeader() {
        tvAddress.text = place?.displayName ?: ""
        mapViewMode.setRippleBackground(healthCareLocatorCustomObject.colorPrimary.getColor(), 50f)
        sortWrapper.setBackgroundWithCorner(
                Color.WHITE,
                healthCareLocatorCustomObject.colorCardBorder.getColor(),
                50f,
                3
        )
        val darkMode = healthCareLocatorCustomObject.darkMode
        modeWrapper.setBackgroundWithCorner(if (darkMode) healthCareLocatorCustomObject.darkModeColor.getColor() else Color.WHITE,
                healthCareLocatorCustomObject.colorCardBorder.getColor(), 50f, 3)
        ivSort.setRippleCircleBackground(
                healthCareLocatorCustomObject.colorSecondary.getColor(),
                255
        )
        ivSort.setIconFromDrawableId(healthCareLocatorCustomObject.iconSort, true, Color.WHITE)
        ivList.setIconFromDrawableId(healthCareLocatorCustomObject.iconList)
        ivMap.setIconFromDrawableId(healthCareLocatorCustomObject.iconMap)
        resultContainer.setBackgroundColor(healthCareLocatorCustomObject.colorListBackground.getColor())
        tvAddress.textSize = healthCareLocatorCustomObject.fontSmall.size.toFloat()
        ivSort.setOnClickListener(this)
    }

    private fun setResult() {
        val result = "${activities.size}"
        tvResult.text = SpannableStringBuilder(result).apply {
            setSpan(
                    ForegroundColorSpan(healthCareLocatorCustomObject.colorPrimary.getColor()),
                    0, result.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    private fun applySortSetMode(sort: Int) {
        this.sorting = sort
        applySorting(sorting)
        HCLSortFragment.newInstance(healthCareLocatorCustomObject, sorting)
    }

    private fun setModeButtons(active: Int) {
        if (active == 0) {
            applySortSetMode(0)
            listViewMode.postDelay({
                val color = context!!.getColor(R.color.white)
                it.setRippleCircleBackground(
                        healthCareLocatorCustomObject.colorPrimary.getColor(),
                        255
                )
                setViewModeColor(listViewMode, color)
            })
            mapViewMode.postDelay({
                val color = context!!.getColor(R.color.colorOneKeyUnselected)
                it.background = null
                setViewModeColor(mapViewMode, color)
            })
        } else {
            applySortSetMode(1)
            mapViewMode.postDelay({
                val color = context!!.getColor(R.color.white)
                it.setRippleCircleBackground(
                        healthCareLocatorCustomObject.colorPrimary.getColor(),
                        255
                )
                setViewModeColor(mapViewMode, color)
            })
            listViewMode.postDelay({
                val color = context!!.getColor(R.color.colorOneKeyUnselected)
                it.background = null
                setViewModeColor(listViewMode, color)
            })
        }
    }

    fun navigateToHCPProfile(obj: ActivityObject) {
        navigateToProfile = true
        healthCareLocatorCustomObject.also {
            (activity as? AppCompatActivity)?.pushFragment(
                    R.id.fragmentContainer,
                    HCLProfileFragment.newInstance(it, null, obj.id, viewModel.isSpeciality, if (speciality.isNotNullable()) {
                        speciality!!.longLbl
                    } else {""}), true
            )
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        HCLLog.d("LifeCycle: onAttach")
    }

    override fun onDetach() {
        super.onDetach()
        HCLLog.d("LifeCycle: onDetach")
    }

    private fun showLoading(state: Boolean) {
        loadingWrapper.visibility = state.getVisibility()
    }

    private fun setViewModeColor(group: ViewGroup, color: Int) {
        group.forEach { view ->
            if (view is ImageView) {
                view.setColorFilter(color)
            } else if (view is HCLTextView) {
                view.setTextColor(color)
            }
        }
    }

    fun applySorting(sort: Int) {
        this.sorting = sort
        viewModel.sortActivities(ArrayList(activities), sorting) {
            (fragmentState.getRootFragments()?.firstOrNull { fragment ->
                fragment::class.java == HCLListResultFragment::class.java
            } as? HCLListResultFragment)?.updateActivities(it)
            (fragmentState.getRootFragments()?.firstOrNull { fragment ->
                fragment::class.java == HCLMapResultFragment::class.java
            } as? HCLMapResultFragment)?.updateActivities(it)
        }
    }

    override fun getActivities(): ArrayList<ActivityObject> = activities
    override fun getRelaunchState(): Boolean = isRelaunch
    override fun setRelaunchState(isRelaunch: Boolean) {
        this.isRelaunch = isRelaunch
    }

    override fun reverseGeoCoding(place: HCLPlace, distance: Double) {
        if (!isAdded) return
        viewModel.reverseGeoCoding(place) {
            if (distance > 0.0 && it.distance < distance)
                it.distance = distance
            forceSearch(it, distance)
        }
    }

    override fun isNearMe(): Boolean = isNearMe
    override fun setNearMeState(state: Boolean) {
        this.isNearMe = state
    }

    override fun forceSearch(place: HCLPlace, distance: Double) {
        if (!isAdded) return
        this.place = place
        tvAddress.text = place.displayName ?: ""
        viewModel.getActivities(context!!, criteria, if (speciality.isNotNullable())
            arrayListOf(speciality!!.id) else specialities, place, false,
                { activities ->
                    this@HCLNearMeFragment.activities = activities
                    runOnUiThread(Runnable {
                        setResult()
                        with(childFragmentManager.fragments) {
                            (firstOrNull() { it is HCLMapResultFragment } as? HCLMapResultFragment)?.updateActivities(
                                    activities
                            )
                            (firstOrNull() { it is HCLListResultFragment } as? HCLListResultFragment)?.updateActivities(
                                    activities
                            )
                        }
                    })
                }) { location ->
            currentLocation = location
        }
    }

    override fun getPlaceDetail(): HCLPlace? {
        return place
    }

    private fun showNoResult() {
        noResult.visibility = View.VISIBLE
        noResult.setBackgroundColor(healthCareLocatorCustomObject.colorViewBackground.getColor())
        btnStartSearch.setRippleBackground(healthCareLocatorCustomObject.colorPrimary)
        noResultWrapper.setBackgroundWithCorner(
                if (healthCareLocatorCustomObject.darkMode)
                    healthCareLocatorCustomObject.darkModeColor.getColor() else Color.WHITE,
                healthCareLocatorCustomObject.colorCardBorder.getColor(),
                15f,
                3
        )
        btnStartSearch.setOnClickListener(this)
    }
}