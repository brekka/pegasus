/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import java.util.List;

import org.brekka.pegasus.core.dao.FileDownloadEventDAO;
import org.brekka.pegasus.core.model.Allocation;
import org.brekka.pegasus.core.model.AllocationFile;
import org.brekka.pegasus.core.model.FileDownloadEvent;
import org.brekka.pegasus.core.model.Transfer;
import org.hibernate.Query;
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
     * @see org.brekka.pegasus.core.dao.FileDownloadEventDAO#fileDownloadCount(org.brekka.pegasus.core.model.AllocationFile, org.brekka.pegasus.core.model.Transfer)
     */
    @Override
    public int fileDownloadCount(AllocationFile bundleFile, Transfer transfer) {
        return ((Number) getCurrentSession().createQuery(
                "select count(id) " +
                "  from FileDownloadEvent" +
                " where bundleFile=:file" +
                "   and transfer=:transfer")
                .setEntity("file", bundleFile)
                .setEntity("transfer", transfer)
                .uniqueResult()).intValue();
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.FileDownloadEventDAO#retrieveFileDownloads(org.brekka.pegasus.core.model.Allocation)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<FileDownloadEvent> retrieveFileDownloads(Allocation allocation) {
        Query query = getCurrentSession().createQuery(
                "select fde from FileDownloadEvent fde" +
                "  join fde.transferFile as tf" +
                " where tf.allocation=:allocation");
        query.setEntity("allocation", allocation);
        return query.list();
    }
}
