package monitoringcamera.transmitterconnect.officeconnectcamera;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class NetworkScanner {

    public static class FoundDevice {
        public final String ip;
        public final int port;
        public final long responseMs;

        public FoundDevice(String ip, int port, long responseMs) {
            this.ip = ip;
            this.port = port;
            this.responseMs = responseMs;
        }
    }

    public interface ScanCallback {
        void onProgress(int scanned, int total, String currentIp);
        void onDeviceFound(FoundDevice device);
        void onScanComplete(List<FoundDevice> devices);
    }

    private static final int[] CAMERA_PORTS = {554, 8554, 80, 8080};
    private static final int SOCKET_TIMEOUT_MS = 600;
    private static final int THREAD_POOL_SIZE = 30;

    private ExecutorService executor;
    private volatile boolean cancelled = false;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public String getDeviceIp(Context context) {
        try {
            WifiManager wm = (WifiManager)
                context.getApplicationContext()
                       .getSystemService(Context.WIFI_SERVICE);
            int ip = wm.getConnectionInfo().getIpAddress();
            if (ip != 0) {
                return String.format("%d.%d.%d.%d",
                    ip & 0xff,
                    (ip >> 8) & 0xff,
                    (ip >> 16) & 0xff,
                    (ip >> 24) & 0xff);
            }
        } catch (Exception e) {
            // fall through to fallback
        }
        return fallbackIp();
    }

    private String fallbackIp() {
        try {
            Enumeration<NetworkInterface> interfaces =
                NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return "192.168.1.1";
    }

    public String getSubnet(String ip) {
        int lastDot = ip.lastIndexOf('.');
        if (lastDot == -1) return "192.168.1";
        return ip.substring(0, lastDot);
    }

    public void scan(Context context, ScanCallback callback) {
        cancelled = false;
        executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        String deviceIp = getDeviceIp(context);
        String subnet = getSubnet(deviceIp);
        int total = 254;

        List<FoundDevice> found =
            Collections.synchronizedList(new ArrayList<>());
        AtomicInteger scanned = new AtomicInteger(0);

        for (int host = 1; host <= 254; host++) {
            if (cancelled) break;
            final String ip = subnet + "." + host;

            executor.submit(() -> {
                if (cancelled) return;
                for (int port : CAMERA_PORTS) {
                    try {
                        long start = System.currentTimeMillis();
                        Socket socket = new Socket();
                        socket.connect(
                            new InetSocketAddress(ip, port),
                            SOCKET_TIMEOUT_MS
                        );
                        socket.close();
                        long ms = System.currentTimeMillis() - start;

                        FoundDevice device = new FoundDevice(ip, port, ms);
                        found.add(device);

                        mainHandler.post(() -> callback.onDeviceFound(device));
                        break;

                    } catch (Exception ignored) {}
                }

                int done = scanned.incrementAndGet();
                mainHandler.post(() ->
                    callback.onProgress(done, total, ip)
                );

                if (done == total) {
                    mainHandler.post(() ->
                        callback.onScanComplete(found)
                    );
                }
            });
        }

        executor.shutdown();
    }

    public void cancel() {
        cancelled = true;
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}
