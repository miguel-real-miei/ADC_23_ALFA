package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.UUID;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Response.Status;

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.EventCreationData;
import pt.unl.fct.di.apdc.firstwebapp.util.Extra;

@Path("/events")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class EventsResource {

	private static final Logger LOG = Logger.getLogger(AuthResource.class.getName());

	private final Gson g = new Gson();

	// private final Datastore datastore =
	// DatastoreOptions.getDefaultInstance().getService();
	private final Datastore datastore = DatastoreOptions.newBuilder().setHost("http://localhost:8081")
			.setProjectId("iconic-valve-379315").build().getService();

	public EventsResource() {

	}

	@POST
	@Path("/create")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createEvent(EventCreationData data, @Context SecurityContext secContext) {

		String username = secContext.getUserPrincipal().getName();
		String role = secContext.getAuthenticationScheme();

		LOG.fine("Creating a event by " + username);

		if (Extra.roleToInt(role) != 1) {
			Transaction txn = datastore.newTransaction();

			String id = DigestUtils.sha256Hex(UUID.randomUUID().toString() + data.name + data.location + data.date);
			try {
				Key eventKey = datastore.newKeyFactory().setKind("Events").newKey(id);
				Entity event = txn.get(eventKey);
				if (event != null) {
					txn.rollback();
					LOG.severe("Event with same id");
					return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Try again").build();
				} else {
					event = Entity.newBuilder(eventKey).set("event_owner", username).set("event_name", data.name)
							.set("event_type", data.type).set("event_date", data.date).set("event_time", data.time)
							.set("event_duration", data.duration).set("event_description", data.description)
							.set("event_local", data.location).build();

					txn.add(event);

					LOG.info("Event registered " + data.name);
					txn.commit();
					return Response.ok(g.toJson(id)).build();
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
		} else {
			return Response.status(Status.METHOD_NOT_ALLOWED).build();
		}

	}

	@DELETE
	@Path("/delete/{eventId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createEvent(@PathParam("eventId") String eventId, @Context SecurityContext secContext) {

		String username = secContext.getUserPrincipal().getName();
		String role = secContext.getAuthenticationScheme();

		LOG.fine("deleting a event " + eventId + " by " + username);

		Key eventKey = datastore.newKeyFactory().setKind("Events").newKey(eventId);

		Transaction txn = datastore.newTransaction();

		try {
			Entity event = txn.get(eventKey);
			if (event == null) {
				txn.rollback();
				LOG.severe("Event does not exist");
				return Response.status(Status.NOT_FOUND).build();
			}

			int roleI = Extra.roleToInt(role);
			if (roleI == 1) {
				return Response.status(Status.BAD_REQUEST).build();
			}
			if (!event.getString("event_owner").equals(username) && roleI <= 3) {
				return Response.status(Status.BAD_REQUEST).build();
			}

			txn.delete(eventKey);

			txn.commit();
			return Response.ok().build();

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
	@Path("/signUp/{eventId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response signForEvent(@PathParam("eventId") String eventId, @Context SecurityContext secContext) {

		String username = secContext.getUserPrincipal().getName();
		String role = secContext.getAuthenticationScheme();

		LOG.fine("deleting a event " + eventId + " by " + username);

		Key eventKey = datastore.newKeyFactory().setKind("Events").newKey(eventId);
		
		
		Key participantKey = datastore.newKeyFactory().addAncestor(PathElement.of("Event", eventId)).setKind("Participant")
				.newKey(username);

		Transaction txn = datastore.newTransaction();

		try {
			Entity event = txn.get(eventKey);
			if (event == null) {
				txn.rollback();
				LOG.severe("Event does not exist");
				return Response.status(Status.NOT_FOUND).build();
			}
			
			Entity participant = txn.get(participantKey);
			
			if (participant != null) {
				txn.rollback();
				LOG.severe("Volunteer already in the event");
				return Response.status(Status.BAD_REQUEST).build();
			}
			
			participant = Entity.newBuilder(participantKey).set("participant_signUp_time",  Timestamp.now()).build();
			txn.put(participant);

			txn.commit();
			return Response.ok().build();

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
	@Path("/volunteer/{eventId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response volunteerForEvent(@PathParam("eventId") String eventId, @Context SecurityContext secContext) {

		String username = secContext.getUserPrincipal().getName();
		String role = secContext.getAuthenticationScheme();

		LOG.fine("deleting a event " + eventId + " by " + username);

		Key eventKey = datastore.newKeyFactory().setKind("Events").newKey(eventId);
		
		
		Key volunteerKey = datastore.newKeyFactory().addAncestor(PathElement.of("Event", eventId)).setKind("Volunteer")
				.newKey(username);

		Transaction txn = datastore.newTransaction();

		try {
			Entity event = txn.get(eventKey);
			
			if (event == null) {
				txn.rollback();
				LOG.severe("Event does not exist");
				return Response.status(Status.NOT_FOUND).build();
			}
			
			
			if(role.equals("Visitante")) {
				txn.rollback();
				LOG.severe("Visitante can not volunteer to event");
				return Response.status(Status.BAD_REQUEST).build();
			}
			
			Entity volunteer = txn.get(volunteerKey);
			
			if (volunteer != null) {
				txn.rollback();
				LOG.severe("Volunteer already in the event");
				return Response.status(Status.BAD_REQUEST).build();
			}
		
			
			volunteer = Entity.newBuilder(volunteerKey).set("volunteer_level",  1l).set("volunteer_signUp_time",  Timestamp.now()).build();
			txn.put(volunteer);

			txn.commit();
			return Response.ok().build();

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
	
	@PUT
	@Path("/edit/{eventId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response volunteerForEvent(@PathParam("eventId") String eventId,  EventCreationData data, @Context SecurityContext secContext) {

		String username = secContext.getUserPrincipal().getName();
		String role = secContext.getAuthenticationScheme();

		LOG.fine("deleting a event " + eventId + " by " + username);

		Key eventKey = datastore.newKeyFactory().setKind("Events").newKey(eventId);
		
		
		Key volunteerKey = datastore.newKeyFactory().addAncestor(PathElement.of("Event", eventId)).setKind("Volunteer")
				.newKey(username);

		Transaction txn = datastore.newTransaction();

		try {
			Entity event = txn.get(eventKey);
			if (event == null) {
				txn.rollback();
				LOG.severe("Event does not exist");
				return Response.status(Status.NOT_FOUND).build();
			}
			
			if(role.equals("Visitante")) {
				txn.rollback();
				LOG.severe("Visitante can not volunteer to event");
				return Response.status(Status.BAD_REQUEST).build();
			}
			
			Entity volunteer = txn.get(volunteerKey);
			
			if (volunteer == null) {
				txn.rollback();
				LOG.severe("Volunteer not in the event");
				return Response.status(Status.BAD_REQUEST).build();
			}
			
			if (volunteer.getLong("volunteer_level") <= 1l) {
				txn.rollback();
				LOG.severe("Volunteer does not have clerance to update");
				return Response.status(Status.BAD_REQUEST).build();
			}
			
			data = Extra.extractData(event, data);
			
			event = Entity.newBuilder(eventKey).set("event_owner", username).set("event_name", data.name)
					.set("event_type", data.type).set("event_date", data.date).set("event_time", data.time)
					.set("event_duration", data.duration).set("event_description", data.description)
					.set("event_local", data.location).build();

			txn.update(event);

			LOG.info("Event edited " + data.name);
			txn.commit();
			
			return Response.ok().build();

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
