package org.jeecg.modules.utils;

import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ImageResizeUtils {

    // 调整图片尺寸（可选保持宽高比）
    public static byte[] resizeImage(byte[] originalBytes, int targetWidth, int targetHeight, boolean keepRatio)
            throws Exception {

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(originalBytes)) {
            BufferedImage originalImage = ImageIO.read(inputStream);

            // 调整尺寸逻辑
            BufferedImage resizedImage;
            if (keepRatio) {
                // 保持宽高比（按宽度等比缩放）
                resizedImage = Scalr.resize(originalImage, Scalr.Mode.FIT_TO_WIDTH, targetWidth);
            } else {
                // 强制指定宽高
                resizedImage = Scalr.resize(originalImage, Scalr.Method.QUALITY,
                        Scalr.Mode.FIT_EXACT, targetWidth, targetHeight);
            }

            // 转换为 PNG 字节数组
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                ImageIO.write(resizedImage, "png", outputStream);
                return outputStream.toByteArray();
            }
        }
    }

    public static byte[] resizeHighQuality(byte[] originalBytes, int targetWidth, int targetHeight) throws Exception {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(originalBytes)) {
            BufferedImage originalImage = ImageIO.read(inputStream);

            // 使用双三次插值（最高质量）
            BufferedImage resizedImage = Scalr.resize(
                    originalImage,
                    Scalr.Method.ULTRA_QUALITY,
                    Scalr.Mode.FIT_EXACT,
                    targetWidth,
                    targetHeight,
                    Scalr.OP_ANTIALIAS
            );

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                ImageIO.write(resizedImage, "png", outputStream);
                return outputStream.toByteArray();
            }
        }
    }
}