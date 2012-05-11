/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import org.brekka.pegasus.core.dao.DivisionDAO;
import org.brekka.pegasus.core.dao.OrganizationDAO;
import org.brekka.pegasus.core.services.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Service
@Transactional
public class OrganizationServiceImpl implements OrganizationService {

    @Autowired
    private DivisionDAO divisionDAO;
    
    @Autowired
    private OrganizationDAO organizationDAO;
    
    
}
