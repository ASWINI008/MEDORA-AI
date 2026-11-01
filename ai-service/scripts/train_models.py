import os
import json
import datetime
import pandas as pd
import numpy as np
import joblib

from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestRegressor
from sklearn.preprocessing import StandardScaler, LabelEncoder
from sklearn.cluster import KMeans
from sklearn.metrics import (
    r2_score, mean_squared_error, mean_absolute_error,
    accuracy_score, precision_score, recall_score, f1_score,
    silhouette_score
)
import xgboost as xgb

def train_and_save_models():
    data_path = "data/raw/synthetic_hospital_data.csv"
    if not os.path.exists(data_path):
        from generate_dataset import generate_hospital_data
        generate_hospital_data()

    df = pd.read_csv(data_path)
    print(f"Loaded {len(df)} records from {data_path}")

    feature_cols = [
        "patient_arrival_rate", "queue_length", "available_doctors",
        "available_nurses", "available_beds", "appointment_count",
        "department_workload", "resource_utilization"
    ]

    X = df[feature_cols]
    y_wait = df["waiting_time"]
    y_risk = df["congestion_risk"]

    # 1. Train Random Forest Regressor (Waiting Time)
    X_train, X_test, y_wait_train, y_wait_test = train_test_split(X, y_wait, test_size=0.2, random_state=42)
    rf_model = RandomForestRegressor(n_estimators=100, random_state=42)
    rf_model.fit(X_train, y_wait_train)

    wait_preds = rf_model.predict(X_test)
    rf_r2 = round(float(r2_score(y_wait_test, wait_preds)), 4)
    rf_mae = round(float(mean_absolute_error(y_wait_test, wait_preds)), 2)
    rf_rmse = round(float(np.sqrt(mean_squared_error(y_wait_test, wait_preds))), 2)
    print(f"Random Forest Regressor -> R2: {rf_r2}, MAE: {rf_mae} mins, RMSE: {rf_rmse} mins")

    # 2. Train XGBoost Classifier (Congestion Risk)
    le = LabelEncoder()
    y_risk_encoded = le.fit_transform(y_risk)

    X_train_xgb, X_test_xgb, y_risk_train, y_risk_test = train_test_split(X, y_risk_encoded, test_size=0.2, random_state=42)
    xgb_model = xgb.XGBClassifier(n_estimators=100, learning_rate=0.1, max_depth=5, random_state=42)
    xgb_model.fit(X_train_xgb, y_risk_train)

    risk_preds = xgb_model.predict(X_test_xgb)
    xgb_acc = round(float(accuracy_score(y_risk_test, risk_preds)), 4)
    xgb_prec = round(float(precision_score(y_risk_test, risk_preds, average='weighted')), 4)
    xgb_rec = round(float(recall_score(y_risk_test, risk_preds, average='weighted')), 4)
    xgb_f1 = round(float(f1_score(y_risk_test, risk_preds, average='weighted')), 4)
    print(f"XGBoost Classifier -> Acc: {xgb_acc*100:.2f}%, Prec: {xgb_prec*100:.2f}%, Rec: {xgb_rec*100:.2f}%, F1: {xgb_f1:.4f}")

    # 3. Train K-Means Clustering (Workload Patterns)
    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)

    kmeans_model = KMeans(n_clusters=3, random_state=42, n_init=10)
    cluster_labels = kmeans_model.fit_predict(X_scaled)
    sil_score = round(float(silhouette_score(X_scaled, cluster_labels)), 4)
    print(f"K-Means Clustering -> 3 Workload Clusters Trained (Silhouette Score: {sil_score})")

    # Save artifacts
    models_dir = "app/models/saved_models"
    os.makedirs(models_dir, exist_ok=True)

    joblib.dump(rf_model, os.path.join(models_dir, "random_forest.joblib"))
    joblib.dump(xgb_model, os.path.join(models_dir, "xgboost.joblib"))
    joblib.dump(kmeans_model, os.path.join(models_dir, "kmeans.joblib"))
    joblib.dump(scaler, os.path.join(models_dir, "scaler.joblib"))
    joblib.dump(le, os.path.join(models_dir, "label_encoder.joblib"))

    metadata = {
        "model_status": "TRAINED",
        "training_timestamp": datetime.datetime.now().isoformat(),
        "dataset_size": len(df),
        "random_forest": {
            "r2_score": rf_r2,
            "mae": rf_mae,
            "rmse": rf_rmse
        },
        "xgboost": {
            "accuracy": round(xgb_acc * 100, 2),
            "precision": round(xgb_prec * 100, 2),
            "recall": round(xgb_rec * 100, 2),
            "f1_score": xgb_f1
        },
        "kmeans": {
            "clusters": 3,
            "silhouette_score": sil_score,
            "labels": {
                "0": "LOW WORKLOAD",
                "1": "MODERATE WORKLOAD",
                "2": "HIGH WORKLOAD"
            }
        }
    }

    with open(os.path.join(models_dir, "model_metadata.json"), "w") as f:
        json.dump(metadata, f, indent=2)

    print(f"All ML models successfully trained and saved in {models_dir}")

if __name__ == "__main__":
    train_and_save_models()

