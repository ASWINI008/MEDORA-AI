package com.medora.backend.service;

import com.medora.backend.entity.Department;
import com.medora.backend.entity.Doctor;
import com.medora.backend.repository.DepartmentRepository;
import com.medora.backend.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    public List<Doctor> getAllDoctors(String department, String specialization, String search) {
        List<Doctor> doctors = doctorRepository.findAll();

        if (department != null && !department.trim().isEmpty() && !"All".equalsIgnoreCase(department)) {
            doctors = doctors.stream()
                    .filter(d -> d.getDepartment() != null && d.getDepartment().getName().toLowerCase().contains(department.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (specialization != null && !specialization.trim().isEmpty() && !"All".equalsIgnoreCase(specialization)) {
            doctors = doctors.stream()
                    .filter(d -> d.getSpecialization() != null && d.getSpecialization().toLowerCase().contains(specialization.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (search != null && !search.trim().isEmpty()) {
            String q = search.toLowerCase();
            doctors = doctors.stream()
                    .filter(d -> (d.getDoctorName() != null && d.getDoctorName().toLowerCase().contains(q)) ||
                                 (d.getSpecialization() != null && d.getSpecialization().toLowerCase().contains(q)) ||
                                 (d.getServices() != null && d.getServices().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }

        return doctors;
    }

    public Optional<Doctor> getDoctorById(Long id) {
        return doctorRepository.findById(id);
    }

    public Doctor saveDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    public Doctor updateDoctor(Long id, Doctor details) {
        Doctor existing = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + id));

        if (details.getDoctorName() != null) existing.setDoctorName(details.getDoctorName());
        if (details.getSpecialization() != null) existing.setSpecialization(details.getSpecialization());
        if (details.getServices() != null) existing.setServices(details.getServices());
        if (details.getAvailability() != null) existing.setAvailability(details.getAvailability());
        if (details.getConsultationHours() != null) existing.setConsultationHours(details.getConsultationHours());
        if (details.getExperienceYears() != null) existing.setExperienceYears(details.getExperienceYears());
        if (details.getContactNumber() != null) existing.setContactNumber(details.getContactNumber());
        if (details.getActive() != null) existing.setActive(details.getActive());

        if (details.getDepartment() != null && details.getDepartment().getId() != null) {
            Department dept = departmentRepository.findById(details.getDepartment().getId()).orElse(null);
            if (dept != null) existing.setDepartment(dept);
        }

        return doctorRepository.save(existing);
    }
}
