package monitoringcamera.transmitterconnect.officeconnectcamera.retrofit

import monitoringcamera.transmitterconnect.officeconnectcamera.RetrofitResponce.AdListResponse
import monitoringcamera.transmitterconnect.officeconnectcamera.RetrofitResponce.LocaladsResponce
import monitoringcamera.transmitterconnect.officeconnectcamera.RetrofitResponce.NativeAdResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Url


interface RetrofitInterface {

    @FormUrlEncoded
    @POST
    fun getAdsDetail(
        @Field("packagename") packageName: String, @Url url: String
    ): Call<AdListResponse>

    @FormUrlEncoded
    @POST
    fun updateCounter(
        @Field("packagename") packageName: String, @Url url: String
    ): Call<Any>

    @FormUrlEncoded
    @POST
    fun localAds(
        @Field("packagename") packageName: String, @Url url: String
    ): Call<LocaladsResponce>

    @FormUrlEncoded
    @POST
    fun nativeAdAds(
        @Field("packagename") packageName: String, @Url url: String
    ): Call<NativeAdResponse>
}
