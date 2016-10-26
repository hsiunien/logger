package wang.xiunian.android;


import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by wangxiunian on 2016/10/11.
 */

@RunWith(JUnit4.class)
public class LoggerReader {
    @Test
    public void testReader() {
        InputStream is = getClass().getResourceAsStream("/a.txt");
        try {
            GZIPInputStream gzipInputStream = new GZIPInputStream(is);
            BufferedReader br = new BufferedReader(new InputStreamReader(gzipInputStream));
            String s;
            int line = 0;
            while ((s = br.readLine()) != null) {
                System.out.println(line++ + ": " + s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRecordBuff() throws DataFormatException {
        InputStream is = getClass().getResourceAsStream("/a.txt");
        // Compress the bytes
        String origin = "1234567890";
        CacheData cd = new CacheData(19);
        for (int i = 0; i < 100; i++) {
            cd.cached(origin.getBytes(), new CacheData.ReceiveCached() {
                @Override
                public void receive(byte[] cached) {
                    System.out.println(new String(cached));
                }
            });
        }

    }

    @Test
    public void testUpload() {
        File file = new File(getClass().getResource("/cache/16_10_1315_34_31_542log.txt").getFile());
        File rootDir = new File(getClass().getResource("/cache/").getFile());
        Assert.assertTrue(rootDir.isDirectory());
        Assert.assertTrue(file.exists());
        LogUploader logUploader = new LogUploader(null, null, "", null);
        try {
            logUploader.zipFiles(rootDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".txt");
                }
            }), new File(rootDir, "file.zip"), "");
        } catch (IOException e) {
            e.printStackTrace();
        }
//        logUploader.upload(file);

    }

    @Test
    public void testDecode() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException {
        File file = new File(getClass().getResource("/test3.txt").getFile());
        File rootDir = new File(getClass().getResource("/cache/").getFile());
        Assert.assertTrue(file.exists());
//        logUploader.upload(file);

        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        secureRandom.setSeed("123".getBytes());
        keyGenerator.init(secureRandom);
        Key key = keyGenerator.generateKey();
        Cipher cp = Cipher.getInstance("AES");
        cp.init(Cipher.DECRYPT_MODE, key);
        InputStream in = new FileInputStream(file);
        CipherInputStream cipherInputStream = new CipherInputStream(in, cp);
        FileOutputStream fos = new FileOutputStream(new File(rootDir, "out.txt"));
        byte[] buff = new byte[1024];
        int len = 0;
        while (-1 != (len = cipherInputStream.read(buff))) {
            fos.write(buff, 0, len);
        }
        cipherInputStream.close();
        fos.close();
    }

}
