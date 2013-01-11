/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.brekka.commons.persistence.model.SnapshotEntity;
import org.brekka.pegasus.core.PegasusConstants;
import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
import org.brekka.xml.pegasus.v2.model.AllocationDocument;
import org.brekka.xml.pegasus.v2.model.AllocationType;
import org.brekka.xml.pegasus.v2.model.BundleType;
import org.brekka.xml.pegasus.v2.model.DetailsType;
import org.hibernate.annotations.Type;

/**
 * An allocation of of some sort that can include files.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`Allocation`", schema=PegasusConstants.SCHEMA)
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name="`Type`", length=10,
    discriminatorType=DiscriminatorType.STRING
)
@DiscriminatorValue("Allocation")
public abstract class Allocation extends SnapshotEntity<UUID> implements XmlEntityAware<AllocationDocument> {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -949222719619428451L;
    
    /**
     * Unique id
     */
    @Id
    @Type(type="pg-uuid")
    @Column(name="`ID`")
    private UUID id;
    
    /**
     * When is this allocation due to expire?
     */
    @Column(name="`Expires`")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expires;

    /**
     * Determines when/whether this allocation has been deleted.
     */
    @Column(name = "`Deleted`")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deleted;
    
    /**
     * The authenticated user that created this allocation (if available).
     */
    @ManyToOne
    @JoinColumn(name="`ActorID`")
    private Actor actor;
    
    /**
     * An allocation could be derived from a dispatch (in the case of a file sent by a member).
     */
    @ManyToOne
    @JoinColumn(name="`DerivedFromID`")
    private Dispatch derivedFrom;
    
    
    /**
     * If set to to true, this allocation will be purged upon successful download.
     */
    @Column(name="`PurgeOnDownload`")
    private Boolean purgeOnDownload;
    
    /**
     * Associated files
     */
    @OneToMany(mappedBy="allocation")
    private List<AllocationFile> files;
    
    /**
     * Token that identifies this allocation
     */
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="`TokenID`")
    private Token token;
    
    /**
     * The disposition of this allocation (optional).
     */
    @Column(name="`Disposition`", length=16)
    @Type(type="org.brekka.pegasus.core.support.AllocationDispositionUserType")
    private AllocationDisposition disposition;
    
    /**
     * The details and bundle for the allocation
     */
    @OneToOne()
    @JoinColumn(name="`XmlEntityID`")
    private XmlEntity<AllocationDocument> xml;
    
    
    public List<AllocationFile> getFiles() {
        return files;
    }

    public void setFiles(List<AllocationFile> files) {
        this.files = files;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    public Date getDeleted() {
        return deleted;
    }

    public void setDeleted(Date deleted) {
        this.deleted = deleted;
    }

    public Dispatch getDerivedFrom() {
        return derivedFrom;
    }

    public void setDerivedFrom(Dispatch derivedFrom) {
        this.derivedFrom = derivedFrom;
    }

    /**
     * @return the id
     */
    public final UUID getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public final void setId(UUID id) {
        this.id = id;
    }

    /**
     * @return the purgeOnDownload
     */
    public Boolean getPurgeOnDownload() {
        return purgeOnDownload;
    }

    /**
     * @param purgeOnDownload the purgeOnDownload to set
     */
    public void setPurgeOnDownload(Boolean purgeOnDownload) {
        this.purgeOnDownload = purgeOnDownload;
    }

    /**
     * @return the xml
     */
    public XmlEntity<AllocationDocument> getXml() {
        return xml;
    }

    /**
     * @param xml the xml to set
     */
    public void setXml(XmlEntity<AllocationDocument> xml) {
        this.xml = xml;
    }
    
    /**
     * Retrieve the details contained within the XML. Named without 'get' so as not to be handled as property.
     * @return
     */
    public DetailsType details() {
        return details(DetailsType.class);
    }
    
    /**
     * @return the disposition
     */
    public AllocationDisposition getDisposition() {
        return disposition;
    }

    /**
     * @param disposition the disposition to set
     */
    public void setDisposition(AllocationDisposition disposition) {
        this.disposition = disposition;
    }
    
    
    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }
    
    public Actor getActor() {
        return actor;
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    /**
     * Retrieve the details contained within the XML. Named without 'get' so as not to be handled as property.
     * @param expectedType
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends DetailsType> T details(Class<T> expectedType) {
        DetailsType details = allocationType().getDetails();
        if (details == null) {
            // perfectly acceptable to be null.
            return null;
        }
        if (expectedType.isAssignableFrom(details.getClass())) {
            return (T) details;
        }
        throw new PegasusException(PegasusErrorCode.PG853, "Expected details of type '%s', actual '%s'", 
                expectedType.getName(), details.getClass().getName());
    }
    
    /**
     * Retrieve the bundle contained within the XML. Named without 'get' so as not to be handled as property.
     * @return
     */
    public BundleType bundle() {
        return allocationType().getBundle();
    }
    
    public AllocationType allocationType() {
        AllocationDocument doc = getAllocationDocument();
        return doc.getAllocation();
    }
    
    private AllocationDocument getAllocationDocument() {
        AllocationDocument doc = xml.getBean();
        if (doc == null) {
            throw new PegasusException(PegasusErrorCode.PG817, "Allocation[%s] XML entity is locked", getId());
        }
        return doc;
    }
}
