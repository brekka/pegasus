/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import java.util.List;

import org.brekka.pegasus.core.dao.DigitalCertificateDAO;
import org.brekka.pegasus.core.model.CertificateSubject;
import org.brekka.pegasus.core.model.DigitalCertificate;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Repository
public class DigitalCertificateHibernateDAO extends AbstractPegasusHibernateDAO<DigitalCertificate> implements DigitalCertificateDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<DigitalCertificate> type() {
        return DigitalCertificate.class;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.DigitalCertificateDAO#retrieveBySubjectAndSignature(org.brekka.pegasus.core.model.CertificateSubject, byte[])
     */
    @Override
    public DigitalCertificate retrieveBySubjectAndSignature(CertificateSubject certificateSubject, byte[] signature) {
        return (DigitalCertificate) getCurrentSession().createCriteria(DigitalCertificate.class)
                .add(Restrictions.eq("certificateSubject", certificateSubject))
                .add(Restrictions.eq("signature", signature))
                .uniqueResult();
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.DigitalCertificateDAO#retrieveForSubject(org.brekka.pegasus.core.model.CertificateSubject)
     */
    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public List<DigitalCertificate> retrieveForSubject(CertificateSubject certificateSubject) {
        return (List<DigitalCertificate>) getCurrentSession().createCriteria(DigitalCertificate.class)
                .add(Restrictions.eq("certificateSubject", certificateSubject))
                .list();
    }
}
