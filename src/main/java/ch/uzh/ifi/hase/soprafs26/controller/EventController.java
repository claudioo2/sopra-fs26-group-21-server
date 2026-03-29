package ch.uzh.ifi.hase.soprafs26.controller;


import ch.uzh.ifi.hase.soprafs26.entity.Event;
import ch.uzh.ifi.hase.soprafs26.rest.dto.EventGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.EventPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.EventService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events")
public class EventController {
    private final EventService eventService;

    EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public EventGetDTO createEvent(@RequestHeader("Authorization") String token, @RequestBody EventPostDTO eventPostDTO){
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Event eventData = DTOMapper.INSTANCE.convertEventGetDTOtoEntity(eventPostDTO);
        Event newEvent = eventService.createEvent(eventData, token);
        return DTOMapper.INSTANCE.convertEntityToEventGetDTO(newEvent);
    }


}
