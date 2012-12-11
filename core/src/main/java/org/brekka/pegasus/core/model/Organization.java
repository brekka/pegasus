/**
 * 
 */
package org.brekka.pegasus.core.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.brekka.xml.pegasus.v2.model.OrganizationDocument;

/**
 * Represents an organization such as a company, community etc. The defining characteristic of an organization is that
 * it can have employees.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@DiscriminatorValue("Org")
public class Organization extends Actor {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 1874104138893719039L; 
    
    /**
     * The token that uniquely identifies this organization to the outside world.
     */
    @OneToOne
    @JoinColumn(name="`TokenID`", unique=true)
    private Token token;
    
    /**
     * The name of this organization. Can be null
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
}
