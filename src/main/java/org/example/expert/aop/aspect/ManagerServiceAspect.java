package org.example.expert.aop.aspect;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.log.enums.LogStatus;
import org.example.expert.domain.log.service.LogService;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class ManagerServiceAspect {

    private final LogService logService;

    @Pointcut("@annotation(org.example.expert.aop.annotation.AopTarget)")
    public void aopTargetMethods() {}

    @Around("aopTargetMethods()")
    public Object handleSaveManager(ProceedingJoinPoint joinPoint) throws Throwable {
        AuthUser authUser = (AuthUser) joinPoint.getArgs()[0];
        long todoId = (long) joinPoint.getArgs()[1];
        ManagerSaveRequest managerSaveRequest =(ManagerSaveRequest) joinPoint.getArgs()[2];
        LogStatus status = LogStatus.SUCCESS;

        try {
            return joinPoint.proceed();
        } catch (InvalidRequestException e) {
            status = LogStatus.FAILURE;
            throw e;
        } finally {
            logService.saveLog(authUser, todoId, managerSaveRequest, status);
        }
    }
}
