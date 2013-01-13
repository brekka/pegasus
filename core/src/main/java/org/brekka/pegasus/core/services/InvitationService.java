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

package org.brekka.pegasus.core.services;

import java.util.List;
import java.util.UUID;

import org.brekka.pegasus.core.model.Invitation;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Token;
import org.brekka.xml.pegasus.v2.model.InvitationType;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public interface InvitationService {

    /**
     * Create a new invitation that will be assigned to the specified member.
     * 
     * @param details
     * @param recipient
     * @return
     */
    Invitation createInvitation(Token token, InvitationType details, Member recipient);
    
    /**
     * Create a new anonymous invitation.
     * 
     * @param details
     * @param password
     * @return
     */
    Invitation createInvitation(Token token, InvitationType details, String password);
    
    /**
     * Retrieve all invitations received by the specified member.
     * @param recipient
     * @return
     */
    List<Invitation> retrieveForMember(Member recipient);
    
    /**
     * Retrieve the invitation by its token
     * 
     * @param token
     * @return
     */
    Invitation retrieveByToken(Token token);
    
    /**
     * Update the invitation
     * 
     * @param invitation
     */
    void update(Invitation invitation);

    /**
     * @param invitationId
     * @return
     */
    Invitation retrieveById(UUID invitationId);
}
