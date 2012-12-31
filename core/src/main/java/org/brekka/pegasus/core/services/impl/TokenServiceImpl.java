/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
import org.brekka.pegasus.core.dao.TokenDAO;
import org.brekka.pegasus.core.model.Token;
import org.brekka.pegasus.core.model.PegasusTokenType;
import org.brekka.pegasus.core.services.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Andrew Taylor
 *
 */
@Service
@Transactional
public class TokenServiceImpl implements TokenService {

    @Autowired
    private TokenDAO tokenDAO;
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.TokenService#createToken(java.lang.String, org.apache.xmlbeans.XmlCursor.TokenType)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Token createToken(String path, PegasusTokenType type) {
        Token token;
        if (path != null) {
            if (tokenDAO.retrieveByPath(path) != null) {
                throw new PegasusException(PegasusErrorCode.PG300, 
                        "The inbox token '%s' is already taken", path);
            }
            token = new Token();
            token.setType(type);
            token.setPath(path);
        } else {
            // Use random token, make sure it is not already in use
            token = chooseRandomToken(type);
        }
        tokenDAO.create(token);
        return token;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.TokenService#allocateAnonymous()
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Token generateToken(PegasusTokenType tokenType) {
        // For now just keep generating random tokens
        Token token = chooseRandomToken(tokenType);
        tokenDAO.create(token);
        return token;
    }

    private Token chooseRandomToken(PegasusTokenType type) {
        Token token = type.generateRandom();
        while (tokenDAO.retrieveByPath(token.getPath()) != null) {
            token = type.generateRandom();
        }
        return token;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.TokenService#retrieveByPath(java.lang.String)
     */
    @Override
    public Token retrieveByPath(String path) {
        return tokenDAO.retrieveByPath(path);
    }
}
