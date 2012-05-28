/**
 * 
 */
package org.brekka.pegasus.web.pages;

import java.io.IOException;
import java.io.InputStream;

import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Response;
import org.brekka.pegasus.core.services.DownloadService;
import org.brekka.pegasus.web.support.Transfers;
import org.brekka.xml.pegasus.v1.model.FileType;

/**
 * @author Andrew Taylor
 *
 */
public class Download {
    
    @Inject
    private DownloadService downloadService;

    @SessionAttribute("transfers")
    private Transfers transfers;
    
    Object onActivate(String uuid, String filename) {
        
        final FileType file = transfers.getFile(uuid);
        return new StreamResponse() {
            @Override
            public void prepareResponse(Response response) {
                response.setHeader("Content-Length", String.valueOf(file.getLength()));
                response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
            }
            
            @Override
            public InputStream getStream() throws IOException {
                return downloadService.download(file);
            }
            
            @Override
            public String getContentType() {
                return file.getMimeType();
            }
        };
    }
}
