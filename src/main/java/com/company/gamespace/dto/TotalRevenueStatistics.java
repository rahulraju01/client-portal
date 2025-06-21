package com.company.gamespace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class TotalRevenueStatistics {
    private UUID id;
    private String month;
    private BigDecimal totalRevenue;
}
