package org.brekka.pegasus.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.web.servlet.AbstractUploadServlet;
import org.brekka.pegasus.web.support.CompletedFileBuilders;

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
        CompletedFileBuilders completedFileBuilders = CompletedFileBuilders.get(req, true);
        completedFileBuilders.add(fileBuilder);
    }
}
