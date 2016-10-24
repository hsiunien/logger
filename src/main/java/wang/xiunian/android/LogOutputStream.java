package wang.xiunian.android;

import java.io.BufferedOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by wangxiunian on 2016/10/20.
 */

public class LogOutputStream extends FilterOutputStream implements CacheData.ReceiveCached {
    private static final int sMaxLength = 512;
    private CacheData mCacheData;

    GZIPOutputStream gzipOutputStream = null;

    public LogOutputStream(OutputStream out) {
        super(new BufferedOutputStream(out));
        mCacheData = new CacheData(sMaxLength);
        try {
            gzipOutputStream = new GZIPOutputStream(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        mCacheData.cached(b, this);
    }


    @Override
    public void receive(byte[] cached) {
        System.out.print("out:" + cached.length);
        try {
            gzipOutputStream.write(cached);
            gzipOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        gzipOutputStream.close();
        super.close();
    }
}
