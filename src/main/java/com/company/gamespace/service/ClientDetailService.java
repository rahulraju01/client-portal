package com.company.gamespace.service;

import com.company.gamespace.dto.MonthlyRevenueDto;
import com.company.gamespace.dto.TotalRevenueStatistics;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ClientDetailService {
    @Autowired
    private EntityManager entityManager;

    private List<MonthlyRevenueDto> getMonthlyRevenue() {
        List<Object[]> results = entityManager.createNativeQuery("""
                        SELECT MONTH(entry_time) AS month, SUM(final_cost) AS total_revenue
                        FROM client_details
                        WHERE entry_time IS NOT NULL
                          AND YEAR(entry_time) = YEAR(CURDATE())
                        GROUP BY MONTH(entry_time)
                        ORDER BY month
                        """)
                .getResultList();

        return results.stream()
                .map(row -> MonthlyRevenueDto.builder()
                        .month(((Number) row[0]).intValue())
                        .totalRevenue((BigDecimal) row[1])
                        .build())
                .toList();
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
