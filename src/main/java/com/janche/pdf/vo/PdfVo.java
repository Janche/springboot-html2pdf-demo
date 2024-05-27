package com.janche.pdf.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @Description:
 * @Auther: lirong
 * @Date: 2024/05/27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfVo {

    private String tokenCode;

    private String orderNo;

    private String bankName;

    private String bankNumber;

    private BigDecimal qty;

    private String updateTime;
}
