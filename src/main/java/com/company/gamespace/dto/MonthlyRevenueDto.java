package com.company.gamespace.dto;

import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Setter @Getter
@Builder
public class MonthlyRevenueDto {
    private Integer month;
    private BigDecimal totalRevenue;
}
