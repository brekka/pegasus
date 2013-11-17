/**
 *
 */
package org.brekka.pegasus.core.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SecondaryTable;
import javax.persistence.UniqueConstraint;

import org.brekka.pegasus.core.PegasusConstants;

/**
 * An associate is an actor that represents the relationship between a member and an organization.
 *
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
     * The person who is an employee.
     * Eagerly fetch as the associate is fairly useless without this information
     */
    @ManyToOne()
    @JoinColumn(name="`MemberID`", table="`Associate`", nullable=false)
    private Member member;

    /**
     * The organization this employee belongs to.
     * Eagerly fetch as the associate is fairly useless without this information.
     */
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="`OrganizationID`", table="`Associate`", nullable=false)
    private Organization organization;

    /**
     * The main e-mail address used to identify this employee (at the organization).
     */
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="`PrimaryEMailAddressID`", table="`Associate`")
    private EMailAddress primaryEMailAddress;

    public Member getMember() {
        return this.member;
    }

    public void setMember(final Member member) {
        this.member = member;
    }

    public Organization getOrganization() {
        return this.organization;
    }

    public void setOrganization(final Organization organization) {
        this.organization = organization;
    }

    public EMailAddress getPrimaryEMailAddress() {
        return this.primaryEMailAddress;
    }

    public void setPrimaryEMailAddress(final EMailAddress primaryEMailAddress) {
        this.primaryEMailAddress = primaryEMailAddress;
    }
}
