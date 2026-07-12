package com.gymmanagement.util;

import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates unique trainer codes, e.g. TRN-2026-04821, which also double
 * as the trainer's Login ID for authentication.
 */
@Component
public class TrainerCodeGenerator {

    public String generate() {
        int year = Year.now().getValue();
        int random = ThreadLocalRandom.current().nextInt(10000, 99999);
        return "TRN-" + year + "-" + random;
    }
}
