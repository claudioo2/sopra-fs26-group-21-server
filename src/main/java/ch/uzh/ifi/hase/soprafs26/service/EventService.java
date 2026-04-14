package ch.uzh.ifi.hase.soprafs26.service;
import ch.uzh.ifi.hase.soprafs26.constant.EventCategory;
import ch.uzh.ifi.hase.soprafs26.entity.Event;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
//import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import ch.uzh.ifi.hase.soprafs26.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.List;

//import org.slf4j.Logger;


//import ch.uzh.ifi.hase.soprafs26.repository.EventRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Service
@Transactional
public class EventService {
    //private final Logger log = LoggerFactory.getLogger(EventService.class);

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
        if (newEvent.getEndTime() != null && newEvent.getStartTime() != null
                && !newEvent.getEndTime().isAfter(newEvent.getStartTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time");
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

        String inviteCode;
        do {
            inviteCode = generateInviteCode();
        } while (eventRepository.findByInviteCode(inviteCode) != null); // ensure invite code is unique
        newEvent.setInviteCode(inviteCode);
        
        return eventRepository.save(newEvent);
    } 

    public Event getEventById(Long id) {
        Event event = eventRepository.findById((long) id);
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
        }
        return event;
    }

    public List<Event> getEventsInRadius(double latitude, double longitude, double radiusKm, String token, Set<EventCategory> categories) {
        User currentUser = userRepository.findByToken(token);
        List<Event> allEvents = eventRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        return allEvents.stream()
            .filter(event -> {
                double distance = calculateDistance(latitude, longitude, event.getLatitude(), event.getLongitude());
                return distance <= radiusKm;
            })
            .filter(event -> event.getEndTime().isAfter(now))
            .filter(event -> {
                if (!event.getIsPrivate()) return true;
                if (currentUser == null) return false;
                return event.getParticipants() != null && event.getParticipants().contains(currentUser);
            })
            .filter(event -> categories == null || categories.isEmpty() || categories.contains(event.getCategory()))
            .toList();
    }

    public Event joinEventById(Long eventId, Long userId) {
        User userFromId = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Event event = eventRepository.findById((long) eventId);
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
        }
        if (event.getIsPrivate()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot join a private event without an invite code");
        }
        if (event.getParticipants().contains(userFromId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already joined the event");
        }
        event.getParticipants().add(userFromId);
        return eventRepository.save(event);
    }

    public Event joinEventByInviteCode(String inviteCode, Long userId) {
        User userFromId = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        
        Event event = eventRepository.findByInviteCode(inviteCode);
        if (event == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
        }
        if (event.getParticipants().contains(userFromId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already joined the event");
        }
        event.getParticipants().add(userFromId);
        return eventRepository.save(event);
    }


    /// helper functions ///
    
    // Haversine Formula to calculate distance between two points on the Earth (needed in getEventsInRadius)
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    // helper function to generate random invite code for events (used in createEvent)
    private String generateInviteCode() {
        // Generate a random alphanumeric string of length 8
        int length = 8;
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder inviteCode = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * characters.length());
            inviteCode.append(characters.charAt(index));
        }
        return inviteCode.toString();
    }


}
