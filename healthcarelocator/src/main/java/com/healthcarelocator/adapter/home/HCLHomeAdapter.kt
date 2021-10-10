package com.healthcarelocator.adapter.home

import android.content.res.Configuration
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.healthcarelocator.R
import com.healthcarelocator.adapter.HCLAdapter
import com.healthcarelocator.adapter.HCLViewHolder
import com.healthcarelocator.extensions.getColor
import com.healthcarelocator.extensions.getDrawableById
import com.healthcarelocator.extensions.setBackgroundWithCorner
import com.healthcarelocator.extensions.setRippleCircleBackground
import com.healthcarelocator.model.config.HealthCareLocatorCustomObject
import com.healthcarelocator.model.home.HCLHomeObject
import kotlinx.android.synthetic.main.layout_one_key_home.view.*

class HCLHomeAdapter(private val healthCareLocatorCustomObject: HealthCareLocatorCustomObject?) : HCLAdapter<HCLHomeObject,
        HCLHomeAdapter.HCLHomeVH>(arrayListOf(R.layout.layout_one_key_home)) {


    override fun initViewHolder(parent: ViewGroup, viewType: Int): HCLHomeVH =
            HCLHomeVH(LayoutInflater.from(parent.context).inflate(layoutIds[0], parent, false))


    inner class HCLHomeVH(itemView: View) : HCLViewHolder<HCLHomeObject>(itemView) {
        override fun bind(position: Int, data: HCLHomeObject) {
            healthCareLocatorCustomObject?.also {
                itemView.apply {
                    if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        setBackgroundWithCorner(if (it.darkMode) it.darkModeLightColor.getColor() else Color.WHITE,
                                it.colorCardBorder.getColor(), 12f, 3)
                    } else
                        setBackgroundColor(if (it.darkMode) it.darkModeLightColor.getColor() else Color.WHITE)
                    ivFind.setImageDrawable(context.getDrawableById(data.drawableId))
                    ivFind.setRippleCircleBackground(it.colorPrimary.getColor(), if (it.darkMode) 255 else 26)
                    ivFind.setColorFilter(if (it.darkMode) Color.WHITE else it.colorPrimary.getColor())
                    tvTitle.text = data.title
                    tvDescription.text = data.description
                }
            }
        }
    }
}