/**
 * 
 */
package org.brekka.pegasus.web.pages;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Response;
import org.brekka.pegasus.core.model.AllocationFile;
import org.brekka.pegasus.core.services.AllocationService;
import org.brekka.pegasus.core.services.DownloadService;
import org.brekka.xml.pegasus.v2.model.FileType;

/**
 * @author Andrew Taylor
 *
 */
public class Download {
    
    @Inject
    private AllocationService allocationService;
    
    @Inject
    private DownloadService downloadService;

    Object onActivate(String uuid, String filename) {
        UUID fileId = UUID.fromString(uuid);
        final AllocationFile file = allocationService.retrieveFile(fileId);
        final FileType fileType = file.getXml();
        if (fileType == null) {
            // TDODO handle this better
            throw new IllegalStateException("File data not available");
        }
        return new StreamResponse() {
            @Override
            public void prepareResponse(Response response) {
                response.setHeader("Content-Length", String.valueOf(fileType.getLength()));
                response.setHeader("Content-Disposition", "attachment; filename=\"" + fileType.getName() + "\"");
            }
            
            @Override
            public InputStream getStream() throws IOException {
                return downloadService.download(file, new DownloadService.ProgressCallback() {
                    @Override
                    public void update(long current, long total) {
                        file.setProgress((float) current / total);
                    }
                });
            }
            
            @Override
            public String getContentType() {
                return fileType.getMimeType();
            }
        };
    }
}
