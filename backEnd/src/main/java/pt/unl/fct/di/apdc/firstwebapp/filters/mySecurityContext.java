package pt.unl.fct.di.apdc.firstwebapp.filters;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

public class mySecurityContext implements SecurityContext{
	
	private final String myRole;
	private final myPrincipal p;
	
	public mySecurityContext(String role, String name) {
		myRole = role;
		p = new myPrincipal(name);
	}

	@Override
	public Principal getUserPrincipal() {
		return p;
	}

	@Override
	public boolean isUserInRole(String role) {
		return role.equals(myRole);
	}

	@Override
	public boolean isSecure() {
		return false;
	}

	//get role
	@Override
	public String getAuthenticationScheme() {
		return myRole;
	}

}
