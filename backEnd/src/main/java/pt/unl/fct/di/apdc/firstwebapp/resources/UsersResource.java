package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Response.Status;
import javax.servlet.http.HttpServletRequest;

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.cloud.datastore.StructuredQuery.*;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;
import pt.unl.fct.di.apdc.firstwebapp.util.SignInData;
import pt.unl.fct.di.apdc.firstwebapp.util.ChangePwdData;
import pt.unl.fct.di.apdc.firstwebapp.util.Extra;
import pt.unl.fct.di.apdc.firstwebapp.util.UserListOut1;
import pt.unl.fct.di.apdc.firstwebapp.util.UserListOut2;
import pt.unl.fct.di.apdc.firstwebapp.util.UserOptionalAdminData;
import pt.unl.fct.di.apdc.firstwebapp.util.UserOptionalData;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class UsersResource {

	private static final Logger LOG = Logger.getLogger(UsersResource.class.getName());

	private final Gson g = new Gson();

	// private final Datastore datastore =
			// DatastoreOptions.getDefaultInstance().getService();
	
	private final Datastore datastore = DatastoreOptions.newBuilder().setHost("http://localhost:8081")
			.setProjectId("iconic-valve-379315").build().getService();

	public UsersResource() {
	}

	

	@PUT
	@Path("/role/{targetUsername}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response changeRole(@PathParam("targetUsername") String targetUsername,
			@QueryParam("newRole") String newRole, @Context HttpHeaders headers, @Context SecurityContext secContext) {

		
		String username = secContext.getUserPrincipal().getName();
		String role = secContext.getAuthenticationScheme();

		LOG.warning(username + " attempt to change user role " + targetUsername);

		Transaction txn = datastore.newTransaction();

		try {
			Key targetUserKey = datastore.newKeyFactory().setKind("User").newKey(targetUsername);

			Entity targetUser = txn.get(targetUserKey);
			if (targetUser == null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User does not exist.").build();
			}

			int userRole = Extra.roleToInt(role);

			int userToChangeRole = Extra.roleToInt(targetUser.getString("user_role"));

			int newRoleCode = Extra.roleToInt(newRole);

			if (userRole == 0) {
				LOG.warning("user without role " + username);
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}

			if (newRoleCode == 0) {
				LOG.warning("wrong new role " + newRole);
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).build();
			}

			if (userToChangeRole == 0) {
				LOG.warning("user without role " + targetUsername);
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}

			if (userToChangeRole >= userRole) {
				LOG.warning(username + " wrong change role request for user " + targetUsername);
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).build();
			}

			if (userRole == 5 || userRole == 4) {
				LOG.warning(username + " wrong change role request for user " + targetUsername);
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).build();
			}

			targetUser = Entity.newBuilder(targetUser).set("user_role", newRole).build();
			txn.update(targetUser);
			txn.commit();
			return Response.ok().build();

		} finally {
			if (txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}

	}
	
	
	//TODO: alterar
	@PUT
	@Path("/{username}/state")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response changeState(@PathParam("username") String userToChange, @Context HttpHeaders headers, @Context SecurityContext secContext) {
		LOG.fine("Attempt to change state user: " + userToChange);

		String username = secContext.getUserPrincipal().getName();
		String role = secContext.getAuthenticationScheme();
		Transaction txn = datastore.newTransaction();

		try {
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(userToChange);
			Entity user = txn.get(userKey);
			if (user == null) {
				txn.rollback();
				LOG.warning("user does not exist " + userToChange);
				return Response.status(Status.BAD_REQUEST).build();
			}
			long state = 1;

			user = Entity.newBuilder(user).set("user_state", state).build();

			txn.update(user);
			txn.commit();
			return Response.ok().build();

		} finally {
			if (txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}

	@DELETE
	@Path("/delete")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteUser(@Context HttpHeaders headers, @Context SecurityContext secContext) {

		
		
		String username = secContext.getUserPrincipal().getName();
		String role = secContext.getAuthenticationScheme();
		
		LOG.fine("Attempt to delete user: " + username);

		Transaction txn = datastore.newTransaction();

		try {
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(username);
			Key tokenKey = datastore.newKeyFactory().addAncestor(PathElement.of("User", username)).setKind("UserToken")
					.newKey("token");
			txn.delete(userKey, tokenKey);
			LOG.info("User deleted " + username);
			txn.commit();
			return Response.ok().build();

		} finally {
			if (txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}

	@DELETE
	@Path("/delete/{userToDelete}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteUser(@PathParam("userToDelete") String usernameToDelete, @Context HttpHeaders headers, @Context SecurityContext secContext) {

		String username = secContext.getUserPrincipal().getName();
		String role = secContext.getAuthenticationScheme();

		LOG.fine("Attempt to delete user: " + username);

		Transaction txn = datastore.newTransaction();

		try {
			Key userToDeleteKey = datastore.newKeyFactory().setKind("User").newKey(usernameToDelete);
			Key tokenKey = datastore.newKeyFactory().addAncestor(PathElement.of("User", usernameToDelete))
					.setKind("UserToken").newKey("token");

			Entity userToDelete = txn.get(userToDeleteKey);
			if (userToDelete == null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User does not exist.").build();
			}

			int userRole = Extra.roleToInt(role);

			int userToDeleteRole = Extra.roleToInt(userToDelete.getString("user_role"));

			if (userRole == 0) {
				LOG.warning("user without role " + username);
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}

			if (userToDeleteRole == 0) {
				LOG.warning("user without role " + usernameToDelete);
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}

			if (userToDeleteRole >= userRole) {
				LOG.warning("Wrong delete request for user " + username + " attempts to delete " + usernameToDelete);
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).build();
			}
			
			if (userRole != 4 || userRole != 5) {
				LOG.warning("Wrong delete request for user " + username + " attempts to delete " + usernameToDelete);
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).build();
			}

			txn.delete(userToDeleteKey, tokenKey);
			LOG.info("User deleted " + usernameToDelete + " by " + username);
			txn.commit();
			return Response.ok().build();

		} finally {
			if (txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}

	@PUT
	@Path("/updateInfo/{targetUsername}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateUserInfo(@PathParam("targetUsername") String targetUsername, UserOptionalAdminData data,
			@Context HttpHeaders headers, @Context SecurityContext secContext) {

		String username = secContext.getUserPrincipal().getName();
		String role = secContext.getAuthenticationScheme();
		

		LOG.fine(targetUsername + " attempt to change user state " + username);

		Transaction txn = datastore.newTransaction();

		try {
			Key targetUser = datastore.newKeyFactory().setKind("User").newKey(targetUsername);

			Entity userToChange = txn.get(targetUser);
			if (userToChange == null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User does not exist.").build();
			}

			int userRole = Extra.roleToInt(role);

			int userToChangeRole = Extra.roleToInt(userToChange.getString("user_role"));

			if (userRole == 0) {
				LOG.warning("user without role " + username);
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}

			if (userToChangeRole == 0) {
				LOG.warning("user without role " + targetUsername);
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}

			if (userToChangeRole >= userRole) {
				LOG.warning("Wrong delete request for user " + username + " attempts to delete " + targetUsername);
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).build();
			}
			
			if (userRole != 4 || userRole != 5) {
				LOG.warning("Wrong delete request for user " + username + " attempts to delete " + targetUsername);
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).build();
			}

			data = Extra.extractData(userToChange, data);

			userToChange = Entity.newBuilder(userToChange).set("user.profile", data.profile).set("user_name", data.name)
					.set("user_email", data.email).set("user_cell", data.cell).set("user_cellHome", data.cellHome)
					.set("user_occupation", data.occupation).set("user_workplace", data.workplace)
					.set("user_address", data.address).set("user_nif", data.nif).build();
			txn.update(userToChange);

			LOG.info(targetUsername + " User updated " + username);

			txn.update(userToChange);
			txn.commit();
			return Response.ok().build();

		} finally {
			if (txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}

	}

	@PUT
	@Path("/updateUserInfo")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateUserInfo(UserOptionalData data, @Context HttpHeaders headers, @Context SecurityContext secContext) {
		
		String username = secContext.getUserPrincipal().getName();
		String role = secContext.getAuthenticationScheme();

		LOG.warning("Attempt to update info user: " + data.cell);

		Transaction txn = datastore.newTransaction();

		try {
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(username);
			Entity user = txn.get(userKey);
			if (user == null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User does not exist.").build();
			} else {

				data = Extra.extractData(user, data);
				LOG.warning("Attempt to update info user: " + data.cell);
				user = Entity.newBuilder(user).set("user_profile", data.profile).set("user_cell", data.cell)
						.set("user_cellHome", data.cellHome).set("user_occupation", data.occupation)
						.set("user_workplace", data.workplace).set("user_address", data.address).set("user_nif", data.nif)
						.build();
				txn.update(user);
				LOG.info("User updated " + user.getString("user_cell"));
				txn.commit();
				return Response.ok().build();
			}
		} finally {
			if (txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}

	@PUT
	@Path("/changePwd")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response changePwd(ChangePwdData data, @Context HttpHeaders headers, @Context SecurityContext secContext) {

		String username = secContext.getUserPrincipal().getName();
		String role = secContext.getAuthenticationScheme();
		

		LOG.fine("Attempt to alter user pwd: " + username);
		
		Transaction txn = datastore.newTransaction();

		try {

			Key userKey = datastore.newKeyFactory().setKind("User").newKey(username);
			Entity user = txn.get(userKey);

			if (user == null) {

				txn.rollback();
				LOG.warning("user does not exist " + username);
				return Response.status(Status.BAD_REQUEST).entity("User does not exists.").build();

			} else {

				if (user.getString("user_pwd").equals(DigestUtils.sha512Hex(data.oldPassword))
						&& data.passwordConfirm.equals(data.newPassword)) {

					user = Entity.newBuilder(user).set("user_pwd", DigestUtils.sha512Hex(data.newPassword)).build();
					txn.update(user);
					LOG.info("pwd altered " + username);
					txn.commit();
					return Response.ok().build();

				} else {

					txn.rollback();
					LOG.warning("user " + username + " passwords did not match");
					return Response.status(Status.BAD_REQUEST).build();
				}
			}
		} finally {
			if (txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}

	@GET
	@Path("/token")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response showToken(@Context HttpHeaders headers) {

		String token = headers.getHeaderString("token");

		AuthToken at = Extra.AuthTokenDecode(token);

		return Response.ok(g.toJson(at)).build();

	}

	@GET
	@Path("/user/me")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getMyUser(@Context HttpHeaders headers, @Context SecurityContext secContext) {

		String username = secContext.getUserPrincipal().getName();
		String role = secContext.getAuthenticationScheme();
		

		Transaction txn = datastore.newTransaction();

		try {
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(username);
			Entity user = txn.get(userKey);
			if (user == null) {
				txn.rollback();
				LOG.warning("user does not exist " + username);
				return Response.status(Status.BAD_REQUEST).build();
			}
			LOG.info("User GET-ed " + username);
			txn.commit();
			return Response.ok(g.toJson(Extra.convert(user))).build();

		} finally {
			if (txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}

	}

	@PUT
	@Path("/token/new")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getToken(@Context HttpHeaders headers, @Context SecurityContext secContext) {

		String username = secContext.getUserPrincipal().getName();
		String role = secContext.getAuthenticationScheme();

		Transaction txn = datastore.newTransaction();

		try {
			Key tokenKey = datastore.newKeyFactory().addAncestor(PathElement.of("User", username)).setKind("UserToken")
					.newKey("token");

			AuthToken at = new AuthToken(username, role);
			String tokenString = Extra.AuthTokenCreate(at);
			Entity token = Entity.newBuilder(tokenKey).set("value", tokenString).build();

			Response r = Response.ok(g.toJson(at)).header("token", tokenString).build();
			txn.put(token);
			txn.commit();
			return r;

		} finally {
			if (txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}

	@GET
	@Path("/list")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getUsers(@Context HttpHeaders headers,  @Context SecurityContext secContext) {

		String username = secContext.getUserPrincipal().getName();
		String role = secContext.getAuthenticationScheme();
		

		LOG.warning(role + "attemp to list");

		QueryResults<Entity> users;

		if (role.equals("USER")) {
			LOG.warning("list user");

			Query<Entity> query_USER = Query.newEntityQueryBuilder().setKind("User")
					.setFilter(CompositeFilter.and(PropertyFilter.eq("user_role", "USER"),
							PropertyFilter.eq("user_state", 1L), PropertyFilter.eq("user_profile", 1L)))
					.build();
			LOG.warning("list user");
			users = datastore.run(query_USER);
			List<UserListOut1> result_USER = new ArrayList<>();
			users.forEachRemaining(user -> {
				LOG.warning(user.getString("user_name"));
				result_USER.add(new UserListOut1(user.getKey().getName(), user.getString("user_email"),
						user.getString("user_name")));
			});
			return Response.ok(g.toJson(result_USER)).build();
		}

		if (role.equals("GBO")) {
			LOG.warning("list GBO");
			Query<Entity> query_GBO = Query.newEntityQueryBuilder().setKind("User")
					.setFilter(CompositeFilter.and(PropertyFilter.eq("user_role", "USER"))).build();

			users = datastore.run(query_GBO);

			List<UserListOut2> result_GBO = new ArrayList<>();

			users.forEachRemaining(user -> {
				LOG.warning(user.getString("user_name"));
				UserListOut2 outUser = Extra.convert(user);
				result_GBO.add(outUser);
			});

			return Response.ok(g.toJson(result_GBO)).build();
		}
		if (role.equals("GS")) {
			LOG.warning("list GS");
			Query<Entity> query_GS = Query.newEntityQueryBuilder().setKind("User")
					.setFilter(CompositeFilter.and(PropertyFilter.eq("user_role", "USER"))).build();

			users = datastore.run(query_GS);

			List<UserListOut2> result_GS = new ArrayList<>();

			users.forEachRemaining(user -> {
				LOG.warning(user.getString("user_name"));
				UserListOut2 outUser = Extra.convert(user);
				result_GS.add(outUser);
			});
			query_GS = Query.newEntityQueryBuilder().setKind("User")
					.setFilter(CompositeFilter.and(PropertyFilter.eq("user_role", "GBO"))).build();

			users = datastore.run(query_GS);

			users.forEachRemaining(user -> {
				LOG.warning(user.getString("user_name"));
				UserListOut2 outUser = Extra.convert(user);
				result_GS.add(outUser);
			});

			return Response.ok(g.toJson(result_GS)).build();
		}

		if (role.equals("SU")) {
			LOG.warning("list SU");
			Query<Entity> query_SU = Query.newEntityQueryBuilder().setKind("User")
					.setFilter(CompositeFilter.and(PropertyFilter.eq("user_role", "USER"))).build();

			users = datastore.run(query_SU);

			List<UserListOut2> result_SU = new ArrayList<>();

			users.forEachRemaining(user -> {
				LOG.warning(user.getString("user_name"));
				UserListOut2 outUser = Extra.convert(user);
				result_SU.add(outUser);
			});

			query_SU = Query.newEntityQueryBuilder().setKind("User")
					.setFilter(CompositeFilter.and(PropertyFilter.eq("user_role", "GBO"))).build();

			users = datastore.run(query_SU);

			users.forEachRemaining(user -> {
				LOG.warning(user.getString("user_name"));
				UserListOut2 outUser = Extra.convert(user);
				result_SU.add(outUser);
			});

			query_SU = Query.newEntityQueryBuilder().setKind("User")
					.setFilter(CompositeFilter.and(PropertyFilter.eq("user_role", "GS"))).build();

			users = datastore.run(query_SU);

			users.forEachRemaining(user -> {
				LOG.warning(user.getString("user_name"));
				UserListOut2 outUser = Extra.convert(user);
				result_SU.add(outUser);
			});

			return Response.ok(g.toJson(result_SU)).build();
		}

		return Response.status(Status.BAD_REQUEST).build();

	}

	// test only
	@PUT
	@Path("/{username}/state/{targetUsername}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response changeState(@PathParam("username") String username,
			@PathParam("targetUsername") String targetUsername, @Context HttpHeaders headers) {

		LOG.fine(targetUsername + " attempt to change user state " + username);

		String token = headers.getHeaderString("token");

		AuthToken at = Extra.AuthTokenDecode(token);

		Transaction txn = datastore.newTransaction();

		try {
			Key targetUser = datastore.newKeyFactory().setKind("User").newKey(targetUsername);

			Entity userToChangeState = txn.get(targetUser);
			if (userToChangeState == null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User does not exists.").build();
			}

			int userRole = Extra.roleToInt(at.role);

			int userToChangeRole = Extra.roleToInt(userToChangeState.getString("user_role"));

			if (userRole == 0) {
				LOG.warning("user without role " + username);
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}

			if (userToChangeRole == 0) {
				LOG.warning("user without role " + targetUsername);
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}

			if (userToChangeRole >= userRole) {
				LOG.warning(username + " wrong change state request for user " + targetUsername);
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).build();
			}
			long state = userToChangeState.getLong("user_state");
			state = 1 - state;
			userToChangeState = Entity.newBuilder(userToChangeState).set("user_state", state).build();

			txn.update(userToChangeState);
			txn.commit();
			return Response.ok().build();

		} finally {
			if (txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}

	}

	// test only
	@GET
	@Path("/user/{username}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getUser(@PathParam("username") String username, @Context HttpHeaders headers) {

		if (!Extra.checkToken(headers.getHeaderString("token"), username)) {
			LOG.warning("token not valid for user " + username);
			return Response.status(Status.BAD_REQUEST).build();
		}

		Transaction txn = datastore.newTransaction();

		try {
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(username);
			Entity user = txn.get(userKey);
			if (user == null) {
				txn.rollback();
				LOG.warning("user does not exist " + username);
				return Response.status(Status.BAD_REQUEST).build();
			}
			LOG.info("User geted " + username);
			txn.commit();
			return Response.ok(g.toJson(user)).build();

		} finally {
			if (txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}

	}

	// error
	@PUT
	@Path("/uploadPhoto/{filename}")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	public Response uploadPhoto(@PathParam("filename") String filename, @Context HttpServletRequest req,
			@Context HttpHeaders headers) throws IOException {

		LOG.warning("uploadddddddddddddddddddddddddd" + req.getContentType());

		String token = headers.getHeaderString("token");

		AuthToken at = Extra.AuthTokenDecode(token);

		String username = at.username;

		Storage storage = StorageOptions.getDefaultInstance().getService();
		BlobId blobId = BlobId.of("iconic-valve-379315.appspot.com", filename);
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
				.setAcl(Collections.singletonList(Acl.newBuilder(Acl.User.ofAllUsers(), Acl.Role.READER).build()))
				.setContentType(req.getContentType()).build();

		Transaction txn = datastore.newTransaction();

		try {
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(username);
			Entity user = txn.get(userKey);
			if (user == null) {
				txn.rollback();
				LOG.warning("user does not exist " + username);
				return Response.status(Status.BAD_REQUEST).build();
			}

			user = Entity.newBuilder(user).set("user_photo", filename).build();

			txn.update(user);
			storage.create(blobInfo, req.getInputStream());
			txn.commit();
			return Response.ok().build();

		} finally {
			if (txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}

	}
}
