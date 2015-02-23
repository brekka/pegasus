/**
 *
 */
package org.brekka.pegasus.core.model;

import java.util.UUID;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.brekka.commons.persistence.model.LongevousEntity;
import org.brekka.pegasus.core.PegasusConstants;
import org.brekka.xml.pegasus.v2.model.ProfileDocument;
import org.hibernate.annotations.Type;

/**
 * Contains the profile information for a user. Depending on what the user selects, this data may be stored encrypted
 * or in compressed plaintext.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`Profile`", schema=PegasusConstants.SCHEMA)
public class Profile extends LongevousEntity<UUID> {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 7351357698414364086L;

    /**
     * Unique id
     */
    @Id
    @Type(type="pg-uuid")
    @Access(AccessType.PROPERTY)
    @Column(name="`ID`")
    private UUID id;

    /**
     * The user that owns this profile.
     */
    @ManyToOne
    @JoinColumn(name="`OwnerID`", nullable=false)
    private Member owner;

    /**
     * The profile XML
     */
    @OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.REMOVE)
    @JoinColumn(name="`XmlEntityID`", nullable=false)
    private XmlEntity<ProfileDocument> xml;


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

    public Member getOwner() {
        return this.owner;
    }

    public void setOwner(final Member owner) {
        this.owner = owner;
    }

    public XmlEntity<ProfileDocument> getXml() {
        return this.xml;
    }

    public void setXml(final XmlEntity<ProfileDocument> xml) {
        this.xml = xml;
    }
}
