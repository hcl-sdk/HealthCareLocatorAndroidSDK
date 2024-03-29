package com.healthcarelocator.adapter.search

import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.healthcarelocator.R
import com.healthcarelocator.adapter.HCLAdapter
import com.healthcarelocator.adapter.HCLViewHolder
import com.healthcarelocator.extensions.getColor
import com.healthcarelocator.extensions.getVisibility
import com.healthcarelocator.extensions.isNotNullAndEmpty
import com.healthcarelocator.extensions.isNotNullable
import com.healthcarelocator.model.HealthCareLocatorSpecialityObject
import com.healthcarelocator.state.HealthCareLocatorSDK
import com.iqvia.onekey.GetIndividualByNameQuery
import kotlinx.android.synthetic.main.layout_item_individual.view.*
import kotlinx.android.synthetic.main.layout_one_key_hcp_item.view.*

class IndividualAdapter : HCLAdapter<Any,
        RecyclerView.ViewHolder>(arrayListOf(R.layout.layout_item_individual, R.layout.layout_one_key_hcp_item)) {
    private val speciality = 0
    private val hcp = 1
    private var keyword: String = ""
    private val theme = HealthCareLocatorSDK.getInstance().getConfiguration()
    var onIndividualClickedListener: OnIndividualClickedListener? = null

    override fun initViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            when (viewType) {
                hcp -> HcpVH(LayoutInflater.from(parent.context).inflate(layoutIds[1], parent, false))
                else -> IndividualVH(LayoutInflater.from(parent.context).inflate(layoutIds[0], parent, false))
            }

    override fun getItemViewType(position: Int): Int {
        return if (getData()[position] is HealthCareLocatorSpecialityObject) speciality
        else hcp
    }

    inner class IndividualVH(itemView: View) :
            HCLViewHolder<HealthCareLocatorSpecialityObject>(itemView) {
        override fun bind(position: Int, data: HealthCareLocatorSpecialityObject) {
            itemView.apply {
                val name: String = data.longLbl
                tvSpeciality.text = SpannableStringBuilder(name).apply {
                    val indexOf = name.toLowerCase().indexOf(keyword.toLowerCase())
                    if (indexOf >= 0)
                        setSpan(ForegroundColorSpan(theme.colorPrimary.getColor()), indexOf,
                                indexOf.plus(keyword.length), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                setOnClickListener {
                    onIndividualClickedListener?.onIndividualClickedListener(data)
                }
            }
        }
    }

    inner class HcpVH(itemView: View) :
            HCLViewHolder<GetIndividualByNameQuery.Individual>(itemView) {
        override fun bind(position: Int, data: GetIndividualByNameQuery.Individual) {
            itemView.apply {
                var name = ""
                if (data.isNotNullable() && data.firstName().isNotNullAndEmpty())
                    name += data.firstName() + " "
                if (data.isNotNullable() && data.middleName().isNotNullAndEmpty())
                    name += data.middleName() + " "
                if (data.isNotNullable() && data.lastName().isNotNullAndEmpty())
                    name += data.lastName()
                tvName.text = SpannableStringBuilder(name).apply {
                    val indexOf = name.toLowerCase().indexOf(keyword.toLowerCase())
                    if (indexOf >= 0)
                        setSpan(ForegroundColorSpan(theme.colorPrimary.getColor()), indexOf,
                                indexOf.plus(keyword.length), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                val specialty = data.professionalType().label()
                val address = data.mainActivity().workplace().address()?.run {
                    "${longLabel()}, ${postalCode()} ${city().label()}"
                } ?: ""
                ivArrow.setColorFilter(theme.colorSecondary.getColor())
                tvHCPSpeciality.visibility = specialty.isNotEmpty().getVisibility()
                tvHCPAddress.visibility = address.isNotEmpty().getVisibility()
                tvHCPSpeciality.text = specialty
                tvHCPAddress.text = address
                setOnClickListener {
                    onIndividualClickedListener?.onHCPClickedListener(data)
                }
            }
        }
    }

    fun setKeyword(key: String) {
        this.keyword = key
    }

    interface OnIndividualClickedListener {
        fun onIndividualClickedListener(data: HealthCareLocatorSpecialityObject)
        fun onHCPClickedListener(data: GetIndividualByNameQuery.Individual)
    }
}