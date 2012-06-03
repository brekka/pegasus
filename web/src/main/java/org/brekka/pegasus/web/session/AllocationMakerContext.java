/**
 * 
 */
package org.brekka.pegasus.web.session;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.brekka.paveway.core.model.UploadPolicy;
import org.brekka.pegasus.core.model.Inbox;
import org.brekka.pegasus.web.support.PolicyHelper;

/**
 * @author Andrew Taylor
 *
 */
public class AllocationMakerContext {
    public static final String SESSION_KEY = "ALLOCATION_MAKER_CONTEXT";

    private transient Map<String, AllocationMaker> makers;
    
    private final UploadPolicy policy;
    
    private AllocationMakerContext(UploadPolicy policy) {
        this.policy = policy;
    }

    public synchronized boolean contains(String makerKey) {
        return map().containsKey(makerKey);
    }
    
    public synchronized AllocationMaker get(String makerKey) {
        return get(makerKey, null);
    }
    
    public synchronized AllocationMaker get(String makerKey, Inbox inbox) {
        Map<String, AllocationMaker> map = map();
        AllocationMaker bundleMaker = map.get(makerKey);
        if (bundleMaker == null) {
            bundleMaker = new AllocationMaker(makerKey, policy, inbox);
            map.put(makerKey, bundleMaker);
        }
        return bundleMaker;
    }
    
    public synchronized void discard() {
        Collection<AllocationMaker> values = map().values();
        for (AllocationMaker bundleMaker : values) {
            bundleMaker.discard();
        }
        makers.clear();
    }
    
    private synchronized Map<String, AllocationMaker> map() {
        Map<String, AllocationMaker> map = this.makers;
        if (map == null) {
            map = new HashMap<>();
        }
        return (this.makers = map);
    }
    

    public static AllocationMakerContext get(HttpServletRequest req, boolean create) {
        HttpSession session = req.getSession(create);
        if (session == null) {
            return null;
        }
        return get(session);
    }
    
    public static AllocationMakerContext get(HttpSession session) {
        AllocationMakerContext content = (AllocationMakerContext) session.getAttribute(AllocationMakerContext.SESSION_KEY);
        UploadPolicy policy = PolicyHelper.identifyPolicy(session.getServletContext());
        if (content == null) {
            content = new AllocationMakerContext(policy);
            session.setAttribute(SESSION_KEY, content);
        }
        return content;
    }
}
