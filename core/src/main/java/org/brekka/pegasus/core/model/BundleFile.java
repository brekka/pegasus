/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.brekka.commons.persistence.model.IdentifiableEntity;
import org.brekka.paveway.core.model.CryptedFile;

/**
 * Stores the relationship between a bundle and {@link CryptedFile}. The id will match that
 * of a corresponding CryptedFile entry (from Paveway).
 * 
 * While this information is available from the bundle XML, that will unavailable to the de-allocation
 * logic (when the bundle files are deleted).
 * 
 * When the crypted parts are deleted, so will the corresponding entry in this table.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`BundleFile`")
public class BundleFile extends IdentifiableEntity {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -5007642578887170101L;
    
    /**
     * 
     */
    public BundleFile() {
    }
    
    
    
    public BundleFile(UUID id, Bundle bundle) {
        super(id);
        this.bundle = bundle;
    }



    @ManyToOne
    @JoinColumn(name="`BundleID`")
    private Bundle bundle;

    
    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }
}
