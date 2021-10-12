package com.healthcarelocator.adapter.search

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.healthcarelocator.R
import com.healthcarelocator.adapter.HCLAdapter
import com.healthcarelocator.adapter.HCLViewHolder
import com.healthcarelocator.extensions.*
import com.healthcarelocator.model.activity.ActivityObject
import com.healthcarelocator.state.HealthCareLocatorSDK
import kotlinx.android.synthetic.main.layout_search_item.view.*

class SearchAdapter(private val screenWidth: Int = -1, private val speciality: String = "") :
        HCLAdapter<ActivityObject, SearchAdapter.SearchVH>(arrayListOf(R.layout.layout_search_item)) {
    private var selectedPosition = -1
    private val themeConfig by lazy { HealthCareLocatorSDK.getInstance().getConfiguration() }
    private val darkMode = themeConfig.darkMode
    var onHCPCardClickedListener: (data: ActivityObject) -> Unit = {}
    var isPlaceAvailable: Boolean = false
    private val config = HealthCareLocatorSDK.getInstance().getConfiguration()

    override fun initViewHolder(parent: ViewGroup, viewType: Int): SearchVH =
            SearchVH(LayoutInflater.from(parent.context).inflate(layoutIds[0], parent, false))

    inner class SearchVH(itemView: View) : HCLViewHolder<ActivityObject>(itemView) {
        override fun bind(position: Int, data: ActivityObject) {
            itemView.apply {
                if (screenWidth > 0)
                    itemView.post {
                        val lp = itemView.layoutParams
                        lp.width = (screenWidth * 0.85f).toInt()
                        itemView.layoutParams = lp
                    }
                var name = ""
                if (data.individual.isNotNullable() && data.individual?.firstName.isNotNullAndEmpty())
                    name += data.individual?.firstName + " "
                if (data.individual.isNotNullable() && data.individual?.middleName.isNotNullAndEmpty())
                    name += data.individual?.middleName + " "
                if (data.individual.isNotNullable() && data.individual?.lastName.isNotNullAndEmpty())
                    name += data.individual?.lastName
                tvName.text = name
                var specialtyDisplay = ""
                if (speciality.isNotNullable() && speciality.isNotEmpty()) {
                    for (i in data.individual?.specialties!!.indices) {
                        if (speciality.equals(data.individual!!.specialties[i].toString(), true)) {
                            specialtyDisplay = data.individual!!.specialties[i].label
                        }
                    }
                } else {
                    specialtyDisplay = data.individual!!.specialties[0].label
                }
                tvSpeciality.text = specialtyDisplay
                tvAddress.text = data.workplace?.address?.getAddress() ?: ""
                if (isPlaceAvailable) {
                    tvDistance.visibility = View.VISIBLE
                    if (config.getDistanceUnit() == "mi") {
                        if (config.convertMeterToMile(data.distance) < 1) {
                            tvDistance.text = String.format(itemView.context.getString(R.string.hcl_distance_unit_android), config.roundingNumber(config.convertMeterToFoot(data.distance)), "ft")
                        } else {
                            tvDistance.text = String.format(itemView.context.getString(R.string.hcl_distance_unit_android), config.roundingNumber(config.convertMeterToMile(data.distance)), config.getDistanceUnit())
                        }
                    } else {
                        if (config.convertMeterToKilometer(data.distance) < 1) {
                            tvDistance.text = String.format(itemView.context.getString(R.string.hcl_distance_unit_android), config.roundingNumber(data.distance), "m")
                        } else {
                            tvDistance.text = String.format(itemView.context.getString(R.string.hcl_distance_unit_android), config.roundingNumber(config.convertMeterToKilometer(data.distance)), config.getDistanceUnit())
                        }
                    }
                } else tvDistance.visibility = View.GONE
                ivArrow.setIconFromDrawableId(themeConfig.iconArrowRight)
                ivArrow.setColorFilter(themeConfig.colorSecondary.getColor())
                setOnClickListener {
                    onHCPCardClickedListener(data)
                }
                if (data.selected)
                    setBackgroundWithCorner(
                            if (darkMode) themeConfig.darkModeColor.getColor() else Color.WHITE,
                            themeConfig.colorMarkerSelected.getColor(), 12f, 8)
                else setBackgroundWithCorner(
                        if (darkMode) themeConfig.darkModeColor.getColor() else Color.WHITE,
                        themeConfig.colorCardBorder.getColor(), 12f, 3)
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

    fun reset() {
        getData().map { it.selected = false }
    }
}