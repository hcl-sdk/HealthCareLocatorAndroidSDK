package com.ekino.onekeysdk.fragments.map

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import base.fragments.AppFragment
import base.fragments.FragmentState
import base.fragments.IFragment
import base.fragments.IFragmentState
import com.ekino.onekeysdk.R
import com.ekino.onekeysdk.extensions.*
import com.ekino.onekeysdk.model.OneKeyLocation
import com.ekino.onekeysdk.model.config.OneKeyViewCustomObject
import com.ekino.onekeysdk.model.map.OneKeyPlace
import com.ekino.onekeysdk.utils.OneKeyConstant
import com.ekino.onekeysdk.viewmodel.map.FullMapViewModel
import kotlinx.android.synthetic.main.fragment_full_map.*

class FullMapFragment : AppFragment<FullMapFragment, FullMapViewModel>(R.layout.fragment_full_map),
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

    private val fragmentState: IFragmentState by lazy { FragmentState(childFragmentManager, R.id.resultContainer) }
    private var resultFragments: ArrayList<IFragment> = arrayListOf()
    override val viewModel: FullMapViewModel = FullMapViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        childFragmentManager.fragments.filter {
            it::class.java.name == OneKeyMapResultFragment::class.java.name
                    || it::class.java.name == OneKeyListResultFragment::class.java.name
        }.map { childFragmentManager.beginTransaction().remove(it).commit() }

        var activeScreen = 0
        if (savedInstanceState != null) {
            val list = savedInstanceState.getParcelableArrayList<OneKeyLocation>(OneKeyConstant.locations)
            if (!list.isNullOrEmpty())
                locations = list
            speciality = savedInstanceState.getString(OneKeyConstant.speciality, "")
            place = savedInstanceState.getParcelable(OneKeyConstant.place)
            activeScreen = savedInstanceState.getInt(OneKeyConstant.activeResultScreen)
        }
        btnBack.setOnClickListener(this)
        initHeader()
        setModeButtons(activeScreen)
        resultFragments = arrayListOf<IFragment>(OneKeyListResultFragment.newInstance(oneKeyViewCustomObject, locations),
                OneKeyMapResultFragment.newInstance(oneKeyViewCustomObject, locations))
        viewModel.apply {
            requestPermissions(this@FullMapFragment)
            permissionRequested.observe(this@FullMapFragment, Observer { granted ->
                if (!granted) return@Observer
                fragmentState.apply {
                    enableAnim(false)
                    setStacksRootFragment(resultFragments)
                    if (resultFragments.isNotEmpty() && activeScreen < resultFragments.size)
                        showStack(activeScreen)
                }

            })
        }
        listViewMode.setOnClickListener(this)
        mapViewMode.setOnClickListener(this)
    }

    override val onPassingEventListener: (data: Any) -> Unit = {
        super.onPassingEventListener
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(OneKeyConstant.locations, locations)
        outState.putParcelable(OneKeyConstant.place, place)
        outState.putString(OneKeyConstant.speciality, speciality)
        outState.putInt(OneKeyConstant.activeResultScreen, fragmentState.currentStack())
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnBack -> activity?.onBackPressed()
            R.id.listViewMode -> {
                setModeButtons(0)
                fragmentState.showStack(0)
            }
            R.id.mapViewMode -> {
                setModeButtons(1)
                fragmentState.showStack(1)
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

    private fun setModeButtons(active: Int) {
        if (active == 0) {
            listViewMode.postDelay({
                val color = context!!.getColor(R.color.white)
                it.background = context!!.getDrawableById(R.drawable.bg_green_corner)
                it.setTextColor(color)
                it.compoundDrawables.firstOrNull()?.setTint(color)
            })
            mapViewMode.postDelay({
                val color = context!!.getColor(R.color.colorOneKeyText)
                it.background = null
                it.setTextColor(color)
                it.compoundDrawables.firstOrNull()?.setTint(color)
            })
        } else {
            mapViewMode.postDelay({
                val color = context!!.getColor(R.color.white)
                it.background = context!!.getDrawableById(R.drawable.bg_green_corner)
                it.setTextColor(color)
                it.compoundDrawables.firstOrNull()?.setTint(color)
            })
            listViewMode.postDelay({
                val color = context!!.getColor(R.color.colorOneKeyText)
                it.background = null
                it.setTextColor(color)
                it.compoundDrawables.firstOrNull()?.setTint(color)
            })
        }
    }
}