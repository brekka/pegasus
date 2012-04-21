package org.brekka.pegasus.web.servlet;

import javax.servlet.http.HttpServletRequest;

import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.web.servlet.AbstractUploadServlet;

public class UploadServlet extends AbstractUploadServlet {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 3530652696553497691L;

    /* (non-Javadoc)
     * @see org.brekka.paveway.web.servlet.AbstractUploadServlet#handleCompletedFile(org.brekka.paveway.core.model.FileBuilder, org.brekka.paveway.core.services.PavewayService)
     */
    @Override
    protected void handleCompletedFile(HttpServletRequest req, FileBuilder fileBuilder) {
        
    }
}
