/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.brekka.commons.persistence.model.LongevousEntity;
import org.brekka.pegasus.core.PegasusConstants;
import org.brekka.xml.pegasus.v2.model.OrganizationDocument;
import org.hibernate.annotations.Type;

/**
 * Represents an organization such as a company, community etc. The defining characteristic of an organization is that
 * it can have employees.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`Organization`", schema=PegasusConstants.SCHEMA)
public class Organization extends LongevousEntity<UUID> {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 1874104138893719039L; 

    /**
     * Unique id
     */
    @Id
    @Type(type="pg-uuid")
    @Column(name="`ID`")
    private UUID id;
    
    /**
     * The token that uniquely identifies this organization to the outside world.
     */
    @OneToOne
    @JoinColumn(name="`TokenID`", nullable = false, unique=true)
    private Token token;
    
    /**
     * The name of this organization.
     */
    @Column(name="`Name`")
    private String name;
    
    /**
     * The primary domain name associated with this organization.
     */
    @OneToOne
    @JoinColumn(name="`PrimaryDomainNameID`")
    private DomainName primaryDomainName;
    
    /**
     * Additional organization details that can be encrypted (ie only employees can view/edit the details).
     */
    @OneToOne()
    @JoinColumn(name="`XmlEntityID`")
    private XmlEntity<OrganizationDocument> xml;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public DomainName getPrimaryDomainName() {
        return primaryDomainName;
    }

    public void setPrimaryDomainName(DomainName primaryDomainName) {
        this.primaryDomainName = primaryDomainName;
    }

    public XmlEntity<OrganizationDocument> getXml() {
        return xml;
    }

    public void setXml(XmlEntity<OrganizationDocument> xml) {
        this.xml = xml;
    }

    /**
     * @return the id
     */
    public UUID getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(UUID id) {
        this.id = id;
    }
}
