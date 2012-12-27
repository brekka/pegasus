/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import java.util.List;

import org.brekka.pegasus.core.dao.ProfileDAO;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Profile;
import org.brekka.pegasus.core.model.XmlEntity;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Repository
public class ProfileHibernateDAO  extends AbstractPegasusHibernateDAO<Profile> implements ProfileDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<Profile> type() {
        return Profile.class;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.ProfileDAO#retrieveByMember(org.brekka.pegasus.core.model.Member)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Profile> retrieveByMember(Member member) {
        return getCurrentSession().createCriteria(Profile.class)
                .add(Restrictions.eq("owner", member))
                .list();
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.ProfileDAO#retrieveByXmlEntity(org.brekka.pegasus.core.model.XmlEntity)
     */
    @Override
    public Profile retrieveByXmlEntity(XmlEntity<?> xmlEntity) {
        return (Profile) getCurrentSession().createCriteria(Profile.class)
                .add(Restrictions.eq("xml", xmlEntity))
                .uniqueResult();
    }
}
