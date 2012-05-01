package org.brekka.pegasus.core.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Recreates the table structure defined by the Spring Security remember-me functionality.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 * @see http://static.springsource.org/spring-security/site/docs/3.0.x/reference/remember-me.html
 */
@Entity
@Table(name="persistent_logins")
public class PersistentLogin implements Serializable {
	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 374697702088091906L;

	@Id
	@Column(length=64)
	private String series;
	
	@Column(nullable=false, length=255)
	private String username;
	
	@Column(nullable=false, length=64)
	private String token;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="last_used", nullable=false)
	private Date lastUsed;

	public String getSeries() {
		return series;
	}

	public void setSeries(String series) {
		this.series = series;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Date getLastUsed() {
		return lastUsed;
	}

	public void setLastUsed(Date lastUsed) {
		this.lastUsed = lastUsed;
	}
}
