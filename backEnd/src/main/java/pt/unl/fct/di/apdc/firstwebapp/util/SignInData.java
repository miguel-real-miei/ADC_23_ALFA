package pt.unl.fct.di.apdc.firstwebapp.util;



public class SignInData {
	
	public String username;
	public String password;
	public String passwordConfirm;
	public String email;
	public String name;
	
	public SignInData() {
		
	}
	
	public boolean validRegistation() {
		boolean valid = true;
		if(!(email.contains("@") && !email.contains(" "))) {
			return false;
		}
		
		if(username.contains(" ")) {
			return false;
		}
		
		if(!password.equals(passwordConfirm)) {
			return false;
		}
		
		return valid;
	}

}
