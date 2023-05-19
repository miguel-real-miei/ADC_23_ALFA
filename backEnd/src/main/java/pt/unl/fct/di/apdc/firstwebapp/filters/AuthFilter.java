package pt.unl.fct.di.apdc.firstwebapp.filters;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.PathElement;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import pt.unl.fct.di.apdc.firstwebapp.resources.UsersResource;
import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;

@Provider
public class AuthFilter implements ContainerRequestFilter, ContainerResponseFilter {

	private static final String SECRET = "38782F413F4428472B4B6250655368566D5970337336763979244226452948404D635166546A576E5A7234743777217A25432A462D4A614E645267556B587032";

	private static final Logger LOG = Logger.getLogger(UsersResource.class.getName());
	
	private final static Datastore datastore =
	 DatastoreOptions.newBuilder().setHost("http://localhost:8081")
	 .setProjectId("iconic-valve-379315").build().getService();

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		if (!requestContext.getUriInfo().getPath().contains("auth")) {
			String tokenString = requestContext.getHeaderString("token");
			
			try {
				AuthToken token = AuthTokenDecode(tokenString);
				if (checkToken(tokenString, token.username)) {
					if(requestContext.getUriInfo().getPath().contains("state") && (!token.role.equals("SU") && !token.role.equals("GS"))) {
						requestContext.abortWith(
				                Response.status(Response.Status.UNAUTHORIZED)
				                        .build());
					}
					LOG.warning("cheguei aqui");
					requestContext.setSecurityContext(new mySecurityContext(token.role, token.username));
				} else {
					requestContext.abortWith(
			                Response.status(Response.Status.UNAUTHORIZED)
			                        .build());
				}

			} catch (Exception e) {
				requestContext.abortWith(
		                Response.status(Response.Status.UNAUTHORIZED)
		                        .build());
			}
		}

	}

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {
		if (!(requestContext.getUriInfo().getPath().contains("auth"))) {
			/*String username = requestContext.getSecurityContext().getUserPrincipal().getName();
			String role = requestContext.getSecurityContext().getAuthenticationScheme();
			AuthToken at = new AuthToken(username, role);
			String token = AuthTokenCreate(at);*/
			String token = requestContext.getHeaderString("token");
			responseContext.getHeaders().add("token", token);
		}
		
		responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
		responseContext.getHeaders().add("Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS, HEAD");
		responseContext.getHeaders().add("Access-Control-Allow-Headers",
                "Authorization, Content-Type");
    

		responseContext.getHeaders().add("Access-Control-Allow-Origin", "http://localhost");

	}

	private String AuthTokenCreate(AuthToken token) {
		Claims claims = new DefaultClaims();
		claims.put("username", token.username);
		claims.put("role", token.role);
		claims.put("tokenID", token.tokenID);
		claims.setExpiration(new Date(token.expirationData));
		claims.setIssuedAt(new Date(token.creationData));
		String jwtToken = Jwts.builder().setClaims(claims).setSubject("AuthenticationADC")
				.signWith(SignatureAlgorithm.HS512, SECRET).compact();
		return jwtToken;

	}

	private AuthToken AuthTokenDecode(String token) {

		Claims claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();
		String tokenUsername = (String) claims.get("username");
		String tokenRole = (String) claims.get("role");
		String tokenID = (String) claims.get("tokenID");
		long creationDate = claims.getIssuedAt().getTime();
		long expirationDate = claims.getExpiration().getTime();

		return new AuthToken(tokenUsername, tokenRole, tokenID, creationDate, expirationDate);

	}

	private boolean checkToken(String token, String User) {

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

}
