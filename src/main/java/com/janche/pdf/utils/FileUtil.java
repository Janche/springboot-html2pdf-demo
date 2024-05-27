package com.janche.pdf.utils;

import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @Description:
 * @Auther: lirong
 * @Date: 2024/04/26
 */
@Slf4j
public class FileUtil {

    /**
     * 功能:压缩多个文件成一个zip文件
     * @param srcFile：源文件列表
     * @param destFile：压缩后的文件
     * @param pwd：加密密码 为空则不加密
     */
    public static void zipFiles(List<File> srcFile, File destFile, String pwd) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        //压缩配置
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setCompressionLevel(CompressionLevel.HIGHER);
        boolean needEncrypt = StringUtils.hasText(pwd);
        if (needEncrypt) {
            zipParameters.setEncryptFiles(true);
            zipParameters.setEncryptionMethod(EncryptionMethod.AES);
            // Below line is optional. AES 256 is used by default.
            // You can override it to use AES 128. AES 192 is supported only for extracting.
            zipParameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
        }

        try (ZipFile zipFile = needEncrypt ? new ZipFile(destFile.getPath(), pwd.toCharArray())
                : new ZipFile(destFile.getPath())) {
            zipFile.addFiles(srcFile, zipParameters);
        } catch (IOException e) {
            log.error("zip file failed, isEncrypt:{}", needEncrypt, e);
            throw new RuntimeException(e);
        } finally {
            stopWatch.stop();
            log.info("zip file spend time:{}, zipLevel:{}, isEncrypt:{}", stopWatch.getTotalTimeMillis(),
                    zipParameters.getCompressionLevel().getLevel(), needEncrypt);
        }
    }

    public static void zipFiles(List<Path> files, Path outputZipFile) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(outputZipFile))) {
            // 设置压缩级别
            zos.setLevel(6);
            for (Path file : files) {
                zos.putNextEntry(new ZipEntry(file.getFileName().toString()));
                Files.copy(file, zos);
                zos.closeEntry();
                // Optionally delete the temp file after adding to zip
                Files.delete(file);
            }
        }
    }

    public static void deleteFile(File file) {
        if (Objects.nonNull(file) && file.exists()) {
            file.delete();
        }
    }

    public static byte[] downloadHttpFile(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //得到输入流
            try (InputStream inputStream = conn.getInputStream()) {
                //获取自己数组
                return FileUtil.inputStreamToByte(inputStream);
            }
        } catch (Exception e) {
            log.error("download url#{} file failed, {}", urlStr, e.getMessage());
            throw new RuntimeException("Download url file failed!");
        }
    }

    /**
     * 下载http文件流
     * @param fileURL
     * @param targetFile
     * @return
     */
    public static void downloadHttpFile(String fileURL, File targetFile) {
        try {
            URL url = new URL(fileURL);
            URLConnection connection = url.openConnection();
            try (InputStream inputStream = connection.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(targetFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        } catch (Exception e) {
            log.error("download url#{} file failed, {}", fileURL, e.getMessage());
            throw new RuntimeException("Download url file failed!");
        }
    }

    //File、FileInputStream 转换为byte数组
    public static byte[] inputStreamToByte(InputStream inputStream) {
        try {
            byte[] buffer = new byte[4096];
            int len = 0;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while ((len = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            bos.close();
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("文件转换失败!");
        }
    }
}
