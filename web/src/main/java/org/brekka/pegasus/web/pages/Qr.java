/**
 * 
 */
package org.brekka.pegasus.web.pages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Response;
import org.brekka.pegasus.web.support.Configuration;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public class Qr {
    
    @Inject
    private Configuration configuration;
    
    
    Object onActivate(String code, String tokenZip) throws Exception {
        return activate(String.format("%s/%s", code, tokenZip));
    }
    
    Object onActivate(String code, String token, String filename) throws Exception {
        return activate(String.format("%s/%s/%s", code, token, filename));
    }
    
    Object activate(String path) throws Exception {
        String fullUrl = configuration.getFetchBase() + "/" + path;
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix;
        try {
            matrix = writer.encode(fullUrl, BarcodeFormat.QR_CODE, 200, 200);
        } catch (WriterException e) {
            throw new IOException(e);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "png", baos);
        
        baos.close();
        final byte[] result = baos.toByteArray();
        
        return new StreamResponse() {
            
            @Override
            public void prepareResponse(Response response) {
                response.setContentLength(result.length);
            }
            
            @Override
            public InputStream getStream() throws IOException {
                return new ByteArrayInputStream(result);
            }
            
            @Override
            public String getContentType() {
                return "image/png";
            }
        };
    }
}
