package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.web.bind.annotation.RestController;

import ch.uzh.ifi.hase.soprafs26.service.EventService;

@RestController
public class EventController {
    private final EventService eventService;

    EventController(EventService eventService) {
        this.eventService = eventService;
    }


}
