package monitoringcamera.transmitterconnect.officeconnectcamera.RetrofitResponce

import com.google.gson.annotations.SerializedName

data class LocaladsResponce(

    @SerializedName("data")
    val data: List<DataItem> = emptyList(),

    @SerializedName("success")
    val success: Int = 0
) {
    val isSuccess: Boolean
        get() = success == 1
}