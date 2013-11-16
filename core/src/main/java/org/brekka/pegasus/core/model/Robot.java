/**
 * 
 */
package org.brekka.pegasus.core.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SecondaryTable;

import org.brekka.pegasus.core.PegasusConstants;
import org.brekka.xml.pegasus.v2.model.RobotDocument;

/**
 * An autonomous member of the system that can act on another users behalf. 
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@DiscriminatorValue("Robot")
@SecondaryTable(name="`Robot`", schema=PegasusConstants.SCHEMA)
public class Robot extends Member {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 1656293508329918826L;
    
    /**
     * The {@link Organization}, {@link Associate} or {@link Person} that owns this robot.
     */
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="`OwnerID`", table="`Robot`", updatable=false, nullable=false)
    private Actor owner;
    
    /**
     * The person that created this robot.
     */
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="`CreatedByID`", table="`Robot`", updatable=false, nullable=false)
    private Person createdBy;
    
    /**
     * Additional organization details that can be encrypted (ie only employees can view/edit the details).
     */
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="`XmlEntityID`", table="`Robot`")
    private XmlEntity<RobotDocument> xml;

    /**
     * @return the owner
     */
    public Actor getOwner() {
        return owner;
    }

    /**
     * @param owner the owner to set
     */
    public void setOwner(Actor owner) {
        this.owner = owner;
    }

    /**
     * @return the xml
     */
    public XmlEntity<RobotDocument> getXml() {
        return xml;
    }

    /**
     * @param xml the xml to set
     */
    public void setXml(XmlEntity<RobotDocument> xml) {
        this.xml = xml;
    }

    /**
     * @return the createdBy
     */
    public Person getCreatedBy() {
        return createdBy;
    }

    /**
     * @param createdBy the createdBy to set
     */
    public void setCreatedBy(Person createdBy) {
        this.createdBy = createdBy;
    }
}
