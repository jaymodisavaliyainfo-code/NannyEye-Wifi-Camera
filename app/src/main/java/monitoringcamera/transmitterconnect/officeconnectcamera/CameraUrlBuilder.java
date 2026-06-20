package monitoringcamera.transmitterconnect.officeconnectcamera;

public class CameraUrlBuilder {

    public enum Brand {
        DAHUA("Dahua / Imou / KBVision"),
        HIKVISION("Hikvision / HiLook"),
        REOLINK("Reolink"),
        TAPO("TP-Link Tapo"),
        UNIVIEW("Uniview / QNAP"),
        AXIS("Axis"),
        FOSCAM("Foscam"),
        ANNKE("Annke / Amcrest"),
        GENERIC("Generic / ONVIF / Other");

        private final String displayName;

        Brand(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public static String buildPath(Brand brand, int channel, boolean mainStream) {
        int sub = mainStream ? 0 : 1;
        switch (brand) {
            case DAHUA:
            case ANNKE:
                return "/cam/realmonitor?channel=" + channel + "&subtype=" + sub;

            case HIKVISION:
                return "/Streaming/Channels/" + channel + "0" + (sub + 1);

            case REOLINK:
                return mainStream ? "/Preview_01_main" : "/Preview_01_sub";

            case TAPO:
                return mainStream ? "/stream1" : "/stream2";

            case UNIVIEW:
                return "/unicast/c" + channel + "/s" + sub + "/live";

            case AXIS:
                return "/axis-media/media.amp";

            case FOSCAM:
                return mainStream ? "/videoMain" : "/videoSub";

            case GENERIC:
            default:
                return mainStream ? "/stream1" : "/stream2";
        }
    }

    public static int defaultPort(Brand brand) {
        return 554;
    }

    public static String defaultUsername(Brand brand) {
        switch (brand) {
            case AXIS: return "root";
            default: return "admin";
        }
    }

    public static String[] getBrandNames() {
        Brand[] brands = Brand.values();
        String[] names = new String[brands.length];
        for (int i = 0; i < brands.length; i++) {
            names[i] = brands[i].getDisplayName();
        }
        return names;
    }
}
