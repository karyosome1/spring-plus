package org.example.expert.domain.log.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.log.entity.Log;
import org.example.expert.domain.log.enums.LogStatus;
import org.example.expert.domain.log.repository.LogRepository;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;
    private final HttpServletRequest request;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog(AuthUser authUser, long todoId, ManagerSaveRequest managerSaveRequest, LogStatus status) {
        String requestUrl = request.getRequestURI();
        Log log = new Log(requestUrl, todoId, authUser.getId(), managerSaveRequest.getManagerUserId(), status);
        logRepository.save(log);
    }
}
