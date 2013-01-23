/*
 * Copyright 2012 the original author or authors.
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

package org.brekka.pegasus.core.services;

import org.brekka.pegasus.core.model.UsernamePassword;

/**
 * TODO Description of UsernamePasswordService
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface UsernamePasswordService {

    UsernamePassword create(String username, String password);
    
    boolean verify(UsernamePassword usernamePassword, String password);
    
    UsernamePassword retrieveByUsername(String username);

    /**
     * @param usernamePassword
     * @param oldPassword
     * @param newPassword
     */
    void changePassword(UsernamePassword usernamePassword, String oldPassword, String newPassword);

    /**
     * @param usernamePassword
     * @param password
     */
    void changePassword(UsernamePassword usernamePassword, String password);
    
    
    /**
     * @param authenticationToken
     */
    void delete(UsernamePassword usernamePassword);

}
