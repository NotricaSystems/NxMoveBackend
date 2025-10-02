package com.next.move.repository;

import com.next.move.enums.GoalStatus;
import com.next.move.models.Goals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goals, Long> {

    @Query("SELECT g FROM Goals g " +
            "JOIN FETCH g.userProfile u " +
            "WHERE g.status = :status")
    List<Goals> retrieveGoals(@Param("status") int status);
}
