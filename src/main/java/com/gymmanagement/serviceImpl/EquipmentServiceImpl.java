package com.gymmanagement.serviceImpl;

import com.gymmanagement.dto.EquipmentRequest;
import com.gymmanagement.entity.Equipment;
import com.gymmanagement.exception.ResourceNotFoundException;
import com.gymmanagement.repository.EquipmentRepository;
import com.gymmanagement.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for managing gym equipment inventory and maintenance status.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentRepository equipmentRepository;

    @Override
    @Transactional
    public Equipment createEquipment(EquipmentRequest request) {
        Equipment equipment = Equipment.builder()
                .name(request.getName())
                .purchaseDate(request.getPurchaseDate())
                .condition(request.getCondition())
                .status(request.getStatus() != null ? request.getStatus() : "AVAILABLE")
                .maintenanceDate(request.getMaintenanceDate())
                .build();
        Equipment saved = equipmentRepository.save(equipment);
        log.info("Equipment added: {}", saved.getName());
        return saved;
    }

    @Override
    @Transactional
    public Equipment updateEquipment(String id, EquipmentRequest request) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found with id: " + id));

        equipment.setName(request.getName());
        equipment.setPurchaseDate(request.getPurchaseDate());
        equipment.setCondition(request.getCondition());
        equipment.setStatus(request.getStatus());
        equipment.setMaintenanceDate(request.getMaintenanceDate());

        Equipment saved = equipmentRepository.save(equipment);
        log.info("Equipment updated: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public void deleteEquipment(String id) {
        if (!equipmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Equipment not found with id: " + id);
        }
        equipmentRepository.deleteById(id);
        log.info("Equipment deleted: {}", id);
    }

    @Override
    public Equipment getEquipmentById(String id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found with id: " + id));
    }

    @Override
    public List<Equipment> getAllEquipment() {
        return equipmentRepository.findAll();
    }
}
