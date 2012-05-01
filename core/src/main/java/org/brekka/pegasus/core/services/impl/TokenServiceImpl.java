/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
import org.brekka.pegasus.core.dao.TokenDAO;
import org.brekka.pegasus.core.model.Token;
import org.brekka.pegasus.core.model.TokenType;
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
     * @see org.brekka.pegasus.core.services.TokenService#allocateAnonymous()
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Token allocateAnonymous() {
        // For now just keep generating random tokens
        Token token = chooseRandomToken(TokenType.ANON);
        tokenDAO.create(token);
        return token;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.TokenService#createForInbox(java.lang.String)
     */
    @Override
    public Token createForInbox(String chosen) {
        Token token;
        if (chosen != null) {
            chosen = chosen.toUpperCase();
            if (chosen.length() != TokenType.INBOX.getGenerateLength()) {
                throw new PegasusException(PegasusErrorCode.PG300, 
                        "The inbox token '%s' must be %d characters", chosen, 
                        TokenType.INBOX.getGenerateLength());
            }
            if (tokenDAO.retrieveByPath(chosen) != null) {
                throw new PegasusException(PegasusErrorCode.PG300, 
                        "The inbox token '%s' is already taken", chosen);
            } else {
                token = new Token();
                token.setType(TokenType.INBOX);
                token.setPath(chosen);
            }
        } else {
            // Use random token, make sure it is not already in use
            token = chooseRandomToken(TokenType.INBOX);
        }
        tokenDAO.create(token);
        return token;
    }
    
    private Token chooseRandomToken(TokenType type) {
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
