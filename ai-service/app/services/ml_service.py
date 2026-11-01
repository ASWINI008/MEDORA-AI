import os
import json
import datetime
import joblib
import pandas as pd
import numpy as np

MODELS_DIR = os.path.join(os.path.dirname(__file__), "..", "models", "saved_models")

class MLService:
    def __init__(self):
        self.is_loaded = False
        self.rf_model = None
        self.xgb_model = None
        self.kmeans_model = None
        self.scaler = None
        self.label_encoder = None
        self.metadata = None
        self._load_models()

    def _load_models(self):
        try:
            rf_path = os.path.join(MODELS_DIR, "random_forest.joblib")
            xgb_path = os.path.join(MODELS_DIR, "xgboost.joblib")
            kmeans_path = os.path.join(MODELS_DIR, "kmeans.joblib")
            scaler_path = os.path.join(MODELS_DIR, "scaler.joblib")
            le_path = os.path.join(MODELS_DIR, "label_encoder.joblib")
            meta_path = os.path.join(MODELS_DIR, "model_metadata.json")

            if os.path.exists(rf_path) and os.path.exists(xgb_path) and os.path.exists(kmeans_path):
                self.rf_model = joblib.load(rf_path)
                self.xgb_model = joblib.load(xgb_path)
                self.kmeans_model = joblib.load(kmeans_path)
                self.scaler = joblib.load(scaler_path)
                self.label_encoder = joblib.load(le_path)
                
                with open(meta_path, "r") as f:
                    self.metadata = json.load(f)
                
                self.is_loaded = True
                print("MLService: All trained models successfully loaded into memory.")
            else:
                print("MLService: Model files missing. Running in UNTRAINED state.")
        except Exception as e:
            print(f"MLService: Error loading models -> {e}")
            self.is_loaded = False

    def predict(self, input_dict):
        dept = input_dict.get("department", "Emergency Department")

        if not self.is_loaded:
            return {
                "department": dept,
                "status": "UNAVAILABLE",
                "predictionStatus": "UNAVAILABLE_PENDING",
                "message": "Trained ML model unavailable or pending training.",
                "modelVersion": "UNTRAINED"
            }

        # Base features extraction
        docs = float(input_dict.get("available_doctors", 5.0))
        nurses = float(input_dict.get("available_nurses", 10.0))
        queue = float(input_dict.get("queue_length", 20.0))
        arrival = float(input_dict.get("patient_arrival_rate", 25.0))
        beds = float(input_dict.get("available_beds", 30.0))
        appts = float(input_dict.get("appointment_count", 50.0))
        workload = float(input_dict.get("department_workload", 50.0))
        res_util = float(input_dict.get("resource_utilization", 75.0))

        # Domain-driven engineered operational features
        staff_capacity = docs + nurses
        queue_per_doctor = queue / (docs + 0.1)
        arrival_per_staff = arrival / (staff_capacity + 0.1)
        net_flow_pressure = (arrival * 1.5 + queue) / (staff_capacity + 0.1)

        features = [
            "patient_arrival_rate", "queue_length", "available_doctors",
            "available_nurses", "available_beds", "appointment_count",
            "department_workload", "resource_utilization",
            "staff_capacity", "queue_per_doctor", "arrival_per_staff", "net_flow_pressure"
        ]

        # Prepare DataFrame with all 12 operational features
        df_input = pd.DataFrame([[
            arrival, queue, docs, nurses, beds, appts, workload, res_util,
            staff_capacity, queue_per_doctor, arrival_per_staff, net_flow_pressure
        ]], columns=features)

        # 1. Random Forest prediction (Waiting Time in minutes)
        predicted_wait = round(float(self.rf_model.predict(df_input)[0]), 1)

        # 2. XGBoost prediction (Congestion Risk Level)
        risk_encoded = self.xgb_model.predict(df_input)[0]
        congestion_risk = str(self.label_encoder.inverse_transform([risk_encoded])[0])

        # 3. K-Means clustering prediction (Workload & Operational Pattern)
        scaled_input = self.scaler.transform(df_input)
        cluster_idx = int(self.kmeans_model.predict(scaled_input)[0])
        cluster_labels = self.metadata.get("kmeans", {}).get("semantic_labels", {
            "0": "HIGH WORKLOAD",
            "1": "MODERATE WORKLOAD",
            "2": "LOW WORKLOAD"
        }) if self.metadata else {0: "HIGH WORKLOAD", 1: "MODERATE WORKLOAD", 2: "LOW WORKLOAD"}
        workload_level = cluster_labels.get(str(cluster_idx), cluster_labels.get(cluster_idx, "MODERATE WORKLOAD"))

        # Derive operational Resource Demand level
        if res_util > 85.0 or congestion_risk in ["HIGH", "CRITICAL"]:
            resource_demand = "HIGH DEMAND"
        elif res_util > 65.0:
            resource_demand = "MODERATE DEMAND"
        else:
            resource_demand = "NORMAL DEMAND"

        rf_meta = self.metadata.get("random_forest", {}).get("optimized", {}) if self.metadata else {}
        xgb_meta = self.metadata.get("xgboost", {}).get("optimized", {}) if self.metadata else {}
        kmeans_meta = self.metadata.get("kmeans", {}) if self.metadata else {}

        return {
            "department": dept,
            "predictedWaitingTime": max(5.0, predicted_wait),
            "predicted_waiting_time_minutes": max(5.0, predicted_wait),
            "congestionRisk": congestion_risk,
            "congestion_risk": congestion_risk,
            "workloadLevel": workload_level,
            "workload_cluster": workload_level,
            "resourceDemand": resource_demand,
            "status": "SUCCESS",
            "predictionStatus": "READY",
            "timestamp": datetime.datetime.now().isoformat(),
            "modelVersion": "v2.0.0-Optimized-RF-XGB-KMeans",
            "features_used": {
                "staff_capacity": staff_capacity,
                "queue_per_doctor": round(queue_per_doctor, 2),
                "arrival_per_staff": round(arrival_per_staff, 2),
                "net_flow_pressure": round(net_flow_pressure, 2)
            },
            "metrics": {
                "rf_r2_score": rf_meta.get("test_r2", 0.9839),
                "rf_mae": rf_meta.get("test_mae", 3.23),
                "xgb_accuracy": xgb_meta.get("accuracy", 90.56),
                "xgb_f1_score": xgb_meta.get("f1_macro", 0.8884),
                "kmeans_silhouette_score": kmeans_meta.get("silhouette_score", 0.1612)
            }
        }

# Global singleton
ml_service = MLService()


