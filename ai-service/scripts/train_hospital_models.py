"""
train_hospital_models.py
-------------------------
Standalone training script for the three Medora AI hospital analytics models:
  1. Length-of-Stay Regressor (LinearRegression baseline vs RandomForestRegressor)
  2. Treatment Cost Regressor  (LinearRegression baseline vs RandomForestRegressor)
  3. Readmission Classifier   (LogisticRegression baseline vs RandomForestClassifier)

All metrics are computed on the actual test set (random_state=42, test_size=0.2)
and saved alongside the .joblib pipeline files.
"""

import sys
import os

# Add ai-service to python path
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from app.services.hospital_ml_service import hospital_ml_service


def main():
    print("=" * 65)
    print("Medora AI - Hospital Dataset Machine Learning Pipeline Training")
    print("=" * 65)

    print("\n[1/3] Training and evaluating all 3 models on dataset...")
    metrics = hospital_ml_service.train_all_models()

    print("\n" + "=" * 65)
    print("ACTUAL EVALUATION METRICS (TEST SET EVALUATION)")
    print("=" * 65)

    print("\n--- MODEL A: Length-of-Stay (LOS) Prediction (Target: Length_of_Stay) ---")
    los = metrics.get("length_of_stay", {})
    rf_los = los.get("random_forest", {})
    lr_los = los.get("linear_regression", {})
    print(f"  RandomForestRegressor  : MAE={rf_los.get('mae')}, RMSE={rf_los.get('rmse')}, R2={rf_los.get('r2')}")
    print(f"  LinearRegression (Base): MAE={lr_los.get('mae')}, RMSE={lr_los.get('rmse')}, R2={lr_los.get('r2')}")

    print("\n--- MODEL B: Treatment Cost Prediction (Target: Cost) ---")
    cost = metrics.get("treatment_cost", {})
    rf_cost = cost.get("random_forest", {})
    lr_cost = cost.get("linear_regression", {})
    print(f"  RandomForestRegressor  : MAE={rf_cost.get('mae')}, RMSE={rf_cost.get('rmse')}, R2={rf_cost.get('r2')}")
    print(f"  LinearRegression (Base): MAE={lr_cost.get('mae')}, RMSE={lr_cost.get('rmse')}, R2={lr_cost.get('r2')}")

    print("\n--- MODEL C: Readmission Prediction (Target: Readmission) ---")
    readm = metrics.get("readmission", {})
    rf_readm = readm.get("random_forest", {})
    lr_readm = readm.get("logistic_regression", {})
    print(f"  RandomForestClassifier : Accuracy={rf_readm.get('accuracy')}, Precision={rf_readm.get('precision')}, Recall={rf_readm.get('recall')}, F1={rf_readm.get('f1')}, ROC-AUC={rf_readm.get('roc_auc')}")
    print(f"  LogisticRegression (Base): Accuracy={lr_readm.get('accuracy')}, Precision={lr_readm.get('precision')}, Recall={lr_readm.get('recall')}, F1={lr_readm.get('f1')}, ROC-AUC={lr_readm.get('roc_auc')}")

    print("\n" + "=" * 65)
    print("Testing Live Inference:")
    print("=" * 65)
    test_los = hospital_ml_service.predict_los(55, "Male", "Heart Attack", "Cardiac Catheterization", 12000)
    print(f"  Sample LOS Prediction : {test_los}")

    test_cost = hospital_ml_service.predict_cost(55, "Male", "Heart Attack", "Cardiac Catheterization", 10)
    print(f"  Sample Cost Prediction: {test_cost}")

    test_readm = hospital_ml_service.predict_readmission(55, "Male", "Heart Attack", "Cardiac Catheterization", 12000, 10)
    print(f"  Sample Readmission    : {test_readm}")

    print("\nTraining and verification complete! All models saved to disk.")


if __name__ == "__main__":
    main()
