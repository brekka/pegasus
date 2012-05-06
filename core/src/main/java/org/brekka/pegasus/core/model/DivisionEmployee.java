/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

/**
 * Defines the relationship between an employee and an orgainzation.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`DivisionEmployee`")
public class DivisionEmployee extends LongevousEntity {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 4972021130097729646L;

    /**
     * The division that the employee is a member of
     */
    @ManyToOne
    @JoinColumn(name="`DivisionID`")
    private Division division;
    
    /**
     * The employee
     */
    @ManyToOne
    @JoinColumn(name="`EmployeeID`")
    private Employee employee;
    
    /**
     * The key pair that gives the user access to the contents of this division.
     */
    @Column(name="KeyPairID")
    @Type(type="pg-uuid")
    private UUID keyPairId;
    
    

    public Division getDivision() {
        return division;
    }

    public void setDivision(Division division) {
        this.division = division;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public UUID getKeyPairId() {
        return keyPairId;
    }

    public void setKeyPairId(UUID keyPairId) {
        this.keyPairId = keyPairId;
    }
}
