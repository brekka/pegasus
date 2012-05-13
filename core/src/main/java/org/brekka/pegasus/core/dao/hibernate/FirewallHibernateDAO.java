/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.pegasus.core.dao.FirewallDAO;
import org.brekka.pegasus.core.model.Firewall;
import org.hibernate.SQLQuery;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Repository
public class FirewallHibernateDAO extends AbstractPegasusHibernateDAO<Firewall> implements FirewallDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<Firewall> type() {
        return Firewall.class;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.FirewallDAO#isAccessAllowed(java.lang.String, org.brekka.pegasus.core.model.Firewall)
     */
    @Override
    public boolean isAccessAllowed(String ipAddress, Firewall firewall) {
        SQLQuery query = getCurrentSession().createSQLQuery(
                "SELECT count(ipn.ID) " +
                "  FROM `FirewallRule` fr" +
                "  JOIN `Network` ipn" +
                "    ON  fr.`NetworkGroupID` = ipn.`NetworkGroupID`" +
                " WHERE ipn.`Network` >> :ip" +
                "   AND fr.`FirewallID` = :firewall");
        query.setString("ip", ipAddress);
        query.setString("firewall", firewall.getId().toString());
        Number count = (Number) query.uniqueResult();
        return count.intValue() != 0;
    }

}
