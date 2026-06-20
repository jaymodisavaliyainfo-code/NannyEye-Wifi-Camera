package monitoringcamera.transmitterconnect.officeconnectcamera.RetrofitResponce

import com.google.gson.annotations.SerializedName

data class AdListResponse(

    @SerializedName("data")
    val data: List<DataItem>,

    @SerializedName("success")
    val success: Int
) {
    val isSuccess: Boolean
        get() = success == 1
}