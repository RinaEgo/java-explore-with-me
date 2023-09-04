package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.StatsDto;
import ru.practicum.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<Hit, Long> {

    @Query("select new ru.practicum.StatsDto(" +
            "   h.app, " +
            "   h.uri, " +
            "   case when :unique = true " +
            "       then count(distinct(h.ip)) " +
            "       else count(h.ip) " +
            "   end " +
            ") " +
            "from Hit h " +
            "where h.timestamp between :start and :end" +
            "   and (coalesce(:uris, null) is null or h.uri in :uris) " +
            "group by h.app, h.uri " +
            "order by 3 desc")
    List<StatsDto> getStats(LocalDateTime start,
                            LocalDateTime end,
                            List<String> uris,
                            Boolean unique);
}
