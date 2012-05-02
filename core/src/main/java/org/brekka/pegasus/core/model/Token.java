/**
 * 
 */
package org.brekka.pegasus.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.brekka.commons.persistence.model.IdentifiableEntity;

/**
 * A token is a URL-safe string fragment used to identify a resource at a moment in time to the outside world.
 * As such tokens may be reused so long as the thing it was referencing is no longer available.
 * 
 * @author Andrew Taylor
 */
@Entity
@Table(name="`Token`")
public class Token extends IdentifiableEntity {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -2345199614401295313L;
    
    @Column(name="`Path`", unique=true)
    private String path;
    
    @Column(name="`Type`")
    private TokenType type;

    
    /**
     * 
     */
    public Token() {
    }

    public Token(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public TokenType getType() {
        return type;
    }

    public void setType(TokenType type) {
        this.type = type;
    }

    
}
