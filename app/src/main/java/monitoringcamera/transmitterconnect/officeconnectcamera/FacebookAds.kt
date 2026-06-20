package monitoringcamera.transmitterconnect.officeconnectcamera

import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.facebook.ads.Ad
import com.facebook.ads.AdOptionsView
import com.facebook.ads.InterstitialAdListener
import com.facebook.ads.NativeAdBase
import com.facebook.ads.NativeAdLayout
import com.facebook.ads.NativeAdListener
import com.facebook.ads.NativeBannerAd
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.gson.Gson
import com.onesignal.OneSignal
import monitoringcamera.transmitterconnect.officeconnectcamera.App.Companion.fetchdatastring
import monitoringcamera.transmitterconnect.officeconnectcamera.FacebookAds.Companion.ADMOBInterstitialAd
import monitoringcamera.transmitterconnect.officeconnectcamera.FacebookAds.Companion.Admob
import monitoringcamera.transmitterconnect.officeconnectcamera.FacebookAds.Companion.Admob_small_native_banner_Ad
import monitoringcamera.transmitterconnect.officeconnectcamera.FacebookAds.Companion.Appopen
import monitoringcamera.transmitterconnect.officeconnectcamera.FacebookAds.Companion.FB
import monitoringcamera.transmitterconnect.officeconnectcamera.FacebookAds.Companion.FB_interstitialAd
import monitoringcamera.transmitterconnect.officeconnectcamera.FacebookAds.Companion.Local
import monitoringcamera.transmitterconnect.officeconnectcamera.FacebookAds.Companion.Qureka
import monitoringcamera.transmitterconnect.officeconnectcamera.FacebookAds.Companion.admobNativeAds
import monitoringcamera.transmitterconnect.officeconnectcamera.FacebookAds.Companion.admob_appOpenAd
import monitoringcamera.transmitterconnect.officeconnectcamera.FacebookAds.Companion.dataItem
import monitoringcamera.transmitterconnect.officeconnectcamera.FacebookAds.Companion.fbNativeAds
import monitoringcamera.transmitterconnect.officeconnectcamera.FacebookAds.Companion.fbNativeBannerAds
import monitoringcamera.transmitterconnect.officeconnectcamera.FacebookAds.Companion.fbadView
import monitoringcamera.transmitterconnect.officeconnectcamera.FacebookAds.Companion.fullScreenContentCallback
import monitoringcamera.transmitterconnect.officeconnectcamera.FacebookAds.Companion.googleBannerAd
import monitoringcamera.transmitterconnect.officeconnectcamera.FacebookAds.Companion.isFBBannerLoaded
import monitoringcamera.transmitterconnect.officeconnectcamera.FacebookAds.Companion.isFBInterLoaded
import monitoringcamera.transmitterconnect.officeconnectcamera.FacebookAds.Companion.isFBNativeLoaded
import monitoringcamera.transmitterconnect.officeconnectcamera.FacebookAds.Companion.isFBNative_Banner_Loaded
import monitoringcamera.transmitterconnect.officeconnectcamera.FacebookAds.Companion.isGoogleBannerLoaded
import monitoringcamera.transmitterconnect.officeconnectcamera.FacebookAds.Companion.isGoogleInterLoaded
import monitoringcamera.transmitterconnect.officeconnectcamera.FacebookAds.Companion.isadmobNativeLoaded
import monitoringcamera.transmitterconnect.officeconnectcamera.FacebookAds.Companion.isadmob_appopen_Loaded
import monitoringcamera.transmitterconnect.officeconnectcamera.FacebookAds.Companion.isadmob_small_native_banner_Loaded
import monitoringcamera.transmitterconnect.officeconnectcamera.FacebookAds.Companion.myCallbackappopen
import monitoringcamera.transmitterconnect.officeconnectcamera.RetrofitResponce.AdListResponse
import monitoringcamera.transmitterconnect.officeconnectcamera.RetrofitResponce.DataItem
import monitoringcamera.transmitterconnect.officeconnectcamera.retrofit.APIClient
import monitoringcamera.transmitterconnect.officeconnectcamera.retrofit.RetrofitInterface
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class FacebookAds public constructor(
    val context: Context
) {

    companion object {
        val dataItem: ArrayList<DataItem> = arrayListOf()
        lateinit var fullScreenContentCallback: FullScreenContentCallback
        var Admob: String = "admob"
        var Admobnativefull: String = "admobnativefull"
        var FB: String = "fb"
        var Local: String = "local"
        var Qureka: String = "qureka"
        var Appopen: String = "appopen"
        var Off: String = "off"
        lateinit var admob_appOpenAd: AppOpenAd
        var isGoogleInterLoaded = false
        lateinit var ADMOBInterstitialAd: InterstitialAd
        lateinit var FB_interstitialAd: com.facebook.ads.InterstitialAd
        var isFBInterLoaded: Boolean = false
        var adshow: Boolean = false
        var isadmobNativeLoaded: Boolean = false
        var isFBNativeLoaded: Boolean = false
        var isadmob_small_native_banner_Loaded: Boolean = false
        var isFBNative_Banner_Loaded: Boolean = false
        var isGoogleBannerLoaded: Boolean = false
        var isFBBannerLoaded: Boolean = false
        var isadmob_appopen_Loaded: Boolean = false
        val admobNativeAds = ArrayList<NativeAd>()
        val Admob_small_native_banner_Ad = ArrayList<NativeAd>()

        val fbNativeAds = ArrayList<com.facebook.ads.NativeAd>()

        val fbNativeBannerAds = ArrayList<NativeBannerAd>()
        lateinit var googleBannerAd: AdView
        lateinit var fbadView: com.facebook.ads.AdView
        var parentView: ViewGroup? = null

        lateinit var myCallbackappopen: MyCallbackAppopen

        @Volatile
        private var instance: FacebookAds? = null

        fun getInstance(context: Context): FacebookAds {
            return instance ?: synchronized(this) {
                instance ?: FacebookAds(context.applicationContext).also {
                    instance = it
                }
            }
        }

        // ---------------- NETWORK ----------------

        fun isOnline(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo: NetworkInfo? = cm.activeNetworkInfo
            return netInfo != null && netInfo.isConnectedOrConnecting
        }
    }

    // ---------------- FETCH ADS DATA ----------------

    fun AdsData(activity: Activity, onComplete: (() -> Unit)? = null) {
        val api = APIClient.client.create(RetrofitInterface::class.java)
        api.getAdsDetail(context.packageName, fetchdatastring())
            .enqueue(object : Callback<AdListResponse> {

                override fun onResponse(
                    call: Call<AdListResponse>, response: Response<AdListResponse>
                ) {
                    val list = response.body()?.data ?: run {
                        onComplete?.invoke()
                        return
                    }
                    Log.e("FacebookAds", "onResponse $$$$$: " + response.body()?.data)
                    dataItem.clear()
                    dataItem.addAll(list)

                    val json = Gson().toJson(dataItem[0])
                    Utils.saveStringToPreference(context, "adresponse", json)


                    AdsDataHolder.adsData = Utils.getResponse(context)
                    Log.e("FacebookAds", "onResponse $$$$$: " + AdsDataHolder.adsData.redirectApp)

                    if (!AdsDataHolder.adsData.redirectApp.isNullOrEmpty()) {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse(
                                    "https://play.google.com/store/apps/details?id=${AdsDataHolder.adsData.redirectApp}"
                                )
                            })
                    } else {
                        Log.e("FacebookAds", "onResponse $$$$$: Else block")
                        preloadAds(activity)
                        onComplete?.invoke()
                    }
                }

                override fun onFailure(call: Call<AdListResponse>, t: Throwable) {
                    Toast.makeText(context, "Responce Fail", Toast.LENGTH_SHORT).show()
                    onComplete?.invoke()
                    activity.startActivity(Intent(activity, MainActivity::class.java).apply {
                        putExtra("skip_splash", true)
                    })
                    activity.finish()
                }
            })
    }

    // ---------------- INSTALL COUNTER ----------------

    external fun updateAppData(): String

    fun installCounter(activity: Activity) {
        val api = APIClient.client.create(RetrofitInterface::class.java)
        api.updateCounter(activity.packageName, updateAppData()).enqueue(object : Callback<Any> {

            override fun onResponse(
                call: Call<Any>, response: Response<Any>
            ) {
                // same behavior as Java (no-op)
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                call.cancel()
            }
        })
    }

    fun ShowInterstitial(
        activity: Activity,
        adMode: String,
        fbInterId: String,
        qurekaInterImg: String,
        onAdFinished: MyCallback
    ) {
        // Fetch ads data if empty
        if (dataItem.isEmpty()) {
            AdsDataHolder.adsData = Utils.getResponse(activity)
            AdsDataHolder.adsData.let { dataItem.add(it) }
        }

        if (dataItem.isNotEmpty()) {
            adshow = true

            when (adMode.lowercase()) {
                Admob.lowercase() -> {
                    showInterstitialAdmob(activity, fbInterId, qurekaInterImg, onAdFinished)
                }

                FB.lowercase() -> {
                    showInterstitialFB(activity, fbInterId, qurekaInterImg, onAdFinished)
                }

                Local.lowercase() -> {
                    showLocalInterAd(activity, onAdFinished)
                }

                Qureka.lowercase() -> {
                    val firstItem = dataItem[0]
                    if (firstItem.QurekaCromeintentonoff.equals("on", ignoreCase = true)) {
                        onAdFinished.onCall()
                        adshow = false
                        myCustom(activity, dataItem.get(0).qurekaUrl1)
                    } else {
                        showQurekaInterad(activity, qurekaInterImg, onAdFinished)
                    }
                }

                Off.lowercase() -> {
                    onAdFinished.onCall()
                }

                else -> {
                    onAdFinished.onCall()
                }
            }
        } else {
            onAdFinished.onCall()
        }
    }

    fun showInterstitialFB(
        activity: Activity,
        fbInterId: String,
        qurekaInterImg: String,
        onAdFinished: MyCallback
    ) {
        if (isFBInterLoaded) {
            adshow = false
            FB_interstitialAd.show()
            isFBInterLoaded = false
            loadInterAdsFB(activity, fbInterId)
        } else if (isGoogleInterLoaded) {
            ADMOBInterstitialAd.setFullScreenContentCallback(
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        adshow = false
                        onAdFinished.onCall()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        super.onAdFailedToShowFullScreenContent(adError)
                    }
                })
            ADMOBInterstitialAd.show(activity)
            isGoogleInterLoaded = false
            loadInterAdsAdmob(activity)
            loadInterAdsFB(activity, fbInterId)
        } else {
            adshow = false
            loadInterAdsFB(activity, fbInterId)
            loadInterAdsAdmob(activity)
            if (dataItem.get(0).QurekaAdmobfaileOnoff.equals("on", ignoreCase = true)) {
                showQurekaInterad(activity, qurekaInterImg, onAdFinished)
            } else {
                onAdFinished.onCall()
            }
        }
    }

    fun loadUrlsSequentially(activity: Activity, onAdFinished: MyCallback) {
        if (dataItem != null && dataItem.get(0).qurekaUrl1Onoff.equals("on")) {
            myCustom(activity, dataItem.get(0).qurekaUrl1) {
                loadUrl2(activity, onAdFinished)
            }
        } else {
            loadUrl2(activity, onAdFinished)
        }
    }

    private fun loadUrl2(activity: Activity, onAdFinished: MyCallback) {
        if (dataItem != null && dataItem.get(0).qurekaUrl2Onoff.equals("on")) {
            myCustom(activity, dataItem.get(0).qurekaUrl2) {
                loadUrl3(activity, onAdFinished)
            }
        } else {
            loadUrl3(activity, onAdFinished)
        }
    }

    private fun loadUrl3(activity: Activity, onAdFinished: MyCallback) {
        if (dataItem != null && dataItem.get(0).qurekaUrl3Onoff.equals("on")) {
            myCustom(activity, dataItem.get(0).qurekaUrl3) {
                finishFlow(onAdFinished)
            }
        } else {
            finishFlow(onAdFinished)
        }
    }

    private fun finishFlow(onAdFinished: MyCallback) {
        onAdFinished.onCall()
    }

    fun showInterstitialAdmob(
        activity: Activity,
        fbInterId: String,
        qurekaInterImg: String,
        onAdFinished: MyCallback
    ) {
        if (isGoogleInterLoaded) {
            ADMOBInterstitialAd.setFullScreenContentCallback(object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    adshow = false
                    loadUrlsSequentially(activity, onAdFinished)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    adshow = false
                    onAdFinished.onCall()
                }
            })
            ADMOBInterstitialAd.show(activity)
            isGoogleInterLoaded = false
            loadInterAdsAdmob(activity)
            adshow = true

        } else if (isFBInterLoaded) {
            adshow = false
            FB_interstitialAd?.show()
            isFBInterLoaded = false
            loadInterAdsAdmob(activity)
            loadInterAdsFB(activity, fbInterId)

        } else {
            adshow = false
            loadInterAdsAdmob(activity)
            loadInterAdsFB(activity, fbInterId)
            if (dataItem.get(0).QurekaAdmobfaileOnoff.equals("on", ignoreCase = true)) {
                showQurekaInterad(activity, qurekaInterImg, onAdFinished)
            } else {
                onAdFinished.onCall()
            }
        }
    }

    fun showLocalInterAd(activity: Activity, onAdFinished: MyCallback) {
        adshow = false
        val dialog = Dialog(activity, R.style.FullWidth_Dialog)
        dialog.setContentView(
            LayoutInflater.from(activity).inflate(R.layout.local_inter_ad, null as ViewGroup?)
        )
        dialog.setCancelable(false)
        dialog.getWindow()!!.setLayout(-1, -1)
        dialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(0))
        val animation = AnimationUtils.loadAnimation(
            activity,
            R.anim.slide_in_bottom
        )
        val linearLayout = dialog.findViewById<View?>(R.id.ad_close) as LinearLayout
        val textView = dialog.findViewById<View?>(R.id.install) as TextView
        val textView2 = dialog.findViewById<View?>(R.id.appname) as TextView
        val imageView = dialog.findViewById<View?>(R.id.app_icon) as ImageView
        val adbanner = dialog.findViewById<View?>(R.id.ad_banner) as ImageView
        val topad = dialog.findViewById<CardView?>(R.id.cvTopAd)
        topad.startAnimation(animation)

        if (dataItem != null) {
            textView2.setText(dataItem.get(0).LocalAppName)
            Glide.with(activity).load(dataItem.get(0).LocalAppIcon)
                .into(imageView)
            Glide.with(activity)
                .load(dataItem.get(0).LocalAppBanner).into(adbanner)
        }

        topad.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                onLocalIntent(activity)
            }
        })

        textView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                onLocalIntent(activity)
            }
        })

        linearLayout.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                adshow = false
                dialog.dismiss()
                onAdFinished.onCall()
            }
        })
        dialog.show()
    }

    fun showQurekaInterad(activity: Activity, qurekaInterImg: String, onAdFinished: MyCallback) {
        adshow = false
        val dialog = Dialog(activity, R.style.FullWidth_Dialog)
        dialog.setContentView(
            LayoutInflater.from(activity).inflate(R.layout.qureka_inter_ad, null as ViewGroup?)
        )
        dialog.setCancelable(false)
        dialog.getWindow()!!.setLayout(-1, -1)
        dialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(0))

        dialog.setOnKeyListener(object : DialogInterface.OnKeyListener {
            override fun onKey(dialog: DialogInterface, keyCode: Int, event: KeyEvent?): Boolean {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    onAdFinished.onCall()
                    dialog.dismiss()
                }
                return true
            }
        })
        val animation = AnimationUtils.loadAnimation(
            activity,
            R.anim.slide_in_bottom
        )
        val textView = dialog.findViewById<View?>(R.id.install) as TextView
        val linearLayout = dialog.findViewById<View?>(R.id.ad_close) as LinearLayout
        val textView2 = dialog.findViewById<View?>(R.id.appname) as TextView
        val imageView2 = dialog.findViewById<View?>(R.id.ad_banner) as ImageView
        val textView3 = dialog.findViewById<View?>(R.id.ad_body) as TextView
        val topad = dialog.findViewById<CardView?>(R.id.cvTopAd)
        val imageView3 = dialog.findViewById<ImageView?>(R.id.adclose_btn)
        topad.startAnimation(animation)

        if (dataItem.get(0).qurekaInterImgUrl1.equals(qurekaInterImg)) {
            Glide.with(activity).load(qurekaInterImg).into(imageView2)
            textView2.setText(dataItem.get(0).QurekaIntertitle1)
            textView3.setText(dataItem.get(0).QurekaIntersubTitle1)
        } else if (dataItem.get(0).qurekaInterImgUrl2.equals(qurekaInterImg)) {
            Glide.with(activity).load(qurekaInterImg).into(imageView2)
            textView2.setText(dataItem.get(0).QurekaIntertitle2)
            textView3.setText(dataItem.get(0).QurekaIntersubTitle2)
        } else if (dataItem.get(0).qurekaInterImgUrl3.equals(qurekaInterImg)) {
            Glide.with(activity).load(qurekaInterImg).into(imageView2)
            textView2.setText(dataItem.get(0).QurekaIntertitle3)
            textView3.setText(dataItem.get(0).QurekaIntersubTitle3)
        } else if (dataItem.get(0).qurekaInterImgUrl4.equals(qurekaInterImg)) {
            Glide.with(activity).load(qurekaInterImg).into(imageView2)
            textView2.setText(dataItem.get(0).QurekaIntertitle4)
            textView3.setText(dataItem.get(0).QurekaIntersubTitle4)
        } else if (dataItem.get(0).qurekaInterImgUrl5.equals(qurekaInterImg)) {
            Glide.with(activity).load(qurekaInterImg).into(imageView2)
            textView2.setText(dataItem.get(0).QurekaIntertitle5)
            textView3.setText(dataItem.get(0).QurekaIntersubTitle5)
        }

        topad.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                myCustom(activity, dataItem.get(0).qurekaUrl1)
            }
        })

        textView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                myCustom(activity, dataItem.get(0).qurekaUrl1)
            }
        })

        linearLayout.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                adshow = false
                dialog.dismiss()
                onAdFinished.onCall()
            }
        })

        imageView3.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                adshow = false
                dialog.dismiss()
                onAdFinished.onCall()
            }
        })
        dialog.show()
    }

    fun myCustom(activity: Activity, qurekaUrl: String) {
        try {
            val intent = Intent("android.intent.action.VIEW")
            val bundle = Bundle()
            bundle.putBinder(CustomTabsIntent.EXTRA_SESSION, null as IBinder?)
            intent.putExtras(bundle)
            intent.putExtra(
                CustomTabsIntent.EXTRA_TOOLBAR_COLOR,
                activity.getResources().getColor(R.color.dark_bg)
            )
            intent.putExtra(CustomTabsIntent.EXTRA_ENABLE_INSTANT_APPS, true)
            intent.setPackage("com.android.chrome")
            intent.setData(Uri.parse(qurekaUrl))
            activity.startActivity(intent)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }


    }

    var listener: (() -> Unit)? = null
    var isCustomTabOpened = false

    fun myCustom(activity: Context, url: String?, onComplete: () -> Unit) {
        if (url.isNullOrEmpty()) {
            onComplete()
            return
        }

        try {
            listener = onComplete
            isCustomTabOpened = true

            val intent = Intent(Intent.ACTION_VIEW)

            val bundle = Bundle()
            bundle.putBinder(CustomTabsIntent.EXTRA_SESSION, null)
            intent.putExtras(bundle)

            intent.putExtra(
                CustomTabsIntent.EXTRA_TOOLBAR_COLOR,
                activity.getResources().getColor(R.color.dark_bg)
            )

            intent.putExtra(CustomTabsIntent.EXTRA_ENABLE_INSTANT_APPS, true)
            intent.setPackage("com.android.chrome")
            intent.setData(Uri.parse(url))

            activity.startActivity(intent)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()

            onComplete()
        }
    }

    // Call this in Activity onResume()
    fun handleResume() {
        if (isCustomTabOpened && listener != null) {
            isCustomTabOpened = false

            val temp: (() -> Unit)? = listener
            listener = null

            temp?.invoke()
        }
    }


    // ---------------- native_ad ----------------

    fun ShowNativeAd(activity: Activity, qurekaNativeUrl1: String, native_id: ViewGroup) {
        if (dataItem != null && dataItem.isNotEmpty()) {

            when (dataItem[0].checkAdNative) {

                Admob -> {

                    if (isadmobNativeLoaded) {
                        val adView = LayoutInflater.from(context)
                            .inflate(R.layout.admob_native, null) as NativeAdView
                        native_id.removeAllViews()
                        native_id.addView(adView)
                        AdmobNativeAdView(admobNativeAds[0], adView)
                        isadmobNativeLoaded = false
                        PreloadAdmobNativeAd(activity)

                    } else if (isFBNativeLoaded) {
                        val adView = LayoutInflater.from(context)
                            .inflate(R.layout.fb_native, null) as NativeAdLayout
                        native_id.removeAllViews()
                        native_id.addView(adView)
                        Fb_Native_inflateAd(fbNativeAds[0], adView, activity)

                        isFBNativeLoaded = false
                        preloadFbNativeAd(activity)
                    } else {
                        PreloadAdmobNativeAd(activity)
                    }
                }

                FB -> {
                    if (isFBNativeLoaded) {

                        val adView = LayoutInflater.from(context)
                            .inflate(R.layout.fb_native, null) as NativeAdLayout
                        native_id.removeAllViews()
                        native_id.addView(adView)
                        Fb_Native_inflateAd(fbNativeAds[0], adView, activity)

                        isFBNativeLoaded = false
                        preloadFbNativeAd(activity)

                    } else if (isadmobNativeLoaded) {

                        val adView = LayoutInflater.from(context)
                            .inflate(R.layout.admob_native1, null) as NativeAdView
                        native_id.removeAllViews()
                        native_id.addView(adView)
                        AdmobNativeAdView(admobNativeAds[0], adView)

                        isadmobNativeLoaded = false
                        PreloadAdmobNativeAd(activity)

                    } else {
                        preloadFbNativeAd(activity)
                    }
                }

                Local -> {
                    ShowLocalNativeAd(activity, native_id)
                }

                Qureka -> {
                    ShowQurekaNativeAd(activity, qurekaNativeUrl1, native_id)
                }
            }
        }
    }

    fun ShowNativeAd1(activity: Activity, qurekaNativeUrl1: String, native_id: ViewGroup) {
        if (dataItem != null && dataItem.isNotEmpty()) {

            when (dataItem[0].checkAdNative) {

                Admob -> {

                    if (isadmobNativeLoaded) {
                        val adView = LayoutInflater.from(context)
                            .inflate(R.layout.admob_native1, null) as NativeAdView
                        native_id.removeAllViews()
                        native_id.addView(adView)
                        AdmobNativeAdView(admobNativeAds[0], adView)
                        isadmobNativeLoaded = false
                        PreloadAdmobNativeAd(activity)

                    } else if (isFBNativeLoaded) {
                        val adView = LayoutInflater.from(context)
                            .inflate(R.layout.fb_native, null) as NativeAdLayout
                        native_id.removeAllViews()
                        native_id.addView(adView)
                        Fb_Native_inflateAd(fbNativeAds[0], adView, activity)

                        isFBNativeLoaded = false
                        preloadFbNativeAd(activity)
                    } else {
                        PreloadAdmobNativeAd(activity)
                    }
                }

                FB -> {
                    if (isFBNativeLoaded) {

                        val adView = LayoutInflater.from(context)
                            .inflate(R.layout.fb_native, null) as NativeAdLayout
                        native_id.removeAllViews()
                        native_id.addView(adView)
                        Fb_Native_inflateAd(fbNativeAds[0], adView, activity)

                        isFBNativeLoaded = false
                        preloadFbNativeAd(activity)
                    } else if (isadmobNativeLoaded) {

                        val adView = LayoutInflater.from(context)
                            .inflate(R.layout.admob_native1, null) as NativeAdView
                        native_id.removeAllViews()
                        native_id.addView(adView)
                        AdmobNativeAdView(admobNativeAds[0], adView)

                        isadmobNativeLoaded = false
                        PreloadAdmobNativeAd(activity)

                    } else {
                        preloadFbNativeAd(activity)
                    }
                }

                Local -> {
                    ShowLocalNativeAd(activity, native_id)
                }

                Qureka -> {
                    ShowQurekaNativeAd(activity, qurekaNativeUrl1, native_id)
                }
            }
        }
    }

    fun AdmobNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {

        val mediaView = adView.findViewById<MediaView>(R.id.ad_media)
        val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
        val bodyView = adView.findViewById<TextView>(R.id.ad_body)
        val ctaView = adView.findViewById<TextView>(R.id.ad_call_to_action)
        val iconView = adView.findViewById<ImageView>(R.id.ad_app_icon)

        adView.mediaView = mediaView
        adView.headlineView = headlineView
        adView.bodyView = bodyView
        adView.callToActionView = ctaView
        adView.iconView = iconView

        // Headline (always required)
        headlineView.text = nativeAd.headline

        // Body
        if (nativeAd.body == null) {
            bodyView.visibility = View.INVISIBLE
        } else {
            bodyView.visibility = View.VISIBLE
            bodyView.text = nativeAd.body
        }

        // Call To Action
        if (nativeAd.callToAction == null) {
            ctaView.visibility = View.INVISIBLE
        } else {
            ctaView.visibility = View.VISIBLE
            ctaView.text = nativeAd.callToAction
        }

        // Icon
        if (nativeAd.icon == null) {
            iconView.visibility = View.GONE
        } else {
            iconView.setImageDrawable(nativeAd.icon!!.drawable)
            iconView.visibility = View.VISIBLE
        }

        adView.setNativeAd(nativeAd)
    }

    fun Fb_Native_inflateAd(
        nativeAd: com.facebook.ads.NativeAd,
        nativeAdLayout: NativeAdLayout,
        activity: Activity
    ) {
        nativeAd.unregisterView()

        val adChoicesContainer =
            nativeAdLayout.findViewById<LinearLayout>(R.id.ad_choices_container)

        val adOptionsView = AdOptionsView(activity, nativeAd, nativeAdLayout)
        adChoicesContainer.removeAllViews()
        adChoicesContainer.addView(adOptionsView, 0)

        val nativeAdMedia =
            nativeAdLayout.findViewById<com.facebook.ads.MediaView>(R.id.native_ad_media)

        val nativeAdIcon = nativeAdLayout.findViewById<ImageView>(R.id.native_ad_icon)

        val nativeAdTitle = nativeAdLayout.findViewById<TextView>(R.id.native_ad_title)

        val nativeAdSocialContext =
            nativeAdLayout.findViewById<TextView>(R.id.native_ad_social_context)

        val nativeAdBody = nativeAdLayout.findViewById<TextView>(R.id.native_ad_body)

        val sponsoredLabel = nativeAdLayout.findViewById<TextView>(R.id.native_ad_sponsored_label)

        val nativeAdCallToAction =
            nativeAdLayout.findViewById<TextView>(R.id.native_ad_call_to_action)

        nativeAdTitle.text = nativeAd.advertiserName
        nativeAdBody.text = nativeAd.adBodyText
        nativeAdSocialContext.text = nativeAd.adSocialContext
        nativeAdCallToAction.visibility =
            if (nativeAd.hasCallToAction()) View.VISIBLE else View.INVISIBLE
        nativeAdCallToAction.text = nativeAd.adCallToAction
        sponsoredLabel.text = nativeAd.sponsoredTranslation

        val clickableViews = ArrayList<View>()
        clickableViews.add(nativeAdTitle)
        clickableViews.add(nativeAdCallToAction)

        nativeAd.registerViewForInteraction(
            nativeAdLayout,
            nativeAdMedia,
            nativeAdIcon,
            clickableViews
        )
    }

    fun ShowLocalNativeAd(activity: Activity, nativeId: ViewGroup) {
        val viewGroup2 = LayoutInflater.from(activity)
            .inflate(R.layout.local_native_ad, null as ViewGroup?) as ViewGroup
        val rlbackground = viewGroup2.findViewById<RelativeLayout?>(R.id.background)
        val textView2 = viewGroup2.findViewById<View?>(R.id.ad_headline) as TextView
        val imageView = viewGroup2.findViewById<View?>(R.id.ad_app_icon) as ImageView
        val adbanner = viewGroup2.findViewById<View?>(R.id.ad_banner) as ImageView
        if (dataItem != null) {
            textView2.setText(dataItem.get(0).LocalAppName)
            Glide.with(activity).load(dataItem.get(0).LocalNativeAdbanner).into(adbanner)
            Glide.with(activity).load(dataItem.get(0).LocalAppIcon).into(imageView)
        }
        rlbackground.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                onLocalIntent(activity)
            }
        })
        nativeId.addView(viewGroup2)
    }

    fun ShowQurekaNativeAd(activity: Activity, qurekaNativeUrl1: String, nativeId: ViewGroup) {
        val viewGroup2 = LayoutInflater.from(activity)
            .inflate(R.layout.qureka_native_ad, null as ViewGroup?) as ViewGroup
        val rlbackground = viewGroup2.findViewById<RelativeLayout?>(R.id.background)
        val adbanner = viewGroup2.findViewById<ImageView?>(R.id.ad_banner)
        val adappicon = viewGroup2.findViewById<ImageView?>(R.id.ad_app_icon)
        val textView2 = viewGroup2.findViewById<TextView?>(R.id.ad_headline)
        val textView3 = viewGroup2.findViewById<TextView?>(R.id.ad_body)

        if (dataItem.get(0).qurekaNativeUrl1.equals(qurekaNativeUrl1)) {
            Glide.with(activity).load(qurekaNativeUrl1).into(adbanner)
            Glide.with(activity).load(dataItem.get(0).QurekaNativeAppicon1).into(adappicon)
            textView2.setText(dataItem.get(0).QurekaNativeTitle1)
            textView3.setText(dataItem.get(0).QurekaNativesubTitle1)
        } else if (dataItem.get(0).qurekaNativeUrl2.equals(qurekaNativeUrl1)) {
            Glide.with(activity).load(qurekaNativeUrl1).into(adbanner)
            Glide.with(activity).load(dataItem.get(0).QurekaNativeAppicon2).into(adappicon)
            textView2.setText(dataItem.get(0).QurekaNativeTitle2)
            textView3.setText(dataItem.get(0).QurekaNativesubTitle2)
        } else if (dataItem.get(0).qurekaNativeUrl3.equals(qurekaNativeUrl1)) {
            Glide.with(activity).load(qurekaNativeUrl1).into(adbanner)
            Glide.with(activity).load(dataItem.get(0).QurekaNativeAppicon3).into(adappicon)
            textView2.setText(dataItem.get(0).QurekaNativeTitle3)
            textView3.setText(dataItem.get(0).QurekaNativesubTitle3)
        } else if (dataItem.get(0).qurekaNativeUrl4.equals(qurekaNativeUrl1)) {
            Glide.with(activity).load(qurekaNativeUrl1).into(adbanner)
            Glide.with(activity).load(dataItem.get(0).QurekaNativeAppicon4).into(adappicon)
            textView2.setText(dataItem.get(0).QurekaNativeTitle4)
            textView3.setText(dataItem.get(0).QurekaNativesubTitle4)
        } else if (dataItem.get(0).qurekaNativeUrl5.equals(qurekaNativeUrl1)) {
            Glide.with(activity).load(qurekaNativeUrl1).into(adbanner)
            Glide.with(activity).load(dataItem.get(0).QurekaNativeAppicon5).into(adappicon)
            textView2.setText(dataItem.get(0).QurekaNativeTitle5)
            textView3.setText(dataItem.get(0).QurekaNativesubTitle5)
        }

        rlbackground.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (dataItem.get(0).qurekaUrl1 != null) {
                    myCustom(
                        activity, dataItem.get(0).qurekaUrl1
                    )
                }
            }
        })
        nativeId.addView(viewGroup2)
    }

    fun onLocalIntent(activity: Activity) {
        if (dataItem != null) {
            try {
                activity.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + dataItem.get(0).LocalPackgename)
                    )
                )
            } catch (anfe: ActivityNotFoundException) {
                activity.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + dataItem.get(0).LocalPackgename)
                    )
                )
            }
        }
    }

    // ---------------- native_full_ad ----------------
    fun ShowNativeAdfull(activity: Activity, native_id: ViewGroup) {
        if (dataItem != null && dataItem.size > 0) {

            if (dataItem[0].checkAdNativefull.equals(Admobnativefull, true)) {

                if (isadmobNativeLoaded && admobNativeAds.isNotEmpty()) {

                    val adView = LayoutInflater.from(context).inflate(
                        R.layout.admob_nativefull, null
                    ) as NativeAdView

                    native_id.removeAllViews()
                    native_id.addView(adView)

                    AdmobNativeAdView(
                        admobNativeAds[0], adView
                    )

                    isadmobNativeLoaded = false
                    PreloadAdmobNativeAd(activity)

                } else {
                    PreloadAdmobNativeAd(activity)
                }
            }
        }
    }

    // ---------------- native_banner_ad ----------------

    fun ShowSmallNativeBannerAd(
        activity: Activity,
        qurekaNativeBannerUrl1: String,
        native_banner_id: ViewGroup
    ) {
        if (dataItem != null && dataItem.isNotEmpty()) {

            when (dataItem[0].checkAdNativeBanner) {

                Admob -> {

                    if (isadmob_small_native_banner_Loaded) {
                        val adView = LayoutInflater.from(context)
                            .inflate(R.layout.admob_samall_native, null) as NativeAdView
                        native_banner_id.removeAllViews()
                        native_banner_id.addView(adView)
                        Small_Native_Banner_Ads(admobNativeAds[0], adView)
                        isadmob_small_native_banner_Loaded = false
                        preloadAdmobNativeBannerAd(activity)

                    } else if (isFBNative_Banner_Loaded) {
                        val adView = LayoutInflater.from(context)
                            .inflate(R.layout.fb_samall_native_banner, null) as NativeAdLayout
                        native_banner_id.removeAllViews()
                        native_banner_id.addView(adView)
                        Fb_Samll_Native_banner_inflateAd(fbNativeBannerAds[0], adView, activity)

                        isFBNative_Banner_Loaded = false
                        preloadFbNativeBannerAd(activity)
                    } else {
                        preloadAdmobNativeBannerAd(activity)
                    }
                }

                FB -> {
                    if (isFBNative_Banner_Loaded) {

                        val adView = LayoutInflater.from(context)
                            .inflate(R.layout.fb_samall_native_banner, null) as NativeAdLayout
                        native_banner_id.removeAllViews()
                        native_banner_id.addView(adView)
                        Fb_Samll_Native_banner_inflateAd(fbNativeBannerAds[0], adView, activity)

                        isFBNative_Banner_Loaded = false
                        preloadFbNativeBannerAd(activity)
                    } else if (isadmob_small_native_banner_Loaded) {

                        val adView = LayoutInflater.from(context)
                            .inflate(R.layout.admob_native1, null) as NativeAdView
                        native_banner_id.removeAllViews()
                        native_banner_id.addView(adView)
                        Small_Native_Banner_Ads(admobNativeAds[0], adView)

                        isadmob_small_native_banner_Loaded = false
                        preloadAdmobNativeBannerAd(activity)

                    } else {
                        preloadFbNativeBannerAd(activity)
                    }
                }

                Local -> {
                    showNativeBannerLocal(activity, native_banner_id)
                }

                Qureka -> {
                    showNativeBannerQureka(activity, qurekaNativeBannerUrl1, native_banner_id)
                }
            }
        }
    }

    fun Small_Native_Banner_Ads(nativeAd: NativeAd, adView: NativeAdView) {
        adView.setVisibility(View.VISIBLE)
        adView.setHeadlineView(adView.findViewById<View?>(R.id.ad_headline))
        adView.setBodyView(adView.findViewById<View?>(R.id.ad_body))
        adView.setCallToActionView(adView.findViewById<View?>(R.id.ad_call_to_action))
        adView.setIconView(adView.findViewById<View?>(R.id.ad_app_icon))
        (adView.getHeadlineView() as TextView).setText(nativeAd.getHeadline())
        if (nativeAd.getBody() == null) {
            adView.getBodyView()!!.setVisibility(4)
        } else {
            adView.getBodyView()!!.setVisibility(0)
            (adView.getBodyView() as TextView).setText(nativeAd.getBody())
        }
        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView()!!.setVisibility(4)
        } else {
            adView.getCallToActionView()!!.setVisibility(0)
            (adView.getCallToActionView() as TextView).setText(nativeAd.getCallToAction())
        }
        if (nativeAd.getIcon() == null) {
            adView.getIconView()!!.setVisibility(8)
        } else {
            (adView.getIconView() as ImageView).setImageDrawable(nativeAd.getIcon()!!.getDrawable())
            adView.getIconView()!!.setVisibility(0)
        }
        adView.setNativeAd(nativeAd)
    }

    fun Fb_Samll_Native_banner_inflateAd(
        nativeBannerAd: NativeBannerAd,
        nativeAdLayout: NativeAdLayout,
        activity: Activity
    ) {

        // Unregister last ad
        nativeBannerAd.unregisterView()
        val adChoicesContainer: LinearLayout =
            nativeAdLayout.findViewById<LinearLayout?>(R.id.ad_choices_container)
        val adOptionsView = AdOptionsView(
            activity, nativeBannerAd, nativeAdLayout
        )
        adChoicesContainer.removeAllViews()
        adChoicesContainer.addView(adOptionsView, 0)


        // Create native UI using the ad metadata.
        val nativeAdIcon: ImageView? = nativeAdLayout.findViewById<ImageView?>(R.id.native_ad_icon)
        val nativeAdTitle: TextView = nativeAdLayout.findViewById<TextView?>(R.id.native_ad_title)
        val nativeAdSocialContext: TextView =
            nativeAdLayout.findViewById<TextView?>(R.id.native_ad_social_context)
        val nativeAdBody: TextView = nativeAdLayout.findViewById<TextView?>(R.id.native_ad_body)
        val sponsoredLabel: TextView =
            nativeAdLayout.findViewById<TextView?>(R.id.native_ad_sponsored_label)
        val nativeAdCallToAction: TextView =
            nativeAdLayout.findViewById<TextView?>(R.id.native_ad_call_to_action)

        nativeAdTitle.setText(nativeBannerAd.getAdvertiserName())
        nativeAdBody.setText(nativeBannerAd.getAdBodyText())
        nativeAdSocialContext.setText(nativeBannerAd.getAdSocialContext())
        nativeAdCallToAction.setVisibility(if (nativeBannerAd.hasCallToAction()) View.VISIBLE else View.INVISIBLE)
        nativeAdCallToAction.setText(nativeBannerAd.getAdCallToAction())
        sponsoredLabel.setText(nativeBannerAd.getSponsoredTranslation())


        // Register the Title and CTA button to listen for clicks.
        val clickableViews: MutableList<View?> = java.util.ArrayList<View?>()
        clickableViews.add(nativeAdTitle)
        clickableViews.add(nativeAdCallToAction)
        nativeBannerAd.registerViewForInteraction(nativeAdLayout, nativeAdIcon, clickableViews)
    }

    fun showNativeBannerLocal(activity: Activity, nativeBannerId: ViewGroup) {
        val viewGroup2 = LayoutInflater.from(activity)
            .inflate(R.layout.local_small_native_ad, null as ViewGroup?) as ViewGroup
        val rlbackground = viewGroup2.findViewById<RelativeLayout?>(R.id.background)
        val textView2 = viewGroup2.findViewById<View?>(R.id.ad_headline) as TextView
        val imageView = viewGroup2.findViewById<View?>(R.id.ad_app_icon) as ImageView
        if (dataItem != null) {
            textView2.setText(dataItem.get(0).LocalAppName)
            Glide.with(activity)
                .load(dataItem.get(0).LocalNativebannerAdbanner).into(imageView)
        }
        rlbackground.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                onLocalIntent(activity)
            }
        })
        nativeBannerId.addView(viewGroup2)
    }

    fun showNativeBannerQureka(
        activity: Activity,
        qurekaNativeBannerUrl1: String,
        nativeBannerId: ViewGroup
    ) {
        val viewGroup2 = LayoutInflater.from(activity)
            .inflate(R.layout.qureka_small_native_ad, null as ViewGroup?) as ViewGroup
        val rlbackground = viewGroup2.findViewById<RelativeLayout?>(R.id.background)
        val adaapicon = viewGroup2.findViewById<ImageView?>(R.id.ad_app_icon)
        val textView2 = viewGroup2.findViewById<TextView?>(R.id.ad_headline)
        val textView3 = viewGroup2.findViewById<TextView?>(R.id.ad_body)

        if (dataItem.get(0).qurekaNativeBannerUrl1.equals(qurekaNativeBannerUrl1)) {
            Glide.with(activity).load(qurekaNativeBannerUrl1).into(adaapicon)
            textView2.setText(dataItem.get(0).QurekaNativeTitle1)
            textView3.setText(dataItem.get(0).QurekaNativesubTitle1)
        } else if (dataItem.get(0).qurekaNativeBannerUrl2.equals(qurekaNativeBannerUrl1)) {
            Glide.with(activity).load(qurekaNativeBannerUrl1).into(adaapicon)
            textView2.setText(dataItem.get(0).QurekaNativeTitle2)
            textView3.setText(dataItem.get(0).QurekaNativesubTitle2)
        } else if (dataItem.get(0).qurekaNativeBannerUrl3.equals(qurekaNativeBannerUrl1)) {
            Glide.with(activity).load(qurekaNativeBannerUrl1).into(adaapicon)
            textView2.setText(dataItem.get(0).QurekaNativeTitle3)
            textView3.setText(dataItem.get(0).QurekaNativesubTitle3)
        } else if (dataItem.get(0).qurekaNativeBannerUrl4.equals(qurekaNativeBannerUrl1)) {
            Glide.with(activity).load(qurekaNativeBannerUrl1).into(adaapicon)
            textView2.setText(dataItem.get(0).QurekaNativeTitle4)
            textView3.setText(dataItem.get(0).QurekaNativesubTitle4)
        } else if (dataItem.get(0).qurekaNativeBannerUrl5.equals(qurekaNativeBannerUrl1)) {
            Glide.with(activity).load(qurekaNativeBannerUrl1).into(adaapicon)
            textView2.setText(dataItem.get(0).QurekaNativeTitle5)
            textView3.setText(dataItem.get(0).QurekaNativesubTitle5)
        }

        rlbackground.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (dataItem.get(0).qurekaUrl1 != null) {
                    myCustom(activity, dataItem.get(0).qurekaUrl1)
                }
            }
        })
        nativeBannerId.addView(viewGroup2)
    }

    // ---------------- banner_ad ----------------

    fun show_banner_ad(activity: Activity, qurekaBannerUrl1: String, banner_id: ViewGroup) {

        if (dataItem != null && dataItem.size > 0) {

            when (dataItem[0].checkAdBanner) {

                Admob -> {

                    if (isGoogleBannerLoaded) {
                        try {
                            parentView?.removeAllViews()
                            if (banner_id != null && googleBannerAd != null) {
                                banner_id.removeAllViews()
                                banner_id.addView(googleBannerAd)
                                parentView = banner_id
                            }
                            isGoogleBannerLoaded = false
                            preloadBannerAdAdmob(activity)
                        } catch (e: Exception) {
                            throw RuntimeException(e)
                        }
                    } else if (isFBBannerLoaded) {
                        try {
                            parentView?.removeAllViews()
                            if (banner_id != null && fbadView != null) {
                                banner_id.removeAllViews()
                                banner_id.addView(fbadView)
                                parentView = banner_id
                            }
                            isFBBannerLoaded = false
                            preloadBannerAdFB(activity)
                        } catch (e: Exception) {
                            throw RuntimeException(e)
                        }
                    } else {
                        preloadBannerAdAdmob(activity)
                    }
                }

                FB -> {
                    if (isFBBannerLoaded) {
                        try {
                            parentView?.removeAllViews()
                            if (banner_id != null && fbadView != null) {
                                banner_id.removeAllViews()
                                banner_id.addView(fbadView)
                                parentView = banner_id
                            }
                            isFBBannerLoaded = false
                            preloadBannerAdFB(activity)
                        } catch (e: Exception) {
                            throw RuntimeException(e)
                        }
                    } else if (isGoogleBannerLoaded) {
                        try {
                            parentView?.removeAllViews()
                            if (banner_id != null && googleBannerAd != null) {
                                banner_id.removeAllViews()
                                banner_id.addView(googleBannerAd)
                                parentView = banner_id
                            }
                            isGoogleBannerLoaded = false
                            preloadBannerAdAdmob(activity)
                        } catch (e: Exception) {
                            throw RuntimeException(e)
                        }
                    } else {
                        preloadBannerAdFB(activity)
                    }
                }

                Local -> {
                    showBannerLocal(activity, banner_id)
                }

                Qureka -> {
                    showBannerQureka(activity, qurekaBannerUrl1, banner_id)
                }
            }
        }
    }

    fun showBannerLocal(activity: Activity, bannerId: ViewGroup) {
        val viewGroup2 = LayoutInflater.from(activity)
            .inflate(R.layout.local_banner_ad, null as ViewGroup?) as ViewGroup
        val rlbackground = viewGroup2.findViewById<RelativeLayout?>(R.id.background)
        val textView2 = viewGroup2.findViewById<View?>(R.id.ad_headline) as TextView
        val imageView = viewGroup2.findViewById<View?>(R.id.ad_app_icon) as ImageView
        if (dataItem != null) {
            textView2.setText(dataItem.get(0).LocalAppName)
            Glide.with(activity)
                .load(dataItem.get(0).LocalBannericonAdbanner).into(imageView)
        }
        rlbackground.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                onLocalIntent(activity)
            }
        })
        bannerId.addView(viewGroup2)
    }

    fun showBannerQureka(activity: Activity, qurekaBannerUrl1: String, bannerId: ViewGroup) {
        val viewGroup2 = LayoutInflater.from(activity)
            .inflate(R.layout.qureka_banner_ad, null as ViewGroup?) as ViewGroup
        val rlbackground = viewGroup2.findViewById<RelativeLayout?>(R.id.background)
        val adaapicon = viewGroup2.findViewById<ImageView?>(R.id.ad_app_icon)
        val textView2 = viewGroup2.findViewById<TextView?>(R.id.ad_headline)
        val textView3 = viewGroup2.findViewById<TextView?>(R.id.ad_body)

        if (dataItem.get(0).qurekaBannericonUrl1.equals(qurekaBannerUrl1)) {
            Glide.with(activity).load(qurekaBannerUrl1).into(adaapicon)
            textView2.setText(dataItem.get(0).QurekaBannerTitle1)
            textView3.setText(dataItem.get(0).QurekaSubBannerTitle1)
        } else if (dataItem.get(0).qurekaBannericonUrl2.equals(qurekaBannerUrl1)) {
            Glide.with(activity).load(qurekaBannerUrl1).into(adaapicon)
            textView2.setText(dataItem.get(0).QurekaBannerTitle2)
            textView3.setText(dataItem.get(0).QurekaSubBannerTitle2)
        } else if (dataItem.get(0).qurekaBannericonUrl3.equals(qurekaBannerUrl1)) {
            Glide.with(activity).load(qurekaBannerUrl1).into(adaapicon)
            textView2.setText(dataItem.get(0).QurekaBannerTitle3)
            textView3.setText(dataItem.get(0).QurekaSubBannerTitle3)
        } else if (dataItem.get(0).qurekaBannericonUrl4.equals(qurekaBannerUrl1)) {
            Glide.with(activity).load(qurekaBannerUrl1).into(adaapicon)
            textView2.setText(dataItem.get(0).QurekaBannerTitle4)
            textView3.setText(dataItem.get(0).QurekaSubBannerTitle4)
        } else if (dataItem.get(0).qurekaBannericonUrl5.equals(qurekaBannerUrl1)) {
            Glide.with(activity).load(qurekaBannerUrl1).into(adaapicon)
            textView2.setText(dataItem.get(0).QurekaNativeTitle5)
            textView3.setText(dataItem.get(0).QurekaNativesubTitle5)
        }

        rlbackground.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (dataItem.get(0).qurekaUrl1 != null) {
                    myCustom(activity, dataItem.get(0).qurekaUrl1)
                }
            }
        })
        bannerId.addView(viewGroup2)
    }

    fun ShowAppopenAds(
        activity: Activity,
        qurekaInterImg: String,
        onAdFinished: MyCallbackAppopen
    ) {
        showAdIfAvailable(
            activity,
            qurekaInterImg,
            onAdFinished
        )
    }

    private fun showAdIfAvailable(
        activity: Activity,
        qurekaInterImg: String,
        onAdFinished: MyCallbackAppopen
    ) {
        if (dataItem != null && dataItem.size > 0) {
            val fullScreenContentCallback: FullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        onAdFinished.onCall()
                        getAdmobAppOpenAdsLoad(activity)
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        getAdmobAppOpenAdsLoad(activity)
                    }
                }

            if (isadmob_appopen_Loaded) {
                admob_appOpenAd.setFullScreenContentCallback(
                    fullScreenContentCallback
                )
                admob_appOpenAd.show(activity)
                isadmob_appopen_Loaded = false
            } else if (dataItem.get(0)
                    .checkAdSplashAppopenmode.equals(Qureka)
            ) {
                showQurekaAppopen(
                    activity,
                    qurekaInterImg,
                    onAdFinished
                )
            } else if (dataItem.get(0)
                    .checkAdSplashAppopenmode.equals(Local)
            ) {
                showLocalAppopen(activity, onAdFinished)
            }
        }
    }

    fun showQurekaAppopen(
        activity: Activity,
        qurekaAppopenImgUrl1: String,
        onAdFinished: MyCallbackAppopen
    ) {
        val dialog = Dialog(activity, R.style.FullWidth_Dialog)
        dialog.setContentView(
            LayoutInflater.from(activity)
                .inflate(R.layout.qureka_appopen, null as ViewGroup?)
        )
        dialog.setCancelable(false)
        dialog.getWindow()!!.setLayout(-1, -1)
        dialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(0))
        val relativeLayout = dialog.findViewById<View?>(R.id.llPersonalAd) as RelativeLayout
        val linearLayout = dialog.findViewById<View?>(R.id.ll_continue_app) as LinearLayout
        val textView2 = dialog.findViewById<View?>(R.id.txt_appname) as TextView
        val adbanner = dialog.findViewById<View?>(R.id.ad_banner) as ImageView
        val textView3 = dialog.findViewById<View?>(R.id.ad_body) as TextView

        if (dataItem.get(0).qurekaAppopenImgUrl1.equals(qurekaAppopenImgUrl1)) {
            Glide.with(activity).load(qurekaAppopenImgUrl1)
                .into(adbanner)
            textView2.setText(dataItem.get(0).QurekaAppopenTitle1)
            textView3.setText(dataItem.get(0).QurekaAppopensubTitle1)
        } else if (dataItem.get(0).qurekaAppopenImgUrl2.equals(qurekaAppopenImgUrl1)) {
            Glide.with(activity).load(qurekaAppopenImgUrl1)
                .into(adbanner)
            textView2.setText(dataItem.get(0).QurekaAppopenTitle2)
            textView3.setText(dataItem.get(0).QurekaAppopensubTitle2)
        } else if (dataItem.get(0).qurekaAppopenImgUrl3.equals(qurekaAppopenImgUrl1)) {
            Glide.with(activity).load(qurekaAppopenImgUrl1)
                .into(adbanner)
            textView2.setText(dataItem.get(0).QurekaAppopenTitle3)
            textView3.setText(dataItem.get(0).QurekaAppopensubTitle3)
        } else if (dataItem.get(0).qurekaAppopenImgUrl4.equals(qurekaAppopenImgUrl1)) {
            Glide.with(activity).load(qurekaAppopenImgUrl1)
                .into(adbanner)
            textView2.setText(dataItem.get(0).QurekaAppopenTitle4)
            textView3.setText(dataItem.get(0).QurekaAppopensubTitle4)
        } else if (dataItem.get(0).qurekaAppopenImgUrl5.equals(qurekaAppopenImgUrl1)) {
            Glide.with(activity).load(qurekaAppopenImgUrl1)
                .into(adbanner)
            textView2.setText(dataItem.get(0).QurekaAppopenTitle5)
            textView3.setText(dataItem.get(0).QurekaAppopensubTitle5)
        }

        linearLayout.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                dialog.dismiss()
                if (onAdFinished == null) {
                    splashinter(activity)
                } else {
                    onAdFinished.onCall()
                }
            }
        })
        relativeLayout.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (dataItem.get(0).qurekaUrl1 != null) {
                    myCustom(activity, dataItem.get(0).qurekaUrl1)
                }
            }
        })
        dialog.show()
    }

    fun showLocalAppopen(activity: Activity, onAdFinished: MyCallbackAppopen) {
        val dialog = Dialog(activity, R.style.FullWidth_Dialog)
        dialog.setContentView(
            LayoutInflater.from(activity)
                .inflate(R.layout.local_appopen, null as ViewGroup?)
        )
        dialog.setCancelable(false)
        dialog.getWindow()!!.setLayout(-1, -1)
        dialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(0))
        val relativeLayout = dialog.findViewById<View?>(R.id.llPersonalAd) as RelativeLayout
        val linearLayout = dialog.findViewById<View?>(R.id.ll_continue_app) as LinearLayout
        val textView2 = dialog.findViewById<View?>(R.id.txt_appname) as TextView
        val adbanner = dialog.findViewById<View?>(R.id.ad_banner) as ImageView
        val applogo = dialog.findViewById<View?>(R.id.iv_myapp_logo) as ImageView
        val applogo1 = dialog.findViewById<View?>(R.id.app_icon) as ImageView
        val textView3 = dialog.findViewById<View?>(R.id.txt_myapp_name) as TextView

        if (dataItem.get(0) != null) {
            textView2.setText(dataItem.get(0).LocalAppName)
            textView3.setText(dataItem.get(0).LocalAppName)
            Glide.with(activity)
                .load(dataItem.get(0).LocalAppopenImgUrl).into(adbanner)
            Glide.with(activity).load(dataItem.get(0).LocalAppIcon)
                .into(applogo)
            Glide.with(activity).load(dataItem.get(0).LocalAppIcon)
                .into(applogo1)
        }
        linearLayout.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                dialog.dismiss()
                if (onAdFinished == null) {
                    splashinter(activity)
                } else {
                    onAdFinished.onCall()
                }
            }
        })
        relativeLayout.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (dataItem.get(0).qurekaUrl1 != null) {
                    myCustom(activity, dataItem.get(0).qurekaUrl1)
                }
            }
        })
        dialog.show()
    }

    fun showSplashAd(activity: Activity) {
        if (dataItem.isNotEmpty()) {
            when (dataItem[0].checkAdSplash) {
                Admob -> showAdmobSplashInterstitial(activity)
                FB -> showFacebookSplashInterstitial(activity)
                Appopen -> handleAppOpenSplash(activity)
                else -> splashinter(activity)
            }
        } else {
            splashinter(activity)
        }
    }
}

// ---------------- CallBack ----------------

interface MyCallback {
    fun onCall()
}

// ---------------- AppopenCallBack ----------------

interface MyCallbackAppopen {
    fun onCall()
}

// ---------------- PRELOAD ----------------

fun preloadAds(activity: Activity) {
    if (dataItem.isNotEmpty()) {

        // 🔹 Preload all ads

        // Inter Ads
        loadInterAdsAdmob(activity)
        loadInterAdsFB(activity, dataItem[0].fbinter1)

        // Native Ads
        PreloadAdmobNativeAd(activity)
        preloadFbNativeAd(activity)

        // Native Banner Ads
        preloadAdmobNativeBannerAd(activity)
        preloadFbNativeBannerAd(activity)

        // Banner Ads
        preloadBannerAdAdmob(activity)
        preloadBannerAdFB(activity)

        getAdmobAppOpenAdsLoad(activity)

        // 🔹 OneSignal Init
        OneSignal.setLogLevel(
            OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE
        )
        OneSignal.initWithContext(activity)
        OneSignal.setAppId(dataItem[0].OnesignalId)
        OneSignal.disablePush(true)

    } else {
        Toast.makeText(
            activity, "Please Check Your Internet Connection", Toast.LENGTH_SHORT
        ).show()
    }
}

//-------------------------------------------- Splash Ads ----------------------------------------------------------------------------
private fun showAdmobSplashInterstitial(activity: Activity) {

    val adRequest = AdRequest.Builder().build()

    InterstitialAd.load(
        activity, dataItem[0].admobInterid, adRequest, object : InterstitialAdLoadCallback() {

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.e("FacebookAds", "" + loadAdError.message)
                splashinter(activity)
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {

                interstitialAd.show(activity)

                Log.e("FacebookAds", "Loaded")

                interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {

                    override fun onAdDismissedFullScreenContent() {
                        splashinter(activity)
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        splashinter(activity)
                    }
                }
            }
        })
}

private fun showFacebookSplashInterstitial(activity: Activity) {

    val fbInterstitial = com.facebook.ads.InterstitialAd(activity, dataItem[0].fbinter1)

    val listener = object : InterstitialAdListener {

        override fun onInterstitialDismissed(ad: Ad) {
            splashinter(activity)
        }

        override fun onError(ad: Ad, adError: com.facebook.ads.AdError) {
            splashinter(activity)
        }

        override fun onAdLoaded(ad: Ad) {
            fbInterstitial.show()
        }

        override fun onInterstitialDisplayed(ad: Ad) {}
        override fun onAdClicked(ad: Ad) {}
        override fun onLoggingImpression(ad: Ad) {}
    }

    fbInterstitial.loadAd(
        fbInterstitial.buildLoadAdConfig().withAdListener(listener).build()
    )
}

fun handleAppOpenSplash(activity: Activity) {

    when (dataItem[0].checkAdSplashAppopenmode) {

        Admob -> {
            fullScreenContentCallback = object : FullScreenContentCallback() {

                override fun onAdDismissedFullScreenContent() {
                    splashinter(activity)
                }
            }

            getSplashAppOpenAdmob(activity)
        }

        Qureka -> {
            FacebookAds.getInstance(activity).showQurekaAppopen(
                activity,
                dataItem.get(0).qurekaAppopenImgUrl1,
                myCallbackappopen
            )
        }

        Local -> {
            FacebookAds.getInstance(activity).showLocalAppopen(activity, myCallbackappopen)
        }
    }
}

fun getSplashAppOpenAdmob(activity: Activity) {
    if (dataItem.isNotEmpty()) {

        val loadCallback = object : AppOpenAdLoadCallback() {

            override fun onAdLoaded(appOpenAd: AppOpenAd) {
                super.onAdLoaded(appOpenAd)
                Log.e("FacebookAds", "onResponse $$$$$: Appopen Show")
                // Show ad immediately
                appOpenAd.show(activity)

                // Set full screen callback
                appOpenAd.fullScreenContentCallback = fullScreenContentCallback
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                splashinter(activity) // Navigate / show next screen
            }
        }

        AppOpenAd.load(
            activity,
            dataItem[0].appopenadId,
            getAdRequest(), // your existing AdRequest builder function
            loadCallback
        )
    }
}

private fun getAdRequest(): AdRequest {
    return AdRequest.Builder().build()
}


private fun splashinter(activity: Activity) {
    activity.startActivity(Intent(activity, MainActivity::class.java).apply {
        putExtra("skip_splash", true)
    })
    activity.finish()
}

//-------------------------------------------- Appopen Ads ----------------------------------------------------------------------------
fun getAdmobAppOpenAdsLoad(activity: Activity) {
    if (dataItem != null && dataItem.size > 0) {
        if (!isadmob_appopen_Loaded) {
            val loadCallback: AppOpenAdLoadCallback = object : AppOpenAdLoadCallback() {
                override fun onAdLoaded(appOpenAd: AppOpenAd) {
                    super.onAdLoaded(appOpenAd)
                    admob_appOpenAd = appOpenAd
                    isadmob_appopen_Loaded = true
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                }
            }
            AppOpenAd.load(
                activity,
                dataItem.get(0).appopenadId,
                getAdRequest(),
                loadCallback
            )
        }
    }
}

//-------------------------------------------- Inter Ads ----------------------------------------------------------------------------
fun loadInterAdsAdmob(activity: Activity) {
    if (dataItem.isNotEmpty() && !isGoogleInterLoaded) {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            activity, dataItem[0].admobInterid, adRequest, object : InterstitialAdLoadCallback() {

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    // Log or handle failure
                    Log.e("Ads", "AdMob Interstitial failed to load: ${loadAdError.message}")
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    super.onAdLoaded(interstitialAd)
                    ADMOBInterstitialAd = interstitialAd
                    isGoogleInterLoaded = true
                    Log.d("Ads", "AdMob Interstitial loaded")

                }
            })
    }
}

fun loadInterAdsFB(activity: Activity, fbInterId: String, myCallback: (() -> Unit)? = null) {
    if (dataItem.isNotEmpty() && !isFBInterLoaded) {

        val fbInterstitialAd = com.facebook.ads.InterstitialAd(activity, fbInterId)

        val listener = object : InterstitialAdListener {

            override fun onInterstitialDisplayed(ad: Ad) {
                // No-op
            }

            override fun onInterstitialDismissed(ad: Ad) {
                myCallback?.invoke()
            }

            override fun onError(ad: Ad, adError: com.facebook.ads.AdError) {
                Log.e("Ads", "Facebook Interstitial failed: ${adError.errorMessage}")
            }

            override fun onAdLoaded(ad: Ad) {
                FB_interstitialAd = fbInterstitialAd
                isFBInterLoaded = true
                Log.d("Ads", "Facebook Interstitial loaded")
            }

            override fun onAdClicked(ad: Ad) {
                // Optional: handle click
            }

            override fun onLoggingImpression(ad: Ad) {
                // Optional: handle impression
            }
        }

        fbInterstitialAd.loadAd(
            fbInterstitialAd.buildLoadAdConfig().withAdListener(listener).build()
        )
    }

}

//-------------------------------------------- Native Ads ----------------------------------------------------------------------------
fun PreloadAdmobNativeAd(activity: Activity) {

    if (dataItem != null && dataItem.isNotEmpty() && !isadmobNativeLoaded) {

        Log.e("FacebookAds", "onResponse $$$$$: Native Loaded")
        val adLoader = AdLoader.Builder(
            activity, dataItem[0].admobNativeId
        ).forNativeAd { nativeAd ->
            admobNativeAds.clear() // destroy old ad if exists
            admobNativeAds.add(nativeAd)
            isadmobNativeLoaded = true
        }.withAdListener(object : AdListener() {

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                isadmobNativeLoaded = false
            }

            override fun onAdLoaded() {
                // optional
            }
        }).build()

        adLoader.loadAd(AdRequest.Builder().build())
    }
}

fun preloadFbNativeAd(activity: Activity) {
    if (dataItem != null && dataItem.size > 0) {
        if (!isFBNativeLoaded) {
            val nativeAd: com.facebook.ads.NativeAd = com.facebook.ads.NativeAd(
                activity, dataItem.get(0).fbNative1
            )
            val nativeAdListener: NativeAdListener = object : NativeAdListener {
                override fun onMediaDownloaded(ad: Ad?) {
                }

                override fun onError(ad: Ad?, adError: com.facebook.ads.AdError?) {
                }

                override fun onAdLoaded(ad: Ad?) {
                    if (fbNativeAds.size > 0) {
                        fbNativeAds.clear()
                        fbNativeAds.add(nativeAd)
                    } else {
                        fbNativeAds.add(nativeAd)
                    }
                    isFBNativeLoaded = true
                }

                override fun onAdClicked(ad: Ad?) {
                }

                override fun onLoggingImpression(ad: Ad?) {
                }
            }
            nativeAd.loadAd(
                nativeAd.buildLoadAdConfig().withAdListener(nativeAdListener)
                    .withMediaCacheFlag(NativeAdBase.MediaCacheFlag.ALL).build()
            )
        }
    }
}

//-------------------------------------------- Small Native Banner Ads ----------------------------------------------------------------------------
fun preloadAdmobNativeBannerAd(activity: Activity) {
    if (dataItem != null && dataItem.size > 0) {
        if (!isadmob_small_native_banner_Loaded) {
            val builder = AdLoader.Builder(
                activity, dataItem[0].admobNativeId
            ).forNativeAd { nativeAd ->
                Admob_small_native_banner_Ad.clear()
                Admob_small_native_banner_Ad.add(nativeAd)
                isadmob_small_native_banner_Loaded = true
            }.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isadmob_small_native_banner_Loaded = false
                }

                override fun onAdLoaded() {
                    // optional
                }
            }).build()
            builder.loadAd(AdRequest.Builder().build())
        }
    }
}

fun preloadFbNativeBannerAd(activity: Activity) {
    if (dataItem != null && dataItem.size > 0) {
        if (!isFBNative_Banner_Loaded) {
            val fb_nativeBanner_Ad: NativeBannerAd = NativeBannerAd(
                activity, dataItem.get(0).fbNativeBanner1
            )
            val nativeAdListener: NativeAdListener = object : NativeAdListener {
                override fun onMediaDownloaded(ad: Ad?) {
                }

                override fun onError(ad: Ad?, adError: com.facebook.ads.AdError?) {
                }

                override fun onAdLoaded(ad: Ad?) {
                    if (fbNativeBannerAds.size > 0) {
                        fbNativeBannerAds.clear()
                        fbNativeBannerAds.add(fb_nativeBanner_Ad)
                    } else {
                        fbNativeBannerAds.add(fb_nativeBanner_Ad)
                    }
                    isFBNative_Banner_Loaded = true
                }

                override fun onAdClicked(ad: Ad?) {
                }

                override fun onLoggingImpression(ad: Ad?) {
                }
            }
            fb_nativeBanner_Ad.loadAd(
                fb_nativeBanner_Ad.buildLoadAdConfig().withAdListener(nativeAdListener).build()
            )
        }
    }
}


//-------------------------------------------- Banner Ads ----------------------------------------------------------------------------

fun preloadBannerAdAdmob(activity: Activity) {
    if (dataItem != null && dataItem.size > 0) {
        if (!isGoogleBannerLoaded) {
            val admob_Banner = AdView(activity)
            admob_Banner.setAdSize(
                AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                    activity,
                    AdSize.FULL_WIDTH
                )
            )
            admob_Banner.setAdUnitId(
                dataItem.get(0).admobBannerid
            )
            val adRequest = AdRequest.Builder().build()
            admob_Banner.loadAd(adRequest)
            admob_Banner.setAdListener(object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                }

                override fun onAdLoaded() {
                    if (isGoogleBannerLoaded) return
                    googleBannerAd = admob_Banner
                    isGoogleBannerLoaded = true
                }
            })
        }
    }
}

fun preloadBannerAdFB(activity: Activity) {
    if (dataItem != null && dataItem.size > 0) {
        val fb_banner = com.facebook.ads.AdView(
            activity,
            dataItem.get(0).fbBanner1,
            com.facebook.ads.AdSize.BANNER_HEIGHT_50
        )
        val adListener: com.facebook.ads.AdListener = object : com.facebook.ads.AdListener {
            override fun onError(ad: Ad?, adError: com.facebook.ads.AdError?) {
            }

            override fun onAdLoaded(ad: Ad?) {
                fbadView = fb_banner
                isFBBannerLoaded = true
            }

            override fun onAdClicked(ad: Ad?) {
            }

            override fun onLoggingImpression(ad: Ad?) {
            }
        }
        fb_banner.loadAd(fb_banner.buildLoadAdConfig().withAdListener(adListener).build())
    }
}
