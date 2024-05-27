package com.janche.pdf.utils;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;


/**
 * @Description:
 * @Auther: lirong
 * @Date: 2024/04/16
 */
@Slf4j
public class FreeMarkerUtils {

    private static Template getTemplate(String templateFileName) {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_29);
        Template template = null;
        try {
            configuration.setObjectWrapper(new DefaultObjectWrapper());
            //设置编码格式
            configuration.setDefaultEncoding("UTF-8");
            //模板文件
            configuration.setClassForTemplateLoading(FreeMarkerUtils.class, "/templates");
            template = configuration.getTemplate(templateFileName + ".ftl", StandardCharsets.UTF_8.toString());
        } catch (IOException e) {
            e.printStackTrace();
            log.error("get template file failed, fileName:{}", templateFileName);
        }
        return template;
    }

    public static void generateFile(Map<String, Object> input, String templateFileName, String savePath, String fileName) {
        Template template = getTemplate(templateFileName);
        File filePath = new File(savePath);
        if (!filePath.exists()) {
            filePath.mkdirs();
        }
        String filename = savePath + "/" + fileName;
        File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }
        Writer writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_8);
            template.process(input, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String generateHtmlStr(Map<String, Object> variables, String templateFileName) {
        Template template = getTemplate(templateFileName);
        StringWriter stringWriter = new StringWriter();
        template.setEncoding("UTF-8");
        try (BufferedWriter writer = new BufferedWriter(stringWriter)) {
            template.process(variables, writer);
            String htmlStr = stringWriter.toString();
            writer.flush();
            return htmlStr;
        } catch (TemplateException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除xml无法识别的非法字符
     * @param content
     * @return
     */
    public static String removeIllegalChar(String content) {
        return content.replaceAll("[\\x00-\\x08\\x0b-\\x0c\\x0e-\\x1f]", "");
    }

    /**
     * 将字符串中的XML特殊字符转义
     * & -> &amp;
     * < -> &lt;
     * > -> &gt;
     * " -> &quot;
     * ' -> &apos;
     * **/
    public static String covertXML(String content) {
        // remove xml无法显示的非法字符
        return content.replaceAll("[\\x00-\\x08\\x0b-\\x0c\\x0e-\\x1f]", "")
                .replaceAll("&lt;<", "&lt;")
                .replaceAll("&gt;>", "&gt;")
                .replaceAll("&amp;&", "&amp;");
    }

    private static boolean isValidXmlChar(char c) {
        return (c == 0x9 || c == 0xA || c == 0xD ||
                (c >= 0x20 && c <= 0xD7FF) ||
                (c >= 0xE000 && c <= 0xFFFD) ||
                (c >= 0x10000 && c <= 0x10FFFF));
    }

}
