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
    private static final int MAX_LENGTH = 512;
    private CacheData mCacheData;

    private GZIPOutputStream gzipOutputStream = null;

    LogOutputStream(OutputStream out, int maxLength) {
        super(new BufferedOutputStream(out));
        mCacheData = new CacheData(maxLength == -1 ? MAX_LENGTH : maxLength);
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
        try {
            gzipOutputStream.write(cached);
            gzipOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        gzipOutputStream.close();
        super.close();
    }
}
