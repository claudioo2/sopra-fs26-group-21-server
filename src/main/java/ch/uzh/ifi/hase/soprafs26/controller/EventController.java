package ch.uzh.ifi.hase.soprafs26.controller;


import ch.uzh.ifi.hase.soprafs26.constant.EventCategory;
import ch.uzh.ifi.hase.soprafs26.entity.Event;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.EventGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.EventJoinByCodePostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.EventJoinByIdPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.EventPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.service.EventService;
import ch.uzh.ifi.hase.soprafs26.service.ParticipantService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events")
public class EventController {
    private final EventService eventService;

    private final ParticipantService participantService;

    private final UserService userService;

    private final UserRepository userRepository;

    EventController(EventService eventService, ParticipantService participantService, UserService userService, UserRepository userRepository) {
        this.eventService = eventService;
        this.participantService = participantService;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public EventGetDTO createEvent(@RequestHeader("Authorization") String token, @Valid @RequestBody EventPostDTO eventPostDTO){
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Event eventData = DTOMapper.INSTANCE.convertEventPostDTOtoEntity(eventPostDTO);
        Event newEvent = eventService.createEvent(eventData, token);
        return DTOMapper.INSTANCE.convertEntityToEventGetDTO(newEvent);
    }

    // Join event by eventId (meant for joins through event search)
    @PostMapping("/{eventId}/participants")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public EventGetDTO joinEventById(@RequestHeader("Authorization") String authHeader, @PathVariable Long eventId, @RequestBody EventJoinByIdPostDTO eventJoinByIdPostDTO) {
        String token = authHeader.replace("Bearer ", "");
        userService.validateToken(token);
        Event event = participantService.joinEventById(eventId, eventJoinByIdPostDTO.getUserId());
        return DTOMapper.INSTANCE.convertEntityToEventGetDTO(event);
    }

    // Join event by inviteCode (meant for joins through invitations)
    @PostMapping("/participants")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public EventGetDTO joinEventBynviteCode(@RequestHeader("Authorization") String authHeader, @RequestBody EventJoinByCodePostDTO eventJoinByCodePostDTO) {
        String token = authHeader.replace("Bearer ", "");
        userService.validateToken(token);
        Event event = participantService.joinEventByInviteCode(eventJoinByCodePostDTO.getInviteCode(), eventJoinByCodePostDTO.getUserId());
        return DTOMapper.INSTANCE.convertEntityToEventGetDTO(event);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EventGetDTO getEventById(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        String token = authHeader.replace("Bearer ", "");
        userService.validateToken(token);
        Event event = eventService.getEventById(id);
        return DTOMapper.INSTANCE.convertEntityToEventGetDTO(event);
    }

    @GetMapping
	@ResponseStatus(HttpStatus.OK)
	public List<EventGetDTO> getRadiusEvents(
        @RequestHeader("Authorization") String authHeader,
        @RequestParam double latitude,
        @RequestParam double longitude,
        @RequestParam double radius,
        @RequestParam(required = false) Set<EventCategory> categories) {
		String token = authHeader.replace("Bearer ", "");
		userService.validateToken(token);

        List<Event> events = eventService.getEventsInRadius(latitude, longitude, radius, token, categories);
        List<EventGetDTO> eventGetDTOs = new ArrayList<>();
        User currentUser = userRepository.findByToken(token);

        for (Event event : events) {
            EventGetDTO dto = DTOMapper.INSTANCE.convertEntityToEventGetDTO(event);
            boolean isParticipant = event.getParticipants() != null && currentUser != null &&
                event.getParticipants().stream()
                    .anyMatch(u -> u.getId().equals(currentUser.getId()));
            dto.setIsParticipant(isParticipant);
            eventGetDTOs.add(dto);
		}

		return eventGetDTOs;

	}

}
