/**
 * 
 */
package org.brekka.pegasus.web.session;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.brekka.pegasus.core.model.Inbox;

/**
 * @author Andrew Taylor
 *
 */
public class BundleMakerContext {
    public static final String SESSION_KEY = "BUNDLE_MAKER_CONTEXT";

    private transient Map<String, BundleMaker> makers;
    
    public synchronized boolean contains(String makerKey) {
        return map().containsKey(makerKey);
    }
    
    public synchronized BundleMaker get(String makerKey) {
        return get(makerKey, null);
    }
    
    public synchronized BundleMaker get(String makerKey, Inbox inbox) {
        Map<String, BundleMaker> map = map();
        BundleMaker bundleMaker = map.get(makerKey);
        if (bundleMaker == null) {
            bundleMaker = new BundleMaker(makerKey, inbox);
            map.put(makerKey, bundleMaker);
        }
        return bundleMaker;
    }
    
    public synchronized void discard() {
        Collection<BundleMaker> values = map().values();
        for (BundleMaker bundleMaker : values) {
            bundleMaker.discard();
        }
        makers.clear();
    }
    
    private synchronized Map<String, BundleMaker> map() {
        Map<String, BundleMaker> map = this.makers;
        if (map == null) {
            map = new HashMap<>();
        }
        return (this.makers = map);
    }
    

    public static BundleMakerContext get(HttpServletRequest req, boolean create) {
        HttpSession session = req.getSession(create);
        if (session == null) {
            return null;
        }
        return get(session);
    }
    
    public static BundleMakerContext get(HttpSession session) {
        BundleMakerContext content = (BundleMakerContext) session.getAttribute(BundleMakerContext.SESSION_KEY);
        if (content == null) {
            content = new BundleMakerContext();
            session.setAttribute(SESSION_KEY, content);
        }
        return content;
    }
}
