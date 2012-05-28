/**
 * 
 */
package org.brekka.pegasus.core.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

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
    
    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }
}
