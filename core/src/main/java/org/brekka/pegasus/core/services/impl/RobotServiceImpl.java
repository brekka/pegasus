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

import java.util.UUID;

import org.brekka.pegasus.core.dao.MemberDAO;
import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.ActorStatus;
import org.brekka.pegasus.core.model.Robot;
import org.brekka.pegasus.core.model.UsernamePassword;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.core.services.RobotService;
import org.brekka.pegasus.core.services.UsernamePasswordService;
import org.brekka.pegasus.core.services.VaultService;
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
    private MemberDAO memberDAO;
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.RobotService#createRobot(java.util.UUID, java.lang.String)
     */
    @Transactional(propagation=Propagation.REQUIRED)
    @Override
    public Robot createRobot(UUID username, String password, Actor owner) {
        Robot robot = new Robot();
        
        UsernamePassword usernamePassword = usernamePasswordService.create(username.toString(), password);
        robot.setAuthenticationToken(usernamePassword);
        
        Vault vault = vaultService.createVault("Default", password, robot);
        robot.setDefaultVault(vault);
        
        robot.setStatus(ActorStatus.ACTIVE);
        robot.setOwner(owner);
        
        
        memberDAO.create(robot);
        return robot;
    }

}
