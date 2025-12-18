package com.SummerProject.Skote.Controllers;



import com.SummerProject.Skote.Services.PresenceService;
import com.SummerProject.Skote.models.UserPresence;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;


@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/presence")
public class PresenceController {

    private final PresenceService presenceService;

    public PresenceController(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @GetMapping
    public Map<UUID, UserPresence> getAllPresence() {
        return presenceService.getAllUsersPresence();
    }
}

