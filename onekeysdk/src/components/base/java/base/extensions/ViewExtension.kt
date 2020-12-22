package base.extensions

import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

fun AppCompatActivity.addFragment(containerId: Int, fragment: Fragment, addBackStack: Boolean = false) {
    with(supportFragmentManager) {
        val transaction = beginTransaction()
        if (addBackStack)
            transaction.replace(containerId, fragment, fragment::class.java.simpleName)
                    .addToBackStack(fragment::class.java.simpleName)
                    .commitAllowingStateLoss()
        else
            transaction.replace(containerId, fragment, fragment::class.java.simpleName)
                    .commitAllowingStateLoss()
    }
}

fun AppCompatActivity.pushFragment(containerId: Int, fragment: Fragment, addBackStack: Boolean = false) {
    with(supportFragmentManager) {
        val transaction = beginTransaction()
        if (addBackStack)
            transaction.add(containerId, fragment, fragment::class.java.simpleName)
                    .addToBackStack(fragment::class.java.simpleName)
                    .commitAllowingStateLoss()
        else
            transaction.add(containerId, fragment, fragment::class.java.simpleName)
                    .commitAllowingStateLoss()
    }
}

fun Fragment.addChildFragment(containerId: Int, fragment: Fragment) {
    with(childFragmentManager) {
        val transaction = beginTransaction()
        transaction.replace(containerId, fragment, fragment::class.java.simpleName)
                .commitAllowingStateLoss()
    }
}

/**
 * Run a runnable on the Main (UI) Thread.
 * @param runnable the runnable
 */
fun runOnUiThread(runnable: Runnable) {
    if (Looper.myLooper() != Looper.getMainLooper()) {
        Handler(Looper.getMainLooper()).post(runnable)
    } else {
        runnable.run()
    }
}