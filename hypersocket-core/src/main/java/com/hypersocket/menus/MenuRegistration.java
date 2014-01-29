/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.menus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.hypersocket.permissions.PermissionType;
import com.hypersocket.realm.Principal;

@XmlRootElement(name = "menu")
public class MenuRegistration {

	String resourceKey;
	String bundle;
	Integer weight;
	PermissionType readPermission;
	PermissionType createPermission;
	PermissionType updatePermission;
	PermissionType deletePermission;
	String resourceName;
	Map<String,PermissionType> additionalPermissions;
	
	List<MenuRegistration> modules = new ArrayList<MenuRegistration>();

	public MenuRegistration() {
	}

	public MenuRegistration(String bundle, String resourceKey, String url, Integer weight,
			PermissionType readPermision, PermissionType createPermission,
			PermissionType updatePermission, PermissionType deletePermission) {
		this(bundle, resourceKey, url, weight, readPermision, createPermission,
				updatePermission, deletePermission, null);
	}
	
	public MenuRegistration(String bundle, String resourceKey, String url, Integer weight,
			PermissionType readPermision, PermissionType createPermission,
			PermissionType updatePermission, PermissionType deletePermission,
			Map<String,PermissionType> additionalPermissions) {
		this.bundle = bundle;
		this.resourceKey = resourceKey;
		this.resourceName = url;
		this.weight = weight;
		this.readPermission = readPermision;
		this.createPermission = createPermission;
		this.updatePermission = updatePermission;
		this.deletePermission = deletePermission;
		this.additionalPermissions = additionalPermissions;
	}

	public String getId() {
		return resourceKey;
	}

	public String getResourceName() {
		return resourceName;
	}

	public String getResourceKey() {
		return resourceKey;
	}

	public void addMenu(MenuRegistration module) {
		modules.add(module);
	}

	public List<MenuRegistration> getMenus() {
		return modules;
	}

	public Integer getWeight() {
		return weight;
	}

	@JsonIgnore
	public PermissionType getReadPermission() {
		return readPermission;
	}

	@JsonIgnore
	public PermissionType getCreatePermission() {
		return createPermission;
	}

	@JsonIgnore
	public PermissionType getUpdatePermission() {
		return updatePermission;
	}

	@JsonIgnore
	public PermissionType getDeletePermission() {
		return deletePermission;
	}

	protected boolean hasAccess(Principal principal) {
		return true;
	}

}
