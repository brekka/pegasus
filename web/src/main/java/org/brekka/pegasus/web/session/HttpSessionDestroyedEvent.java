package org.brekka.pegasus.web.session;

import javax.servlet.http.HttpSession;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.session.SessionDestroyedEvent;

import java.util.*;

/**
 * Copied from Spring security to fix SEC-1870. Remove once Spring Security 3.1.1 becomes available. 
 * 
 * Published by the {@link HttpSessionEventPublisher} when a HttpSession is created in the container
 *
 * @author Ray Krueger
 * @author Luke Taylor
 */
public class HttpSessionDestroyedEvent extends SessionDestroyedEvent {
    //~ Constructors ===================================================================================================

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -8195491404594222910L;

    public HttpSessionDestroyedEvent(HttpSession session) {
        super(session);
    }

    public HttpSession getSession() {
        return (HttpSession) getSource();
    }

    @Override
    public List<SecurityContext> getSecurityContexts() {
        HttpSession session = (HttpSession)getSource();

        Enumeration<String> attributes = session.getAttributeNames();

        ArrayList<SecurityContext> contexts = new ArrayList<SecurityContext>();

        while(attributes.hasMoreElements()) {
            String attribute = attributes.nextElement();
            Object value = session.getAttribute(attribute);
            if (value instanceof SecurityContext) {
                contexts.add((SecurityContext) value);
            }
        }

        return contexts;
    }

    @Override
    public String getId() {
        return getSession().getId();
    }
}