package com.xiang.pic.xiangPicBackend.utils;

import java.awt.*;
import java.util.regex.Pattern;

/**
 * 工具类：计算颜色相似度（基于 CIE Lab + CIEDE2000）
 *
 * 修正点：
 * 1. 修复 RGB -> XYZ -> Lab 转换中的白点尺度错误
 *    - 原代码 XYZ 结果是 0~1 范围
 *    - 但参考白点用了 95.047 / 100 / 108.883（这是 0~100 范围）
 *    - 量纲不一致会导致 Lab 值严重失真
 *
 * 2. 调整“色差 -> 相似度”的映射方式
 *    - 原来用线性映射：1 - deltaE / 38
 *    - 这个映射过于粗暴，会让差异较大的颜色也拿到偏高相似度
 *    - 改成指数衰减映射，更符合直觉
 */
public class ColorSimilarUtils {

    /**
     * 默认相似阈值
     */
    private static final double DEFAULT_THRESHOLD = 0.87;

    /**
     * 色差转相似度的衰减参数
     * 数值越大，相似度下降越慢
     */
    private static final double SIMILARITY_SCALE = 25.0;

    /**
     * D65 标准光源参考白点（注意这里是 0~1 范围）
     */
    private static final double REF_X = 0.95047;
    private static final double REF_Y = 1.00000;
    private static final double REF_Z = 1.08883;

    private ColorSimilarUtils() {
        // 工具类不需要实例化
    }

    /**
     * 统一处理颜色格式，支持：
     * 1. #RRGGBB
     * 2. #RGB
     * 3. 0xRRGGBB
     * 4. RRGGBB
     *
     * @param hexColor 十六进制颜色字符串
     * @return Color 对象
     */
    private static Color parseColor(String hexColor) {
        if (hexColor == null || hexColor.trim().isEmpty()) {
            throw new IllegalArgumentException("颜色值不能为空");
        }

        String colorStr = hexColor.trim();

        // 0xRRGGBB / 0XRRGGBB
        if (colorStr.startsWith("0x") || colorStr.startsWith("0X")) {
            return Color.decode(colorStr);
        }

        // #RRGGBB / #RGB
        if (colorStr.startsWith("#")) {
            String hex = colorStr.substring(1);

            if (hex.length() == 3) {
                // #RGB -> #RRGGBB
                hex = ""
                        + hex.charAt(0) + hex.charAt(0)
                        + hex.charAt(1) + hex.charAt(1)
                        + hex.charAt(2) + hex.charAt(2);
            }

            if (!Pattern.matches("[0-9A-Fa-f]{6}", hex)) {
                throw new IllegalArgumentException("不支持的颜色格式: " + hexColor);
            }

            return Color.decode("0x" + hex);
        }

        // 纯 6 位十六进制
        if (Pattern.matches("[0-9A-Fa-f]{6}", colorStr)) {
            return Color.decode("0x" + colorStr);
        }

        throw new IllegalArgumentException("不支持的颜色格式: " + hexColor);
    }

    /**
     * 将 sRGB 颜色转换到 CIE Lab 空间
     *
     * @param color RGB 颜色
     * @return Lab 数组 [L, a, b]
     */
    private static double[] rgbToLab(Color color) {
        // 获取 sRGB 分量（0~1）
        double r = color.getRed() / 255.0;
        double g = color.getGreen() / 255.0;
        double b = color.getBlue() / 255.0;

        // sRGB -> Linear RGB
        r = linearize(r);
        g = linearize(g);
        b = linearize(b);

        // Linear RGB -> XYZ（D65，结果范围也是 0~1）
        double x = r * 0.4124564 + g * 0.3575761 + b * 0.1804375;
        double y = r * 0.2126729 + g * 0.7151522 + b * 0.0721750;
        double z = r * 0.0193339 + g * 0.1191920 + b * 0.9503041;

        // XYZ -> Lab（这里白点也必须是 0~1 范围）
        x = f(x / REF_X);
        y = f(y / REF_Y);
        z = f(z / REF_Z);

        double l = 116.0 * y - 16.0;
        double a = 500.0 * (x - y);
        double bVal = 200.0 * (y - z);

        return new double[]{l, a, bVal};
    }

    /**
     * sRGB gamma 去除，转为线性 RGB
     *
     * @param value sRGB 分量（0~1）
     * @return 线性 RGB 分量
     */
    private static double linearize(double value) {
        if (value <= 0.04045) {
            return value / 12.92;
        } else {
            return Math.pow((value + 0.055) / 1.055, 2.4);
        }
    }

    /**
     * Lab 转换辅助函数
     *
     * @param t 输入值
     * @return 转换结果
     */
    private static double f(double t) {
        double delta = 6.0 / 29.0;
        double delta3 = delta * delta * delta;

        if (t > delta3) {
            return Math.cbrt(t);
        } else {
            return t / (3 * delta * delta) + 4.0 / 29.0;
        }
    }

    /**
     * 计算 CIEDE2000 色差
     *
     * @param lab1 颜色1的 Lab 值
     * @param lab2 颜色2的 Lab 值
     * @return ΔE00，越小表示颜色越接近
     */
    private static double deltaE00(double[] lab1, double[] lab2) {
        double L1 = lab1[0], a1 = lab1[1], b1 = lab1[2];
        double L2 = lab2[0], a2 = lab2[1], b2 = lab2[2];

        double avgL = (L1 + L2) / 2.0;
        double C1 = Math.sqrt(a1 * a1 + b1 * b1);
        double C2 = Math.sqrt(a2 * a2 + b2 * b2);
        double avgC = (C1 + C2) / 2.0;

        double G = 0.5 * (1 - Math.sqrt(Math.pow(avgC, 7) / (Math.pow(avgC, 7) + Math.pow(25.0, 7))));
        double a1p = (1 + G) * a1;
        double a2p = (1 + G) * a2;

        double C1p = Math.sqrt(a1p * a1p + b1 * b1);
        double C2p = Math.sqrt(a2p * a2p + b2 * b2);
        double avgCp = (C1p + C2p) / 2.0;

        double h1p = Math.toDegrees(Math.atan2(b1, a1p));
        if (h1p < 0) {
            h1p += 360;
        }

        double h2p = Math.toDegrees(Math.atan2(b2, a2p));
        if (h2p < 0) {
            h2p += 360;
        }

        double avgHp;
        if (C1p * C2p == 0) {
            avgHp = h1p + h2p;
        } else if (Math.abs(h1p - h2p) > 180) {
            avgHp = (h1p + h2p + 360) / 2.0;
        } else {
            avgHp = (h1p + h2p) / 2.0;
        }

        double T = 1
                - 0.17 * Math.cos(Math.toRadians(avgHp - 30))
                + 0.24 * Math.cos(Math.toRadians(2 * avgHp))
                + 0.32 * Math.cos(Math.toRadians(3 * avgHp + 6))
                - 0.20 * Math.cos(Math.toRadians(4 * avgHp - 63));

        double deltaLp = L2 - L1;
        double deltaCp = C2p - C1p;

        double deltahp;
        if (C1p * C2p == 0) {
            deltahp = 0;
        } else {
            deltahp = h2p - h1p;
            if (deltahp > 180) {
                deltahp -= 360;
            } else if (deltahp < -180) {
                deltahp += 360;
            }
        }

        double deltaHp = 2 * Math.sqrt(C1p * C2p) * Math.sin(Math.toRadians(deltahp / 2.0));

        double SL = 1 + (0.015 * Math.pow(avgL - 50, 2)) / Math.sqrt(20 + Math.pow(avgL - 50, 2));
        double SC = 1 + 0.045 * avgCp;
        double SH = 1 + 0.015 * avgCp * T;

        double deltaTheta = 30 * Math.exp(-Math.pow((avgHp - 275) / 25.0, 2));
        double RC = 2 * Math.sqrt(Math.pow(avgCp, 7) / (Math.pow(avgCp, 7) + Math.pow(25.0, 7)));
        double RT = -RC * Math.sin(Math.toRadians(2 * deltaTheta));

        double termL = deltaLp / SL;
        double termC = deltaCp / SC;
        double termH = deltaHp / SH;

        return Math.sqrt(
                termL * termL +
                        termC * termC +
                        termH * termH +
                        RT * termC * termH
        );
    }

    /**
     * 计算两个颜色的色差（ΔE00）
     *
     * @param color1 第一个颜色
     * @param color2 第二个颜色
     * @return 色差值，越小越接近
     */
    public static double calculateDeltaE(Color color1, Color color2) {
        double[] lab1 = rgbToLab(color1);
        double[] lab2 = rgbToLab(color2);
        return deltaE00(lab1, lab2);
    }

    /**
     * 根据十六进制颜色代码计算色差（ΔE00）
     *
     * @param hexColor1 第一个颜色
     * @param hexColor2 第二个颜色
     * @return 色差值，越小越接近
     */
    public static double calculateDeltaE(String hexColor1, String hexColor2) {
        Color color1 = parseColor(hexColor1);
        Color color2 = parseColor(hexColor2);
        return calculateDeltaE(color1, color2);
    }

    /**
     * 计算两个颜色的相似度（0~1）
     *
     * 说明：
     * - ΔE00 = 0    -> 相似度 = 1
     * - ΔE00 越大   -> 相似度越接近 0
     *
     * 这里使用指数衰减映射，避免线性映射过于宽松：
     * similarity = e^(-deltaE / SIMILARITY_SCALE)
     *
     * @param color1 第一个颜色
     * @param color2 第二个颜色
     * @return 相似度（0~1）
     */
    public static double calculateSimilarity(Color color1, Color color2) {
        double deltaE = calculateDeltaE(color1, color2);
        return Math.exp(-deltaE / SIMILARITY_SCALE);
    }

    /**
     * 根据十六进制颜色代码计算相似度
     *
     * @param hexColor1 第一个颜色
     * @param hexColor2 第二个颜色
     * @return 相似度（0~1）
     */
    public static double calculateSimilarity(String hexColor1, String hexColor2) {
        Color color1 = parseColor(hexColor1);
        Color color2 = parseColor(hexColor2);
        return calculateSimilarity(color1, color2);
    }

    /**
     * 判断两个颜色是否相似
     *
     * @param hexColor1 第一个颜色
     * @param hexColor2 第二个颜色
     * @param threshold 相似度阈值（推荐 0.80 ~ 0.90）
     * @return 是否相似
     */
    public static boolean isSimilar(String hexColor1, String hexColor2, double threshold) {
        return calculateSimilarity(hexColor1, hexColor2) >= threshold;
    }

    /**
     * 判断两个颜色是否相似（默认阈值 0.87）
     *
     * @param hexColor1 第一个颜色
     * @param hexColor2 第二个颜色
     * @return 是否相似
     */
    public static boolean isSimilar(String hexColor1, String hexColor2) {
        return isSimilar(hexColor1, hexColor2, DEFAULT_THRESHOLD);
    }

    /**
     * 示例
     */
    public static void main(String[] args) {
        // 1. 同一个颜色，不同格式，应该完全相同
        String sameColor1 = "#0910ec";
        String sameColor2 = "0x104020";
        double sameDeltaE = calculateDeltaE(sameColor1, sameColor2);
        double sameSimilarity = calculateSimilarity(sameColor1, sameColor2);
        System.out.println("同色不同格式色差：" + sameDeltaE);
        System.out.println("同色不同格式相似度：" + sameSimilarity);
        System.out.println("是否相似（阈值0.87）：" + isSimilar(sameColor1, sameColor2));

        System.out.println("--------------------------------------------------");

        // 2. 明显差很多的颜色
        String targetColor = "#ce8383";
        String dbColor = "0x104020";
        double deltaE1 = calculateDeltaE(targetColor, dbColor);
        double sim1 = calculateSimilarity(targetColor, dbColor);
        System.out.println("目标颜色 " + targetColor + " 与数据库颜色 " + dbColor + " 色差：" + deltaE1);
        System.out.println("目标颜色 " + targetColor + " 与数据库颜色 " + dbColor + " 相似度：" + sim1);
        System.out.println("是否相似（阈值0.87）：" + isSimilar(targetColor, dbColor));

        System.out.println("--------------------------------------------------");

        // 3. 真正相近的颜色
        String similar1 = "#ce8383";
        String similar2 = "#d18a8a";
        double deltaE2 = calculateDeltaE(similar1, similar2);
        double sim2 = calculateSimilarity(similar1, similar2);
        System.out.println("真正相似的颜色色差：" + deltaE2);
        System.out.println("真正相似的颜色相似度：" + sim2);
        System.out.println("是否相似（阈值0.87）：" + isSimilar(similar1, similar2));
    }
}