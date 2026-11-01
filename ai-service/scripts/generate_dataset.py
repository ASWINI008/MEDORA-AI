import os
import numpy as np
import pandas as pd

def generate_hospital_data(num_samples=1200, output_path="data/raw/synthetic_hospital_data.csv"):
    np.random.seed(42)

    # Operational features
    patient_arrival_rate = np.random.randint(5, 65, size=num_samples)
    queue_length = np.random.randint(2, 55, size=num_samples)
    available_doctors = np.random.randint(1, 16, size=num_samples)
    available_nurses = np.random.randint(2, 32, size=num_samples)
    available_beds = np.random.randint(0, 52, size=num_samples)
    appointment_count = np.random.randint(10, 130, size=num_samples)

    # Derived workload and utilization
    department_workload = np.clip(
        (queue_length * 1.2 + patient_arrival_rate * 0.8) / (available_doctors * 2.5 + available_nurses + 0.1) * 20,
        15.0, 99.0
    ).round(2)

    resource_utilization = np.clip(
        (1.0 - (available_beds / 55.0)) * 100 + np.random.normal(0, 5, num_samples),
        10.0, 99.5
    ).round(2)

    # Calculated target: waiting_time (in minutes) with realistic queuing dynamics
    noise = np.random.normal(0, 4.0, num_samples)
    raw_wait = (queue_length * 2.2) + (patient_arrival_rate * 0.75) - (available_doctors * 3.5) - (available_nurses * 0.8) + noise
    waiting_time = np.clip(raw_wait, 5.0, 120.0).round(1)

    # Calculated target: congestion_risk (Categorical)
    congestion_risk = []
    for wait, beds, workload in zip(waiting_time, available_beds, department_workload):
        if wait > 65 or beds <= 3 or workload > 90:
            congestion_risk.append("CRITICAL")
        elif wait > 42 or beds <= 8 or workload > 78:
            congestion_risk.append("HIGH")
        elif wait > 22 or workload > 55:
            congestion_risk.append("MEDIUM")
        else:
            congestion_risk.append("LOW")

    df = pd.DataFrame({
        "patient_arrival_rate": patient_arrival_rate,
        "queue_length": queue_length,
        "available_doctors": available_doctors,
        "available_nurses": available_nurses,
        "available_beds": available_beds,
        "appointment_count": appointment_count,
        "department_workload": department_workload,
        "resource_utilization": resource_utilization,
        "waiting_time": waiting_time,
        "congestion_risk": congestion_risk
    })

    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    df.to_csv(output_path, index=False)
    print(f"Generated {num_samples} operational records at {output_path}")
    return df

if __name__ == "__main__":
    generate_hospital_data()
