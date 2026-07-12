package com.gymmanagement.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document representing global, singleton application settings
 * (gym name, contact info, expiry reminder threshold, theme, etc).
 */
@Document(collection = "settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Settings {

    @Id
    private String id;

    @Builder.Default
    private String gymName = "My Gym";

    private String gymAddress;

    private String gymContact;

    private String gymEmail;

    /** How many days before expiry a reminder notification is created. */
    @Builder.Default
    private int expiryReminderDays = 7;

    @Builder.Default
    private String theme = "light";
}
