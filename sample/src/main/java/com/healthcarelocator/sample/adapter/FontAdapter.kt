package com.healthcarelocator.sample.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ekino.sample.onekeysdk.R
import com.healthcarelocator.adapter.HCLAdapter
import com.healthcarelocator.adapter.HCLViewHolder
import com.healthcarelocator.model.config.HealthCareLocatorViewFontObject
import kotlinx.android.synthetic.main.layout_item_font.view.*

class FontAdapter : HCLAdapter<HealthCareLocatorViewFontObject, FontAdapter.FontVH>(arrayListOf(R.layout.layout_item_font)) {
    var onItemSelectedListener: (data: HealthCareLocatorViewFontObject, position: Int) -> Unit = { _, _ -> }
    override fun initViewHolder(parent: ViewGroup, viewType: Int): FontVH =
            FontVH(LayoutInflater.from(parent.context).inflate(layoutIds[0], parent, false))

    inner class FontVH(itemView: View) : HCLViewHolder<HealthCareLocatorViewFontObject>(itemView) {
        override fun bind(position: Int, data: HealthCareLocatorViewFontObject) {
            itemView.apply {
                tvFontName.text = data.title
                setOnClickListener { onItemSelectedListener(data, position) }
            }
        }
    }
}