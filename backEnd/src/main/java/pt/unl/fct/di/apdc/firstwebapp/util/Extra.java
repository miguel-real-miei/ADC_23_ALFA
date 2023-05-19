package pt.unl.fct.di.apdc.firstwebapp.util;

import java.util.Date;

import com.google.cloud.datastore.*;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;

public class Extra {
	private static final String SECRET = "38782F413F4428472B4B6250655368566D5970337336763979244226452948404D635166546A576E5A7234743777217A25432A462D4A614E645267556B587032";

	//private final static Datastore datastore = DatastoreOptions.newBuilder().setHost("http://localhost:8081")
		//	.setProjectId("iconic-valve-379315").build().getService();

	private final static Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	
	public Extra() {}

	public static int roleToInt(String role) {
		switch (role) {
		case "Visitante":
			return 1;
		case "Aluno":
			return 2;
		case "Docente":
			return 3;
		case "GS":
			return 4;
		case "SU":
			return 5;
		}
		return 0;
	}

	public static String AuthTokenCreate(AuthToken token) {
		Claims claims = new DefaultClaims();
		claims.put("username", token.username);
		claims.put("role", token.role);
		claims.put("tokenID", token.tokenID);
		claims.setExpiration(new Date(token.expirationData));
		claims.setIssuedAt(new Date(token.creationData));

		return Jwts.builder().setClaims(claims).setSubject("AuthenticationADC")
				.signWith(SignatureAlgorithm.HS512, SECRET).compact();
	}

	public static AuthToken AuthTokenDecode(String token) {
		Claims claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();
		String tokenUsername = (String) claims.get("username");
		String tokenRole = (String) claims.get("role");
		String tokenID = (String) claims.get("tokenID");
		long creationDate = claims.getIssuedAt().getTime();
		long expirationDate = claims.getExpiration().getTime();

		return new AuthToken(tokenUsername, tokenRole, tokenID, creationDate, expirationDate);
	}

	public static boolean checkToken(String token, String User) {
		Key tokenKey = datastore.newKeyFactory().addAncestor(PathElement.of("User", User)).setKind("UserToken")
				.newKey("token");
		try {
			Entity saveToken = datastore.get(tokenKey);
			if (saveToken == null) {
				return false;
			}

			String value = saveToken.getString("value");

			Claims claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();

			long expirationDate = claims.getExpiration().getTime();

			return value.equals(token) && expirationDate >= System.currentTimeMillis();
		} catch (Exception e) {
			datastore.delete(tokenKey);
			return false;
		}
	}

	public static UserOptionalData extractData(Entity user, UserOptionalData data) {
		if (data.profile.equals("none")) {
			data.profile = user.getString("user_profile");
		}
		if (data.address.equals("none")) {
			data.address = user.getString("user_address");
		}
		if (data.cell.equals("none")) {
			data.cell = user.getString("user_cell");
		}
		if (data.cellHome.equals("none")) {
			data.cellHome = user.getString("user_cellHome");
		}
		if (data.occupation.equals("none")) {
			data.occupation = user.getString("user_occupation");
		}
		if (data.workplace.equals("none")) {
			data.workplace = user.getString("user_workplace");
		}
		if (data.nif.equals("none")) {
			data.nif = user.getString("user_nif");
		}

		return data;
	}

	public static UserOptionalAdminData extractData(Entity userToChange, UserOptionalAdminData data) {
		if (data.profile.equals("none")) {
			data.profile = userToChange.getString("user_profile");
		}
		if (data.address.equals("none")) {
			data.address = userToChange.getString("user_address");
		}
		if (data.cell.equals("none")) {
			data.cell = userToChange.getString("user_cell");
		}
		if (data.cellHome.equals("none")) {
			data.cellHome = userToChange.getString("user_cellHome");
		}
		if (data.occupation.equals("none")) {
			data.occupation = userToChange.getString("user_occupation");
		}
		if (data.workplace.equals("none")) {
			data.workplace = userToChange.getString("user_workplace");
		}
		if (data.nif.equals("none")) {
			data.nif = userToChange.getString("user_nif");
		}
		if (data.name.equals("none")) {
			data.name = userToChange.getString("user_name");
		}
		if (data.email.equals("none")) {
			data.email = userToChange.getString("user_email");
		}

		return data;
	}

	public static UserListOut2 convert(Entity user) {
		return new UserListOut2(user.getKey().getName(), user.getString("user_email"), user.getString("user_name"),
				user.getString("user_role"), user.getLong("user_state"), user.getString("user_cell"),
				user.getString("user_cellHome"), user.getString("user_occupation"), user.getString("user_workplace"),
				user.getString("user_address"), user.getString("user_nif"), user.getString("user_profile"));
	}
	
	public static EventCreationData extractData(Entity event, EventCreationData data) {
		if (data.name == null) {
			data.name = event.getString("event_name");
		}
		if (data.type == null) {
			data.type = event.getString("event_type");
		}
		if (data.date == null) {
			data.date = event.getString("event_date");
		}
		if (data.time == null) {
			data.time = event.getString("event_time");
		}
		if (data.duration == null) {
			data.duration = event.getString("event_duration");
		}
		if (data.description == null) {
			data.description = event.getString("event_description");
		}
		if (data.location == null) {
			data.location = event.getString("event_location");
		}

		return data;
	}

}
