"""
hospital_analytics_service.py
------------------------------
HospitalAnalyticsService: Loads, validates, and analyses the hospital_data_analysis.csv dataset.
All statistics are computed directly from the actual data – nothing is hard-coded.
"""

import os
import pandas as pd
import numpy as np

# Resolve the CSV path relative to this file's location
_BASE_DIR = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
DATA_PATH = os.path.join(_BASE_DIR, "data", "raw", "hospital_data_analysis.csv")

REQUIRED_COLUMNS = [
    "Patient_ID", "Age", "Gender", "Condition", "Procedure",
    "Cost", "Length_of_Stay", "Readmission", "Outcome", "Satisfaction"
]


class HospitalAnalyticsService:
    """
    Provides analytics over the hospital_data_analysis.csv dataset.

    On instantiation the CSV is loaded and validated.  Every public method
    computes its results from ``self.df`` so the numbers are always derived
    from the real data.
    """

    def __init__(self):
        self.df: pd.DataFrame = pd.DataFrame()
        self.is_loaded: bool = False
        self._load_and_preprocess()

    # ------------------------------------------------------------------
    # Private helpers
    # ------------------------------------------------------------------

    def _load_and_preprocess(self) -> None:
        """Load CSV, validate schema, coerce types, and cache as self.df."""
        try:
            if not os.path.exists(DATA_PATH):
                raise FileNotFoundError(f"Dataset not found at: {DATA_PATH}")

            df = pd.read_csv(DATA_PATH)

            # --- 1. Column validation ---
            missing_cols = [c for c in REQUIRED_COLUMNS if c not in df.columns]
            if missing_cols:
                raise ValueError(f"Missing columns in dataset: {missing_cols}")

            # --- 2. Type coercion ---
            df["Age"] = pd.to_numeric(df["Age"], errors="coerce")
            df["Cost"] = pd.to_numeric(df["Cost"], errors="coerce")
            df["Length_of_Stay"] = pd.to_numeric(df["Length_of_Stay"], errors="coerce")
            df["Satisfaction"] = pd.to_numeric(df["Satisfaction"], errors="coerce")

            # Strip whitespace from string columns
            for col in ["Gender", "Condition", "Procedure", "Readmission", "Outcome"]:
                df[col] = df[col].astype(str).str.strip()

            # --- 3. Drop rows with NaN in critical numeric columns ---
            df.dropna(subset=["Age", "Cost", "Length_of_Stay", "Satisfaction"], inplace=True)
            df.reset_index(drop=True, inplace=True)

            self.df = df
            self.is_loaded = True
            print(f"HospitalAnalyticsService: Loaded {len(df)} records from dataset.")
        except Exception as exc:
            print(f"HospitalAnalyticsService: Error loading dataset -> {exc}")
            self.is_loaded = False

    def _require_loaded(self) -> None:
        if not self.is_loaded:
            raise RuntimeError("Dataset is not loaded. Check the data path and CSV integrity.")

    # ------------------------------------------------------------------
    # Public API – summary & KPIs
    # ------------------------------------------------------------------

    def get_kpi_summary(self) -> dict:
        """Return top-level KPIs computed from the full dataset."""
        self._require_loaded()
        df = self.df
        readmission_yes = (df["Readmission"].str.upper() == "YES").sum()
        return {
            "total_records": int(len(df)),
            "avg_age": round(float(df["Age"].mean()), 2),
            "avg_cost": round(float(df["Cost"].mean()), 2),
            "avg_los": round(float(df["Length_of_Stay"].mean()), 2),
            "readmission_rate": round(float(readmission_yes / len(df) * 100), 2),
            "avg_satisfaction": round(float(df["Satisfaction"].mean()), 2),
        }

    # ------------------------------------------------------------------
    # Distributions
    # ------------------------------------------------------------------

    def get_condition_distribution(self) -> list:
        """Return count of each medical condition."""
        self._require_loaded()
        counts = self.df["Condition"].value_counts().reset_index()
        counts.columns = ["condition", "count"]
        return counts.to_dict(orient="records")

    def get_procedure_distribution(self) -> list:
        """Return count of each procedure."""
        self._require_loaded()
        counts = self.df["Procedure"].value_counts().reset_index()
        counts.columns = ["procedure", "count"]
        return counts.to_dict(orient="records")

    def get_readmission_distribution(self) -> list:
        """Return readmission counts and percentages."""
        self._require_loaded()
        counts = self.df["Readmission"].value_counts().reset_index()
        counts.columns = ["readmission", "count"]
        total = counts["count"].sum()
        counts["percentage"] = (counts["count"] / total * 100).round(2)
        return counts.to_dict(orient="records")

    def get_outcome_distribution(self) -> list:
        """Return outcome counts and percentages."""
        self._require_loaded()
        counts = self.df["Outcome"].value_counts().reset_index()
        counts.columns = ["outcome", "count"]
        total = counts["count"].sum()
        counts["percentage"] = (counts["count"] / total * 100).round(2)
        return counts.to_dict(orient="records")

    def get_satisfaction_distribution(self) -> list:
        """Return count of each satisfaction score."""
        self._require_loaded()
        counts = (
            self.df["Satisfaction"]
            .astype(int)
            .value_counts()
            .sort_index()
            .reset_index()
        )
        counts.columns = ["satisfaction", "count"]
        return counts.to_dict(orient="records")

    def get_age_group_distribution(self) -> list:
        """Return patient counts for age bands: 25-34, 35-44, 45-54, 55-64, 65-78."""
        self._require_loaded()
        bins = [24, 34, 44, 54, 64, 78]
        labels = ["25-34", "35-44", "45-54", "55-64", "65-78"]
        age_groups = pd.cut(self.df["Age"], bins=bins, labels=labels)
        counts = age_groups.value_counts().sort_index().reset_index()
        counts.columns = ["age_group", "count"]
        counts["age_group"] = counts["age_group"].astype(str)
        return counts.to_dict(orient="records")

    def get_gender_distribution(self) -> list:
        """Return count of each gender."""
        self._require_loaded()
        counts = self.df["Gender"].value_counts().reset_index()
        counts.columns = ["gender", "count"]
        return counts.to_dict(orient="records")

    # ------------------------------------------------------------------
    # Cost analysis
    # ------------------------------------------------------------------

    def get_cost_by_condition(self) -> list:
        """Return avg/min/max cost grouped by condition."""
        self._require_loaded()
        grouped = (
            self.df.groupby("Condition")["Cost"]
            .agg(avg_cost="mean", min_cost="min", max_cost="max")
            .reset_index()
            .rename(columns={"Condition": "condition"})
        )
        grouped["avg_cost"] = grouped["avg_cost"].round(2)
        grouped["min_cost"] = grouped["min_cost"].round(2)
        grouped["max_cost"] = grouped["max_cost"].round(2)
        return grouped.sort_values("avg_cost", ascending=False).to_dict(orient="records")

    def get_cost_by_procedure(self) -> list:
        """Return avg cost grouped by procedure."""
        self._require_loaded()
        grouped = (
            self.df.groupby("Procedure")["Cost"]
            .mean()
            .round(2)
            .reset_index()
            .rename(columns={"Procedure": "procedure", "Cost": "avg_cost"})
        )
        return grouped.sort_values("avg_cost", ascending=False).to_dict(orient="records")

    def get_cost_stats(self) -> dict:
        """Return descriptive statistics for the Cost column."""
        self._require_loaded()
        cost = self.df["Cost"]
        return {
            "min": round(float(cost.min()), 2),
            "max": round(float(cost.max()), 2),
            "mean": round(float(cost.mean()), 2),
            "median": round(float(cost.median()), 2),
            "std": round(float(cost.std()), 2),
        }

    def get_cost_vs_los(self) -> list:
        """Return a sample of 200 records for Cost vs Length_of_Stay scatter plot."""
        self._require_loaded()
        sample = self.df[["Cost", "Length_of_Stay", "Condition"]].sample(
            n=min(200, len(self.df)), random_state=42
        )
        sample = sample.rename(columns={"Length_of_Stay": "length_of_stay", "Cost": "cost", "Condition": "condition"})
        return sample.to_dict(orient="records")

    # ------------------------------------------------------------------
    # Length of Stay analysis
    # ------------------------------------------------------------------

    def get_los_by_condition(self) -> list:
        """Return avg length-of-stay grouped by condition."""
        self._require_loaded()
        grouped = (
            self.df.groupby("Condition")["Length_of_Stay"]
            .mean()
            .round(2)
            .reset_index()
            .rename(columns={"Condition": "condition", "Length_of_Stay": "avg_los"})
        )
        return grouped.sort_values("avg_los", ascending=False).to_dict(orient="records")

    def get_los_by_procedure(self) -> list:
        """Return avg length-of-stay grouped by procedure."""
        self._require_loaded()
        grouped = (
            self.df.groupby("Procedure")["Length_of_Stay"]
            .mean()
            .round(2)
            .reset_index()
            .rename(columns={"Procedure": "procedure", "Length_of_Stay": "avg_los"})
        )
        return grouped.sort_values("avg_los", ascending=False).to_dict(orient="records")

    def get_los_by_outcome(self) -> list:
        """Return avg length-of-stay grouped by outcome."""
        self._require_loaded()
        grouped = (
            self.df.groupby("Outcome")["Length_of_Stay"]
            .mean()
            .round(2)
            .reset_index()
            .rename(columns={"Outcome": "outcome", "Length_of_Stay": "avg_los"})
        )
        return grouped.to_dict(orient="records")

    # ------------------------------------------------------------------
    # Readmission analysis
    # ------------------------------------------------------------------

    def get_readmission_by_condition(self) -> list:
        """Return readmission count, total, and rate per condition."""
        self._require_loaded()
        df = self.df.copy()
        df["is_readmitted"] = (df["Readmission"].str.upper() == "YES").astype(int)
        grouped = (
            df.groupby("Condition")["is_readmitted"]
            .agg(readmission_count="sum", total_count="count")
            .reset_index()
            .rename(columns={"Condition": "condition"})
        )
        grouped["rate"] = (grouped["readmission_count"] / grouped["total_count"] * 100).round(2)
        return grouped.sort_values("rate", ascending=False).to_dict(orient="records")

    def get_readmission_by_procedure(self) -> list:
        """Return readmission count, total, and rate per procedure."""
        self._require_loaded()
        df = self.df.copy()
        df["is_readmitted"] = (df["Readmission"].str.upper() == "YES").astype(int)
        grouped = (
            df.groupby("Procedure")["is_readmitted"]
            .agg(readmission_count="sum", total="count")
            .reset_index()
            .rename(columns={"Procedure": "procedure"})
        )
        grouped["rate"] = (grouped["readmission_count"] / grouped["total"] * 100).round(2)
        return grouped.sort_values("rate", ascending=False).to_dict(orient="records")

    # ------------------------------------------------------------------
    # Satisfaction analysis
    # ------------------------------------------------------------------

    def get_satisfaction_by_outcome(self) -> list:
        """Return avg satisfaction score grouped by outcome."""
        self._require_loaded()
        grouped = (
            self.df.groupby("Outcome")["Satisfaction"]
            .mean()
            .round(2)
            .reset_index()
            .rename(columns={"Outcome": "outcome", "Satisfaction": "avg_satisfaction"})
        )
        return grouped.to_dict(orient="records")

    # ------------------------------------------------------------------
    # Filtered stats
    # ------------------------------------------------------------------

    def get_filtered_stats(self, filters: dict) -> dict:
        """
        Return KPI summary after applying arbitrary filters.

        Supported filter keys:
            age_min, age_max   – int  (inclusive range)
            gender             – str  e.g. "Female"
            condition          – str  e.g. "Diabetes"
            procedure          – str
            readmission        – str  "Yes" | "No"
            outcome            – str  "Recovered" | "Stable"
            satisfaction       – int  (exact match)
        """
        self._require_loaded()
        df = self.df.copy()

        age_min = filters.get("age_min")
        age_max = filters.get("age_max")
        gender = filters.get("gender")
        condition = filters.get("condition")
        procedure = filters.get("procedure")
        readmission = filters.get("readmission")
        outcome = filters.get("outcome")
        satisfaction = filters.get("satisfaction")

        if age_min is not None:
            df = df[df["Age"] >= int(age_min)]
        if age_max is not None:
            df = df[df["Age"] <= int(age_max)]
        if gender:
            df = df[df["Gender"].str.lower() == str(gender).lower()]
        if condition:
            df = df[df["Condition"].str.lower() == str(condition).lower()]
        if procedure:
            df = df[df["Procedure"].str.lower() == str(procedure).lower()]
        if readmission:
            df = df[df["Readmission"].str.lower() == str(readmission).lower()]
        if outcome:
            df = df[df["Outcome"].str.lower() == str(outcome).lower()]
        if satisfaction is not None:
            df = df[df["Satisfaction"].astype(int) == int(satisfaction)]

        if df.empty:
            return {
                "total_records": 0,
                "avg_age": None,
                "avg_cost": None,
                "avg_los": None,
                "readmission_rate": None,
                "avg_satisfaction": None,
                "filters_applied": filters,
            }

        readmission_yes = (df["Readmission"].str.upper() == "YES").sum()
        return {
            "total_records": int(len(df)),
            "avg_age": round(float(df["Age"].mean()), 2),
            "avg_cost": round(float(df["Cost"].mean()), 2),
            "avg_los": round(float(df["Length_of_Stay"].mean()), 2),
            "readmission_rate": round(float(readmission_yes / len(df) * 100), 2),
            "avg_satisfaction": round(float(df["Satisfaction"].mean()), 2),
            "filters_applied": filters,
        }


# Global singleton
hospital_analytics_service = HospitalAnalyticsService()
