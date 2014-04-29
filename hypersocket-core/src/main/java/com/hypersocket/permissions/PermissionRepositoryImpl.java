/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.permissions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.hypersocket.properties.ResourceKeyRestriction;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmOrGlobalRestriction;
import com.hypersocket.realm.RealmRestriction;
import com.hypersocket.repository.AbstractRepositoryImpl;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.repository.DetachedCriteriaConfiguration;
import com.hypersocket.repository.DistinctRootEntity;
import com.hypersocket.repository.HiddenCriteria;
import com.hypersocket.tables.ColumnSort;

@Repository
public class PermissionRepositoryImpl extends AbstractRepositoryImpl<Long> implements PermissionRepository {

	DetachedCriteriaConfiguration JOIN_PERMISSIONS = new DetachedCriteriaConfiguration() {
		@Override
		public void configure(DetachedCriteria criteria) {
			criteria.setFetchMode("permissions", FetchMode.JOIN);
			criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);		
		}		
	};
	
	DetachedCriteriaConfiguration JOIN_PRINCIPALS_PERMISSIONS = new DetachedCriteriaConfiguration() {
		@Override
		public void configure(DetachedCriteria criteria) {
			criteria.setFetchMode("permissions", FetchMode.JOIN);
			criteria.setFetchMode("principals", FetchMode.JOIN);
			criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);		
		}		
	};
	
	
	DetachedCriteriaConfiguration JOIN_ROLES = new DetachedCriteriaConfiguration() {
		@Override
		public void configure(DetachedCriteria criteria) {
			criteria.setFetchMode("roles", FetchMode.JOIN);
			criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);		
		}	
	};
	

	@Override
	public PermissionCategory createCategory(String resourceBundle, String resourceKey) {
		
		PermissionCategory category = new PermissionCategory();
		category.setResourceBundle(resourceBundle);
		category.setResourceKey(resourceKey);
		save(category);
		return category;
	}

	@Override
	public Permission createPermission(String name, PermissionCategory category, boolean hidden) {
		
		Permission permission = new Permission();
		permission.setResourceKey(name);
		permission.setCategory(category);
		permission.setHidden(hidden);
		save(permission);
		return permission;
	}
	
	@Override
	public Role createRole(String name, Realm realm) {
		
		Role role = new Role();
		role.setName(name);
		role.setRealm(realm);
		save(role);
		return role;
	}

	@Override
	public PermissionCategory getCategoryByKey(String resourceBundle, String resourceKey) {
		return get("resourceBundle", resourceBundle, PermissionCategory.class, new ResourceKeyRestriction(resourceKey), new DistinctRootEntity());
	}

	@Override
	public PermissionCategory getCategoryById(Long id) {
		return getCategory("id", id);
	}
	
	protected PermissionCategory getCategory(String column, Object value) {
		return get(column, value, PermissionCategory.class, JOIN_PERMISSIONS);
	}
	
	@Override
	public Permission getPermissionByResourceKey(String name) {
		return getPermission("resourceKey", name);
	}

	@Override
	public Permission getPermissionById(Long id) {
		return getPermission("id", id);
	}
	
	protected Permission getPermission(String column, Object value) {
		return get(column, value, Permission.class, JOIN_ROLES);
	}
	
	@Override
	public Role getRoleByName(String name, Realm realm) {
		return get("name", name, Role.class, JOIN_PRINCIPALS_PERMISSIONS, new RealmRestriction(realm));
	}

	@Override
	public Role getRoleById(Long id) {
		return get("id", id, Role.class, JOIN_PRINCIPALS_PERMISSIONS);
	}
	
	@Override
	public void grantPermission(Role role, Permission permission) {
		role.getPermissions().add(permission);
		save(role);
	}

	@Override
	public void revokePermission(Role role, Permission permission) {
		role.getPermissions().remove(permission);
		save(role);
	}
	
	@Override
	public void grantPermissions(Role role, Collection<Permission> permissions) {
		role.getPermissions().addAll(permissions);
		save(role);
	}

	@Override
	public void revokePermission(Role role, Collection<Permission> permissions) {
		role.getPermissions().removeAll(permissions);
		save(role);
	}

	@Override
	public void deleteCategory(PermissionCategory category) {
		delete(category);
	}

	@Override
	public void deletePermission(Permission permission) {
		delete(permission);
	}

	@Override
	public void deleteRole(Role role) {
		delete(role);
	}

	@Override
	public List<PermissionCategory> getAllCatgories() {
		return allEntities(PermissionCategory.class, JOIN_PERMISSIONS);
	}

	@Override
	public List<Permission> getAllPermissions() {
		return allEntities(Permission.class, JOIN_ROLES, new HiddenCriteria(false));
	}
	
	@Override
	public void saveRole(Role role) {
		save(role);
	}
	
	@Override
	public void assignRole(Role role, Principal... principals) {
		
		for(Principal p : principals) {
			role.getPrincipals().add(p);
		}
		
		save(role);
		
	}

	@Override
	public void unassignRole(Role role, Principal... principals) {
		for(Principal p : principals) {
			role.getPrincipals().remove(p);
		}
		
		save(role);
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Set<Permission> getPrincipalPermissions(Collection<Principal> principals, PermissionType...permissionTypes) {
		
		if(principals==null) {
			return new HashSet<Permission>();
		}

		Criteria crit = sessionFactory.getCurrentSession().createCriteria(Permission.class)
				.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		
		List<String> list = new ArrayList<String>();
		for(PermissionType t : permissionTypes) {
			list.add(t.getResourceKey());
		}
		
		if(list.size() > 0) {
			crit = crit.add(Restrictions.in("resourceKey", list));
		}
		
		List<Long> ids = new ArrayList<Long>();
		for(Principal p : principals) {
			ids.add(p.getId());
		}
		
		crit = crit.createCriteria("roles")
				.createCriteria("principals")
				.add(Restrictions.in("id", ids));
		
		return new HashSet<Permission>(crit.list());
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Set<Principal> getPrincipalsWithPermissions(
			PermissionType permission) {


		Criteria crit = sessionFactory.getCurrentSession().createCriteria(Principal.class)
				.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
				.createCriteria("roles")
				.createCriteria("permissions")
				.add(Restrictions.eq("resourceKey", permission.getResourceKey()));
		
		return new HashSet<Principal>(crit.list());
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public Set<Role> getRolesWithPermissions(
			PermissionType permission) {


		Criteria crit = sessionFactory.getCurrentSession().createCriteria(Role.class)
				.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
				.createCriteria("permissions")
				.add(Restrictions.eq("resourceKey", permission.getResourceKey()));
		
		return new HashSet<Role>(crit.list());
	}


	@SuppressWarnings("unchecked")
	@Override
	public Set<Role> getRolesWithPermissions(
			Permission permission) {


		Criteria crit = sessionFactory.getCurrentSession().createCriteria(Role.class)
				.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
				.createCriteria("permissions")
				.add(Restrictions.eq("resourceKey", permission.getResourceKey()));
		
		return new HashSet<Role>(crit.list());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Set<Principal> getPrincipalsWithPermissions(
			Permission permission) {


		Criteria crit = sessionFactory.getCurrentSession().createCriteria(Principal.class)
				.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
				.createCriteria("roles")
				.createCriteria("permissions")
				.add(Restrictions.eq("resourceKey", permission.getResourceKey()));
		
		return new HashSet<Principal>(crit.list());
	}
	
	@Override
	public List<Role> getRolesForRealm(Realm realm) {
		return allEntities(Role.class, JOIN_PRINCIPALS_PERMISSIONS, new RealmOrGlobalRestriction(realm));
	}
	
	@Override
	public List<Role> searchRoles(final Realm realm, String searchPattern, int start, int length, ColumnSort[] sorting) {
		return search(Role.class, "name", searchPattern, start, length, sorting, new CriteriaConfiguration() {
			
			@Override
			public void configure(Criteria criteria) {
				criteria.setFetchMode("permissions", FetchMode.JOIN);
				criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);		
				criteria.add(Restrictions.or(Restrictions.eq("realm", realm), Restrictions.isNull("realm")));
			}
		});
	}
	
	@Override
	public Long countRoles(final Realm realm, String searchPattern) {
		return getCount(Role.class, "name", searchPattern, new CriteriaConfiguration() {
			
			@Override
			public void configure(Criteria criteria) {
				criteria.setFetchMode("permissions", FetchMode.JOIN);
				criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);		
				criteria.add(Restrictions.or(Restrictions.eq("realm", realm), Restrictions.isNull("realm")));
			}
		});
	}

	@Override
	public List<Permission> getPermissionsByCategory(final PermissionCategory category) {
		return allEntities(Permission.class, JOIN_ROLES, new DetachedCriteriaConfiguration() {
			
			@Override
			public void configure(DetachedCriteria criteria) {
				criteria.add(Restrictions.eq("category", category));
				
			}
		});
	}

}
