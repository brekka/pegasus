/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.util.List;
import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.commons.persistence.model.ListingCriteria;
import org.brekka.pegasus.core.model.AllocationDisposition;
import org.brekka.pegasus.core.model.Deposit;
import org.brekka.pegasus.core.model.Inbox;
import org.brekka.pegasus.core.model.Member;
import org.joda.time.DateTime;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 * 
 */
public interface DepositDAO extends EntityDAO<UUID, Deposit> {

    /**
     * @param inbox
     * @return
     */
    List<Deposit> retrieveByInbox(Inbox inbox);

    int retrieveListingRowCount(Inbox inbox, DateTime from, DateTime until, boolean showExpired, boolean dispatchBased);

    List<Deposit> retrieveListing(Inbox inbox, DateTime from, DateTime until, boolean showExpired,
            ListingCriteria listingCriteria, boolean dispatchBased);

    /**
     * @param member
     * @param allocationDisposition
     * @return
     */
    List<Deposit> retrieveDepositsForConscript(Member member, AllocationDisposition allocationDisposition);
}
