package com.company.gamespace.config;

import com.company.gamespace.entity.ClientDetails;
import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class StartupRunner implements CommandLineRunner {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    @Override
    public void run(String... args) {
        systemAuthenticator.withSystem(() -> {
            if (dataManager.load(ClientDetails.class).all().maxResults(1).list().isEmpty()) {
                List<ClientDetails> clients = List.of(
                        createClient("Alice", "Smith"),
                        createClient("Bob", "Johnson"),
                        createClient("Charlie", "Williams"),
                        createClient("Diana", "Brown"),
                        createClient("Ethan", "Davis")
                );

                clients.forEach(dataManager::save);
            }
            return null;
        });
    }

    private ClientDetails createClient(String firstName, String lastName) {
        OffsetDateTime entry = OffsetDateTime.now().minusHours((long) (Math.random() * 5 + 1));
        OffsetDateTime exit = entry.plusHours((long) (Math.random() * 5 + 1));
        BigDecimal totalHours = BigDecimal.valueOf(exit.toEpochSecond() - entry.toEpochSecond())
                .divide(BigDecimal.valueOf(3600), 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal finalCost = totalHours.multiply(BigDecimal.valueOf(10)); // assuming 10 per hour

        ClientDetails client = new ClientDetails();
        client.setId(UUID.randomUUID());
        client.setFirstName(firstName);
        client.setLastName(lastName);
        client.setEntryTime(entry);
        client.setExitTime(exit);
        client.setTotalHours(totalHours);
        client.setFinalCost(finalCost);

        return client;
    }
}
