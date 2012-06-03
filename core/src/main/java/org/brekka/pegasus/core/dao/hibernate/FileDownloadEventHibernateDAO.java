/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.pegasus.core.dao.FileDownloadEventDAO;
import org.brekka.pegasus.core.model.BundleFile;
import org.brekka.pegasus.core.model.FileDownloadEvent;
import org.brekka.pegasus.core.model.Transfer;
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

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.FileDownloadEventDAO#fileDownloadCount(org.brekka.pegasus.core.model.BundleFile, org.brekka.pegasus.core.model.Transfer)
     */
    @Override
    public int fileDownloadCount(BundleFile bundleFile, Transfer transfer) {
        return ((Number) getCurrentSession().createQuery(
                "select count(id) " +
                "  from FileDownloadEvent" +
                " where bundleFile=:file" +
                "   and transfer=:transfer")
                .setEntity("file", bundleFile)
                .setEntity("transfer", transfer)
                .uniqueResult()).intValue();
    }
}
