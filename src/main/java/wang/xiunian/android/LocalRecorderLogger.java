package wang.xiunian.android;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 对输出的日志进行记录,根据规则上送日志
 * Created by wangxiunian on 2016/10/10.
 */

public class LocalRecorderLogger implements L.ILog, Runnable {
    private FileOutputStream mFileOutputStream;
    private BlockingQueue<String> mCachedQueue;
    private File mCachedFolder;
    private File mOutputFile;
    private Builder mBuilder;
    private String mDeviceId;
    private HashMap<String, String> mDeviceAttrMap;
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.CHINA);
    private static final DateFormat DATE_FORMAT_FILE_NAME = new SimpleDateFormat("yy_MM_ddHH_mm_ss_SSS", Locale.CHINA);

    private LocalRecorderLogger(Context context, Builder builder) {
        mCachedFolder = context.getCacheDir();
        mDeviceId = builder.uniqueDeviceId;
        this.mBuilder = builder;
        try {
            mOutputFile = new File(mCachedFolder.getPath() + File.separator
                    + DATE_FORMAT_FILE_NAME.format(new Date()) + "log.gz");
            mFileOutputStream = new FileOutputStream(mOutputFile);
            mCachedQueue = new LinkedBlockingDeque<>();
            collectDeviceInfo(context);
            new Thread(this).start();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void v(String tag, Object... message) {
       //write(tag, message);
    }

    @Override
    public void d(String tag, Object... message) {
        write(tag, message);
    }

    @Override
    public void i(String tag, Object... message) {
        write(tag, message);
    }

    @Override
    public void w(String tag, Object... message) {
        write(tag, message);
    }

    @Override
    public void e(String tag, Object... message) {
        for (int i = 0; i < message.length; i++) {
            Object o = message[i];
            if (o instanceof Exception) {
                Exception ex = (Exception) o;
                Writer writer = new StringWriter();
                PrintWriter printWriter = new PrintWriter(writer);
                ex.printStackTrace(printWriter);
                Throwable cause = ex.getCause();
                while (cause != null) {
                    cause.printStackTrace(printWriter);
                    cause = cause.getCause();
                }
                printWriter.close();
                message[i] = writer.toString();
            }
        }
        write(tag + "/E", message);
    }

    public void upload() {
        checkShouldUploade();
    }

    private void checkShouldUploade() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String result = sendGet(mBuilder.checkUrl, "id=" + mDeviceId);
                    System.out.println("result" + result);
                    File[] files = mCachedFolder.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return name.endsWith(".gz") && !TextUtils.equals(mOutputFile.getName(), name);
                        }
                    });
                    new LogUploader(files, mCachedFolder, mBuilder.uploadUrl, mDeviceAttrMap).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    private void write(String tag, Object... messages) {
        try {
            for (Object message : messages) {
                String outMessage = DATE_FORMAT.format(new Date()) + " " + tag + ":" + message.toString()+"\r\n";
                mCachedQueue.put(outMessage);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        LogOutputStream bos = null;
        try {
            bos = new LogOutputStream(mFileOutputStream);
            while (!Thread.interrupted()) {
                String msg = mCachedQueue.take();
                bos.write(msg.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bos != null) {
                    bos.write("the end-------".getBytes());
                    bos.close();
                }
                mFileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    /**
     * 收集设备参数信息
     */
    public void collectDeviceInfo(Context ctx) {
        if (mDeviceAttrMap == null) {
            mDeviceAttrMap = new HashMap<>();
        }
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                mDeviceAttrMap.put("versionName", versionName);
                mDeviceAttrMap.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                mDeviceAttrMap.put(field.getName(), field.get(null).toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String sendGet(String url, String param) throws IOException {
        String result = "";
        String urlName = url + "?" + param;
        URL realUrl = new URL(urlName);
        // 打开和URL之间的连接
        HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
        // 设置通用的请求属性
        conn.setRequestProperty("accept", "*/*");
        conn.setRequestProperty("connection", "Keep-Alive");
        // 建立实际的连接
        conn.connect();
        // 获取所有响应头字段
//            Map<String, List<String>> map = conn.getHeaderFields();
        // 遍历所有的响应头字段
//            for (String key : map.keySet()) {
//                System.out.println(key + "--->" + map.get(key));
//            }
        // 定义BufferedReader输入流来读取URL的响应

        int status = conn.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            BufferedReader in = null;
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += "/n" + line;
            }
            in.close();
        } else {
            throw new IOException("Server returned non-OK status: " + status);
        }
        return result;
    }

    public static class Builder {
        //// TODO: 2016/10/17  setrelease url
        private String checkUrl = "http://172.30.61.41:9001/check.json";
        private String uploadUrl = "http://172.30.61.41:9001/image.json";
        private String uniqueDeviceId;
        private static LocalRecorderLogger sLogger;

        /**
         * @param context
         * @param deviceId 设备的唯一编号 和用于区分设备的唯一标识的字段一致
         * @return
         */
        public LocalRecorderLogger build(Context context, String deviceId) {
            uniqueDeviceId = deviceId;
            if (sLogger == null) {
                synchronized (Builder.class) {
                    if (sLogger == null) {
                        sLogger = new LocalRecorderLogger(context, this);
                    }
                }
            }
            return sLogger;
        }

        public Builder setCheckUrl(String url) {
            this.checkUrl = url;
            return this;
        }

        public Builder setUploadUrl(String url) {
            this.checkUrl = url;
            return this;
        }

    }
}
