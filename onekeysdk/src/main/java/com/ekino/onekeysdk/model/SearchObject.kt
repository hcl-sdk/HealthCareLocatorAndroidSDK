package com.ekino.onekeysdk.model

import android.os.Parcel
import android.os.Parcelable
import com.ekino.onekeysdk.model.map.OneKeyPlace
import com.google.gson.annotations.SerializedName

class SearchObject(@SerializedName("speciality") var speciality: OneKeySpecialityObject? = null,
                   @SerializedName("place") var place: OneKeyPlace? = null,
                   @SerializedName("createdAt") var createdAt: Long = System.currentTimeMillis(),
                   @SerializedName("createdDate") var createdDate: String = "") : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readParcelable(OneKeySpecialityObject::class.java.classLoader),
            parcel.readParcelable(OneKeyPlace::class.java.classLoader),
            parcel.readLong(),
            parcel.readString() ?: "") {
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.apply {
            writeLong(createdAt)
            writeString(createdDate)
            writeParcelable(speciality, Parcelable.PARCELABLE_WRITE_RETURN_VALUE)
            writeParcelable(place, Parcelable.PARCELABLE_WRITE_RETURN_VALUE)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SearchObject> {
        override fun createFromParcel(parcel: Parcel): SearchObject {
            return SearchObject(parcel)
        }

        override fun newArray(size: Int): Array<SearchObject?> {
            return arrayOfNulls(size)
        }
    }
}