package com.healthcarelocator.adapter.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.healthcarelocator.R
import com.healthcarelocator.adapter.HCLAdapter
import com.healthcarelocator.adapter.HCLViewHolder
import com.healthcarelocator.extensions.*
import com.healthcarelocator.model.activity.ActivityObject
import com.healthcarelocator.model.config.HealthCareLocatorCustomObject
import com.healthcarelocator.state.HealthCareLocatorSDK
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.layout_one_key_last_consulted.view.*

class LastConsultedAdapter(private val theme: HealthCareLocatorCustomObject =
                                   HealthCareLocatorSDK.getInstance().getConfiguration()) :
        HCLAdapter<ActivityObject, LastConsultedAdapter.LastSearchVH>(arrayListOf(R.layout.layout_one_key_last_consulted)) {
    var onItemRemovedListener: (data: ActivityObject, position: Int) -> Unit = { _, _ -> }
    var onItemClickedListener: (data: ActivityObject) -> Unit = {}
    override fun initViewHolder(parent: ViewGroup, viewType: Int): LastSearchVH =
            LastSearchVH(LayoutInflater.from(parent.context).inflate(layoutIds[0], parent, false))

    inner class LastSearchVH(itemView: View) : HCLViewHolder<ActivityObject>(itemView) {
        override fun bind(position: Int, data: ActivityObject) {
            itemView.apply {
                var name = ""
                if (data.individual.isNotNullable() && data.individual?.firstName.isNotNullAndEmpty())
                    name += data.individual?.firstName + " "
                if (data.individual.isNotNullable() && data.individual?.middleName.isNotNullAndEmpty())
                    name += data.individual?.middleName + " "
                if (data.individual.isNotNullable() && data.individual?.lastName.isNotNullAndEmpty())
                    name += data.individual?.lastName
                tvDoctorName.text = name
                tvSpeciality.text = data.individual?.professionalType?.label ?: ""
                tvAddress.text = data.workplace?.address?.getAddress() ?: ""
                tvCreateAt.text = data.createdDate
                tvCreateAt.textSize = theme.fontSmall.size.toFloat()

                ivClear.setIconFromDrawableId(theme.iconCross, true, theme.colorGrey.getColor())
                ivClear.setOnClickListener {
                    remove(position)
                    onItemRemovedListener(data, position)
                }
                setOnClickListener {
                    onItemClickedListener(data)
                }
                bottomLine.visibility = (position != getData().size - 1).getVisibility()
            }
        }
    }
}