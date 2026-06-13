package com.example._ayelcommunitybe.service.listener;

import com.example._ayelcommunitybe.event.PostViewedEvent;
import com.example._ayelcommunitybe.service.PostViewCountService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PostViewedEventListener {

    private final PostViewCountService postViewCountService;

    @EventListener
    public void handle(PostViewedEvent event) {
        postViewCountService.increase(event.postId());
    }
}