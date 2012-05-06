/**
 * 
 */
package org.brekka.pegasus.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Represents an organization such as a company, community etc. The defining characteristic of an organization is that
 * it can have employees.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`Organization`")
public class Organization extends LongevousEntity {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 1874104138893719039L; 

    @Column(name="`Name`")
    private String name;
    
    /**
     * The root division, every organization must have one.
     */
    @OneToOne
    @JoinColumn(name="`DivisionID`", nullable=false, updatable=false)
    private Division division;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Division getDivision() {
        return division;
    }

    public void setDivision(Division division) {
        this.division = division;
    }
    
    
}
