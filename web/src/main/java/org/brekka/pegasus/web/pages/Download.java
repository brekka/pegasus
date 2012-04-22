/**
 * 
 */
package org.brekka.pegasus.web.pages;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.RequestGlobals;
import org.apache.tapestry5.services.Response;
import org.brekka.pegasus.core.services.DownloadService;
import org.brekka.pegasus.web.support.Bundles;
import org.brekka.xml.pegasus.v1.model.FileType;

/**
 * @author Andrew Taylor
 *
 */
public class Download {
    
    @Inject
    private DownloadService downloadService;

    @Inject
    private RequestGlobals requestGlobals;
    
    @SessionAttribute("bundles")
    private Bundles bundles;
    
    Object onActivate(String uuid, String filename) {
        
        HttpServletRequest req = requestGlobals.getHTTPServletRequest();
        final String userAgent = req.getHeader("User-Agent");
        final String onBehalfOfAddress = req.getHeader("X-Forwarded-For");
        final String remoteAddr = req.getRemoteAddr();
        
        final FileType file = bundles.getFile(uuid);
        return new StreamResponse() {
            @Override
            public void prepareResponse(Response response) {
                response.setHeader("Content-Length", String.valueOf(file.getLength()));
                response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
            }
            
            @Override
            public InputStream getStream() throws IOException {
                return downloadService.download(file, remoteAddr, onBehalfOfAddress, userAgent);
            }
            
            @Override
            public String getContentType() {
                return file.getMimeType();
            }
        };
    }
}
