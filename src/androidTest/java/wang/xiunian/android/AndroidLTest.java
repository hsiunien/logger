package wang.xiunian.android;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * Created by wangxiunian on 2016/10/9.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class AndroidLTest {

    @Test
    public void testLog1() {
        L.setLogLevel(L.LogLevel.VERBOSE);
        L.d("aaa", "testasdfjasldfj;asldkfj;asfvoisaudfp\noiweqporijasdpoifjweapiorji12345");
        Map<String, String> va = new HashMap<>();
        va.put("aaa", "bbb");
        L.d("kk", va, va);
        Assert.assertNotNull(null);
    }

    @Test
    public void testLocalLogger() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = null;
        try {
            byte[] originByte = "abcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefg".getBytes();
            gzipOutputStream = new GZIPOutputStream(baos);
            gzipOutputStream.write(originByte);
            gzipOutputStream.close();
            byte[] after = baos.toByteArray();
            Assert.assertTrue(after.length < originByte.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
