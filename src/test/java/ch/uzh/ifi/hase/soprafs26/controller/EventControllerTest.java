package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.constant.EventCategory;
import ch.uzh.ifi.hase.soprafs26.entity.Event;
import ch.uzh.ifi.hase.soprafs26.service.EventService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import ch.uzh.ifi.hase.soprafs26.service.ParticipantService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
//import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
//import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.EventRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * EntryControllerTest
 * This is a WebMvcTest which allows to test the EntryController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the EntryController works.
 */
@WebMvcTest(EventController.class)
@AutoConfigureMockMvc(addFilters = false)
public class EventControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private EventService eventService;

	@MockitoBean
	private EventRepository eventRepository;

	@MockitoBean
	private UserService userService;

	@MockitoBean
	private UserRepository userRepository;

	@MockitoBean
	private ParticipantService participantService;

	@Test
	public void givenEvents_whenGetEvents_thenReturnJsonArray() throws Exception {
		// given
		Event event = new Event();
		event.setTitle("Test Event");
		event.setDescription("Test Event");
		event.setIsPrivate(false);
		event.setLatitude(47.3769);
		event.setLongitude(8.5417);

		List<Event> allEvents = Collections.singletonList(event);

		Mockito.doNothing().when(userService).validateToken(Mockito.anyString());

		// this mocks the EventService -> we define above what the eventService should
		// return when getEventsInRadius() is called
		given(eventService.getEventsInRadius(Mockito.anyDouble(), Mockito.anyDouble(), Mockito.anyDouble(), Mockito.anyString(), Mockito.<Set<EventCategory>>any()))
			.willReturn(allEvents);

		// when
		MockHttpServletRequestBuilder getRequest = get("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer test-token")
                .param("latitude", "47.3769")
                .param("longitude", "8.5417")
                .param("radius", "20");

		// then
		mockMvc.perform(getRequest).andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].title", is(event.getTitle())))
				.andExpect(jsonPath("$[0].description", is(event.getDescription())))
				.andExpect(jsonPath("$[0].isPrivate", is(event.getIsPrivate())))
				.andExpect(jsonPath("$[0].latitude", is(event.getLatitude())));
	}

	@Test
	public void getEventById_whenExists_returnsEvent() throws Exception {
		User creator = new User();
		creator.setUsername("alice");

		Event event = new Event();
		event.setTitle("Test Event");
		event.setDescription("A description");
		event.setIsPrivate(false);
		event.setLatitude(47.3769);
		event.setLongitude(8.5417);
		event.setCreator(creator);
		event.setParticipants(List.of(creator));

		Mockito.doNothing().when(userService).validateToken(Mockito.anyString());
		given(eventService.getEventById(1L)).willReturn(event);

		MockHttpServletRequestBuilder getRequest = get("/events/1")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer test-token");

		mockMvc.perform(getRequest)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title", is(event.getTitle())))
				.andExpect(jsonPath("$.description", is(event.getDescription())))
				.andExpect(jsonPath("$.creatorUsername", is("alice")))
				.andExpect(jsonPath("$.participantCount", is(1)));
	}

	@Test
	public void getEventById_whenNotFound_returns404() throws Exception {
		Mockito.doNothing().when(userService).validateToken(Mockito.anyString());
		given(eventService.getEventById(99L))
				.willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

		MockHttpServletRequestBuilder getRequest = get("/events/99")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer test-token");

		mockMvc.perform(getRequest).andExpect(status().isNotFound());
	}

	// POST /events/{eventId}/participants
	@Test
	public void joinEventById_whenEventPublic_returns201() throws Exception {
		User user = new User();
		user.setId(1L);
		user.setUsername("alice");

		Event event = new Event();
		event.setId(1L);
		event.setTitle("Public Event");
		event.setIsPrivate(false);
		event.setParticipants(List.of());

		Mockito.doNothing().when(userService).validateToken(Mockito.anyString());

		given(participantService.joinEventById(1L, 1L)).willReturn(event);

		MockHttpServletRequestBuilder postRequest = post("/events/1/participants")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer test-token")
				.content("{\"userId\": 1}");

		mockMvc.perform(postRequest).andExpect(status().isCreated())
				.andExpect(jsonPath("$.title", is(event.getTitle())))
				.andExpect(jsonPath("$.isPrivate", is(event.getIsPrivate())))
				.andExpect(jsonPath("$.participantCount", is(0)));
	}


	@Test 
	public void joinEventById_whenEventIsPrivate_returns403() throws Exception {
		User user = new User();
		user.setId(1L);
		user.setUsername("alice");

		Event event = new Event();
		event.setId(1L);
		event.setTitle("Private Event");
		event.setIsPrivate(true);
		event.setParticipants(List.of());

		Mockito.doNothing().when(userService).validateToken(Mockito.anyString());

		given(participantService.joinEventById(1L, 1L))
			.willThrow(new ResponseStatusException(
				HttpStatus.FORBIDDEN,
				"Cannot join a private event without an invite code"
			));

		MockHttpServletRequestBuilder postRequest = post("/events/1/participants")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer test-token")
				.content("{\"userId\": 1}");

		mockMvc.perform(postRequest).andExpect(status().isForbidden());
	}

	@Test
	public void joinEventByinviteCode_whenEventIsPrivate_returns201() throws Exception {
		User user = new User();
		user.setId(1L);
		user.setUsername("alice");

		Event event = new Event();
		event.setId(1L);
		event.setTitle("Private Event");
		event.setIsPrivate(true);
		event.setParticipants(List.of());

		Mockito.doNothing().when(userService).validateToken(Mockito.anyString());

		given(participantService.joinEventByInviteCode("invite-code", 1L)).willReturn(event);

		MockHttpServletRequestBuilder postRequest = post("/events/participants")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer test-token")
				.content("{\"inviteCode\": \"invite-code\", \"userId\": 1}");

		mockMvc.perform(postRequest).andExpect(status().isCreated())
				.andExpect(jsonPath("$.title", is(event.getTitle())))
				.andExpect(jsonPath("$.isPrivate", is(event.getIsPrivate())))
				.andExpect(jsonPath("$.participantCount", is(0)));
	}

	/**
	 * Helper Method to convert userPostDTO into a JSON string such that the input
	 * can be processed
	 * Input will look like this: {"name": "Test User", "username": "testUsername"}
	 * 
	 * @param object
	 * @return string
	 */
	// private String asJsonString(final Object object) {
	// 	try {
	// 		return new ObjectMapper().writeValueAsString(object);
	// 	} catch (JacksonException e) {
	// 		throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
	// 				String.format("The request body could not be created.%s", e.toString()));
	// 	}
	// }
}