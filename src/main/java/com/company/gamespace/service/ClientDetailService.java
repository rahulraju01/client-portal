package com.company.gamespace.service;

import com.company.gamespace.dto.MonthlyRevenueDto;
import com.company.gamespace.dto.TotalRevenueStatistics;
import io.jmix.core.DataManager;
import io.jmix.core.entity.KeyValueEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClientDetailService {
    @PersistenceContext
    private EntityManager entityManager;

    public List<MonthlyRevenueDto> getMonthlyRevenue() {

        @SuppressWarnings("unchecked")
        List<Object[]> resultList = entityManager.createNativeQuery("""
                    SELECT\s
                        MONTH(entry_time) AS month,\s
                        SUM(final_cost) AS total_revenue
                    FROM\s
                        client_details
                    WHERE\s
                        entry_time IS NOT NULL\s
                        AND YEAR(entry_time) = YEAR(CURDATE())
                    GROUP BY\s
                        MONTH(entry_time)
                    ORDER BY\s
                        MONTH(entry_time)
               \s""").getResultList();

        return resultList.stream()
                .map(row -> new MonthlyRevenueDto(
                        ((Number) row[0]).intValue(),
                        (BigDecimal) row[1])
                ).toList();
    }

    public List<TotalRevenueStatistics> getTotalRevenueStats() {
        List<TotalRevenueStatistics> totalRevenueStatistics = new ArrayList<>();
        Map<String, BigDecimal> revenueMap = Arrays.stream(Month.values())
                .collect(Collectors.toMap(
                        m -> m.getDisplayName(TextStyle.SHORT, Locale.ENGLISH), // "Jan", "Feb", ...
                        m -> BigDecimal.ZERO,
                        (a, b) -> b,
                        LinkedHashMap::new
                ));

        for (MonthlyRevenueDto dto : getMonthlyRevenue()) {
            String monthName = Month.of(dto.getMonth())
                    .getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

            revenueMap.put(monthName, dto.getTotalRevenue());
        }

        for (Map.Entry<String, BigDecimal> entry : revenueMap.entrySet()) {
            TotalRevenueStatistics statistics = TotalRevenueStatistics.builder()
                    .id(UUID.randomUUID())
                    .month(entry.getKey())
                    .totalRevenue(entry.getValue())
                    .build();
            totalRevenueStatistics.add(statistics);
        }
        return totalRevenueStatistics;
    }

}
