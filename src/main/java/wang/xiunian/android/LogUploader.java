package wang.xiunian.android;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by wangxiunian on 2016/10/14.
 */

public class LogUploader extends Thread implements Comparator<File> {
    private File[] mCachedFiles;
    private File mCacheDir;
    private Map<String, String> mInfoMap;
    private String mUploadUrl;

    LogUploader(File[] cachedFiles, File cacheDir, String url, Map<String, String> info) {
        mCacheDir = cacheDir;
        mCachedFiles = pruneRecord(cachedFiles);
        mInfoMap = info;
        mUploadUrl = url;
        try {
            String command = "chmod 777 " + mCacheDir.getPath();
            Runtime runtime = Runtime.getRuntime();
            runtime.exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();
        File zipFile = null;
        try {
            StringBuffer sb = new StringBuffer();
            for (Map.Entry<String, String> entry : mInfoMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                sb.append(key + "=" + value + "\n");
            }
            zipFile = zipFiles(mCachedFiles, new File(mCacheDir, "package.zip"), sb.toString());
            upload(zipFile);
            zipFile.delete();
            for (File cachedFile : mCachedFiles) {
                boolean delSuc = cachedFile.delete();
                System.out.println(delSuc);
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (zipFile != null) {
                zipFile.delete();
            }
        }
    }

    @Override
    public int compare(File o1, File o2) {
        if (o1 == null || o2 == null) {
            return 0;
        }
        long t1 = o1.lastModified(), t2 = o2.lastModified();
        if (t1 > t2) {
            return -1;
        } else if (t1 < t2) {
            return 1;
        }
        return 0;
    }


    protected File zipFiles(File[] files, File outputZip, String comment) throws IOException {
        FileOutputStream outStream = new FileOutputStream(outputZip);
        CheckedOutputStream cos = new CheckedOutputStream(outStream, new Adler32());
        ZipOutputStream zos = new ZipOutputStream(cos);
        BufferedOutputStream out = new BufferedOutputStream(zos);
        zos.setComment(comment);
        for (File file : files) {
            zos.putNextEntry(new ZipEntry(file.getName()));
            FileChannel in = new FileInputStream(file).getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while (in.read(buffer) != -1) {
                buffer.flip();
                out.write(buffer.array());
                buffer.clear();
            }
            in.close();
            out.flush();
        }
        out.close();
        System.out.println("checkSum:" + cos.getChecksum().getValue());
        return outputZip;
    }

    protected void upload(File file) throws IOException {
        MultipartUtility utility = new MultipartUtility(mUploadUrl, "utf-8");
        utility.addFilePart("image", file);
        String result = utility.finish();
        System.out.println(result);

    }

    private File[] pruneRecord(File[] files) {
        if (files == null || files.length == 0) {
            return files;
        }
        Arrays.sort(files, this);
        ArrayList<File> savedFile = new ArrayList<>();
        for (File file : files) {
            if (file.isFile() && file.length() == 0) {
                file.delete();
            } else {
                savedFile.add(file);
                if (savedFile.size() >= 5) {
                    break;
                }
            }
        }
        return savedFile.toArray(new File[savedFile.size()]);
    }
}
