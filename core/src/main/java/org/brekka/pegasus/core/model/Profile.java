/**
 * 
 */
package org.brekka.pegasus.core.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.brekka.commons.persistence.model.LongevousEntity;
import org.brekka.xml.pegasus.v1.model.ProfileDocument;

/**
 * Contains the profile information for a user. Depending on what the user selects, this data may be stored encrypted
 * or in compressed plaintext.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`Profile`")
public class Profile extends LongevousEntity {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 7351357698414364086L;

    /**
     * The user that owns this profile.
     */
    @ManyToOne
    @JoinColumn(name="`OwnerID`", nullable=false)
    private Member owner;
    
    /**
     * The profile XML
     */
    @OneToOne()
    @JoinColumn(name="`XmlEntityID`", nullable=false)
    private XmlEntity<ProfileDocument> xml;
    

    public Member getOwner() {
        return owner;
    }

    public void setOwner(Member owner) {
        this.owner = owner;
    }

    public XmlEntity<ProfileDocument> getXml() {
        return xml;
    }

    public void setXml(XmlEntity<ProfileDocument> xml) {
        this.xml = xml;
    }
}
