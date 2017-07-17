/**
 *
 */
package org.brekka.pegasus.core.dao.hibernate;

import java.util.List;

import org.brekka.pegasus.core.dao.FirewallRuleDAO;
import org.brekka.pegasus.core.model.Firewall;
import org.brekka.pegasus.core.model.FirewallRule;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Repository
public class FirewallRuleHibernateDAO extends AbstractPegasusHibernateDAO<FirewallRule> implements FirewallRuleDAO {

    @Override
    protected Class<FirewallRule> type() {
        return FirewallRule.class;
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    public List<FirewallRule> findApplicableRules(final Firewall firewall, final String ipAddress) {
        SQLQuery query = getCurrentSession().createSQLQuery(
                "SELECT fr.* " +
                "  FROM \"Pegasus\".\"FirewallRule\" fr" +
                "  JOIN \"Pegasus\".\"Network\" ipn" +
                "    ON  fr.\"NetworkGroupID\" = ipn.\"NetworkGroupID\"" +
                " WHERE ipn.\"Block\" >>= cast(:ip as cidr)" +
                "   AND fr.\"FirewallID\" = cast(:firewall as uuid)" +
                " ORDER BY fr.\"Priority\" ASC");
        query.setParameter("ip", ipAddress);
        query.setString("firewall", firewall.getId().toString());
        query.addEntity(FirewallRule.class);
        return query.list();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<FirewallRule> retrieveForFirewall(final Firewall firewall) {
        return getCurrentSession().createCriteria(FirewallRule.class)
                .add(Restrictions.eq("firewall", firewall))
                .list();
    }
}
