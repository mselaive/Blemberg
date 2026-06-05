package com.blemberg.refresh.infrastructure;

import com.blemberg.refresh.domain.RefreshRunItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RefreshRunItemRepository extends JpaRepository<RefreshRunItem, Long> {

    List<RefreshRunItem> findByRunIdOrderByStartedAtAscIdAsc(UUID runId);
}
