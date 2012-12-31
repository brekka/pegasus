/**
 * 
 */
package org.brekka.pegasus.core.services;

import org.brekka.pegasus.core.model.Token;
import org.brekka.pegasus.core.model.PegasusTokenType;

/**
 * Manipulate tokens
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface TokenService {

    Token generateToken(PegasusTokenType tokenType);
    
    Token createToken(String path, PegasusTokenType type);

    /**
     * @param inboxToken
     * @return
     */
    Token retrieveByPath(String path);

}
