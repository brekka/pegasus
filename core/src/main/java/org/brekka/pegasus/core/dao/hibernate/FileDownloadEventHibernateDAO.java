/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.pegasus.core.dao.FileDownloadEventDAO;
import org.brekka.pegasus.core.model.FileDownloadEvent;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Repository
public class FileDownloadEventHibernateDAO extends AbstractPegasusHibernateDAO<FileDownloadEvent> implements
        FileDownloadEventDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<FileDownloadEvent> type() {
        return FileDownloadEvent.class;
    }

}
