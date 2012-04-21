/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import org.apache.commons.lang3.RandomStringUtils;
import org.brekka.pegasus.core.dao.SlugDAO;
import org.brekka.pegasus.core.model.Slug;
import org.brekka.pegasus.core.model.SlugType;
import org.brekka.pegasus.core.services.SlugService;
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
public class SlugServiceImpl implements SlugService {

    @Autowired
    private SlugDAO slugDAO;
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.SlugService#allocateAnonymous()
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Slug allocateAnonymous() {
        Slug slug = new Slug();
        slug.setType(SlugType.ANON);
        slug.setPath(RandomStringUtils.randomAlphanumeric(5));
        slugDAO.create(slug);
        return slug;
    }

}
