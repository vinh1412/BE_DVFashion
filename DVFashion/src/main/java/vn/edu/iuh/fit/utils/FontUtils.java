/*
 * @ {#} FontUtils.java   1.0     10/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.utils;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/*
 * @description: Utility class for handling fonts in PDF generation
 * @author: Tran Hien Vinh
 * @date:   10/11/2025
 * @version:    1.0
 */
@UtilityClass
@Slf4j
public class FontUtils {
    private static final String VIETNAMESE_FONT_PATH = "fonts/Roboto-Regular.ttf";

    private static final String VIETNAMESE_FONT_BOLD_PATH = "fonts/Roboto-SemiBold.ttf";

    // Get font that supports Vietnamese characters
    public static PdfFont getVietnameseFont() {
        try {
            return PdfFontFactory.createFont(
                    VIETNAMESE_FONT_PATH,
                    PdfEncodings.IDENTITY_H,
                    PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
            );
        } catch (IOException e) {
            log.warn("Cannot load Vietnamese font, falling back to Arial Unicode MS", e);
            try {
                return PdfFontFactory.createFont(
                        "C:/Windows/Fonts/arialuni.ttf",
                        PdfEncodings.IDENTITY_H,
                        PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
                );
            } catch (IOException ex) {
                log.warn("Cannot load Arial Unicode MS, using built-in Helvetica as fallback", ex);
                try {
                    return PdfFontFactory.createFont(
                            "Helvetica",
                            PdfEncodings.IDENTITY_H,
                            PdfFontFactory.EmbeddingStrategy.PREFER_NOT_EMBEDDED
                    );
                } catch (IOException exc) {
                    throw new RuntimeException("Failed to create font for Vietnamese text", exc);
                }
            }
        }
    }

    // Get bold font that supports Vietnamese characters
    public static PdfFont getVietnameseFontBold() {
        try {
            return PdfFontFactory.createFont(
                    VIETNAMESE_FONT_BOLD_PATH,
                    PdfEncodings.IDENTITY_H,
                    PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
            );
        } catch (IOException e) {
            log.warn("Cannot load bold font, fallback to regular font", e);
            return getVietnameseFont();
        }
    }
}

