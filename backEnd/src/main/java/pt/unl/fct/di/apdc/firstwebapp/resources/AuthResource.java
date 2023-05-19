package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Response.Status;

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.SignInData;
import pt.unl.fct.di.apdc.firstwebapp.util.UserListOut2;
import pt.unl.fct.di.apdc.firstwebapp.util.Extra;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;


@Path("/auth")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class AuthResource {
	
	private static final Logger LOG = Logger.getLogger(AuthResource.class.getName());
	
	private final Gson g = new Gson();
	
	// private final Datastore datastore =
		// DatastoreOptions.getDefaultInstance().getService();
	private final Datastore datastore = DatastoreOptions.newBuilder().setHost("http://localhost:8081")
				.setProjectId("iconic-valve-379315").build().getService();
	
	public AuthResource() {
		
	}
	
	@POST
	@Path("/signInSU")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response SU(@QueryParam("password") String password) {
		LOG.fine("Creating SuperUSER Ruben Belo");

		Transaction txn = datastore.newTransaction();

		try {
			Key userKey = datastore.newKeyFactory().setKind("User").newKey("rbnBeloSU");
			Key tokenKey = datastore.newKeyFactory().addAncestor(PathElement.of("User", "rbnBeloSU"))
					.setKind("UserToken").newKey("token");
			Entity user = txn.get(userKey);
			if (user != null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User already exists.").build();
			} else {
				String n = "none";
				user = Entity.newBuilder(userKey).set("user_name", "Ruben Belo")
						.set("user_pwd", DigestUtils.sha512Hex(password)).set("user_email", "su@email")
						.set("user_role", "SU").set("user_state", 1l).set("user_profile", "publico").set("user_cell", n)
						.set("user_cellHome", n).set("user_occupation", n).set("user_workplace", n)
						.set("user_address", n).set("user_nif", n).set("user_photo", n)
						.set("user_creation_time", Timestamp.now()).build();

				AuthToken at = new AuthToken("rbnBeloSU", user.getString("user_role"));
				String tokenString = Extra.AuthTokenCreate(at);
				Entity token = Entity.newBuilder(tokenKey).set("value", tokenString).build();
				Response r = Response.ok(g.toJson(at)).header("token", Extra.AuthTokenCreate(at)).build();

				txn.add(user, token);

				LOG.info("SuperUSER registered " + password);
				txn.commit();
				return r;
			}
		} catch (Exception e) {
			txn.rollback();
			LOG.severe(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			if (txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}

	}

	@POST
	@Path("/visit/signIn")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response SigninVisit(SignInData data) {
		return doRegister("Visitante", data);
		
	}
	
	@POST
	@Path("/student/signIn")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response SigninStudent(SignInData data) {
		return doRegister("Estudante", data);
	}
	
	@POST
	@Path("/prof/signIn")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response SigninProf(SignInData data) {
		return doRegister("Docente", data);
	}
	@POST
	@Path("/GS/signIn")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response SigninGS(SignInData data) {
		return doRegister("GS", data);
	}
	
	@POST
	@Path("/login")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doLogin(LoginData data, @Context HttpServletRequest request, @Context HttpHeaders headers) {

		LOG.fine("Attempt to Login user: " + data.username);

		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
		Key counterKey = datastore.newKeyFactory().addAncestor(PathElement.of("User", data.username))
				.setKind("UserStats").newKey("counters");

		Key logKey = datastore.allocateId(datastore.newKeyFactory().addAncestor(PathElement.of("User", data.username))
				.setKind("UserLog").newKey());

		Key tokenKey = datastore.newKeyFactory().addAncestor(PathElement.of("User", data.username)).setKind("UserToken")
				.newKey("token");

		Transaction txn = datastore.newTransaction();
		try {
			Entity user = txn.get(userKey);
			if (user == null) {
				LOG.warning("User " + data.username + " does not exist");
				return Response.status(Status.FORBIDDEN).build();
			}
			Entity stats = txn.get(counterKey);

			if (stats == null) {
				stats = Entity.newBuilder(counterKey).set("user_stats_logins", 0L).set("user_stats_failed", 0L)
						.set("user_first_login", Timestamp.now()).set("user_last_login", Timestamp.now()).build();
			}

			String HashedPWD = user.getString("user_pwd");
			long state = user.getLong("user_state");

			if (HashedPWD.equals(DigestUtils.sha512Hex(data.password)) && state == 1l) {

				Entity log = Entity.newBuilder(logKey).set("user_login_ip", request.getRemoteAddr())
						.set("user_login_host", request.getRemoteHost()).set("user_login_time", Timestamp.now())
						.build();
				stats = Entity.newBuilder(stats).set("user_stats_logins", 1L + stats.getLong("user_stats_logins"))
						.set("user_stats_failed", 0L).set("user_first_login", stats.getTimestamp("user_first_login"))
						.set("user_last_login", Timestamp.now()).build();

				AuthToken at = new AuthToken(data.username, user.getString("user_role"));
				String tokenString = Extra.AuthTokenCreate(at);
				Entity token = Entity.newBuilder(tokenKey).set("value", tokenString).build();
				UserListOut2 outUser = Extra.convert(user);
				Response r = Response.ok(g.toJson(outUser)).header("token", tokenString).build();
				txn.put(log, stats, token);
				txn.commit();
				LOG.info("User " + data.username + " logged in successfully");

				return r;

			} else {
				stats = Entity.newBuilder(counterKey).set("user_stats_logins", stats.getLong("user_stats_logins"))
						.set("user_stats_failed", 1L + stats.getLong("user_stats_logins"))
						.set("user_first_login", stats.getTimestamp("user_first_login"))
						.set("user_last_login", Timestamp.now()).set("user_last_attempt", Timestamp.now()).build();
				LOG.warning("Failed login attempt for " + data.username);
				txn.put(stats);
				txn.commit();
				return Response.status(Status.FORBIDDEN).build();

			}
		} finally {
			if (txn.isActive()) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).build();
			}
		}
	}
	
	@DELETE
	@Path("/logout")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response Logout(@Context HttpHeaders headers, @Context SecurityContext secContext) {

		String username = secContext.getUserPrincipal().getName();
		String role = secContext.getAuthenticationScheme();
		

		Transaction txn = datastore.newTransaction();

		try {
			Key tokenKey = datastore.newKeyFactory().addAncestor(PathElement.of("User", username)).setKind("UserToken")
					.newKey("token");
			txn.delete(tokenKey);
			LOG.info("User logout " + username);
			txn.commit();
			return Response.ok().build();

		} finally {
			if (txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}

	}
	
	private Response doRegister(String Role, SignInData  data) {
		LOG.fine("Attempt to register user: " + data.username);

		if (!data.validRegistation()) {
			return Response.status(Status.BAD_REQUEST).entity("Missing or wrong parameter.").build();
		}

		Transaction txn = datastore.newTransaction();

		try {
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
			Key tokenKey = datastore.newKeyFactory().addAncestor(PathElement.of("User", data.username))
					.setKind("UserToken").newKey("token");
			Entity user = txn.get(userKey);
			if (user != null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User already exists.").build();
			} else {
				String n = "none";
				user = Entity.newBuilder(userKey).set("user_name", data.name)
						.set("user_pwd", DigestUtils.sha512Hex(data.password)).set("user_email", data.email)
						.set("user_role", Role).set("user_state", 0l).set("user_profile", "publico")
						.set("user_cell", n).set("user_cellHome", n).set("user_occupation", n).set("user_workplace", n)
						.set("user_address", n).set("user_nif", n).set("user_photo", n)
						.set("user_creation_time", Timestamp.now()).build();

				AuthToken at = new AuthToken(data.username, user.getString("user_role"));
				String tokenString = Extra.AuthTokenCreate(at);
				Entity token = Entity.newBuilder(tokenKey).set("value", tokenString).build();
				Response r = Response.ok(g.toJson(at)).header("token", Extra.AuthTokenCreate(at)).build();

				txn.add(user, token);

				LOG.info("User registered " + data.username);
				txn.commit();
				return r;
			}
		} catch (Exception e) {
			txn.rollback();
			LOG.severe(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			if (txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}

}
