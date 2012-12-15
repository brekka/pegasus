/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SecondaryTable;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.brekka.pegasus.core.PegasusConstants;
import org.brekka.phalanx.api.model.PrivateKeyToken;
import org.hibernate.annotations.Type;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@DiscriminatorValue("Associate")
@SecondaryTable(name="`Associate`", schema=PegasusConstants.SCHEMA, uniqueConstraints={ 
    // Associate unique key
    @UniqueConstraint(columnNames = {"`MemberID`", "`OrganizationID`" }),
})
public class Associate extends Actor {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -7785708271201081308L;

    /**
     * The person who is an employee
     */
    @ManyToOne
    @JoinColumn(name="`MemberID`", table="`Associate`", nullable=false)
    private Member member;
    
    /**
     * The organization this employee belongs to
     */
    @ManyToOne
    @JoinColumn(name="`OrganizationID`", table="`Associate`", nullable=false)
    private Organization organization;
    
    /**
     * The main e-mail address used to identify this employee (at the organization).
     */
    @OneToOne
    @JoinColumn(name="`PrimaryEMailAddressID`", table="`Associate`")
    private EMailAddress primaryEMailAddress;
    
    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public EMailAddress getPrimaryEMailAddress() {
        return primaryEMailAddress;
    }

    public void setPrimaryEMailAddress(EMailAddress primaryEMailAddress) {
        this.primaryEMailAddress = primaryEMailAddress;
    }
}
