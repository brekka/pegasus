/**
 * 
 */
package org.brekka.pegasus.core.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

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
     */
    @Column(name="`OpenID`", unique = true)
    private String openId;

    @Column(name="`Name`")
    private String name;

    // TODO consider a separate table
    @Column(name="`Email`")
    private String email;
    
    

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
