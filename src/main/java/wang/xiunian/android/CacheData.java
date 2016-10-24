package wang.xiunian.android;

import java.util.Arrays;

/**
 * 用于缓存一些数组，提高压缩效率
 * Created by wangxiunian on 2016/10/23.
 */

public class CacheData {

    private byte[] mBuffer;
    private byte[] mOverFlow;
    private int mPos;

    CacheData(int maxSize) {
        mBuffer = new byte[maxSize];
        mPos = -1;
    }

    public void cached(byte[] data, ReceiveCached cached) {
        int out = mPos + data.length - mBuffer.length + 1;
        int len = out < 0 ? data.length : data.length - out;
        System.arraycopy(data, 0, mBuffer, mPos + 1, len);
        mPos = mPos + len;
        boolean isOverflow = out >= 0;
        if (isOverflow) {
            mOverFlow = new byte[out];
            System.arraycopy(data, len, mOverFlow, 0, out);
            cached.receive(mBuffer);
            reset();
            cached(mOverFlow, cached);
        }
    }

    public void reset() {
        mPos = -1;
        Arrays.fill(mBuffer, (byte) 0);
    }

    public interface ReceiveCached {
        void receive(byte[] cached);
    }
}
