package com.bankapp.accountservice.adapters.out.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface InboxJpaRepository extends JpaRepository<InboxJpaEntity, UUID> {


    @Modifying
    @Transactional
    @Query(
            value = """
                    INSERT INTO inbox (message_id, event_timestamp, received_at)
                    VALUES (:message_id, :event_timestamp, :received_at)
                    ON CONFLICT (message_id) DO NOTHING
                    """,
            nativeQuery = true
    )

    int insertIfNotExists(@Param("message_id") UUID messageId,
                          @Param("event_timestamp") LocalDateTime eventTimestamp,
                          @Param("received_at") LocalDateTime receivedAt
    );
}
