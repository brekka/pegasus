/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 
 * TODO add way to expire cached elements.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public class AccessorContext implements Serializable {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 1769337117320776327L;
    
    private transient Map<Serializable, Object> map;
    
    public synchronized void retain(Serializable key, Object value) {
        map().put(key, value);
    }
    
    @SuppressWarnings("unchecked")
    public synchronized <V> V retrieve(Serializable key, Class<V> expectedType) {
        Object object = map().get(key);
        if (object == null) {
            return null;
        }
        if (expectedType.isAssignableFrom(object.getClass()) == false) {
            throw new PegasusException(PegasusErrorCode.PG500, 
                    "Expected value of type '%s', actual '%s'",
                    expectedType.getName(), object.getClass().getName());
        }
        return (V) object;
    }
    
    public synchronized void remove(Serializable key) {
        map().remove(key);
    }
    
    private synchronized Map<Serializable, Object> map() {
        if (map == null) {
            map = new HashMap<Serializable, Object>();
        }
        return map;
    }
    
    public static AccessorContext getCurrent() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        if (authentication instanceof Accessor) {
            return ((Accessor) authentication).getContext();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Accessor) {
            return ((Accessor) principal).getContext();
        }
        // TODO better handling
        throw new IllegalStateException("No context available");
    }
}
