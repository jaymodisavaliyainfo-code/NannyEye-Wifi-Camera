package monitoringcamera.transmitterconnect.officeconnectcamera

import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import com.facebook.ads.AudienceNetworkAds
import com.google.android.gms.ads.MobileAds
import com.google.gson.Gson
import com.izooto.iZooto
import monitoringcamera.transmitterconnect.officeconnectcamera.RetrofitResponce.AdListResponse
import monitoringcamera.transmitterconnect.officeconnectcamera.RetrofitResponce.DataItem
import monitoringcamera.transmitterconnect.officeconnectcamera.RetrofitResponce.LocaladsResponce
import monitoringcamera.transmitterconnect.officeconnectcamera.RetrofitResponce.NativeAdResponse
import monitoringcamera.transmitterconnect.officeconnectcamera.retrofit.APIClient
import monitoringcamera.transmitterconnect.officeconnectcamera.retrofit.RetrofitInterface
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class App : Application() {

    lateinit var appOpenManager: AppOpen
    private var brd: NetworkChangeReceiver? = null

    var check = false
    private var wifinet = false
    private var datanet = false

    override fun onCreate() {
        super.onCreate()
        Companion.instance = this
        context = this

        // Facebook Ads
        AudienceNetworkAds.initialize(this)

        // AppOpen
        appOpenManager = AppOpen(this)

        if (!FacebookAds.isOnline(this)) {
            registerNetworkReceiver(this)
        }

        iZooto.initialize(this).setTokenReceivedListener { token ->
            Log.d("iZooto", "Token: $token")
        }.build()
        iZooto.promptForPushNotifications()
        iZooto.setSubscription(true)
    }

    init {
        System.loadLibrary("native-lib")
    }

    private fun registerNetworkReceiver(ctx: Context) {
        try {
            brd = NetworkChangeReceiver()
            val filter = IntentFilter().apply {
                addAction("android.net.wifi.WIFI_STATE_CHANGED")
                addAction("android.net.conn.CONNECTIVITY_CHANGE")
            }
            ctx.registerReceiver(brd, filter)
        } catch (_: Exception) {
        }
    }

    inner class NetworkChangeReceiver : BroadcastReceiver() {
        private var firstTime = true

        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {

            if (firstTime) {
                firstTime = false
                check = true
            }

            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = cm.activeNetworkInfo

            activeNetwork?.let {
                when (it.type) {
                    ConnectivityManager.TYPE_WIFI -> wifinet = true
                    ConnectivityManager.TYPE_MOBILE -> datanet = true
                }
            }

            if ((wifinet || datanet) && FacebookAds.isOnline(context)) {
                check = false
                fetchAppData(this@App)
            }
        }
    }

    private fun fetchAppData(app: App) {
        val api = APIClient.client.create(RetrofitInterface::class.java)
        api.getAdsDetail(app.packageName, fetchdatastring())
            .enqueue(object : Callback<AdListResponse> {

                override fun onResponse(
                    call: Call<AdListResponse>,
                    response: Response<AdListResponse>
                ) {
                    if (response.isSuccessful && response.body()?.data?.isNotEmpty() == true) {

                        val dataItem = response.body()!!.data!!
                        val json = Gson().toJson(dataItem[0])

                        Utils.saveStringToPreference(app, "adresponse", json)
                        AdsDataHolder.adsData = Utils.getResponse(app)
                    }
                }

                override fun onFailure(call: Call<AdListResponse>, t: Throwable) {
                    call.cancel()
                }
            })
    }

    // ---------------- LOCAL ADS ----------------

    fun fetchStartApps() {
        arrAdDataStart.clear()

        val api = APIClient.client.create(RetrofitInterface::class.java)
        api.localAds(packageName, getLocalAdsData()).enqueue(object : Callback<LocaladsResponce> {

            override fun onResponse(
                call: Call<LocaladsResponce>, response: Response<LocaladsResponce>
            ) {
                if (response.isSuccessful && !response.body()?.data.isNullOrEmpty()) {
                    arrAdDataStart.addAll(response.body()!!.data!!)
                }
            }

            override fun onFailure(call: Call<LocaladsResponce>, t: Throwable) {
                call.cancel()
            }
        })
    }

    fun fetchNativeAdApps() {
        helinativeData.clear()

        val api = APIClient.client.create(RetrofitInterface::class.java)
        api.nativeAdAds(packageName, getHelinativeAdsData())
            .enqueue(object : Callback<NativeAdResponse> {

                override fun onResponse(
                    call: Call<NativeAdResponse>, response: Response<NativeAdResponse>
                ) {
                    if (response.isSuccessful && !response.body()?.data.isNullOrEmpty()) {
                        helinativeData.addAll(response.body()!!.data!!)
                    }
                }

                override fun onFailure(call: Call<NativeAdResponse>, t: Throwable) {
                    call.cancel()
                }
            })
    }

    companion object {
        lateinit var instance: App
            private set

        lateinit var context: App
            private set

        val arrAdDataStart = ArrayList<DataItem>()
        val helinativeData = ArrayList<DataItem>()

        init {
            System.loadLibrary("native-lib")
        }

        external fun getLocalAdsData(): String
        external fun getHelinativeAdsData(): String
        external fun fetchdatastring(): String
    }
}