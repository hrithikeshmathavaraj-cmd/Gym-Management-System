package com.gymmanagement.repository;

import com.gymmanagement.entity.LoginAudit;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoginAuditRepository extends MongoRepository<LoginAudit, String> {
    List<LoginAudit> findAllByOrderByTimestampDesc();
    List<LoginAudit> findByUsernameIgnoreCaseOrderByTimestampDesc(String username);
}
