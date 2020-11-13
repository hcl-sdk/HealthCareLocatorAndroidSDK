package com.ekino.onekeysdk.fragments

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import base.extensions.addFragment
import base.fragments.AppFragment
import com.ekino.onekeysdk.R
import com.ekino.onekeysdk.model.config.OneKeyViewCustomObject
import com.ekino.onekeysdk.fragments.search.SearchFragment
import com.ekino.onekeysdk.viewmodel.home.HomeViewModel
import kotlinx.android.synthetic.main.fragment_home.*

class OneKeyHomeFragment(private val oneKeyViewCustomObject: OneKeyViewCustomObject) :
        AppFragment<OneKeyHomeFragment, HomeViewModel>(R.layout.fragment_home) {
    companion object {
        fun newInstance(oneKeyViewCustomObject: OneKeyViewCustomObject =
                                OneKeyViewCustomObject.Builder().build()) =
                OneKeyHomeFragment(oneKeyViewCustomObject)
    }

    override val viewModel: HomeViewModel = HomeViewModel()

    override fun initView(view: View) {
        newSearchWrapper.setOnClickListener { startNewSearch() }
        btnStartSearch.setOnClickListener { startNewSearch() }
        tvHomeHeader.setTextColor(ContextCompat.getColor(context!!, oneKeyViewCustomObject.titleColor))
//        tvHomeHeader.textSize = resources.getDimension(oneKeyViewCustomObject.textTitleSize)
    }

    private fun startNewSearch() {
        (activity as? AppCompatActivity)?.addFragment(R.id.fragmentContainer,
                SearchFragment.newInstance(oneKeyViewCustomObject), true)
    }
}