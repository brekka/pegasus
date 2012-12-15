/**
 * 
 */
package org.brekka.pegasus.core.model;


import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SecondaryTable;
import javax.persistence.Transient;

import org.brekka.pegasus.core.PegasusConstants;

/**
 * A person represents someone who logs into the system with an OpenID account in order to carry out tasks.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@DiscriminatorValue("Person")
@SecondaryTable(name="`Person`", schema=PegasusConstants.SCHEMA)
public class Person extends Member {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 4825064100521455268L;


    /**
     * The user's default e-mail address
     */
    @OneToOne
    @JoinColumn(name="`EMailAddressID`", table="`Person`")
    private EMailAddress defaultEmailAddress;

    /**
     * The persons full name (normally kept in the profile).
     */
    @Transient
    private transient String fullName;
    

    public EMailAddress getDefaultEmailAddress() {
        return defaultEmailAddress;
    }

    public void setDefaultEmailAddress(EMailAddress defaultEmailAddress) {
        this.defaultEmailAddress = defaultEmailAddress;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
