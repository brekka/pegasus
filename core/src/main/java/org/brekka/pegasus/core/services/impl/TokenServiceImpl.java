/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import org.springframework.transaction.annotation.Transactional;

/**
 * Tokens act as a form of URL shortner. A unique token of customisable length is assigned a type that can be use to route a request
 * to the correct part of the system. 
 * 
 * TODO Ultimately it should be possible to re-use tokens after a cooling-off period from when they are no longer used.
 *
 * @author Andrew Taylor (andrew@brekka.org)
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
    @Transactional()
    public Token createToken(String path, TokenType type) {
        Token token;
        if (path != null) {
            if (tokenDAO.retrieveByPath(path) != null) {
                throw new PegasusException(PegasusErrorCode.PG300, 
                        "The token '%s' of type '%s' is already taken", path, type.getKey());
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
    @Transactional()
    public Token generateToken(TokenType tokenType) {
        // For now just keep generating random tokens
        Token token = chooseRandomToken(tokenType);
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
