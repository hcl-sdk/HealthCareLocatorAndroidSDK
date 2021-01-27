package com.ekino.onekeysdk.adapter.search

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ekino.onekeysdk.R
import com.ekino.onekeysdk.adapter.OneKeyAdapter
import com.ekino.onekeysdk.adapter.OneKeyViewHolder
import com.ekino.onekeysdk.extensions.getColor
import com.ekino.onekeysdk.extensions.setBackgroundWithCorner
import com.ekino.onekeysdk.extensions.setIconFromDrawableId
import com.ekino.onekeysdk.model.activity.ActivityObject
import com.ekino.onekeysdk.state.HealthCareLocatorSDK
import kotlinx.android.synthetic.main.layout_search_item.view.*

class SearchAdapter(private val screenWidth: Int = -1) :
        OneKeyAdapter<ActivityObject, SearchAdapter.SearchVH>(arrayListOf(R.layout.layout_search_item)) {
    private var selectedPosition = -1
    private val themeConfig by lazy { HealthCareLocatorSDK.getInstance().getConfiguration() }
    var onHCPCardClickedListener: (data: ActivityObject) -> Unit = {}

    override fun initViewHolder(parent: ViewGroup, viewType: Int): SearchVH =
            SearchVH(LayoutInflater.from(parent.context).inflate(layoutIds[0], parent, false))

    inner class SearchVH(itemView: View) : OneKeyViewHolder<ActivityObject>(itemView) {
        override fun bind(position: Int, data: ActivityObject) {
            itemView.apply {
                if (screenWidth > 0)
                    itemView.post {
                        val lp = itemView.layoutParams
                        lp.width = (screenWidth * 0.85f).toInt()
                        itemView.layoutParams = lp
                    }
                tvName.text = data.individual?.mailingName ?: ""
                tvSpeciality.text = data.individual?.professionalType?.label ?: ""
                tvAddress.text = data.workplace?.address?.getAddress() ?: ""
                tvDistance.text = itemView.context.getString(R.string.hcl_distance_unit_android, "${Math.round(data.distance)}")
                ivArrow.setIconFromDrawableId(themeConfig.iconArrowRight)
                ivArrow.setColorFilter(themeConfig.colorSecondary.getColor())
                setOnClickListener {
                    onHCPCardClickedListener(data)
                }
                if (data.selected)
                    setBackgroundWithCorner(Color.WHITE, themeConfig.colorMarkerSelected.getColor(), 12f, 8)
                else setBackgroundWithCorner(Color.WHITE, themeConfig.colorCardBorder.getColor(), 12f, 3)
            }
        }
    }

    fun setSelectedPosition(indexes: ArrayList<Int>) {
        reset()
        indexes.forEach { index ->
            getData()[index].selected = true
        }
        notifyDataSetChanged()
    }

    private fun reset() {
        getData().map { it.selected = false }
    }
}