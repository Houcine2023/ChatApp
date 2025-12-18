package com.SummerProject.Skote.Repositories;

import com.SummerProject.Skote.models.Chat;
import com.SummerProject.Skote.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ChatRepo extends JpaRepository<Chat, UUID> {
}
