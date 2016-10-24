package wang.xiunian.android;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.util.Arrays;

/**
 * Created by wangxiunian on 2016/10/14.
 */
@RunWith(JUnit4.class)
public class SortTest {

    @Test
    public void sortTest() {
        System.out.println(this.toString());
        File[] files = new File[]{
                new File("16_10_1310_56_19_716log.txt"), new File("16_10_1310_58_57_459log.txt")
                , new File("16_10_1315_20_01_638log.txt"), new File("16_10_1315_20_01_634log.txt")
        };
        Arrays.sort(files);
        for (File file : files) {
            System.out.println(file);
        }
    }
}
