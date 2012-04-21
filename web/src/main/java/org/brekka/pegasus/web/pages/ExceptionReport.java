package org.brekka.pegasus.web.pages;


import org.apache.tapestry5.services.ExceptionReporter;
import org.brekka.commons.tapestry.base.AbstractExceptionReport;

public class ExceptionReport extends AbstractExceptionReport implements ExceptionReporter {
    @Override
    protected boolean isAdministrator() {
        // TODO
        return true;
//        return SecurityUtils.hasRole(Role.ADMIN);
    }
}
