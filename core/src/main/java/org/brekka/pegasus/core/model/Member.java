/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.brekka.commons.persistence.model.IdentifiableEntity;
import org.hibernate.annotations.Type;

/**
 * A member of the site
 * 
 * @author Andrew Taylor
 */
//@Entity
//@Table(name="\"Member\"")
public class Member extends IdentifiableEntity {

    /**
     * The principal Id of this member
     */
    @Type(type="pg-uuid")
    @Column(name="PrincipalID")
    private UUID principalId;
}
