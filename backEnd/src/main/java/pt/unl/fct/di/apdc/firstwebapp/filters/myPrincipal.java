package pt.unl.fct.di.apdc.firstwebapp.filters;

import java.security.Principal;

public class myPrincipal implements Principal{
	
	private final String name;
	
	public myPrincipal(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

}
