
package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.uzh.ifi.hase.soprafs26.repository.EventRepository;

import org.slf4j.Logger;


//import ch.uzh.ifi.hase.soprafs26.repository.EventRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;


@Service
@Transactional
public class EventService {
    private final Logger log = LoggerFactory.getLogger(UserService.class);

	private final EventRepository eventRepository;

    public EventService(@Qualifier("eventRepository") EventRepository eventRepository) {
		this.eventRepository = eventRepository;
	}

    
}
