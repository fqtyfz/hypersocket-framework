/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.resource;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;

import com.hypersocket.permissions.Role;
import com.hypersocket.realm.Realm;

@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
public abstract class AssignableResource extends Resource {

	@ManyToMany(fetch=FetchType.EAGER)
	@JoinTable(name = "resource_roles", joinColumns={@JoinColumn(name="resource_id")}, 
			inverseJoinColumns={@JoinColumn(name="role_id")})
	Set<Role> roles = new HashSet<Role>();

	@OneToOne
	@JoinColumn(name="realm_id")
	Realm realm;
	
	public AssignableResource() {
	}
	
	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public Realm getRealm() {
		return realm;
	}

	public void setRealm(Realm realm) {
		this.realm = realm;
	}
	
}
