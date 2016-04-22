package com.hypersocket.auth;

import org.springframework.stereotype.Repository;

import com.hypersocket.auth.AuthenticationScheme;
import com.hypersocket.resource.AbstractResourceRepositoryImpl;

@Repository
public class UsernameAndPasswordRepositoryImpl extends
		AbstractResourceRepositoryImpl<AuthenticationScheme> implements
		UsernameAndPasswordRepository {

	@Override
	protected Class<AuthenticationScheme> getResourceClass() {
		return AuthenticationScheme.class;
	}
	
	
}
