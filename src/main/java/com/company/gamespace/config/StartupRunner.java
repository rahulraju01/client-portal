package com.company.gamespace.config;

import com.company.gamespace.entity.ClientDetails;
import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class StartupRunner implements CommandLineRunner {

    @Autowired
    private DataManager dataManager;
    @Autowired
    private SystemAuthenticator systemAuthenticator;

    @Override
    public void run(String... args) {
        OffsetDateTime baseTime = OffsetDateTime.now()
                .withDayOfMonth(10)
                .withHour(10)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        int year = baseTime.getYear();

        for (int month = 1; month <= 12; month++) {
            OffsetDateTime entryTime = baseTime.withMonth(month);
            OffsetDateTime exitTime = entryTime.plusHours(2); // Assume 2 hours of session

            BigDecimal hourlyRate = BigDecimal.valueOf(80);
            BigDecimal totalHours = BigDecimal.valueOf(2); // 2 hours
            BigDecimal finalCost = hourlyRate.multiply(totalHours).add(BigDecimal.valueOf(month * 100)); // Add month-specific variation

            ClientDetails client = dataManager.create(ClientDetails.class);
            client.setFirstName("Client " + month);
            client.setLastName("Test");
            client.setEntryTime(entryTime);
            client.setExitTime(exitTime);
            client.setTotalHours(totalHours);
            client.setFinalCost(finalCost);

            systemAuthenticator.withSystem(() -> {
                dataManager.save(client);
                return null;
            });
        }
    }
}
