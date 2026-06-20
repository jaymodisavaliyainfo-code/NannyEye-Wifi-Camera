
#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_monitoringcamera_transmitterconnect_officeconnectcamera_retrofit_APIClient_baseurl(
        JNIEnv *env, jobject thiz) {
    // TODO: implement baseurl
    return env->NewStringUTF("https://quanrix.com/");
}

extern "C"
JNIEXPORT jstring JNICALL
Java_monitoringcamera_transmitterconnect_officeconnectcamera_App_00024Companion_fetchdatastring(
        JNIEnv *env,
        jobject thiz) {
    // TODO: implement fetchdatastring()
    return env->NewStringUTF("appservice/get_Kriyu_Info.php");
}

extern "C"
JNIEXPORT jstring JNICALL
Java_monitoringcamera_transmitterconnect_officeconnectcamera_App_00024Companion_getLocalAdsData(
        JNIEnv *env, jobject thiz) {
    // TODO: implement getLocalAdsData()
    return env->NewStringUTF("devcount/localapp.php");
}

extern "C"
JNIEXPORT jstring JNICALL
Java_monitoringcamera_transmitterconnect_officeconnectcamera_FacebookAds_updateAppData(
        JNIEnv *env, jobject thiz) {
    // TODO: implement updateAppData
    return env->NewStringUTF("devcount/upcount.php");
}

extern "C"
JNIEXPORT jstring JNICALL
Java_monitoringcamera_transmitterconnect_officeconnectcamera_App_00024Companion_getHelinativeAdsData(JNIEnv *env, jobject thiz) {
    // TODO: implement getHelinativeAdsData()
    return env->NewStringUTF("devcount/getnativebanner.php");
}