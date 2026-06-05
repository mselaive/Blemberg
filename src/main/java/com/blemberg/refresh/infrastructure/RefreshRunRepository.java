package com.blemberg.refresh.infrastructure;

import com.blemberg.refresh.domain.RefreshRun;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshRunRepository extends JpaRepository<RefreshRun, UUID> {

    Optional<RefreshRun> findTopByOrderByStartedAtDesc();

    List<RefreshRun> findAllByOrderByStartedAtDesc(Pageable pageable);
}
