package org.jeecg.modules.utils;

import cn.hutool.core.util.StrUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jeecg.common.exception.JeecgBootException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

import static com.aliyuncs.auth.AcsURLEncoder.percentEncode;


/**
 * @className: CommonUtils
 * @author: Cikian
 * @date: 2024/10/22 13:38
 * @Version: 1.0
 * @description: 通用工具类
 */

@Component
public class CommonUtils {
    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    public static <T> T getBatchMapper(SqlSession sqlSession, Class<T> clazz) {
        return sqlSession.getMapper(clazz);
    }

    /**
     * 获取人员唯一确定标识name@sex@birth，eg：张三@男@19900101
     *
     * @param name
     * @param sex
     * @param birth
     * @return hashId
     */
    public static String hashId(String name, String sex, String birth) {
        if (name == null || sex == null || birth == null) {
            throw new JeecgBootException("姓名、性别、出生日期不能为空");
        }

        name = name.replaceAll("\\s+", "");
        sex = sex.replaceAll("\\s+", "");
        birth = formatDate(birth);

        return name + "@" + sex + "@" + birth;
    }

    /**
     * 通过hashId获取姓名
     *
     * @param hashId
     * @return name
     */
    public static String getNameByHashId(String hashId) {
        if (hashId == null) {
            throw new JeecgBootException("hashId不能为空");
        }

        String[] arr = hashId.split("@");
        if (arr.length < 1) {
            throw new JeecgBootException("hashId格式不正确");
        }

        return arr[0];
    }

    /**
     * 通过hashId获取性别
     *
     * @param hashId
     * @return sex
     */
    public static String getSexByHashId(String hashId) {
        if (hashId == null) {
            throw new JeecgBootException("hashId不能为空");
        }

        String[] arr = hashId.split("@");
        if (arr.length < 2) {
            throw new JeecgBootException("hashId格式不正确");
        }

        return arr[1];
    }

    /**
     * 通过hashId获取出生日期
     *
     * @param hashId
     * @return birth
     */
    public static String getBirthStrByHashId(String hashId) {
        if (hashId == null) {
            throw new JeecgBootException("hashId不能为空");
        }

        String[] arr = hashId.split("@");
        if (arr.length < 3) {
            throw new JeecgBootException("hashId格式不正确");
        }

        return arr[2];
    }

    /**
     * 通过hashId获取出生日期
     *
     * @param hashId
     * @return birth
     */
    public static Date getBirthByHashId(String hashId) {
        if (hashId == null) {
            throw new JeecgBootException("hashId不能为空");
        }

        String[] arr = hashId.split("@");
        if (arr.length < 3) {
            throw new JeecgBootException("hashId格式不正确");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        try {
            return sdf.parse(arr[2]);
        } catch (Exception e) {
            throw new JeecgBootException("出生日期格式不正确");
        }
    }

    /**
     * 将日期格式统一为8位数字
     *
     * @param date
     * @return
     * @description: birth可能的形式：19900101、1990-01-01、1990/01/01、1990.01.01、1990年01月01日、1990年1月1日、1990年1月01日、1990.1.1等
     * 为了保证唯一性，需要将birth转换为统一格式：19900101
     */
    public static String formatDate(String date) {
        if (date == null) {
            throw new IllegalArgumentException("日期不能为空");
        }

        date = date.replaceAll("\\s+", "");
        date = date.replaceAll("[^0-9]", "");

        if (date.length() < 6) {
            throw new IllegalArgumentException("日期格式不正确");
        }

        // 如果日期长度为6位，表示格式为YYYYMM
        if (date.length() == 6) {
            return date.substring(0, 4) + "0" + date.charAt(4) + "0" + date.charAt(5);
        }

        // todo：实际处理不正确，需要修改
        // 如果日期长度为7位，表示格式为YYYYMDD或YYYYMMDD
        if (date.length() == 7) {
            if (date.charAt(4) == '0' || date.charAt(5) == '0') {
                return date.substring(0, 4) + "0" + date.substring(4);
            } else {
                return date.substring(0, 4) + date.substring(4, 6) + "0" + date.substring(6);
            }
        }

        // 如果日期长度为8位，表示格式为YYYYMMDD
        if (date.length() == 8) {
            return date;
        }

        throw new IllegalArgumentException("日期格式不正确");
    }

    /**
     * 日期转字符串
     * 转换为YYYYMMDD格式
     *
     * @param date
     * @return
     */
    public static String dateToString(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("日期不能为空");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return sdf.format(date);
    }

    /**
     * 用于随机选的数字
     */
    public static final String BASE_NUMBER = "23456789";
    /**
     * 用于随机选的字符
     */
    public static final String BASE_CHAR = "abcdefghjkmnpqrstuvwxy";
    /**
     * 用于随机选的字符和数字
     */
    public static final String BASE_CHAR_NUMBER = BASE_CHAR + BASE_NUMBER;

    /**
     * 获得一个随机的字符串
     *
     * @param baseString 随机字符选取的样本
     * @param length     字符串的长度
     * @return 随机字符串
     */
    public static String randomString(String baseString, int length) {
        if (StrUtil.isEmpty(baseString)) {
            return StrUtil.EMPTY;
        }
        final StringBuilder sb = new StringBuilder(length);

        if (length < 1) {
            length = 1;
        }
        int baseLength = baseString.length();
        for (int i = 0; i < length; i++) {
            int number = randomInt(baseLength);
            sb.append(baseString.charAt(number));
        }
        return sb.toString();
    }

    /**
     * 获得指定范围内的随机数 [0,limit)
     *
     * @param limit 限制随机数的范围，不包括这个数
     * @return 随机数
     */
    public static int randomInt(int limit) {
        return getRandom().nextInt(limit);
    }

    /**
     * 获取随机数生成器对象<br>
     * ThreadLocalRandom是JDK 7之后提供并发产生随机数，能够解决多个线程发生的竞争争夺。
     *
     * @return {@link ThreadLocalRandom}
     * @since 3.1.2
     */
    public static ThreadLocalRandom getRandom() {
        return ThreadLocalRandom.current();
    }

    /**
     * 获得一个随机的字符串（只包含数字和字符）
     *
     * @param length 字符串的长度
     * @return 随机字符串
     */
    public static String randomString(int length) {
        return randomString(BASE_CHAR_NUMBER, length);
    }

    /**
     * 读取资源目录下的文件的内容
     *
     * @param fileName 文件名
     * @return 文件内容String
     */
    public static String readFileContentFromResources(String fileName) throws NullPointerException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
            return content.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 读取资源目录下的文件
     *
     * @param fileName 文件名
     * @return InputStream
     */
    public static InputStream getFileStreamFromResources(String fileName) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader.getResourceAsStream(fileName);
    }

    public static String getFormatDateStr(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }


    public static void downTem(HttpServletResponse response, String filePath, String realFileName) throws UnsupportedEncodingException {

        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(realFileName, "UTF-8"));
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

        try {
            // InputStream inputStream = null;
            // inputStream = new FileInputStream(filePath);
            // byte[] data = null;
            // data = new byte[inputStream.available()];
            // inputStream.read(data);
            //
            // OutputStream os = response.getOutputStream();
            // os.write(data);
            // os.flush();
            // os.close();
            // inputStream.close();

            InputStream is = new FileInputStream(filePath);
            OutputStream os = response.getOutputStream();
            os = response.getOutputStream();


            IOUtils.copy(is,os);
            // byte[] buffer = new byte[1024]; // 文件流缓存池
            // int data = 0;
            // while ((data = is.read(buffer)) != -1) {
            //     os.write(buffer, 0, data);
            // }

            // os.flush();
            // os.close();
            // is.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
