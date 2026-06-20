package monitoringcamera.transmitterconnect.officeconnectcamera

import android.content.Context
import com.google.gson.Gson
import monitoringcamera.transmitterconnect.officeconnectcamera.RetrofitResponce.DataItem

object Utils {

    private const val PREFS_NAME = "shared_preference"
    private const val FIRST_TIME_PREF = "first_time_user"
    private const val FAVORITE_DATA_PREF = "favorite_data"
    private const val INTRO_PAGE_COUNT_PREF = "intro_page_count"
    private const val AD_RESPONSE_KEY = "adresponse"

    // -------------------- STRING --------------------

    fun saveStringToPreference(
        context: Context,
        key: String,
        value: String
    ) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(key, value)
            .apply()
    }

    // -------------------- ADS RESPONSE --------------------

    fun getResponse(context: Context): DataItem {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(AD_RESPONSE_KEY, null)
        return json.let {
            Gson().fromJson(it, DataItem::class.java)
        }
    }

    // -------------------- FIRST TIME USER --------------------

    fun setFirstTime(context: Context, value: Boolean) {
        context.getSharedPreferences(FIRST_TIME_PREF, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(FIRST_TIME_PREF, value)
            .apply()
    }

    fun isFirstTime(context: Context): Boolean {
        return context.getSharedPreferences(FIRST_TIME_PREF, Context.MODE_PRIVATE)
            .getBoolean(FIRST_TIME_PREF, true)
    }

    // -------------------- FAVORITE DATA --------------------

    fun setFavoriteData(context: Context, favoriteData: String) {
        context.getSharedPreferences(FAVORITE_DATA_PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(FAVORITE_DATA_PREF, favoriteData)
            .apply()
    }

    fun getFavoriteData(context: Context): String {
        return context.getSharedPreferences(FAVORITE_DATA_PREF, Context.MODE_PRIVATE)
            .getString(FAVORITE_DATA_PREF, "") ?: ""
    }

    // -------------------- INTRO PAGE COUNT --------------------

    fun setIntroPageCount(context: Context, value: Int) {
        context.getSharedPreferences(INTRO_PAGE_COUNT_PREF, Context.MODE_PRIVATE)
            .edit()
            .putInt(INTRO_PAGE_COUNT_PREF, value)
            .apply()
    }

    fun getIntroPageCount(context: Context): Int {
        return context.getSharedPreferences(INTRO_PAGE_COUNT_PREF, Context.MODE_PRIVATE)
            .getInt(INTRO_PAGE_COUNT_PREF, 1)
    }
}

