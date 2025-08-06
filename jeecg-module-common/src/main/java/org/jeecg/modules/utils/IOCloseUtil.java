package org.jeecg.modules.utils;


import java.io.Closeable;
import java.io.IOException;

/**
 * @className: IOCloseUtil
 * @author: Cikian
 * @date: 2024/11/14 09:00
 * @Version: 1.0
 * @description:
 */

public class IOCloseUtil {
    /**
     * IO流关闭工具类
     */
    public static void close(Closeable... io) {
        for (Closeable temp : io) {
            try {
                if (null != temp)
                    temp.close();
            } catch (IOException e) {
                System.out.println("" + e.getMessage());
            }
        }
    }

    public static <T extends Closeable> void closeAll(T... io) {
        for (Closeable temp : io) {
            try {
                if (null != temp)
                    temp.close();
            } catch (IOException e) {
                System.out.println("" + e.getMessage());
            }
        }

    }
}
