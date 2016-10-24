package wang.xiunian.android;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

/**
 * Created by wangxiunian on 2016/10/14.
 */

public class DecodeFile {
    public static void main(String[] args) {
        try {
            InputStream is = new FileInputStream(args[0]);
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
}
