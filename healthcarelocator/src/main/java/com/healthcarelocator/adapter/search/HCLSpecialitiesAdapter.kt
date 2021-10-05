package com.healthcarelocator.adapter.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.healthcarelocator.R
import com.healthcarelocator.adapter.HCLAdapter
import com.healthcarelocator.adapter.HCLViewHolder
import com.healthcarelocator.model.LabelObject
import kotlinx.android.synthetic.main.layout_specialities_item.view.*

class HCLSpecialitiesAdapter(private val isSpeciality: Boolean = false) :
        HCLAdapter<LabelObject, HCLSpecialitiesAdapter.HCLSpecialitiesViewHolder>(
                arrayListOf(R.layout.layout_specialities_item)
        ) {

    override fun initViewHolder(parent: ViewGroup, viewType: Int): HCLSpecialitiesViewHolder =
            HCLSpecialitiesViewHolder(
                    LayoutInflater.from(parent.context).inflate(layoutIds[0], parent, false)
            )

    inner class HCLSpecialitiesViewHolder(itemView: View) :
            HCLViewHolder<LabelObject>(itemView) {
        override fun bind(position: Int, data: LabelObject) {
            itemView.apply {
                tvSpecialities.text = data.label ?: ""
                if (position == 0 && isSpeciality) {
                    tvSpecialities.setTextColor(resources.getColor(R.color.white))
                    itemSpecialities.setBackgroundResource(R.drawable.bg_orange)
                } else {
                    tvSpecialities.setTextColor(resources.getColor(R.color.colorOneKeyText))
                    itemSpecialities.setBackgroundResource(R.drawable.bg_blue_border)
                }
            }

        }
    }
}