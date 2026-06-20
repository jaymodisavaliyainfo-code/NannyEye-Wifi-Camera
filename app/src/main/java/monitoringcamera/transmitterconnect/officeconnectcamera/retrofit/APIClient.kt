package monitoringcamera.transmitterconnect.officeconnectcamera.retrofit

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by anupamchugh on 05/01/17.
 */
object APIClient {

    init {
        System.loadLibrary("native-lib")
    }

    // Native method (unchanged)
    external fun baseurl(): String

    val client: Retrofit by lazy {

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(baseurl())
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }
}

