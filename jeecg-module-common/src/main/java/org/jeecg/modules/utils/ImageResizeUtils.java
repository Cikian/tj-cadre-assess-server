package org.jeecg.modules.utils;

import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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

    public static byte[] resizeHighQuality(byte[] imageData, int targetWidth, int targetHeight) throws IOException {
        BufferedImage sourceImage = ImageIO.read(new ByteArrayInputStream(imageData));

        // 创建高质量的目标图像
        BufferedImage destImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = destImage.createGraphics();

        // 设置高质量渲染参数
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

        // 绘制缩放后的图像
        g2d.drawImage(sourceImage, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();

        // 将图像写入字节数组
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(destImage, "png", baos);
        return baos.toByteArray();
    }
}