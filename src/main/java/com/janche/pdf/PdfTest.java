package com.janche.pdf;


import com.janche.pdf.utils.FileUtil;
import com.janche.pdf.utils.FreeMarkerUtils;
import com.janche.pdf.vo.PdfVo;
import com.lowagie.text.pdf.BaseFont;
import freemarker.cache.ClassTemplateLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * @Description:
 * @Auther: lirong
 * @Date: 2024/04/16
 */
@Slf4j
public class PdfTest {

    public static void main(String[] args) {

        ITextRenderer renderer = new ITextRenderer();
        try {
            addPdfFont(renderer);
            String htmlStr = FreeMarkerUtils.generateHtmlStr(loadPdfData(), "pdfTemplate");
            renderer.setDocumentFromString(FreeMarkerUtils.removeIllegalChar(htmlStr));
            renderer.layout();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        File pdfFile = new File("demo.pdf");
        File encryptPdfFile = new File("demo_encrypt.pdf");
        File zipFile = new File("demo.zip");

        try (OutputStream os = new FileOutputStream("demo.pdf")) {
            renderer.createPDF(os);

            // encrypt pdf, if needn't encrypt pdf file, can remove pdfBox dependency
            PDDocument document = PDDocument.load(pdfFile);
            StandardProtectionPolicy policy = new StandardProtectionPolicy("123456", "1234", new AccessPermission());

            policy.setEncryptionKeyLength(128);
            policy.setPermissions(new AccessPermission());
            document.protect(policy);
            document.save(encryptPdfFile);
            document.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        FileUtil.zipFiles(List.of(pdfFile, encryptPdfFile), zipFile, "1234");
        log.info("PDF file generated successfully!");
    }

    private static void addPdfFont(ITextRenderer renderer) throws IOException {
        // add English font
        ClassTemplateLoader classTemplateLoader = new ClassTemplateLoader(FreeMarkerUtils.class, "/static/font");
        String enFontPath = classTemplateLoader.getBasePackagePath() + "Poppins-Medium.ttf";
        ITextFontResolver fontResolver = renderer.getFontResolver();
        // the first font needn't set alias
        fontResolver.addFont(enFontPath, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
        // add Chinese font, the second font must set alias
        String chFontPath = classTemplateLoader.getBasePackagePath() + "PingFang-Regular.ttf";
        fontResolver.addFont(chFontPath, "PingFang", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED, null);
    }

    public static Map<String, Object> loadPdfData() {
        Map<String, Object> data = new HashMap<>();
        // 启用
        data.put("display", "block");
        // 隐藏
//        data.put("display", "none");
        data.put("footerName", "footer-龍");
        data.put("userName", "龍年發财");
        data.put("accountName", "Jackson-龍");
        data.put("accountId", "234324");
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        data.put("headerDate", now.format(dateFormatter));

        ArrayList<PdfVo> tableList = new ArrayList<>();
        IntStream.range(1, 20).forEach(i -> {
            LocalDateTime dateTime = now.plusDays(i);
            PdfVo pdfVo = PdfVo.builder()
                    .tokenCode("USD" + i)
                    .bankName("HK Bank" + i)
                    .bankNumber("&23**94&" + i)
                    .qty(BigDecimal.TEN.add(new BigDecimal(i)))
                    .orderNo("ab123445" + i)
                    .updateTime(dateTime.format(dateFormatter))
                    .build();
            tableList.add(pdfVo);
        });

        data.put("list", tableList);
        return data;
    }
}
