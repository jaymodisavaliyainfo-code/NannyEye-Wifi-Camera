package monitoringcamera.transmitterconnect.officeconnectcamera.RetrofitResponce

import com.google.gson.annotations.SerializedName

data class NativeAdResponse(

    @SerializedName("data")
    val data: List<DataItem> = emptyList()
)