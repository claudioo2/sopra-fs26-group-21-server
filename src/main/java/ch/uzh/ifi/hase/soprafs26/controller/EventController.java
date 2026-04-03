package ch.uzh.ifi.hase.soprafs26.controller;


import ch.uzh.ifi.hase.soprafs26.entity.Event;
import ch.uzh.ifi.hase.soprafs26.rest.dto.EventGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.EventPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.EventService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events")
public class EventController {
    private final EventService eventService;

    private final UserService userService;

    EventController(EventService eventService, UserService userService) {
        this.eventService = eventService;
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public EventGetDTO createEvent(@RequestHeader("Authorization") String token, @Valid @RequestBody EventPostDTO eventPostDTO){
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Event eventData = DTOMapper.INSTANCE.convertEventGetDTOtoEntity(eventPostDTO);
        Event newEvent = eventService.createEvent(eventData, token);
        return DTOMapper.INSTANCE.convertEntityToEventGetDTO(newEvent);
    }

    @GetMapping
	@ResponseStatus(HttpStatus.OK)
	public List<EventGetDTO> getRadiusEvents(
        @RequestHeader("Authorization") String authHeader, 
        @RequestParam double latitude, 
        @RequestParam double longitude, 
        @RequestParam double radius) {
		String token = authHeader.replace("Bearer ", "");
		userService.validateToken(token);
		
        List<Event> events = eventService.getEventsInRadius(latitude, longitude, radius);
        List<EventGetDTO> eventGetDTOs = new ArrayList<>();

        for (Event event : events) {
			eventGetDTOs.add(DTOMapper.INSTANCE.convertEntityToEventGetDTO(event));
		}

		return eventGetDTOs;

	}

}
