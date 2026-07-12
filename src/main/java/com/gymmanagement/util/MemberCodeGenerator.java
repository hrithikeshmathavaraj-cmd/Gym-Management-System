package com.gymmanagement.util;

import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates human-friendly, unique member codes, e.g. GYM-2026-04821.
 */
@Component
public class MemberCodeGenerator {

    public String generate() {
        int year = Year.now().getValue();
        int random = ThreadLocalRandom.current().nextInt(10000, 99999);
        return "GYM-" + year + "-" + random;
    }
}
