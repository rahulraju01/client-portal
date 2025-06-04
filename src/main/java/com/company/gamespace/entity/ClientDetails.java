package com.company.gamespace.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@JmixEntity
@Table(name = "Client_Details")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientDetails {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;
    @InstanceName
    @Column(name = "FIRST_NAME")
    private String firstName;
    @Column(name = "LAST_NAME")
    private String lastName;
    @Column(name = "ENTRY_TIME")
    private OffsetDateTime entryTime;
    @Column(name = "EXIT_TIME")
    private OffsetDateTime exitTime;
    @Column(name = "FINAL_COST")
    private BigDecimal finalCost;
    @Column(name = "TOTAL_HOURS")
    private BigDecimal totalHours;
    @CreatedBy
    @Column(name = "CREATED_BY")
    private String createdBy;
    @CreatedDate
    @Column(name = "CREATED_DATE")
    private OffsetDateTime createdDate;
    @LastModifiedBy
    @Column(name = "LAST_MODIFIED_BY")
    private String lastModifiedBy;
    @LastModifiedDate
    @Column(name = "LAST_MODIFIED_DATE")
    private OffsetDateTime lastModifiedDate;
}