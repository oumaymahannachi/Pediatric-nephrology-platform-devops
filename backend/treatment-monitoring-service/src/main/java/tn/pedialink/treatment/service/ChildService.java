package tn.pedialink.treatment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.pedialink.treatment.entity.Child;
import tn.pedialink.treatment.exception.BadRequestException;
import tn.pedialink.treatment.exception.NotFoundException;
import tn.pedialink.treatment.h2.service.TreatmentH2BackupService;
import tn.pedialink.treatment.repository.ChildRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChildService {

    private final ChildRepository childRepository;
    private final TreatmentH2BackupService h2BackupService;

    public List<Child> getChildrenByParent(String parentId) {
        return childRepository.findByParentId(parentId);
    }

    public Child addChild(String parentId, Child child) {
        child.setParentId(parentId);
        if (child.getDoctorIds() == null) {
            child.setDoctorIds(new ArrayList<>());
        }
        Child saved = childRepository.save(child);
        h2BackupService.backupChild(saved);
        return saved;
    }

    public Child updateChild(String parentId, String childId, Child data) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new NotFoundException("Child not found"));
        if (!child.getParentId().equals(parentId)) {
            throw new BadRequestException("Not authorized to update this child");
        }
        child.setFullName(data.getFullName());
        child.setDateOfBirth(data.getDateOfBirth());
        child.setGender(data.getGender());
        child.setNotes(data.getNotes());
        Child saved = childRepository.save(child);
        h2BackupService.backupChild(saved);
        return saved;
    }

    public void deleteChild(String parentId, String childId) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new NotFoundException("Child not found"));
        if (!child.getParentId().equals(parentId)) {
            throw new BadRequestException("Not authorized to delete this child");
        }
        childRepository.deleteById(childId);
        h2BackupService.deleteChild(childId);
    }

    public Child assignDoctor(String parentId, String childId, String doctorId) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new NotFoundException("Child not found"));
        if (!child.getParentId().equals(parentId)) {
            throw new BadRequestException("Not authorized");
        }
        if (child.getDoctorIds() == null) {
            child.setDoctorIds(new ArrayList<>());
        }
        if (!child.getDoctorIds().contains(doctorId)) {
            child.getDoctorIds().add(doctorId);
        }
        Child saved = childRepository.save(child);
        h2BackupService.backupChild(saved);
        return saved;
    }

    public Child removeDoctor(String parentId, String childId, String doctorId) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new NotFoundException("Child not found"));
        if (!child.getParentId().equals(parentId)) {
            throw new BadRequestException("Not authorized");
        }
        if (child.getDoctorIds() != null) {
            child.getDoctorIds().remove(doctorId);
        }
        Child saved = childRepository.save(child);
        h2BackupService.backupChild(saved);
        return saved;
    }

    public List<Child> getChildrenByDoctor(String doctorId) {
        return childRepository.findByDoctorIdsContaining(doctorId);
    }

    public Child getChildById(String childId) {
        return childRepository.findById(childId)
                .orElseThrow(() -> new NotFoundException("Child not found"));
    }

    public long countByParent(String parentId) {
        return childRepository.countByParentId(parentId);
    }

    public List<Child> getAllChildren() {
        return childRepository.findAll();
    }

    public long countAll() {
        return childRepository.count();
    }
}
