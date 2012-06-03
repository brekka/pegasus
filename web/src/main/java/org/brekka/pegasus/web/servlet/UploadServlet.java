package org.brekka.pegasus.web.servlet;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.brekka.paveway.core.model.FilesContext;
import org.brekka.paveway.web.servlet.AbstractUploadServlet;
import org.brekka.pegasus.web.session.AllocationMakerContext;

public class UploadServlet extends AbstractUploadServlet {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 3530652696553497691L;

    
    /* (non-Javadoc)
     * @see org.brekka.paveway.web.servlet.AbstractUploadServlet#getFilesContext(javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected FilesContext getFilesContext(HttpServletRequest req) {
        String contextPath = req.getContextPath();
        String requestURI = req.getRequestURI();
        requestURI = requestURI.substring(contextPath.length());
        String makerKey = StringUtils.substringAfterLast(requestURI, "/");
        AllocationMakerContext bundleMakerContext = AllocationMakerContext.get(req, true);
        return bundleMakerContext.get(makerKey);
    }
}
