/**
 * 
 */
package org.brekka.pegasus.core.model;


import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

/**
 * A person represents someone who logs into the system with an OpenID account in order to carry out tasks.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@DiscriminatorValue("Person")
public class Person extends Member {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 4825064100521455268L;

    /**
     * The open ID of this member
     * Can't be made non-nullable due to the inheritence.
     */
    @Column(name="`OpenID`", unique = true)
    private String openId;

    /**
     * The persons full name (normally kept in the profile).
     */
    @Transient
    private transient String fullName;

    /**
     * The user's default e-mail address
     */
    @OneToOne
    @JoinColumn(name="`EMailAddress`")
    private EMailAddress defaultEmailAddress;
    
    

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

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
