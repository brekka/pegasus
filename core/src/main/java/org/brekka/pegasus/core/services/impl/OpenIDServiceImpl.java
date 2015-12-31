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

package org.brekka.pegasus.core.services.impl;

import org.brekka.pegasus.core.dao.OpenIdDAO;
import org.brekka.pegasus.core.model.OpenID;
import org.brekka.pegasus.core.services.OpenIDService;
import org.brekka.stillingar.api.annotations.Configured;
import org.brekka.xml.pegasus.v2.config.OpenIDServiceDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * OpenID Service
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Service("openIdService")
@Transactional
@Configured
public class OpenIDServiceImpl implements OpenIDService {

    @Autowired
    private OpenIdDAO openIdDAO;

    @Configured
    private OpenIDServiceDocument.OpenIDService config;

//    @Override
//    @Transactional()
//    public OpenID loadUserByUsername(final String openIdUri) throws UsernameNotFoundException {
//        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
//        OpenID openID = openIdDAO.retrieveByURI(openIdUri);
//        if (openID == null) {
//            openID = new OpenID();
//            openID.setUri(openIdUri);
//            openIdDAO.create(openID);
//        }
//
//        Person person = memberService.retrieveMember(openID, Person.class);
//        if (person != null
//                && person.getStatus() != ActorStatus.NEW) {
//            List<String> userOpenIDList = config.getAdminOpenIDList();
//            for (String adminOpenId : userOpenIDList) {
//                if (adminOpenId.equals(openIdUri)) {
//                    authorities.add(PegasusAuthority.ADMIN);
//                    break;
//                }
//            }
//            authorities.add(PegasusAuthority.USER);
//        } else {
//            person = memberService.createPerson(openID);
//            authorities.add(PegasusAuthority.MEMBER_SIGNUP);
//            authorities.add(PegasusAuthority.ANONYMOUS);
//        }
//        MemberContext authMember = new AuthenticatedPersonImpl(person, authorities);
//        return authMember;
//    }

    @Override
    @Transactional(readOnly=true)
    public OpenID retreieveByOpenID(final String openIdUri) {
        return openIdDAO.retrieveByURI(openIdUri);
    }
}
