/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.pegasus.core.dao.TokenDAO;
import org.brekka.pegasus.core.model.Token;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Repository
public class TokenHibernateDAO extends AbstractPegasusHibernateDAO<Token> implements TokenDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<Token> type() {
        return Token.class;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.TokenDAO#retrieveByPath(java.lang.String)
     */
    @Override
    public Token retrieveByPath(String path) {
        Criteria criteria = getCurrentSession().createCriteria(Token.class);
        criteria.add(Restrictions.eq("path", path));
        return (Token) criteria.uniqueResult();
    }
}
