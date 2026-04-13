package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Message;
import ch.uzh.ifi.hase.soprafs26.rest.dto.MessageGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.MessagePostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.MessageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService){
        this.messageService = messageService;
    }

    @MessageMapping("/chat/{eventId}")
    @SendTo("/topic/chat/{eventId}")
    public MessageGetDTO sendMessage(MessagePostDTO messagePostDTO){
        Message message = messageService.saveMessage((messagePostDTO));
        return DTOMapper.INSTANCE.convertEntityToMessageGetDTO(message);
    }

    @GetMapping("/events/{eventId}/messages")
    public List<MessageGetDTO> getChatHistory(@PathVariable Long eventId, @RequestHeader("Authorization") String token){
        if(token != null && token.startsWith("Bearer ")){
            token = token.substring(7);
        }
        List<MessageGetDTO> result = new ArrayList<>();
        for (Message m : messageService.getChatHistory(eventId, token)) {
            result.add(DTOMapper.INSTANCE.convertEntityToMessageGetDTO(m));
        }
        return result;
    }
}
