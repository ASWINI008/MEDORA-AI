package com.medora.backend.config;

import com.medora.backend.entity.*;
import com.medora.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MockDataLoader implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private BottleneckRepository bottleneckRepository;

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private NurseRepository nurseRepository;

    @Autowired
    private ParamedicalTaskRepository paramedicalTaskRepository;

    @Autowired
    private AmbulanceRepository ambulanceRepository;

    @Autowired
    private AmbulanceRequestRepository ambulanceRequestRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;



    @Override
    public void run(String... args) throws Exception {
        if (departmentRepository.count() == 0) {
            System.out.println("No departments found. Populating comprehensive operational seed data...");
            seedData();
        } else {
            System.out.println("Operational seed data already exists. Skipping database seeding.");
        }
    }

    private void seedData() {
        // 1. Seed Departments
        Department ed = departmentRepository.save(new Department("Emergency Department", "ED"));
        Department icu = departmentRepository.save(new Department("Intensive Care Unit", "ICU"));
        Department ward = departmentRepository.save(new Department("General Ward", "GW"));
        Department ped = departmentRepository.save(new Department("Pediatrics", "PED"));
        Department opd = departmentRepository.save(new Department("Outpatient Department", "OPD"));

        // 2. Seed System Users (All 6 Target Roles + Legacy Accounts)
        userRepository.save(new User("System Administrator", "admin@medora.ai", "admin", passwordEncoder.encode("admin123"), "ADMIN", null));
        userRepository.save(new User("Dr. Sarah Connor", "doctor@medora.ai", "doctor", passwordEncoder.encode("doctor123"), "DOCTOR", ed));
        userRepository.save(new User("Nurse John Doe", "nurse@medora.ai", "nurse", passwordEncoder.encode("nurse123"), "NURSE", ed));
        userRepository.save(new User("Alex Paramedic", "paramedic@medora.ai", "paramedic", passwordEncoder.encode("paramedic123"), "PARAMEDICAL_STAFF", ed));
        userRepository.save(new User("Sam Driver", "driver@medora.ai", "driver", passwordEncoder.encode("driver123"), "AMBULANCE_DRIVER", ed));

        // Legacy compatibility users
        userRepository.save(new User("Sarah Manager", "manager@medora.ai", "manager", passwordEncoder.encode("manager123"), "DEPARTMENT_MANAGER", ed));
        userRepository.save(new User("Emergency Manager", "emergency_mgr@medora.ai", "emergency_mgr", passwordEncoder.encode("emergency123"), "DEPARTMENT_MANAGER", ed));
        userRepository.save(new User("ICU Manager", "icu_mgr@medora.ai", "icu_mgr", passwordEncoder.encode("icu123"), "DEPARTMENT_MANAGER", icu));
        userRepository.save(new User("Ward Manager", "ward_mgr@medora.ai", "ward_mgr", passwordEncoder.encode("ward123"), "DEPARTMENT_MANAGER", ward));
        userRepository.save(new User("Pediatrics Manager", "ped_mgr@medora.ai", "ped_mgr", passwordEncoder.encode("ped123"), "DEPARTMENT_MANAGER", ped));
        userRepository.save(new User("OPD Manager", "opd_mgr@medora.ai", "opd_mgr", passwordEncoder.encode("opd123"), "DEPARTMENT_MANAGER", opd));
        userRepository.save(new User("John Staff", "staff@medora.ai", "staff", passwordEncoder.encode("staff123"), "STAFF", ed));
        
        User patientUser = new User("Alice Johnson", "patient@medora.ai", "patient", passwordEncoder.encode("patient123"), "PATIENT", ward);
        patientUser.setPhoneNumber("+1-555-9012");
        patientUser.setDateOfBirth("1994-06-15");
        patientUser.setGender("Female");
        userRepository.save(patientUser);


        // 3. Seed Doctors (Demonstration synthetic doctor profiles)
        Doctor d1 = doctorRepository.save(new Doctor("Dr. Sarah Connor", ed, "Emergency Medicine", "Emergency Triage & Acute Resuscitation", "ON_DUTY", "08:00 AM - 04:00 PM", 12, "+1-555-0192"));
        Doctor d2 = doctorRepository.save(new Doctor("Dr. Alan Grant", icu, "Critical Care & Intensive Medicine", "ICU Monitoring & Mechanical Ventilation", "ON_DUTY", "09:00 AM - 05:00 PM", 15, "+1-555-0124"));
        Doctor d3 = doctorRepository.save(new Doctor("Dr. Meredith Grey", ward, "Internal Medicine", "Inpatient Care & Ward Rounds", "AVAILABLE", "10:00 AM - 06:00 PM", 8, "+1-555-0201"));
        Doctor d4 = doctorRepository.save(new Doctor("Dr. Ian Malcolm", ped, "Pediatrics & Child Health", "Pediatric Consultation & Neonatal Care", "AVAILABLE", "09:00 AM - 03:00 PM", 10, "+1-555-0116"));
        Doctor d5 = doctorRepository.save(new Doctor("Dr. Gregory House", opd, "Outpatient Diagnostics", "Outpatient Consultation & Diagnostic Review", "AVAILABLE", "11:00 AM - 07:00 PM", 18, "+1-555-0202"));

        // 3b. Seed Nurses (Phase 3)
        nurseRepository.save(new Nurse("Nurse John Doe", ed, "Morning Shift (07:00 AM - 03:00 PM)", "ON_DUTY", 65.0, "+1-555-0183"));
        nurseRepository.save(new Nurse("Nurse Ellie Sattler", icu, "Morning Shift (07:00 AM - 03:00 PM)", "ON_DUTY", 85.0, "+1-555-0155"));
        nurseRepository.save(new Nurse("Nurse Beverly Crusher", ward, "Evening Shift (03:00 PM - 11:00 PM)", "AVAILABLE", 40.0, "+1-555-0167"));
        nurseRepository.save(new Nurse("Nurse Christine Chapel", ed, "Morning Shift (07:00 AM - 03:00 PM)", "ON_DUTY", 55.0, "+1-555-0199"));
        nurseRepository.save(new Nurse("Nurse Clara Oswald", ped, "Morning Shift (07:00 AM - 03:00 PM)", "AVAILABLE", 30.0, "+1-555-0203"));

        // 3c. Seed Ambulances & Emergency Dispatch Requests (Phase 3)
        Ambulance amb1 = ambulanceRepository.save(new Ambulance("AMB-104", "Sam Driver", "ON_ROUTE", "Emergency Call Sector 4", "Sector 4 Hwy", "Emergency Department", "12 mins"));
        Ambulance amb2 = ambulanceRepository.save(new Ambulance("AMB-102", "Alex Paramedic", "AVAILABLE", "Standby Bay", "Hospital Emergency Gate", "Emergency Department", "0 mins"));
        Ambulance amb3 = ambulanceRepository.save(new Ambulance("AMB-108", "David Miller", "ASSIGNED", "Inter-facility ICU Transfer", "North Clinic", "Intensive Care Unit", "18 mins"));

        ambulanceRequestRepository.save(new AmbulanceRequest("AMB-REQ-104", amb1, "CRITICAL", "Sector 4 Hwy Mile 12", "Emergency Department", "ON_ROUTE", "12 mins"));
        ambulanceRequestRepository.save(new AmbulanceRequest("AMB-REQ-108", amb3, "HIGH", "Northside Urgent Care", "Intensive Care Unit", "ASSIGNED", "18 mins"));


        // Seed Staff Members (12 realistic operational records)
        Staff s1 = staffRepository.save(new Staff("Dr. Sarah Connor", "DOCTOR", ed, "ON_DUTY", "+1-555-0192", 88.0));
        Staff s2 = staffRepository.save(new Staff("Nurse John Doe", "NURSE", ed, "AVAILABLE", "+1-555-0183", 45.0));
        Staff s3 = staffRepository.save(new Staff("Dr. Alan Grant", "DOCTOR", icu, "ON_DUTY", "+1-555-0124", 95.0));
        Staff s4 = staffRepository.save(new Staff("Nurse Ellie Sattler", "NURSE", icu, "OFF_DUTY", "+1-555-0155", 0.0));
        Staff s5 = staffRepository.save(new Staff("Dr. Ian Malcolm", "DOCTOR", ped, "AVAILABLE", "+1-555-0116", 25.0));
        Staff s6 = staffRepository.save(new Staff("Nurse Beverly Crusher", "NURSE", ward, "ON_DUTY", "+1-555-0167", 68.0));
        Staff s7 = staffRepository.save(new Staff("Dr. Leonard McCoy", "DOCTOR", ed, "ON_DUTY", "+1-555-0198", 82.0));
        Staff s8 = staffRepository.save(new Staff("Nurse Christine Chapel", "NURSE", ed, "AVAILABLE", "+1-555-0199", 50.0));
        Staff s9 = staffRepository.save(new Staff("Dr. Meredith Grey", "DOCTOR", ward, "ON_DUTY", "+1-555-0201", 72.0));
        Staff s10 = staffRepository.save(new Staff("Dr. Gregory House", "DOCTOR", opd, "AVAILABLE", "+1-555-0202", 30.0));
        Staff s11 = staffRepository.save(new Staff("Nurse Clara Oswald", "NURSE", ped, "ON_DUTY", "+1-555-0203", 60.0));
        Staff s12 = staffRepository.save(new Staff("Dr. Doogie Howser", "DOCTOR", opd, "ON_DUTY", "+1-555-0204", 40.0));


        // 4. Seed Patients (Operational queue & ward tracking IDs)
        Patient p1 = patientRepository.save(new Patient("Subject #9283", 45, "M", "WAITING", ed, "Triage Queue", "HIGH"));
        Patient p2 = patientRepository.save(new Patient("Subject #8472", 67, "F", "ADMITTED", ed, "Ward In-Bed", "MEDIUM"));
        Patient p3 = patientRepository.save(new Patient("Subject #1294", 12, "M", "WAITING", ped, "Triage Queue", "LOW"));
        Patient p4 = patientRepository.save(new Patient("Subject #5612", 29, "F", "ADMITTED", ward, "Treatment", "LOW"));
        Patient p5 = patientRepository.save(new Patient("Subject #3391", 72, "M", "ADMITTED", icu, "Ward In-Bed", "HIGH"));
        Patient p6 = patientRepository.save(new Patient("Subject #7743", 34, "F", "WAITING", ed, "Triage Queue", "HIGH"));
        Patient p7 = patientRepository.save(new Patient("Subject #6109", 58, "M", "DISCHARGED", ward, "Discharge", "LOW"));
        Patient p8 = patientRepository.save(new Patient("Subject #4028", 22, "F", "WAITING", opd, "Registration", "MEDIUM"));
        Patient p9 = patientRepository.save(new Patient("Subject #5519", 41, "M", "ADMITTED", ed, "Treatment", "HIGH"));
        Patient p10 = patientRepository.save(new Patient("Subject #8831", 63, "F", "ADMITTED", icu, "Ward In-Bed", "HIGH"));
        Patient p11 = patientRepository.save(new Patient("Subject #2044", 8, "M", "WAITING", ped, "Registration", "LOW"));
        Patient p12 = patientRepository.save(new Patient("Subject #9015", 51, "F", "DISCHARGED", ed, "Discharge", "MEDIUM"));
        Patient p13 = patientRepository.save(new Patient("Subject #3182", 39, "M", "WAITING", ed, "Triage Queue", "MEDIUM"));
        Patient p14 = patientRepository.save(new Patient("Subject #7721", 80, "F", "ADMITTED", icu, "Ward In-Bed", "HIGH"));
        Patient p15 = patientRepository.save(new Patient("Subject #6490", 25, "M", "DISCHARGED", opd, "Discharge", "LOW"));

        // 4b. Seed Paramedical Tasks (Phase 3)
        paramedicalTaskRepository.save(new ParamedicalTask("Stat Blood Chemistry Sample", "LAB_TECH", ed, p1, "HIGH", "Alex Paramedic"));
        paramedicalTaskRepository.save(new ParamedicalTask("Chest X-Ray Imaging", "RADIOLOGY_TECH", ed, p6, "HIGH", "Rad Tech Sarah"));
        paramedicalTaskRepository.save(new ParamedicalTask("Post-Op Mobility Rehab", "PHYSIO", ward, p4, "MEDIUM", "Physio Mark"));
        paramedicalTaskRepository.save(new ParamedicalTask("Pediatric Blood Typing", "LAB_TECH", ped, p3, "LOW", "Alex Paramedic"));


        // 5. Seed Resources (Beds, Rooms, Equipment)
        for (int i = 1; i <= 6; i++) {
            resourceRepository.save(new Resource("ICU Bed #" + i, "BED", i <= 5 ? "OCCUPIED" : "AVAILABLE", icu, 1, i <= 5 ? 95.0 : 0.0));
        }
        for (int i = 1; i <= 12; i++) {
            resourceRepository.save(new Resource("Emergency Bed #" + i, "BED", i <= 9 ? "OCCUPIED" : "AVAILABLE", ed, 1, i <= 9 ? 85.0 : 0.0));
        }
        for (int i = 1; i <= 8; i++) {
            resourceRepository.save(new Resource("Ward Bed #" + i, "BED", i <= 4 ? "OCCUPIED" : "AVAILABLE", ward, 1, i <= 4 ? 50.0 : 0.0));
        }
        resourceRepository.save(new Resource("Operating Theatre A", "ROOM", "AVAILABLE", ward, 1, 55.0));
        resourceRepository.save(new Resource("Operating Theatre B", "ROOM", "OCCUPIED", ed, 1, 85.0));
        resourceRepository.save(new Resource("Emergency Resuscitation Bay", "ROOM", "OCCUPIED", ed, 1, 90.0));
        resourceRepository.save(new Resource("Ventilator V-88", "EQUIPMENT", "OCCUPIED", icu, 1, 100.0));
        resourceRepository.save(new Resource("Ventilator V-89", "EQUIPMENT", "OCCUPIED", icu, 1, 95.0));
        resourceRepository.save(new Resource("Mobile X-Ray Unit 1", "EQUIPMENT", "AVAILABLE", ed, 1, 40.0));
        resourceRepository.save(new Resource("Ultrasound Scanner 2", "EQUIPMENT", "AVAILABLE", opd, 1, 30.0));

        // 6. Seed Appointments
        appointmentRepository.save(new Appointment(p1, s1, ed, LocalDateTime.now().plusHours(1), "SCHEDULED", "Emergency Triage Check-in"));
        appointmentRepository.save(new Appointment(p3, s5, ped, LocalDateTime.now().plusHours(2), "SCHEDULED", "Pediatric Consultation Slot"));
        appointmentRepository.save(new Appointment(p4, s6, ward, LocalDateTime.now().minusHours(3), "COMPLETED", "General Ward Checkup"));
        appointmentRepository.save(new Appointment(p6, s7, ed, LocalDateTime.now().plusHours(3), "SCHEDULED", "Emergency Follow-up"));
        appointmentRepository.save(new Appointment(p8, s10, opd, LocalDateTime.now().plusHours(4), "SCHEDULED", "Outpatient Routine Slot"));
        appointmentRepository.save(new Appointment(p11, s11, ped, LocalDateTime.now().minusHours(1), "COMPLETED", "Pediatric Review"));
        appointmentRepository.save(new Appointment(p13, s1, ed, LocalDateTime.now().plusHours(5), "SCHEDULED", "Emergency Observation Check"));
        appointmentRepository.save(new Appointment(p2, s3, icu, LocalDateTime.now().minusHours(5), "CANCELLED", "ICU Transfer Slot"));

        // 7. Seed Operational Alerts
        alertRepository.save(new Alert(ed, "HIGH_WAIT_TIME", "Emergency average wait time has exceeded 60 minutes.", "HIGH"));
        alertRepository.save(new Alert(icu, "LOW_BED_AVAILABILITY", "ICU bed occupancy has reached 95% threshold limit.", "CRITICAL"));
        alertRepository.save(new Alert(ed, "STAFF_SHORTAGE", "Low nurse availability detected in ED Triage zone.", "MEDIUM"));

        // 8. Seed Operational Bottlenecks
        Bottleneck bn1 = bottleneckRepository.save(new Bottleneck(ed, "HIGH_PATIENT_QUEUE", "HIGH", "High arrivals (38/hr) + low nurse availability in triage", "Estimated +25 mins wait escalation across Emergency Department"));
        Bottleneck bn2 = bottleneckRepository.save(new Bottleneck(icu, "BED_SHORTAGE", "CRITICAL", "95% bed occupancy rate + 2 incoming critical transfers", "Risk of bed diversion and transfer delays to step-down wards"));
        Bottleneck bn3 = bottleneckRepository.save(new Bottleneck(opd, "APPOINTMENT_OVERLOAD", "MEDIUM", "Concentration of 45 appointments during afternoon shift change", "Increased lobby wait times and delayed consultation starts"));

        // 9. Seed Operational Recommendations (STRICTLY NON-CLINICAL)
        recommendationRepository.save(new Recommendation(bn1, ed, 
                "Emergency Department congestion (wait time exceeds 60m)", 
                "Review staff allocation in the Emergency Department and optimize shift coverage.", 
                "HIGH", 
                "Emergency arrivals are at peak (38/hr) while nurse availability is low. Adjusting shift scheduling will reduce bottlenecking."));
        
        recommendationRepository.save(new Recommendation(bn2, icu, 
                "ICU near-capacity limit (95% occupied)", 
                "Review available bed capacity and reallocate operational resources across wards.", 
                "CRITICAL", 
                "Bed utilization in ICU is currently at 95%. Reallocating available step-down bed capacity will buffer upcoming admissions."));

        recommendationRepository.save(new Recommendation(bn3, opd, 
                "Outpatient appointment slot density imbalance", 
                "Review appointment load during peak hours and adjust queue check-in slots.", 
                "MEDIUM", 
                "Shift change period coincides with high scheduled appointment volume (45 bookings). Smoothing slot distribution optimizes throughput."));

        System.out.println("Comprehensive operational seed data populating complete!");
    }
}

