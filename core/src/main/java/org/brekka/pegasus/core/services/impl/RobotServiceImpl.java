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

import java.util.List;

import org.brekka.commons.persistence.model.ListingCriteria;
import org.brekka.pegasus.core.dao.MemberDAO;
import org.brekka.pegasus.core.dao.RobotDAO;
import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.ActorStatus;
import org.brekka.pegasus.core.model.Associate;
import org.brekka.pegasus.core.model.AuthenticatedMember;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.model.Person;
import org.brekka.pegasus.core.model.Robot;
import org.brekka.pegasus.core.model.UsernamePassword;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.model.XmlEntity;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.core.services.RobotService;
import org.brekka.pegasus.core.services.UsernamePasswordService;
import org.brekka.pegasus.core.services.VaultService;
import org.brekka.pegasus.core.services.XmlEntityService;
import org.brekka.xml.pegasus.v2.model.RobotDocument;
import org.brekka.xml.pegasus.v2.model.RobotType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Robot Service 
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Transactional
@Service
public class RobotServiceImpl implements RobotService {

    @Autowired
    private UsernamePasswordService usernamePasswordService; 
    
    @Autowired
    private VaultService vaultService;
    
    @Autowired
    private MemberService memberService;
    
    @Autowired
    private RobotDAO robotDAO;
    
    @Autowired
    private XmlEntityService xmlEntityService;
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.RobotService#createRobot(java.util.UUID, java.lang.String)
     */
    @Transactional(propagation=Propagation.REQUIRED)
    @Override
    public Robot create(String key, String code, Actor owner, RobotType details) {
        KeySafe<?> detailsProtectedBy;
        
        if (owner instanceof Organization) {
            Organization organizationOwner = (Organization) owner;
            detailsProtectedBy = organizationOwner.getGlobalDivision();
        } else if (owner instanceof Associate) {
            Associate associateOwner = (Associate) owner;
            detailsProtectedBy = associateOwner.getOrganization().getGlobalDivision();
        } else if (owner instanceof Person) {
            Person personOwner = (Person) owner;
            detailsProtectedBy = personOwner.getDefaultVault();
        } else {
            throw new IllegalStateException(String.format(
                    "Only Organization, Associate or Person based actors can create robots, not '%s'", 
                    owner.getClass().getName()));
        }
        
        Robot robot = new Robot();
        
        UsernamePassword usernamePassword = usernamePasswordService.create(key, code);
        robot.setAuthenticationToken(usernamePassword);
        
        Vault vault = vaultService.createVault("Default", code, robot);
        robot.setDefaultVault(vault);
        
        robot.setStatus(ActorStatus.ACTIVE);
        robot.setOwner(owner);
        
        RobotDocument robotDocument = RobotDocument.Factory.newInstance();
        robotDocument.setRobot(details);
        
        // Robot cannot see its own details, why would it need to?
        XmlEntity<RobotDocument> encryptedEntity = xmlEntityService.persistEncryptedEntity(robotDocument, detailsProtectedBy, false);
        robot.setXml(encryptedEntity);
        
        AuthenticatedMember<Person> current = memberService.getCurrent(Person.class);
        robot.setCreatedBy(current.getMember());
        
        robotDAO.create(robot);
        return robot;
    }
    
    @Transactional(propagation=Propagation.REQUIRED)
    @Override
    public int retrieveListingRowCount(Actor owner) {
        return robotDAO.retrieveListingRowCount(owner);
    }
    
    @Transactional(propagation=Propagation.REQUIRED)
    @Override
    public List<Robot> retrieveListing(Actor owner, ListingCriteria listingCriteria) {
        return robotDAO.retrieveListing(owner, listingCriteria);
    }
    
    @Transactional(propagation=Propagation.REQUIRED)
    @Override
    public void delete(Robot robot) {
        Robot managed = robotDAO.retrieveById(robot.getId());
        vaultService.deleteVault(managed.getDefaultVault());
        usernamePasswordService.delete(managed.getAuthenticationToken());
        xmlEntityService.delete(managed.getXml().getId());
        robotDAO.delete(managed.getId());
    }
}
