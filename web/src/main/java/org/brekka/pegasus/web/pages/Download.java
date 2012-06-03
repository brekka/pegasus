/**
 * 
 */
package org.brekka.pegasus.web.pages;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Response;
import org.brekka.pegasus.core.model.BundleFile;
import org.brekka.pegasus.core.model.Transfer;
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
        UUID fileId = UUID.fromString(uuid);
        final Transfer transfer = transfers.getTransferWithFile(fileId);
        final BundleFile file = transfer.getBundle().getFiles().get(fileId);
        final FileType fileType = file.getXml();
        final MutableFloat progress = transfers.downloadStartProgress(file);
        return new StreamResponse() {
            @Override
            public void prepareResponse(Response response) {
                response.setHeader("Content-Length", String.valueOf(fileType.getLength()));
                response.setHeader("Content-Disposition", "attachment; filename=\"" + fileType.getName() + "\"");
            }
            
            @Override
            public InputStream getStream() throws IOException {
                return downloadService.download(file, transfer, new DownloadService.ProgressCallback() {
                    @Override
                    public void update(long current, long total) {
                        progress.setValue((float) current / total);
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
