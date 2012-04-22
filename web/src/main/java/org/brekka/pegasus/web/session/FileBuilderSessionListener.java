/**
 * 
 */
package org.brekka.pegasus.web.session;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.brekka.paveway.web.servlet.MultipartFileBuilderCache;
import org.brekka.pegasus.web.support.CompletedFileBuilders;

/**
 * @author Andrew Taylor
 *
 */
public class FileBuilderSessionListener implements HttpSessionListener {

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpSessionListener#sessionCreated(javax.servlet.http.HttpSessionEvent)
     */
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        // Not required
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpSessionListener#sessionDestroyed(javax.servlet.http.HttpSessionEvent)
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        System.out.println("Session destroyed");
        HttpSession session = se.getSession();
        // 1
        MultipartFileBuilderCache cache = MultipartFileBuilderCache.get(session);
        cache.discard();
        
        // 2
        CompletedFileBuilders builders = CompletedFileBuilders.get(session);
        builders.discard();
    }

}
