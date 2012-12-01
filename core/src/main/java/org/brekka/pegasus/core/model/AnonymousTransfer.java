/**
 * 
 */
package org.brekka.pegasus.core.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/**
 * Enables an anonymous transfer
 * 
 * @author Andrew Taylor
 */
@Entity
@DiscriminatorValue("Anonymous")
public class AnonymousTransfer extends Transfer {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -4542707737980018991L;

    /**
     * Token that identifies this transfer
     */
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="`TokenID`")
    private Token token;
    
    /**
     * The creator can specify how man unlock attempts can be made.
     */
    @Column(name="MaxUnlockAttempts")
    private Integer maxUnlockAttempts;
    
    @Transient
    private transient String code;
    
    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return the maxUnlockAttempts
     */
    public Integer getMaxUnlockAttempts() {
        return maxUnlockAttempts;
    }

    /**
     * @param maxUnlockAttempts the maxUnlockAttempts to set
     */
    public void setMaxUnlockAttempts(Integer maxUnlockAttempts) {
        this.maxUnlockAttempts = maxUnlockAttempts;
    }
}
