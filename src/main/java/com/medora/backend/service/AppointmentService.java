package com.medora.backend.service;

import com.medora.backend.entity.*;
import com.medora.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PatientFlowRepository patientFlowRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private EventPublisherService eventPublisherService;

    @Autowired
    private NotificationService notificationService;

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public Optional<Appointment> getAppointmentById(Long id) {
        return appointmentRepository.findById(id);
    }

    public List<Appointment> getAppointmentsByStatus(String status) {
        if (status == null || status.equalsIgnoreCase("All")) {
            return appointmentRepository.findAll();
        }
        return appointmentRepository.findAll().stream()
                .filter(a -> a.getStatus() != null && a.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());
    }

    public Appointment saveAppointment(Appointment appointment) {
        if (appointment.getStatus() == null) {
            appointment.setStatus("SCHEDULED");
        }
        if (appointment.getAppointmentTime() == null) {
            appointment.setAppointmentTime(LocalDateTime.now().plusHours(2));
        }

        // Resolve Department reference
        if (appointment.getDepartment() != null && appointment.getDepartment().getId() != null) {
            departmentRepository.findById(appointment.getDepartment().getId())
                    .ifPresent(appointment::setDepartment);
        } else if (appointment.getDepartment() == null) {
            departmentRepository.findAll().stream().findFirst().ifPresent(appointment::setDepartment);
        }

        // Resolve Doctor reference
        if (appointment.getDoctor() != null && appointment.getDoctor().getId() != null) {
            doctorRepository.findById(appointment.getDoctor().getId())
                    .ifPresent(appointment::setDoctor);
        }

        // Resolve or Auto-Create Patient reference
        Patient patient = appointment.getPatient();
        if (patient != null && patient.getId() != null) {
            patientRepository.findById(patient.getId()).ifPresent(appointment::setPatient);
        } else {
            String pName = (patient != null && patient.getName() != null) ? patient.getName() : "Patient #" + (100 + (int)(Math.random() * 900));
            Patient newPatient = new Patient(pName, 32, "OTHER", "WAITING", appointment.getDepartment(), "CONSULTATION", "MEDIUM");
            if (appointment.getDoctor() != null) {
                newPatient.setAssignedDoctor(appointment.getDoctor());
            }
            newPatient = patientRepository.save(newPatient);
            appointment.setPatient(newPatient);
        }

        Appointment saved = appointmentRepository.save(appointment);

        // Auto-create Patient Flow record for operational queue tracking
        try {
            if (saved.getPatient() != null && saved.getDepartment() != null) {
                PatientFlow flow = new PatientFlow(saved.getPatient(), saved.getDepartment(), "SCHEDULED", 0, "ACTIVE");
                patientFlowRepository.save(flow);
            }
        } catch (Exception e) {
            // Ignore optional flow logging errors
        }

        eventPublisherService.publishEvent("appointment-update", Map.of(
                "appointmentId", saved.getId(),
                "status", saved.getStatus(),
                "department", saved.getDepartment() != null ? saved.getDepartment().getName() : "Unassigned"
        ));
        notificationService.createNotification(
                "New Appointment Scheduled",
                "Appointment #" + saved.getId() + " was successfully booked.",
                "SUCCESS",
                "ALL",
                "APPOINTMENT"
        );
        return saved;
    }

    public Optional<Appointment> updateAppointment(Long id, Appointment details) {
        return appointmentRepository.findById(id).map(a -> {
            if (details.getStatus() != null) a.setStatus(details.getStatus());
            if (details.getReason() != null) a.setReason(details.getReason());
            if (details.getAppointmentTime() != null) a.setAppointmentTime(details.getAppointmentTime());
            Appointment updated = appointmentRepository.save(a);

            eventPublisherService.publishEvent("appointment-update", Map.of(
                    "appointmentId", updated.getId(),
                    "status", updated.getStatus()
            ));

            return updated;
        });
    }

    public boolean cancelAppointment(Long id) {
        return appointmentRepository.findById(id).map(a -> {
            a.setStatus("CANCELLED");
            appointmentRepository.save(a);
            eventPublisherService.publishEvent("appointment-update", Map.of(
                    "appointmentId", a.getId(),
                    "status", "CANCELLED"
            ));
            notificationService.createNotification(
                    "Appointment Cancelled",
                    "Appointment #" + a.getId() + " was successfully cancelled.",
                    "SUCCESS",
                    "ALL",
                    "APPOINTMENT"
            );
            return true;
        }).orElse(false);
    }
}
