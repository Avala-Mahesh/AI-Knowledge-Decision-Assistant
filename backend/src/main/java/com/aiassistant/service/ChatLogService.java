package com.aiassistant.service;

import com.aiassistant.entity.ChatLog;
import com.aiassistant.entity.Organization;
import com.aiassistant.repository.ChatLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatLogService {

    private static final Logger logger = LoggerFactory.getLogger(ChatLogService.class);

    private final ChatLogRepository chatLogRepository;

    public ChatLogService(ChatLogRepository chatLogRepository) {
        this.chatLogRepository = chatLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordChatLog(Organization org, String question, String answer, String status,
                              String questionType, boolean decisionUsed, String summary, long timeMs) {
        try {
            ChatLog log = new ChatLog(org, question, answer, status, questionType, decisionUsed, summary, timeMs);
            chatLogRepository.save(log);
            logger.debug("Successfully saved chat log for Org ID: {}", org.getId());
        } catch (Exception e) {
            logger.error("Failed to persist chat log in isolated transaction: {}", e.getMessage());
        }
    }
}
