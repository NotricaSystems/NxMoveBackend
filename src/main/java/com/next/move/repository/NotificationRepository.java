package com.next.move.repository;

import com.next.move.enums.NotifType;
import com.next.move.models.Notifications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notifications, Long> {


    @Query("""
              SELECT n
              FROM Notifications n
              WHERE n.phone = :phone
                AND n.sendingDate >= :since
                AND n.usersReply IS NULL
              ORDER BY n.sendingDate DESC
              """)
    List<Notifications> getTheLatestNotification(@Param("phone") String phone,
                                                 @Param("since") Instant since);

    @Query("""
              SELECT n
              FROM Notifications n
              WHERE n.goalId = :goalId
                AND n.sendingDate >= :since
                AND (n.browserNotified IS NULL OR n.browserNotified = false)
              ORDER BY n.sendingDate DESC
              """)
    List<Notifications> notificationsBrowserNotNotified(@Param("goalId") Long goalId,
                                                 @Param("since") Instant since);

    @Query("""
              SELECT n
              FROM Notifications n
              WHERE n.goalId = :goalId
              ORDER BY n.sendingDate DESC
              """)
    List<Notifications> latestNotifications(@Param("goalId") Long goalId,
                                                        Pageable pageable);

}
