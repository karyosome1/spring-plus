package org.example.expert.domain.todo.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.comment.entity.QComment;
import org.example.expert.domain.manager.entity.QManager;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.user.entity.QUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TodoRepositoryCustomImpl implements TodoRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {
        QTodo todo = QTodo.todo;
        QUser user = QUser.user;

        Todo result = queryFactory
                .selectFrom(todo)
                .leftJoin(todo.user, user).fetchJoin() // N+1 문제 방지
                .where(todo.id.eq(todoId))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Page<TodoSearchResponse> searchTodos(String keyword, String nickname, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        QTodo todo = QTodo.todo;
        QUser user = QUser.user;
        QComment comment = QComment.comment;
        QManager manager = QManager.manager;

        BooleanBuilder builder = new BooleanBuilder();

        builder.and(keywordContains(keyword));
        builder.and(nicknameContains(nickname));
        builder.and(createdDateBetween(startDate, endDate));

        List<TodoSearchResponse> results = queryFactory
                .select(Projections.constructor(TodoSearchResponse.class,
                        todo.id,
                        todo.title,
                        manager.countDistinct(),
                        comment.countDistinct()
                ))
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(todo.comments, comment)
                .leftJoin(todo.user, user)
                .where(builder)
                .groupBy(todo.id)
                .orderBy(todo.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(todo.count())
                .from(todo)
                .where(builder)
                .fetchOne();

        total = (total != null) ? total : 0L;

        return new PageImpl<>(results, pageable, total);
    }

    private BooleanExpression keywordContains(String keyword) {
        return StringUtils.hasText(keyword) ? QTodo.todo.title.containsIgnoreCase(keyword) : Expressions.TRUE;
    }

    private BooleanExpression nicknameContains(String nickname) {
        return StringUtils.hasText(nickname) ? QUser.user.nickname.containsIgnoreCase(nickname) : Expressions.TRUE;
    }

    private BooleanExpression createdDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null) {
            return QTodo.todo.createdAt.between(startDate, endDate);
        } else if (startDate != null) {
            return QTodo.todo.createdAt.goe(startDate);
        } else if (endDate != null) {
            return QTodo.todo.createdAt.loe(endDate);
        }
        return Expressions.TRUE;
    }

}
