package org.brekka.pegasus.web.services.impl;


import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.services.PageResponseRenderer;
import org.apache.tapestry5.internal.services.RequestPageCache;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.runtime.ComponentEventException;
import org.apache.tapestry5.services.RequestGlobals;
import org.apache.tapestry5.services.Response;
import org.brekka.commons.tapestry.services.BaseExceptionHandler;
import org.slf4j.Logger;

public class PegasusExceptionHandler extends BaseExceptionHandler {
    private static final String ACCESS_DENIED_PAGENAME = "AccessDenied";
    private static final String NOT_FOUND_PAGENAME = "NotFound";

    /**
     * @param pageCache
     * @param renderer
     * @param logger
     * @param pageName
     * @param requestGlobals
     * @param response
     */
    public PegasusExceptionHandler(RequestPageCache pageCache, PageResponseRenderer renderer, Logger logger,
            @Inject @Symbol(SymbolConstants.EXCEPTION_REPORT_PAGE) String pageName, RequestGlobals requestGlobals, Response response) {
        super(pageCache, renderer, logger, pageName, requestGlobals, response);
    }
    
    @Override
    protected ErrorResponseType determineResponse(String requestPath, Throwable incomingException) {
        Throwable exception = incomingException;
        ErrorResponseType errorResponseType = null;
        if (exception instanceof ComponentEventException) {
            exception = exception.getCause();
        }
        if (errorResponseType == null) {
            // Give the default implementation a chance to inspect the error.
            errorResponseType = super.determineResponse(requestPath, exception);
        }
        return errorResponseType;
    }
    
    @Override
    protected Page prepareNotFoundPage(Throwable exception, RequestPageCache pageCache) {
        return pageCache.get(NOT_FOUND_PAGENAME);
    }
    
    @Override
    protected Page prepareAccessDeniedPage(Throwable exception, RequestPageCache pageCache) {
        return pageCache.get(ACCESS_DENIED_PAGENAME);
    }

    @Override
    protected String getVisitorIdentity() {
        String visitor = "Guest";
        // TODO
        return visitor;
    }
}
