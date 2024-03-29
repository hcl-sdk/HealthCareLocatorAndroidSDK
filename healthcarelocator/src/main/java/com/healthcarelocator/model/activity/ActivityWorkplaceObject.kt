package com.healthcarelocator.model.activity

import android.os.Parcel
import android.os.Parcelable
import com.healthcarelocator.extensions.isNullable
import com.iqvia.onekey.GetActivitiesQuery
import com.iqvia.onekey.GetActivityByIdQuery

class ActivityWorkplaceObject(var id: String = "", var name: String = "", var localPhone: String = "",
                              var intlPhone: String = "", var emailAddress: String = "", var address: AddressObject? = null) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readParcelable(AddressObject::class.java.classLoader)) {
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.apply {
            writeString(id)
            writeString(name)
            writeString(localPhone)
            writeString(intlPhone)
            writeString(emailAddress)
            writeParcelable(address, Parcelable.PARCELABLE_WRITE_RETURN_VALUE)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ActivityWorkplaceObject> {
        override fun createFromParcel(parcel: Parcel): ActivityWorkplaceObject {
            return ActivityWorkplaceObject(parcel)
        }

        override fun newArray(size: Int): Array<ActivityWorkplaceObject?> {
            return arrayOfNulls(size)
        }
    }

    /**
     * Convert data from GraphQL
     */

    fun parse(data: GetActivityByIdQuery.Workplace?): ActivityWorkplaceObject {
        if (data.isNullable()) return this
        this.id = data!!.id()
        this.name = data.name()
        this.localPhone = data.localPhone() ?: ""
        this.intlPhone = data.intlPhone() ?: ""
        this.emailAddress = data.emailAddress() ?: ""
        this.address = AddressObject().parse(data.address())
        return this
    }

    fun parse(data: GetActivitiesQuery.Workplace?): ActivityWorkplaceObject {
        if (data.isNullable()) return this
        this.id = data!!.id()
        this.name = data.name()
        this.address = AddressObject().parse(data.address())
        return this
    }

    fun parse(data: GetActivityByIdQuery.Workplace1?, activityId: String): ActivityWorkplaceObject {
        if (data.isNullable()) return this
        this.id = data!!.id()
        this.name = data.name()
        this.address = AddressObject().parse(data.address(), activityId)
        return this
    }
}