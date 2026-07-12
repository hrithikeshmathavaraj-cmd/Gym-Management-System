package com.gymmanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Enables automatic population of @CreatedDate / @LastModifiedDate fields
 * across all MongoDB documents.
 */
@Configuration
@EnableMongoAuditing
public class MongoAuditingConfig {
}
