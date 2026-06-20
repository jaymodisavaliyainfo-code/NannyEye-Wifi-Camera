package monitoringcamera.transmitterconnect.officeconnectcamera

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import monitoringcamera.transmitterconnect.officeconnectcamera.FacebookAds.Companion.adshow
import monitoringcamera.transmitterconnect.officeconnectcamera.FacebookAds.Companion.dataItem

class AppOpen(
    private val app: App
) : Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    private var currentActivity: Activity? = null
    private var pause = 1

    init {
        app.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    // ---------------- Activity lifecycle ----------------

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity === activity) {
            currentActivity = null
        }
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}

    // ---------------- App lifecycle ----------------

    override fun onStart(owner: LifecycleOwner) {
        showAppOpenAdIfNeeded()
    }

    // ---------------- Ad logic ----------------

    private fun showAppOpenAdIfNeeded() {
        val activity = currentActivity ?: return

        if (dataItem.isNullOrEmpty()) return
        if (!AdsDataHolder.adsData.AppopenOnoff.equals("on", ignoreCase = true)) return
        if (adshow) return

        val imageUrl = when (pause) {
            1 ->AdsDataHolder.adsData.qurekaAppopenImgUrl1
            2 -> AdsDataHolder.adsData.qurekaAppopenImgUrl2
            3 -> AdsDataHolder.adsData.qurekaAppopenImgUrl3
            4 -> AdsDataHolder.adsData.qurekaAppopenImgUrl4
            5 -> AdsDataHolder.adsData.qurekaAppopenImgUrl5
            else -> AdsDataHolder.adsData.qurekaAppopenImgUrl1
        }

        FacebookAds.getInstance(activity).ShowAppopenAds(activity,imageUrl, object : MyCallbackAppopen {
            override fun onCall() {
                pause = if (pause == 5) 1 else pause + 1
                currentActivity = null
            }
        })

    }
}
