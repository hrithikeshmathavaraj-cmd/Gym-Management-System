package com.gymmanagement.repository;

import com.gymmanagement.entity.Equipment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipmentRepository extends MongoRepository<Equipment, String> {
}
