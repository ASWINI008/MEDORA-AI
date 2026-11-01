"""
hospital_ml_service.py
-----------------------
HospitalMLService: Trains and serves three scikit-learn ML pipelines:
  A) Length-of-Stay prediction       (RandomForestRegressor vs LinearRegression)
  B) Treatment Cost prediction        (RandomForestRegressor vs LinearRegression)
  C) Readmission classification       (RandomForestClassifier vs LogisticRegression)

All metrics are computed from sklearn on the actual hold-out test set.
Models are persisted under app/models/saved_models/ as .joblib files.
"""

import os
import json
import warnings
import numpy as np
import pandas as pd
import joblib

from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.compose import ColumnTransformer
from sklearn.preprocessing import OneHotEncoder, StandardScaler, LabelEncoder
from sklearn.linear_model import LinearRegression, LogisticRegression
from sklearn.ensemble import RandomForestRegressor, RandomForestClassifier
from sklearn.metrics import (
    mean_absolute_error,
    mean_squared_error,
    r2_score,
    precision_score,
    recall_score,
    f1_score,
    roc_auc_score,
    confusion_matrix,
    accuracy_score,
)

warnings.filterwarnings("ignore")

# ---------------------------------------------------------------------------
# Path configuration
# ---------------------------------------------------------------------------
_BASE_DIR = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
DATA_PATH = os.path.join(_BASE_DIR, "data", "raw", "hospital_data_analysis.csv")
MODELS_DIR = os.path.join(_BASE_DIR, "app", "models", "saved_models")

RANDOM_STATE = 42


# ---------------------------------------------------------------------------
# Helper: build a sklearn ColumnTransformer pipeline
# ---------------------------------------------------------------------------

def _build_pipeline(estimator, numeric_features: list, categorical_features: list) -> Pipeline:
    """Return a full sklearn Pipeline with preprocessing + estimator."""
    numeric_transformer = StandardScaler()
    categorical_transformer = OneHotEncoder(handle_unknown="ignore", sparse_output=False)

    preprocessor = ColumnTransformer(
        transformers=[
            ("num", numeric_transformer, numeric_features),
            ("cat", categorical_transformer, categorical_features),
        ]
    )

    return Pipeline(steps=[("preprocessor", preprocessor), ("model", estimator)])


# ---------------------------------------------------------------------------
# Main service class
# ---------------------------------------------------------------------------

class HospitalMLService:
    """
    Manages three hospital ML models.

    On startup: tries to load saved models from disk.
    If any model is missing: trains all three from scratch.

    Public methods:
        predict_los(age, gender, condition, procedure, cost)
        predict_cost(age, gender, condition, procedure, los)
        predict_readmission(age, gender, condition, procedure, cost, los)
        get_model_metrics()
    """

    def __init__(self):
        self.is_trained: bool = False

        # Model pipelines
        self.los_pipeline = None
        self.cost_pipeline = None
        self.readmission_pipeline = None

        # Evaluation metrics (populated during training / loaded from JSON)
        self._metrics: dict = {}

        os.makedirs(MODELS_DIR, exist_ok=True)
        self._try_load_or_train()

    # -----------------------------------------------------------------------
    # Load / Train entry point
    # -----------------------------------------------------------------------

    def _try_load_or_train(self) -> None:
        los_path = os.path.join(MODELS_DIR, "los_model.joblib")
        cost_path = os.path.join(MODELS_DIR, "cost_model.joblib")
        readm_path = os.path.join(MODELS_DIR, "readmission_model.joblib")
        meta_los = os.path.join(MODELS_DIR, "los_model_metadata.json")
        meta_cost = os.path.join(MODELS_DIR, "cost_model_metadata.json")
        meta_readm = os.path.join(MODELS_DIR, "readmission_model_metadata.json")

        if (
            os.path.exists(los_path)
            and os.path.exists(cost_path)
            and os.path.exists(readm_path)
        ):
            try:
                self.los_pipeline = joblib.load(los_path)
                self.cost_pipeline = joblib.load(cost_path)
                self.readmission_pipeline = joblib.load(readm_path)

                # Load metrics from JSON metadata files
                self._metrics = {}
                for key, path in [("los", meta_los), ("cost", meta_cost), ("readmission", meta_readm)]:
                    if os.path.exists(path):
                        with open(path, "r") as f:
                            self._metrics[key] = json.load(f)

                self.is_trained = True
                print("HospitalMLService: All 3 models loaded from disk.")
                return
            except Exception as exc:
                print(f"HospitalMLService: Load failed ({exc}). Re-training.")

        # Models not found -> train
        self.train_all_models()

    # -----------------------------------------------------------------------
    # Data loading shared by training
    # -----------------------------------------------------------------------

    def _load_data(self) -> pd.DataFrame:
        df = pd.read_csv(DATA_PATH)
        for col in ["Gender", "Condition", "Procedure", "Readmission", "Outcome"]:
            df[col] = df[col].astype(str).str.strip()
        df["Age"] = pd.to_numeric(df["Age"], errors="coerce")
        df["Cost"] = pd.to_numeric(df["Cost"], errors="coerce")
        df["Length_of_Stay"] = pd.to_numeric(df["Length_of_Stay"], errors="coerce")
        df.dropna(subset=["Age", "Cost", "Length_of_Stay"], inplace=True)
        df.reset_index(drop=True, inplace=True)
        return df

    # -----------------------------------------------------------------------
    # Model A: Length-of-Stay Prediction
    # -----------------------------------------------------------------------

    def _train_los_model(self, df: pd.DataFrame) -> dict:
        """Train LOS regression; return metrics dict."""
        feature_cols = ["Age", "Gender", "Condition", "Procedure", "Cost"]
        target_col = "Length_of_Stay"

        X = df[feature_cols]
        y = df[target_col]

        numeric_features = ["Age", "Cost"]
        categorical_features = ["Gender", "Condition", "Procedure"]

        X_train, X_test, y_train, y_test = train_test_split(
            X, y, test_size=0.2, random_state=RANDOM_STATE
        )

        # Baseline: LinearRegression
        lr_pipe = _build_pipeline(LinearRegression(), numeric_features, categorical_features)
        lr_pipe.fit(X_train, y_train)
        lr_pred = lr_pipe.predict(X_test)
        lr_metrics = {
            "mae": round(float(mean_absolute_error(y_test, lr_pred)), 4),
            "rmse": round(float(np.sqrt(mean_squared_error(y_test, lr_pred))), 4),
            "r2": round(float(r2_score(y_test, lr_pred)), 4),
        }

        # Best: RandomForestRegressor
        rf_pipe = _build_pipeline(
            RandomForestRegressor(n_estimators=100, random_state=RANDOM_STATE),
            numeric_features,
            categorical_features,
        )
        rf_pipe.fit(X_train, y_train)
        rf_pred = rf_pipe.predict(X_test)
        rf_metrics = {
            "mae": round(float(mean_absolute_error(y_test, rf_pred)), 4),
            "rmse": round(float(np.sqrt(mean_squared_error(y_test, rf_pred))), 4),
            "r2": round(float(r2_score(y_test, rf_pred)), 4),
        }

        # Save best (RF) pipeline
        self.los_pipeline = rf_pipe
        joblib.dump(rf_pipe, os.path.join(MODELS_DIR, "los_model.joblib"))

        metadata = {
            "model": "RandomForestRegressor",
            "target": "Length_of_Stay",
            "features": feature_cols,
            "train_size": len(X_train),
            "test_size": len(X_test),
            "random_state": RANDOM_STATE,
            "linear_regression_baseline": lr_metrics,
            "random_forest": rf_metrics,
            "best_model": "RandomForest" if rf_metrics["r2"] >= lr_metrics["r2"] else "LinearRegression",
        }
        with open(os.path.join(MODELS_DIR, "los_model_metadata.json"), "w") as f:
            json.dump(metadata, f, indent=2)

        print(
            f"  [LOS] LR  -> MAE={lr_metrics['mae']}, RMSE={lr_metrics['rmse']}, R2={lr_metrics['r2']}"
        )
        print(
            f"  [LOS] RF  -> MAE={rf_metrics['mae']}, RMSE={rf_metrics['rmse']}, R2={rf_metrics['r2']}"
        )
        return metadata

    # -----------------------------------------------------------------------
    # Model B: Treatment Cost Prediction
    # -----------------------------------------------------------------------

    def _train_cost_model(self, df: pd.DataFrame) -> dict:
        """Train Cost regression; return metrics dict."""
        feature_cols = ["Age", "Gender", "Condition", "Procedure", "Length_of_Stay"]
        target_col = "Cost"

        X = df[feature_cols]
        y = df[target_col]

        numeric_features = ["Age", "Length_of_Stay"]
        categorical_features = ["Gender", "Condition", "Procedure"]

        X_train, X_test, y_train, y_test = train_test_split(
            X, y, test_size=0.2, random_state=RANDOM_STATE
        )

        # Baseline: LinearRegression
        lr_pipe = _build_pipeline(LinearRegression(), numeric_features, categorical_features)
        lr_pipe.fit(X_train, y_train)
        lr_pred = lr_pipe.predict(X_test)
        lr_metrics = {
            "mae": round(float(mean_absolute_error(y_test, lr_pred)), 4),
            "rmse": round(float(np.sqrt(mean_squared_error(y_test, lr_pred))), 4),
            "r2": round(float(r2_score(y_test, lr_pred)), 4),
        }

        # Best: RandomForestRegressor
        rf_pipe = _build_pipeline(
            RandomForestRegressor(n_estimators=100, random_state=RANDOM_STATE),
            numeric_features,
            categorical_features,
        )
        rf_pipe.fit(X_train, y_train)
        rf_pred = rf_pipe.predict(X_test)
        rf_metrics = {
            "mae": round(float(mean_absolute_error(y_test, rf_pred)), 4),
            "rmse": round(float(np.sqrt(mean_squared_error(y_test, rf_pred))), 4),
            "r2": round(float(r2_score(y_test, rf_pred)), 4),
        }

        # Save best (RF) pipeline
        self.cost_pipeline = rf_pipe
        joblib.dump(rf_pipe, os.path.join(MODELS_DIR, "cost_model.joblib"))

        metadata = {
            "model": "RandomForestRegressor",
            "target": "Cost",
            "features": feature_cols,
            "train_size": len(X_train),
            "test_size": len(X_test),
            "random_state": RANDOM_STATE,
            "linear_regression_baseline": lr_metrics,
            "random_forest": rf_metrics,
            "best_model": "RandomForest" if rf_metrics["r2"] >= lr_metrics["r2"] else "LinearRegression",
        }
        with open(os.path.join(MODELS_DIR, "cost_model_metadata.json"), "w") as f:
            json.dump(metadata, f, indent=2)

        print(
            f"  [Cost] LR  -> MAE={lr_metrics['mae']}, RMSE={lr_metrics['rmse']}, R2={lr_metrics['r2']}"
        )
        print(
            f"  [Cost] RF  -> MAE={rf_metrics['mae']}, RMSE={rf_metrics['rmse']}, R2={rf_metrics['r2']}"
        )
        return metadata

    # -----------------------------------------------------------------------
    # Model C: Readmission Classification
    # -----------------------------------------------------------------------

    def _train_readmission_model(self, df: pd.DataFrame) -> dict:
        """Train Readmission classifier; return metrics dict."""
        # NOTE: Outcome & Satisfaction excluded to avoid leakage
        feature_cols = ["Age", "Gender", "Condition", "Procedure", "Cost", "Length_of_Stay"]
        target_col = "Readmission"

        df = df.copy()
        df["Readmission_bin"] = (df[target_col].str.upper() == "YES").astype(int)

        X = df[feature_cols]
        y = df["Readmission_bin"]

        # Class balance check
        class_counts = y.value_counts().to_dict()
        print(f"  [Readmission] Class distribution -> {class_counts}")

        numeric_features = ["Age", "Cost", "Length_of_Stay"]
        categorical_features = ["Gender", "Condition", "Procedure"]

        X_train, X_test, y_train, y_test = train_test_split(
            X, y, test_size=0.2, random_state=RANDOM_STATE, stratify=y
        )

        # Baseline: LogisticRegression
        lr_pipe = _build_pipeline(
            LogisticRegression(max_iter=1000, random_state=RANDOM_STATE),
            numeric_features,
            categorical_features,
        )
        lr_pipe.fit(X_train, y_train)
        lr_pred = lr_pipe.predict(X_test)
        lr_proba = lr_pipe.predict_proba(X_test)[:, 1]
        lr_metrics = {
            "accuracy": round(float(accuracy_score(y_test, lr_pred)), 4),
            "precision": round(float(precision_score(y_test, lr_pred, zero_division=0)), 4),
            "recall": round(float(recall_score(y_test, lr_pred, zero_division=0)), 4),
            "f1": round(float(f1_score(y_test, lr_pred, zero_division=0)), 4),
            "roc_auc": round(float(roc_auc_score(y_test, lr_proba)), 4),
            "confusion_matrix": confusion_matrix(y_test, lr_pred).tolist(),
        }

        # Best: RandomForestClassifier
        rf_pipe = _build_pipeline(
            RandomForestClassifier(n_estimators=100, random_state=RANDOM_STATE),
            numeric_features,
            categorical_features,
        )
        rf_pipe.fit(X_train, y_train)
        rf_pred = rf_pipe.predict(X_test)
        rf_proba = rf_pipe.predict_proba(X_test)[:, 1]
        rf_metrics = {
            "accuracy": round(float(accuracy_score(y_test, rf_pred)), 4),
            "precision": round(float(precision_score(y_test, rf_pred, zero_division=0)), 4),
            "recall": round(float(recall_score(y_test, rf_pred, zero_division=0)), 4),
            "f1": round(float(f1_score(y_test, rf_pred, zero_division=0)), 4),
            "roc_auc": round(float(roc_auc_score(y_test, rf_proba)), 4),
            "confusion_matrix": confusion_matrix(y_test, rf_pred).tolist(),
        }

        # Select best by F1
        best_pipe = rf_pipe if rf_metrics["f1"] >= lr_metrics["f1"] else lr_pipe
        self.readmission_pipeline = best_pipe
        joblib.dump(best_pipe, os.path.join(MODELS_DIR, "readmission_model.joblib"))

        metadata = {
            "model": "RandomForestClassifier",
            "target": "Readmission",
            "features": feature_cols,
            "class_distribution": {str(k): int(v) for k, v in class_counts.items()},
            "train_size": int(len(X_train)),
            "test_size": int(len(X_test)),
            "random_state": RANDOM_STATE,
            "logistic_regression_baseline": lr_metrics,
            "random_forest": rf_metrics,
            "best_model": "RandomForest" if rf_metrics["f1"] >= lr_metrics["f1"] else "LogisticRegression",
        }
        with open(os.path.join(MODELS_DIR, "readmission_model_metadata.json"), "w") as f:
            json.dump(metadata, f, indent=2)

        print(
            f"  [Readmission] LR  -> Acc={lr_metrics['accuracy']}, P={lr_metrics['precision']}, "
            f"R={lr_metrics['recall']}, F1={lr_metrics['f1']}, AUC={lr_metrics['roc_auc']}"
        )
        print(
            f"  [Readmission] RF  -> Acc={rf_metrics['accuracy']}, P={rf_metrics['precision']}, "
            f"R={rf_metrics['recall']}, F1={rf_metrics['f1']}, AUC={rf_metrics['roc_auc']}"
        )
        return metadata

    # -----------------------------------------------------------------------
    # Public: train all models
    # -----------------------------------------------------------------------

    def train_all_models(self) -> dict:
        """Train all three models and persist to disk. Returns combined metrics."""
        print("HospitalMLService: Starting model training …")
        df = self._load_data()
        print(f"  Dataset loaded: {len(df)} records.")

        los_meta = self._train_los_model(df)
        cost_meta = self._train_cost_model(df)
        readm_meta = self._train_readmission_model(df)

        self._metrics = {
            "los": los_meta,
            "cost": cost_meta,
            "readmission": readm_meta,
        }
        self.is_trained = True
        print("HospitalMLService: All models trained and saved.")
        return self._metrics

    # -----------------------------------------------------------------------
    # Public: inference methods
    # -----------------------------------------------------------------------

    def predict_los(
        self,
        age: float,
        gender: str,
        condition: str,
        procedure: str,
        cost: float,
    ) -> dict:
        """Predict Length_of_Stay for a patient."""
        if not self.is_trained:
            return {"error": "Models not trained yet."}
        row = pd.DataFrame(
            [[age, gender, condition, procedure, cost]],
            columns=["Age", "Gender", "Condition", "Procedure", "Cost"],
        )
        prediction = float(self.los_pipeline.predict(row)[0])
        return {
            "predicted_los_days": round(prediction, 2),
            "model": "RandomForestRegressor",
            "inputs": {
                "age": age, "gender": gender, "condition": condition,
                "procedure": procedure, "cost": cost,
            },
        }

    def predict_cost(
        self,
        age: float,
        gender: str,
        condition: str,
        procedure: str,
        los: float,
    ) -> dict:
        """Predict treatment Cost for a patient."""
        if not self.is_trained:
            return {"error": "Models not trained yet."}
        row = pd.DataFrame(
            [[age, gender, condition, procedure, los]],
            columns=["Age", "Gender", "Condition", "Procedure", "Length_of_Stay"],
        )
        prediction = float(self.cost_pipeline.predict(row)[0])
        return {
            "predicted_cost_usd": round(prediction, 2),
            "model": "RandomForestRegressor",
            "inputs": {
                "age": age, "gender": gender, "condition": condition,
                "procedure": procedure, "length_of_stay": los,
            },
        }

    def predict_readmission(
        self,
        age: float,
        gender: str,
        condition: str,
        procedure: str,
        cost: float,
        los: float,
    ) -> dict:
        """Predict readmission probability for a patient."""
        if not self.is_trained:
            return {"error": "Models not trained yet."}
        row = pd.DataFrame(
            [[age, gender, condition, procedure, cost, los]],
            columns=["Age", "Gender", "Condition", "Procedure", "Cost", "Length_of_Stay"],
        )
        label = int(self.readmission_pipeline.predict(row)[0])
        proba = float(self.readmission_pipeline.predict_proba(row)[0][1])
        return {
            "predicted_readmission": "Yes" if label == 1 else "No",
            "readmission_probability": round(proba, 4),
            "model": "RandomForestClassifier",
            "inputs": {
                "age": age, "gender": gender, "condition": condition,
                "procedure": procedure, "cost": cost, "length_of_stay": los,
            },
        }

    # -----------------------------------------------------------------------
    # Public: metrics retrieval
    # -----------------------------------------------------------------------

    def get_model_metrics(self) -> dict:
        """Return all stored evaluation metrics for all three models."""
        if not self._metrics:
            return {"error": "Models not yet trained. No metrics available."}
        return self._metrics


# Global singleton – auto-trains on first import if models are not on disk
hospital_ml_service = HospitalMLService()
