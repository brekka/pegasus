/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.brekka.phalanx.api.model.PrivateKeyToken;
import org.brekka.xml.pegasus.v2.model.DivisionDocument;
import org.hibernate.annotations.Type;

/**
 * A partition within an organization which has its own encryption key pair
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@DiscriminatorValue("Division")
public class Division extends KeySafe {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -3361259068549984723L;
    
    /**
     * The parent of this division. The parent can open the resources of this 
     * division via the key pair.
     */
    @ManyToOne
    @JoinColumn(name="`ParentDivisionID`")
    private Division parent;
    
    /**
     * The organization that this division belongs to
     */
    @ManyToOne
    @JoinColumn(name="`OrganizationID`")
    private Organization organization;

    /**
     * Additional division details that can be encrypted (ie only associates with access can view/edit the details).
     */
    @OneToOne()
    @JoinColumn(name="`XmlEntityID`")
    private XmlEntity<DivisionDocument> xml;
    
    /**
     * The key pair of this division.
     */
    @Column(name="`KeyPairID`")
    @Type(type="pg-uuid")
    private UUID keyPairId;
    
    /**
     * The private key token which becomes available when the key pair above is unlocked.
     */
    @Transient
    private transient PrivateKeyToken privateKeyToken;


    public Division getParent() {
        return parent;
    }

    public void setParent(Division parent) {
        this.parent = parent;
    }

    public UUID getKeyPairId() {
        return keyPairId;
    }

    public void setKeyPairId(UUID keyPairId) {
        this.keyPairId = keyPairId;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public XmlEntity<DivisionDocument> getXml() {
        return xml;
    }

    public void setXml(XmlEntity<DivisionDocument> xml) {
        this.xml = xml;
    }

    public PrivateKeyToken getPrivateKeyToken() {
        return privateKeyToken;
    }

    public void setPrivateKeyToken(PrivateKeyToken privateKeyToken) {
        this.privateKeyToken = privateKeyToken;
    }
}
