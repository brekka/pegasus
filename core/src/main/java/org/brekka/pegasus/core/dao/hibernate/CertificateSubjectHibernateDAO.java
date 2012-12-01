/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.pegasus.core.dao.CertificateSubjectDAO;
import org.brekka.pegasus.core.model.CertificateSubject;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Repository
public class CertificateSubjectHibernateDAO extends AbstractPegasusHibernateDAO<CertificateSubject> implements CertificateSubjectDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<CertificateSubject> type() {
        return CertificateSubject.class;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.CertificateSubjectDAO#retrieveByDistinguishedNameDigest(byte[])
     */
    @Override
    public CertificateSubject retrieveByDistinguishedNameDigest(byte[] distinguishedNameDigest) {
        return (CertificateSubject) getCurrentSession().createCriteria(CertificateSubject.class)
                .add(Restrictions.eq("distinguishedNameDigest", distinguishedNameDigest))
                .uniqueResult();
    }
}
