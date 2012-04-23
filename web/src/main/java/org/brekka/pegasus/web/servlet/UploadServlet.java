package org.brekka.pegasus.web.servlet;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.web.servlet.AbstractUploadServlet;
import org.brekka.pegasus.web.session.BundleMaker;
import org.brekka.pegasus.web.session.BundleMakerContext;

public class UploadServlet extends AbstractUploadServlet {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 3530652696553497691L;

    
    /* (non-Javadoc)
     * @see org.brekka.paveway.web.servlet.AbstractUploadServlet#completeFileBuilder(javax.servlet.http.HttpServletRequest, org.brekka.paveway.core.model.FileBuilder)
     */
    @Override
    protected void completeFileBuilder(HttpServletRequest req, FileBuilder fileBuilder) {
        BundleMaker maker = getMaker(req);
        maker.makeComplete(fileBuilder);
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.web.servlet.AbstractUploadServlet#retainFileBuilder(javax.servlet.http.HttpServletRequest, java.lang.String, org.brekka.paveway.core.model.FileBuilder)
     */
    @Override
    protected void retainFileBuilder(HttpServletRequest req, String fileName, FileBuilder fileBuilder) {
        BundleMaker maker = getMaker(req);
        maker.retainInProgress(fileName, fileBuilder);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.paveway.web.servlet.AbstractUploadServlet#retrieveFileBuilder(javax.servlet.http.HttpServletRequest, java.lang.String)
     */
    @Override
    protected FileBuilder retrieveFileBuilder(HttpServletRequest req, String fileName) {
        BundleMaker maker = getMaker(req);
        return maker.retrieveInProgress(fileName);
    }
    
    
    /**
     * @param req
     * @return
     */
    private BundleMaker getMaker(HttpServletRequest req) {
        String contextPath = req.getContextPath();
        String requestURI = req.getRequestURI();
        requestURI = requestURI.substring(contextPath.length());
        String makerKey = StringUtils.substringAfterLast(requestURI, "/");
        BundleMakerContext bundleMakerContext = BundleMakerContext.get(req, true);
        return bundleMakerContext.get(makerKey);
    }
}
