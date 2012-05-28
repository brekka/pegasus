/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.pegasus.core.dao.AgreementAcceptedEventDAO;
import org.brekka.pegasus.core.model.AgreementAcceptedEvent;
import org.brekka.pegasus.core.model.Transfer;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 * 
 */
@Repository
public class AgreementAcceptedEventHiberateDAO extends AbstractPegasusHibernateDAO<AgreementAcceptedEvent> implements
        AgreementAcceptedEventDAO {

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<AgreementAcceptedEvent> type() {
        return AgreementAcceptedEvent.class;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.AgreementAcceptedEventDAO#retrieveByBundle(org.brekka.pegasus.core.model.Bundle)
     */
    @Override
    public AgreementAcceptedEvent retrieveByTransfer(Transfer transfer) {
        return (AgreementAcceptedEvent) getCurrentSession().createCriteria(AgreementAcceptedEvent.class)
                .add(Restrictions.eq("transfer", transfer))
                .uniqueResult();
    }
}
