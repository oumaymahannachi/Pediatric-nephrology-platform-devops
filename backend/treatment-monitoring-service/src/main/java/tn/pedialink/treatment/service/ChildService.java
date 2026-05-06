package tn.pedialink.treatment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.pedialink.treatment.entity.Child;
import tn.pedialink.treatment.exception.BadRequestException;
import tn.pedialink.treatment.exception.NotFoundException;
import tn.pedialink.treatment.repository.ChildRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChildService {

    private final ChildRepository childRepository;

    public List<Child> getChildrenByParent(String parentId) {
        return childRepository.findByParentId(parentId);
    }

    public Child addChild(String parentId, Child child) {
        child.setParentId(parentId);
        if (child.getDoctorIds() == null) {
            child.setDoctorIds(new ArrayList<>());
        }
        return childRepository.save(child);
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
        return childRepository.save(child);
    }

    public void deleteChild(String parentId, String childId) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new NotFoundException("Child not found"));
        if (!child.getParentId().equals(parentId)) {
            throw new BadRequestException("Not authorized to delete this child");
        }
        childRepository.deleteById(childId);
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
        return childRepository.save(child);
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
        return childRepository.save(child);
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
