package com.gymmanagement.service;

import com.gymmanagement.dto.EquipmentRequest;
import com.gymmanagement.entity.Equipment;

import java.util.List;

public interface EquipmentService {
    Equipment createEquipment(EquipmentRequest request);
    Equipment updateEquipment(String id, EquipmentRequest request);
    void deleteEquipment(String id);
    Equipment getEquipmentById(String id);
    List<Equipment> getAllEquipment();
}
