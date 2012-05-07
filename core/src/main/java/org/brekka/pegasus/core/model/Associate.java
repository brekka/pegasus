/**
 * 
 */
package org.brekka.pegasus.core.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@DiscriminatorValue("Associate")
public class Associate extends Actor {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -7785708271201081308L;

    /**
     * The person who is an employee
     */
    @ManyToOne
    @JoinColumn(name="`MemberID`")
    private Member member;
    
    /**
     * The organization this employee belongs to
     */
    @ManyToOne
    @JoinColumn(name="`OrganizationID`")
    private Organization organization;

    
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
}
