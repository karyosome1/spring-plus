package org.example.expert.domain.log.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.expert.domain.common.entity.Timestamped;
import org.example.expert.domain.log.enums.LogStatus;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "log")
public class Log extends Timestamped {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String requestUrl;
    private Long requestUserId;
    private Long savedManagerId;
    private Long todoId;

    @Enumerated(EnumType.STRING)
    private LogStatus status;

    public Log(String requestUrl, Long requestUserId, Long savedManagerId, Long todoId, LogStatus status) {
        this.requestUrl = requestUrl;
        this.requestUserId = requestUserId;
        this.savedManagerId = savedManagerId;
        this.todoId = todoId;
        this.status = status;
    }
}
