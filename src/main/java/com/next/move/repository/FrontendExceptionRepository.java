package com.next.move.repository;

import com.next.move.models.FrontendException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FrontendExceptionRepository extends JpaRepository<FrontendException, Long> {
}
