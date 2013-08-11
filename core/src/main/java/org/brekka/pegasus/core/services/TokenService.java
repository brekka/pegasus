/**
 * 
 */
package org.brekka.pegasus.core.services;

import org.brekka.pegasus.core.model.Token;
import org.brekka.pegasus.core.model.TokenType;

/**
 * Manipulate tokens
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface TokenService {

    Token generateToken(TokenType tokenType);
    
    Token createToken(String path, TokenType type);

    /**
     * @param inboxToken
     * @return
     */
    Token retrieveByPath(String path);

}
