/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import org.apache.commons.lang3.RandomStringUtils;
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

    private static final char[] ALLOWED = 
            "BCDFGHJKLMNPQRSTVWXYZ1234567890".toCharArray();
    
    @Autowired
    private TokenDAO tokenDAO;
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.TokenService#allocateAnonymous()
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Token allocateAnonymous() {
        Token token = new Token();
        token.setType(TokenType.ANON);
        token.setPath(RandomStringUtils.random(5, 0, ALLOWED.length, false, false, ALLOWED));
        tokenDAO.create(token);
        return token;
    }

}
