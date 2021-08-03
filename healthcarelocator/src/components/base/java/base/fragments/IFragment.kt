package base.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment

abstract class IFragment : Fragment() {
    open var fragmentTitle = ""
    open val locationSelection = "Pref.locationSelection"
    open val isLocationSelection = "Pref.isLocationSelection"
    companion object {
        lateinit var sharedPreferences: SharedPreferences
    }

    open fun onUpdateFragment() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = context!!.getSharedPreferences("OneKeySDK", Context.MODE_PRIVATE)
//        activity?.onBackPressedDispatcher?.addCallback(activity!!, object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                isEnabled = false
//                activity?.onBackPressed()
//            }
//
//        })
    }

    open fun shouldInterceptBackPress(): Boolean = false
}
