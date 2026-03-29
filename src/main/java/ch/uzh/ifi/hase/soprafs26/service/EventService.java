
    package ch.uzh.ifi.hase.soprafs26.service;

    import ch.uzh.ifi.hase.soprafs26.entity.Event;
    import ch.uzh.ifi.hase.soprafs26.entity.User;
    import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
    import org.slf4j.LoggerFactory;
    import org.springframework.http.HttpStatus;
    import org.springframework.stereotype.Service;

    import ch.uzh.ifi.hase.soprafs26.repository.EventRepository;

    import org.slf4j.Logger;


    //import ch.uzh.ifi.hase.soprafs26.repository.EventRepository;
    import jakarta.transaction.Transactional;
    import org.springframework.beans.factory.annotation.Qualifier;
    import org.springframework.web.server.ResponseStatusException;

    import java.util.ArrayList;
    import java.util.List;


    @Service
    @Transactional
    public class EventService {
        private final Logger log = LoggerFactory.getLogger(EventService.class);

        private final EventRepository eventRepository;
        private final UserRepository userRepository;

        public EventService(@Qualifier("eventRepository") EventRepository eventRepository, @Qualifier("userRepository") UserRepository userRepository) {
            this.eventRepository = eventRepository;
            this.userRepository = userRepository;
        }


        public Event createEvent(Event newEvent, String token) {
            User userFromToken = userRepository.findByToken(token);
            if (userFromToken == null){
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid token");
            }
            List<User> participants = new ArrayList<>();
            // add creator to participants list
            participants.add(userFromToken);

            // get the participants that are already sent in the request
            List<User> existingParticipants = newEvent.getParticipants();
            if (existingParticipants != null && !existingParticipants.isEmpty()){
                participants.addAll(existingParticipants);
            }

            // set participants to be all the participants including the creator
            newEvent.setParticipants(participants);
            newEvent.setCreator(userFromToken);
            return eventRepository.save(newEvent);
        }
    }
