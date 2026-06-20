package monitoringcamera.transmitterconnect.officeconnectcamera.RetrofitResponce

import com.google.gson.annotations.SerializedName

data class DataItem(

    @SerializedName("allactivity") val allActivity: String,

    @SerializedName("admob_nativeid") val admobNativeId: String,

    @SerializedName("admob_interid") val admobInterid: String,

    @SerializedName("appopenad_id") val appopenadId: String,

    @SerializedName("check_ad_small_native") val checkAdSmallNative: String,

    @SerializedName("app_icon") val appIcon: String,

    @SerializedName("check_ad_native_banner") val checkAdNativeBanner: String,

    @SerializedName("check_ad_banner") val checkAdBanner: String,

    @SerializedName("fb_native1") val fbNative1: String,

    @SerializedName("redirect_app") val redirectApp: String,

    @SerializedName("check_ad_splash") val checkAdSplash: String,

    @SerializedName("check_ad_splash_appopenmode") val checkAdSplashAppopenmode: String,

    @SerializedName("appopen_onoff") val AppopenOnoff: String,

    @SerializedName("admob_bannerid") val admobBannerid: String,

    @SerializedName("fb_native_banner1") val fbNativeBanner1: String,

    @SerializedName("admob_appid") val admobAppid: String,

    @SerializedName("fbinter1") val fbinter1: String,

    @SerializedName("fbinter2") val fbinter2: String,

    @SerializedName("fbinter3") val fbinter3: String,

    @SerializedName("fbinter4") val fbinter4: String,

    @SerializedName("fbinter5") val fbinter5: String,

    @SerializedName("packagename") val packagename: String,

    @SerializedName("fb_banner1") val fbBanner1: String,

    @SerializedName("qureka_url1_onoff") val qurekaUrl1Onoff: String,

    @SerializedName("qureka_url2_onoff") val qurekaUrl2Onoff: String,

    @SerializedName("qureka_url3_onoff") val qurekaUrl3Onoff: String,

    @SerializedName("qureka_url1") val qurekaUrl1: String,

    @SerializedName("qureka_url2") val qurekaUrl2: String,

    @SerializedName("qureka_url3") val qurekaUrl3: String,

    @SerializedName("check_ad_native") val checkAdNative: String,

    @SerializedName("local_app_name") val LocalAppName: String,

    @SerializedName("local_app_icon") val LocalAppIcon: String,

    @SerializedName("local_app_banner") val LocalAppBanner: String,

    @SerializedName("local_packgename") val LocalPackgename: String,

    @SerializedName("local_native_adbanner") val LocalNativeAdbanner: String,

    @SerializedName("qureka_nativetitle1") val QurekaNativeTitle1: String,

    @SerializedName("qureka_nativesubtitle1") val QurekaNativesubTitle1: String,

    @SerializedName("qureka_nativetitle2") val QurekaNativeTitle2: String,

    @SerializedName("qureka_nativesubtitle2") val QurekaNativesubTitle2: String,

    @SerializedName("qureka_nativetitle3") val QurekaNativeTitle3: String,

    @SerializedName("qureka_nativesubtitle3") val QurekaNativesubTitle3: String,

    @SerializedName("qureka_inter_img_url1") val qurekaInterImgUrl1: String,
    @SerializedName("qureka_inter_img_url2") val qurekaInterImgUrl2: String,

    @SerializedName("qureka_inter_img_url3") val qurekaInterImgUrl3: String,

    @SerializedName("qureka_native_appicon1") val QurekaNativeAppicon1: String,

    @SerializedName("qureka_native_appicon2") val QurekaNativeAppicon2: String,

    @SerializedName("qureka_native_appicon3") val QurekaNativeAppicon3: String,

    @SerializedName("qureka_native_url1") val qurekaNativeUrl1: String,

    @SerializedName("qureka_native_url2") val qurekaNativeUrl2: String,

    @SerializedName("qureka_native_url3") val qurekaNativeUrl3: String,

    @SerializedName("local_nativebanner_adbanner") val LocalNativebannerAdbanner: String,

    @SerializedName("check_native") val checkNative: Boolean?,

    @SerializedName("native_url") val nativeUrl: String,

    @SerializedName("onesignal_id") val OnesignalId: String,

    @SerializedName("app_name") val appName: String,

    @SerializedName("qureka_admobfaile_onoff") val QurekaAdmobfaileOnoff: String,

    @SerializedName("qureka_fbfaile_onoff") val QurekaFbfaileOnoff: String,

    @SerializedName("check_ad_addcamera_inter") val checkAdAddCameraInter: String,

    @SerializedName("check_ad_privacy_policy_inter") val checkAdPrivacyPolicyInter: String,

    @SerializedName("check_ad_gender_inter") val checkAdGenderInter: String,
    @SerializedName("check_ad_language_inter") val checkAdLanguageInter: String,

    @SerializedName("check_ad_country_inter") val checkAdCountryInter: String,

    @SerializedName("qureka_appopen_img_url1") val qurekaAppopenImgUrl1: String,

    @SerializedName("qureka_appopen_img_url2") val qurekaAppopenImgUrl2: String,

    @SerializedName("qureka_appopen_img_url3") val qurekaAppopenImgUrl3: String,

    @SerializedName("qureka_native_url4") val qurekaNativeUrl4: String,

    @SerializedName("qureka_native_url5") val qurekaNativeUrl5: String,

    @SerializedName("qureka_native_appicon4") val QurekaNativeAppicon4: String,

    @SerializedName("qureka_native_appicon5") val QurekaNativeAppicon5: String,

    @SerializedName("qureka_nativetitle4") val QurekaNativeTitle4: String,

    @SerializedName("qureka_nativesubtitle4") val QurekaNativesubTitle4: String,

    @SerializedName("qureka_nativetitle5") val QurekaNativeTitle5: String,

    @SerializedName("qureka_nativesubtitle5") val QurekaNativesubTitle5: String,


    @SerializedName("qureka_native_banner_url1") val qurekaNativeBannerUrl1: String,

    @SerializedName("qureka_native_banner_url2") val qurekaNativeBannerUrl2: String,

    @SerializedName("qureka_native_banner_url3") val qurekaNativeBannerUrl3: String,

    @SerializedName("qureka_native_banner_url4") val qurekaNativeBannerUrl4: String,

    @SerializedName("qureka_native_banner_url5") val qurekaNativeBannerUrl5: String,

    @SerializedName("qureka_nativebanner_title1") val QurekaNativebannerTitle1: String,

    @SerializedName("qureka_nativebanner_subtitle1") val QurekaSubNativebannerTitle1: String,

    @SerializedName("qureka_nativebanner_title2") val QurekaNativebannerTitle2: String,

    @SerializedName("qureka_nativebanner_subtitle2") val QurekaSubNativebannerTitle2: String,

    @SerializedName("qureka_nativebanner_title3") val QurekaNativebannerTitle3: String,

    @SerializedName("qureka_nativebanner_subtitle3") val QurekaSubNativebannerTitle3: String,

    @SerializedName("qureka_nativebanner_title4") val QurekaNativebannerTitle4: String,

    @SerializedName("qureka_nativebanner_subtitle4") val QurekaSubNativebannerTitle4: String,

    @SerializedName("qureka_nativebanner_title5") val QurekaNativebannerTitle5: String,

    @SerializedName("qureka_nativebanner_subtitle5") val QurekaSubNativebannerTitle5: String,


    @SerializedName("qureka_inter_img_url4") val qurekaInterImgUrl4: String,

    @SerializedName("qureka_inter_img_url5") val qurekaInterImgUrl5: String,

    @SerializedName("qureka_intertitle4") val QurekaIntertitle4: String,

    @SerializedName("qureka_intersubtitle4") val QurekaIntersubTitle4: String,

    @SerializedName("qureka_intertitle5") val QurekaIntertitle5: String,

    @SerializedName("qureka_intersubtitle5") val QurekaIntersubTitle5: String,


    @SerializedName("qureka_appopen_img_url4") val qurekaAppopenImgUrl4: String,

    @SerializedName("qureka_appopen_img_url5") val qurekaAppopenImgUrl5: String,

    @SerializedName("qureka_appopentitle4") val QurekaAppopenTitle4: String,

    @SerializedName("qureka_appopensubtitle4") val QurekaAppopensubTitle4: String,

    @SerializedName("qureka_appopentitle5") val QurekaAppopenTitle5: String,

    @SerializedName("qureka_appopensubtitle5") val QurekaAppopensubTitle5: String,


    @SerializedName("qureka_banner_url1") val qurekaBannericonUrl1: String,

    @SerializedName("qureka_banner_url2") val qurekaBannericonUrl2: String,

    @SerializedName("qureka_banner_url3") val qurekaBannericonUrl3: String,

    @SerializedName("qureka_banner_url4") val qurekaBannericonUrl4: String,

    @SerializedName("qureka_banner_url5") val qurekaBannericonUrl5: String,

    @SerializedName("qureka_banner_title1") val QurekaBannerTitle1: String,

    @SerializedName("qureka_banner_subtitle1") val QurekaSubBannerTitle1: String,

    @SerializedName("qureka_banner_title2") val QurekaBannerTitle2: String,

    @SerializedName("qureka_banner_subtitle2") val QurekaSubBannerTitle2: String,

    @SerializedName("qureka_banner_title3") val QurekaBannerTitle3: String,

    @SerializedName("qureka_banner_subtitle3") val QurekaSubBannerTitle3: String,

    @SerializedName("qureka_banner_title4") val QurekaBannerTitle4: String,

    @SerializedName("qureka_banner_subtitle4") val QurekaSubBannerTitle4: String,

    @SerializedName("qureka_banner_title5") val QurekaBannerTitle5: String,

    @SerializedName("qureka_banner_subtitle5") val QurekaSubBannerTitle5: String,

    @SerializedName("taptostart") val taptostart: String,

//    @SerializedName("allactivity") val Allactivity: String ,

    @SerializedName("gender_onoff") val GenderOnoff: String,
    @SerializedName("language_onoff") val LanguageOnoff: String,

    @SerializedName("country_onoff") val CountryOnoff: String,

    @SerializedName("local_bannericon_adbanner") val LocalBannericonAdbanner: String,

    @SerializedName("qureka_cromeintent_onoff") val QurekaCromeintentonoff: String,

    @SerializedName("local_appopen_img_url") val LocalAppopenImgUrl: String,

    @SerializedName("qureka_intertitle1") val QurekaIntertitle1: String,

    @SerializedName("qureka_intersubtitle1") val QurekaIntersubTitle1: String,

    @SerializedName("qureka_intertitle2") val QurekaIntertitle2: String,

    @SerializedName("qureka_intersubtitle2") val QurekaIntersubTitle2: String,

    @SerializedName("qureka_intertitle3") val QurekaIntertitle3: String,

    @SerializedName("qureka_intersubtitle3") val QurekaIntersubTitle3: String,

    @SerializedName("qureka_appopentitle1") val QurekaAppopenTitle1: String,

    @SerializedName("qureka_appopensubtitle1") val QurekaAppopensubTitle1: String,

    @SerializedName("qureka_appopentitle2") val QurekaAppopenTitle2: String,

    @SerializedName("qureka_appopensubtitle2") val QurekaAppopensubTitle2: String,

    @SerializedName("qureka_appopentitle3") val QurekaAppopenTitle3: String,

    @SerializedName("qureka_appopensubtitle3") val QurekaAppopensubTitle3: String,

    @SerializedName("privacy_policy") val PrivacyPolicy: String,

    @SerializedName("privacy_policy_onoff") val privacyPolicyOnoff: String,

    @SerializedName("check_ad_final_exit") val checkAdFinalExit: String,

    @SerializedName("check_ad_adview_exit") val checkAdAdviewExit: String,

    @SerializedName("intropage_count") val IntropageCount: Int,

    @SerializedName("admob_collesible_bannerid") val admobCollesibleBannerid: String,


    @SerializedName("check_ad_btn_wifi_camera") val checkadbtnwificamera: String,

    @SerializedName("check_ad_btn_manual_camera") val checkadbtnmanualcamera: String,

    @SerializedName("check_ad_btn_ip_monitor") val checkadbtnipmonitor: String,

    @SerializedName("check_ad_btn_camera_preview") val checkadbtncamerapreview: String,

    @SerializedName("check_ad_power_next") val checkadpowernext: String,

    @SerializedName("check_ad_btn_confirm_next") val checkadbtnconfirmnext: String,


    @SerializedName("check_ad_btn_qrcode_next") val checkadbtnqrcodenext: String,


    @SerializedName("check_ad_btn_adddevice_next") val checkadbtnadddevicenext: String,


    @SerializedName("check_ad_btn_dvr1") val checkadbtndvr1: String,

    @SerializedName("check_ad_btn_dvr2") val checkadbtndvr2: String,

    @SerializedName("check_ad_btn_dvr3") val checkadbtndvr3: String,

    @SerializedName("check_ad_btn_dvr4") val checkadbtndvr4: String,

    @SerializedName("check_ad_btn_dvr5") val checkadbtndvr5: String,

    @SerializedName("check_ad_btn_dvr6") val checkadbtndvr6: String,

    @SerializedName("check_ad_dvr_back") val checkaddvrback: String,


    @SerializedName("check_ad_btn_room1") val checkadbtnroom1: String,

    @SerializedName("check_ad_btn_room2") val checkadbtnroom2: String,

    @SerializedName("check_ad_btn_room3") val checkadbtnroom3: String,

    @SerializedName("check_ad_btn_room4") val checkadbtnroom4: String,

    @SerializedName("check_ad_btn_room5") val checkadbtnroom5: String,

    @SerializedName("check_ad_btn_room6") val checkadbtnroom6: String,

    @SerializedName("check_ad_btn_room7") val checkadbtnroom7: String,

    @SerializedName("check_ad_btn_room8") val checkadbtnroom8: String,

    @SerializedName("check_ad_room_back") val checkadroomback: String,


    @SerializedName("check_ad_btn_videotype1") val checkadbtnvideotype1: String,

    @SerializedName("check_ad_btn_videotype2") val checkadbtnvideotype2: String,

    @SerializedName("check_ad_btn_videotype3") val checkadbtnvideotype3: String,

    @SerializedName("check_ad_btn_videotype4") val checkadbtnvideotype4: String,

    @SerializedName("check_ad_btn_videotype5") val checkadbtnvideotype5: String,

    @SerializedName("check_ad_btn_videotype6") val checkadbtnvideotype6: String,

    @SerializedName("check_ad_btn_videotype7") val checkadbtnvideotype7: String,

    @SerializedName("check_ad_btn_videotype8") val checkadbtnvideotype8: String,

    @SerializedName("check_ad_btn_videotype_auto") val checkadbtnvideotypeauto: String,

    @SerializedName("check_ad_videotype_back") val checkadvideotypeback: String,


    @SerializedName("check_ad_btn_cameratype1") val checkadbtncameratype1: String,

    @SerializedName("check_ad_btn_cameratype2") val checkadbtncameratype2: String,

    @SerializedName("check_ad_btn_cameratype3") val checkadbtncameratype3: String,

    @SerializedName("check_ad_btn_cameratype4") val checkadbtncameratype4: String,

    @SerializedName("check_ad_btn_cameratype5") val checkadbtncameratype5: String,


    @SerializedName("check_ad_btn_cameratype6") val checkadbtncameratype6: String,

    @SerializedName("check_ad_btn_cameratype7") val checkadbtncameratype7: String,

    @SerializedName("check_ad_btn_cameratype8") val checkadbtncameratype8: String,

    @SerializedName("check_ad_btn_cameratype_auto") val checkadbtncameratypeauto: String,

    @SerializedName("check_ad_cameratype_back") val checkadcameratypeback: String,

    @SerializedName("check_ad_btn_host") val checkadbtnhost: String,

    @SerializedName("check_ad_btn_client") val checkadbtnclient: String,

    @SerializedName("check_ad_host_client_back") val checkadhostclientback: String,


    @SerializedName("check_ad_btn_connect_to_camera") val checkadbtnconnecttocamera: String,

    @SerializedName("check_ad_connect_to_camera_back") val checkadconnecttocameraback: String,

    @SerializedName("check_ad_btn_my_home") val checkadbtnmyhome: String,

    @SerializedName("check_ad_btn_livingroom") val checkadbtnlivingroom: String,

    @SerializedName("check_ad_btn_bedroom") val checkadbtnbedroom: String,

    @SerializedName("check_ad_btn_kitchen") val checkadbtnkitchen: String,

    @SerializedName("check_ad_btn_hall") val checkadbtnhall: String,

    @SerializedName("check_ad_btn_card_security") val checkadbtncardsecurity: String,


    @SerializedName("check_ad_btn_card_cctv") val checkadbtncardcctv: String,

    @SerializedName("check_ad_btn_card_lighting") val checkadbtncardlighting: String,

    @SerializedName("check_ad_btn_card_internet") val checkadbtncardinternet: String,

    @SerializedName("check_ad_camera_canection_back") val checkadcameracanectionback: String,

    @SerializedName("check_ad_room_controller_back") val checkadroomcontrollerback: String,

    @SerializedName("check_ad_camera_liveprivew_back") val checkadcameraliveprivewback: String,

    @SerializedName("check_ad_camera_details_back") val checkadcameradetailsback: String,

    @SerializedName("check_ad_btn_ip_surveillance_guide") val checkadbtnipsurveillanceguide: String,

    @SerializedName("check_ad_btn_analogvsip_guide") val checkadbtnanalogvsipguide: String,

    @SerializedName("check_ad_btn_ipcamera_features_guide") val checkadbtnipcamerafeaturesguide: String,

    @SerializedName("check_ad_btn_imaging_lenses_guide") val checkadbtnimaginglensesguide: String,

    @SerializedName("check_ad_btn_imaging_day_night_guide") val checkadbtnimagingdaynightguide: String,

    @SerializedName("check_ad_manualcamera_back") val checkadmanualcameraback: String,

    @SerializedName("check_ad_ipcamera_features_back") val checkadipcamerafeaturesback: String,

    @SerializedName("check_ad_analogvsip_back") val checkadanalogvsipback: String,

    @SerializedName("check_ad_ipconfig_back") val checkadipconfigback: String,

    @SerializedName("check_ad_mainactivity_back") val checkadmainactivityback: String,

    @SerializedName("check_ad_power_camera_back") val checkadpowercameraback: String,

    @SerializedName("check_ad_sound_back") val checkadsoundback: String,

    @SerializedName("check_ad_add_device_back") val checkadadddeviceback: String,

    @SerializedName("check_ad_deviceInfo_back") val check_ad_deviceInfo_back: String,

    @SerializedName("check_ad_btn_speedtest_inter") val checkadbtnspeedtestinter: String,
    @SerializedName("check_ad_internet_speedtest_btn_inter") val checkAdInternetSpeedTestInter: String,
    @SerializedName("check_ad_device_info_btn_inter") val checkAdDeviceInfoBtnInter: String,
    @SerializedName("check_ad_wifi_list_btn_inter") val checkAdWifiInfoBtnInter: String,
    @SerializedName("maualcameraonoff") val ManualCameraonoff: String,

    @SerializedName("check_ad_btn_wifi_casting_inter") val checkadbtnWifiCastinginter: String,

    @SerializedName("check_ad_casting_view_inter") val checkAdCastingViewInter: String,

    @SerializedName("check_ad_selectphone_inter") val checkAdPelectphoneInter: String,

    @SerializedName("check_ad_hiddencamerabtn_inter") val checkAdHiddencamerabtnInter: String,

    @SerializedName("check_ad_minicamerabtn_inter") val checkAdMinicamerabtnInter: String,

    @SerializedName("check_ad_devicedetailbtn_inter") val checkAdDevicedetailbtnInter: String,

    @SerializedName("check_ad_deviceoptionbtn_inter") val checkAdDeviceoptionbtnInter: String,

    @SerializedName("check_ad_bluetoothconnectionbtn_inter") val checkAdBluetoothconnectionbtnInter: String,

    @SerializedName("check_ad_wificonnectionbtn_inter") val checkAdWificonnectionbtnInter: String,

    @SerializedName("check_ad_qrconnectionbtn_inter") val checkAdQrconnectionbtnInter: String,

    @SerializedName("check_ad_livecamerabtn_inter") val checkAdLivecamerabtnInter: String,

    @SerializedName("check_ad_qrscaningbtn_inter") val checkAdQrscaningbtnInter: String,

    @SerializedName("check_ad_calculator_btn_inter") val checkAdCalculatorBtnInter: String,

    @SerializedName("check_ad_btn_ipv4calculator_inter") val checkadbtnipv4calculatorinter: String,

    @SerializedName("check_ad_btn_ipv6calculator_inter") val checkadbtnipv6calculatorinter: String,

    @SerializedName("check_ad_btn_ipconverter_inter") val checkadbtnipconverterinter: String,

    @SerializedName("check_ad_btn_calculate_inter") val checkadbtncalculateinter: String,

    @SerializedName("check_ad_btn_reset_inter") val checkadbtnresetinter: String,

    @SerializedName("check_ad_welcom_intro_onoff") val checkAdWelcomIntroadOnoff: String,

    @SerializedName("check_ad_nativefull") val checkAdNativefull: String,

    @SerializedName("check_ad_nativefull_onoff") val checkAdNativefullOnoff: String,

    @SerializedName("check_ad_welcom_nativead_onoff") val checkAdWelcomNativeadOnoff: String,
    @SerializedName("app_banner") val appBanner: String,
    @SerializedName("banner_link") val bannerLink: String,
    @SerializedName("addonoff") val addOnOff: String,
)

