/**
 *
 */
package org.brekka.pegasus.core.model;

import java.util.UUID;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.brekka.commons.persistence.model.SnapshotEntity;
import org.brekka.pegasus.core.PegasusConstants;
import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
import org.hibernate.annotations.Type;

/**
 * A token is a URL-safe string fragment used to identify a resource at a moment in time to the outside world.
 * As such tokens may be reused so long as the thing it was referencing is no longer available.
 *
 * @author Andrew Taylor
 */
@Entity
@Table(name="`Token`", schema=PegasusConstants.SCHEMA)
public class Token extends SnapshotEntity<UUID> {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -2345199614401295313L;

    /**
     * Unique id
     */
    @Id
    @Type(type="pg-uuid")
    @Access(AccessType.PROPERTY)
    @Column(name="`ID`")
    private UUID id;

    /**
     * The token path that must consist of URL-safe characters
     */
    @Column(name="`Path`", nullable=false, unique=true, length=32)
    private String path;

    /**
     * The type of this token
     */
    @Column(name="`Type`", nullable=false, length=12)
    @Type(type="org.brekka.pegasus.core.support.TokenTypeUserType")
    private TokenType type;


    public Token() {
    }

    public Token(final String path) {
        this.path = path;
    }

    public Token(final UUID id) {
        this.id = id;
    }

    /**
     * @return the id
     */
    @Override
    public UUID getId() {
        return this.id;
    }

    /**
     * @param id the id to set
     */
    @Override
    public void setId(final UUID id) {
        this.id = id;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public TokenType getType() {
        return this.type;
    }

    @SuppressWarnings("unchecked")
    public <T extends TokenType> T getType(final Class<T> expectedType) {
        TokenType type = getType();
        if (type == null) {
            return null;
        }
        if (expectedType.isAssignableFrom(type.getClass())) {
            return (T) type;
        }
        throw new PegasusException(PegasusErrorCode.PG265,
                "Expected '%s' actual '%s'", expectedType.getName(), type.getClass().getName());
    }

    public void setType(final TokenType type) {
        this.type = type;
    }


}
