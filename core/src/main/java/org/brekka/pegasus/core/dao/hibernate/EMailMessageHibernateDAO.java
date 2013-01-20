/*
 * Copyright 2013 the original author or authors.
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

package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.pegasus.core.dao.EMailMessageDAO;
import org.brekka.pegasus.core.model.EMailMessage;
import org.springframework.stereotype.Repository;

/**
 * EMail Message HibernateDAO
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Repository
public class EMailMessageHibernateDAO extends AbstractPegasusHibernateDAO<EMailMessage> implements EMailMessageDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.hibernate.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<EMailMessage> type() {
        return EMailMessage.class;
    }

}
