/**
 * 
 */
package org.brekka.pegasus.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.brekka.commons.persistence.model.IdentifiableEntity;

/**
 * @author Andrew Taylor
 *
 */
@Entity
@Table(name="\"Slug\"")
public class Slug extends IdentifiableEntity {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -2345199614401295313L;
    
    @Column(name="Path", unique=true)
    private String path;
    
    @Column(name="\"Type\"")
    private SlugType type;


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public SlugType getType() {
        return type;
    }

    public void setType(SlugType type) {
        this.type = type;
    }

    
}
