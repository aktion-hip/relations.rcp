/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2026, Benno Luthiger
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 ***************************************************************************/
package org.elbe.relations.internal.utility;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/** Renders a text as QR code into SWT image data, without AWT.
 *
 * @author lbenno */
public final class QrCodeImage {
    /** The width of the white border around the code, in modules (as the QR standard requires). */
    public static final int QUIET_ZONE = 4;

    private static final int DARK = 1;

    private QrCodeImage() {
        // prevent instantiation
    }

    /** Renders the specified text as QR code with error correction level M.
     *
     * @param text String the content to encode
     * @param modulePixels int the number of pixels per module (the code's smallest square), at least 1
     * @return {@link ImageData} a square, 1-bit black on white image including the quiet zone
     * @throws IllegalArgumentException if the text cannot be encoded */
    public static ImageData render(final String text, final int modulePixels) {
        final Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, QUIET_ZONE);
        final BitMatrix matrix;
        try {
            // a requested size of 0 yields one pixel per module
            matrix = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, 0, 0, hints);
        }
        catch (final WriterException exc) {
            throw new IllegalArgumentException(exc.getMessage(), exc);
        }

        final int scale = Math.max(1, modulePixels);
        final int size = matrix.getWidth() * scale;
        final ImageData data = new ImageData(size, size, 1,
                new PaletteData(new RGB(255, 255, 255), new RGB(0, 0, 0)));
        final byte[] darkRow = new byte[scale];
        Arrays.fill(darkRow, (byte) DARK);
        for (int y = 0; y < matrix.getHeight(); y++) {
            for (int x = 0; x < matrix.getWidth(); x++) {
                if (matrix.get(x, y)) {
                    for (int line = 0; line < scale; line++) {
                        data.setPixels(x * scale, y * scale + line, scale, darkRow, 0);
                    }
                }
            }
        }
        return data;
    }

}
