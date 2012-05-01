/**
 * 
 */
package org.brekka.pegasus.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * A member of the site
 * 
 * @author Andrew Taylor
 */
@Entity
@Table(name = "\"Member\"")
public class Member extends LongevousEntity {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -6815079717237157048L;

    /**
     * The open ID of this member
     */
    @Column(name = "OpenID", unique = true, nullable=false)
    private String openId;
    
    @Enumerated(EnumType.STRING)
    @Column(name="Status", nullable=false)
    private MemberStatus status = MemberStatus.NEW;

    @Column(name = "Name")
    private String name;

    // TODO consider a separate table
    @Column(name = "Email")
    private String email;
    
    @OneToOne
    @JoinColumn(name="DefaultVaultID")
    private Vault defaultVault;
    
    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public void setStatus(MemberStatus status) {
        this.status = status;
    }

    public Vault getDefaultVault() {
        return defaultVault;
    }

    public void setDefaultVault(Vault defaultVault) {
        this.defaultVault = defaultVault;
    }
}
