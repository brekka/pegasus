/**
 * 
 */
package org.brekka.pegasus.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import org.brekka.commons.persistence.model.SnapshotEntity;
import org.brekka.pegasus.core.PegasusConstants;

/**
 * A token is a URL-safe string fragment used to identify a resource at a moment in time to the outside world.
 * As such tokens may be reused so long as the thing it was referencing is no longer available.
 * 
 * @author Andrew Taylor
 */
@Entity
@Table(name="`Token`", schema=PegasusConstants.SCHEMA)
public class Token extends SnapshotEntity {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -2345199614401295313L;
    
    /**
     * The token path that must consist of URL-safe characters
     */
    @Column(name="`Path`", nullable=false, unique=true, length=32)
    private String path;
    
    /**
     * The type of this token
     */
    @Column(name="`Type`", nullable=false, length=8)
    @Enumerated(EnumType.STRING)
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
