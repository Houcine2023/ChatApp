package com.SummerProject.Skote.Controllers;

import com.SummerProject.Skote.DTO.ChatMessageDTO;
import com.SummerProject.Skote.DTO.MessageRequest;
import com.SummerProject.Skote.DTO.ModifyMessageRequest;
import com.SummerProject.Skote.Services.ChatServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = "http://localhost:4200")
@Controller
public class WebSocketChatController {

    @Autowired
    private ChatServiceImpl chatService;

    @MessageMapping("/sendGroupMessage")
    public void sendGroupMessage(@Payload MessageRequest request) {
        // Backend handles UUID conversion and validation
        ChatMessageDTO sentMessage = chatService.sendGroupMessage(request);

        // Send message to dynamic destination based on chatId
        chatService.getSimpMessagingTemplate()
                .convertAndSend("/topic/chat/" + request.getChatId(), sentMessage);
    }

    @MessageMapping("/modifyMessage")
    public void modifyGroupMessage(@Payload ModifyMessageRequest request) {
        ChatMessageDTO modifiedMessage = chatService.modifyMessage(request);
        chatService.getSimpMessagingTemplate()
                .convertAndSend("/topic/chat/" + request.getChatId(), modifiedMessage);
    }

    // Optional: You can also add a delete message mapping if needed
}
