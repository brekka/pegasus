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

package org.brekka.pegasus.core.security;

import org.apache.commons.lang3.StringUtils;
import org.brekka.pegasus.core.services.AnonymousTransferService;
import org.brekka.phalanx.api.PhalanxErrorCode;
import org.brekka.phalanx.api.PhalanxException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Provider for unlocking anonymous transfers
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class UnlockAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {
    @Autowired
    private AnonymousTransferService anonymousService;
    
    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
            UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        // None required
    }

    @Override
    protected UserDetails retrieveUser(String token, UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {
        Object credentials = authentication.getCredentials();
        String password = credentials.toString();
        if (StringUtils.isBlank(password)) {
            throw new BadCredentialsException("A code is required");
        }
        
        AnonymousTransferUser anonymousTransferUser = new AnonymousTransferUser(token);
        SecurityContext context = SecurityContextHolder.getContext();
        
        // Temporarily bind the authentication user to the security context so that we can do the unlock
        // this is primarily for the EventService to capture the IP/remote user.
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(anonymousTransferUser, anonymousTransferUser);
        auth.setDetails(authentication.getDetails());
        try {
            context.setAuthentication(auth);
            anonymousService.unlock(token, password);
            context.setAuthentication(null);
            return anonymousTransferUser;
        } catch (PhalanxException e) {
            if (e.getErrorCode() == PhalanxErrorCode.CP302) {
                throw new BadCredentialsException("Code appears to be incorrect");
            }
            throw e;
        }
    }
}
