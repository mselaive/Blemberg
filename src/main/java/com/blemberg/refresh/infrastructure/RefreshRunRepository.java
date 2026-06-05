package com.blemberg.refresh.infrastructure;

import com.blemberg.refresh.domain.RefreshRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RefreshRunRepository extends JpaRepository<RefreshRun, UUID> {
}
