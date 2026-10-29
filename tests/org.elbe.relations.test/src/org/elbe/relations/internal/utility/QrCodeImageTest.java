package org.elbe.relations.internal.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.eclipse.swt.graphics.ImageData;
import org.junit.jupiter.api.Test;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

/** @author lbenno */
public class QrCodeImageTest {
    private static final String ADDRESS = "/ip4/192.168.1.5/tcp/9042/p2p/12D3KooWAv1A9pyJsVn43oo47WRhq2eqpfyVaaZkiMSc6kPecHeL";

    @Test
    public void testRoundTrip() throws Exception {
        // the dialog uses 5 px per module at 100% zoom and more at higher zooms
        for (final int modulePixels : new int[] { 2, 5, 8 }) {
            assertEquals(ADDRESS, decode(QrCodeImage.render(ADDRESS, modulePixels), false),
                    "module size " + modulePixels);
        }
        // zxing's detector can't locate finder patterns at 1 px per module, its pure-barcode mode can
        assertEquals(ADDRESS, decode(QrCodeImage.render(ADDRESS, 1), true));
    }

    @Test
    public void testGeometry() {
        final ImageData one = QrCodeImage.render(ADDRESS, 1);
        final ImageData five = QrCodeImage.render(ADDRESS, 5);
        assertEquals(one.width, one.height);
        assertEquals(5 * one.width, five.width);
        assertEquals(5 * one.height, five.height);
        // the quiet zone is white
        for (int i = 0; i < QrCodeImage.QUIET_ZONE * 5; i++) {
            assertEquals(0, five.getPixel(i, i));
            assertEquals(0, five.getPixel(five.width - 1 - i, i));
        }
    }

    private String decode(final ImageData data, final boolean pure) throws Exception {
        final int[] pixels = new int[data.width * data.height];
        for (int y = 0; y < data.height; y++) {
            for (int x = 0; x < data.width; x++) {
                pixels[y * data.width + x] = data.getPixel(x, y) == 0 ? 0xFFFFFFFF : 0xFF000000;
            }
        }
        final BinaryBitmap bitmap = new BinaryBitmap(
                new HybridBinarizer(new RGBLuminanceSource(data.width, data.height, pixels)));
        return new QRCodeReader().decode(bitmap, pure ? Map.of(DecodeHintType.PURE_BARCODE, Boolean.TRUE) : null)
                .getText();
    }

}
