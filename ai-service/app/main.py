from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from typing import Optional, Dict, Any, List

from app.services.ml_service import ml_service
from app.services.agent_service import agent_orchestrator
from app.services.genai_service import genai_service
from app.services.hospital_analytics_service import hospital_analytics_service
from app.services.hospital_ml_service import hospital_ml_service
from app.services.hospital_agent_service import hospital_agent_orchestrator

app = FastAPI(
    title="Medora AI - Machine Learning & Agentic Service",
    description="Python FastAPI service providing ML inferences, 6-agent orchestration, hospital dataset analytics, and GenAI explanations.",
    version="1.0.0"
)

# Enable CORS for Spring Boot & React Frontend
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class OperationalInput(BaseModel):
    department: Optional[str] = "Emergency Department"
    patient_arrival_rate: Optional[int] = 25
    queue_length: Optional[int] = 18
    average_waiting_time: Optional[float] = 35.0
    available_doctors: Optional[int] = 4
    available_nurses: Optional[int] = 8
    staff_workload: Optional[float] = 72.0
    available_beds: Optional[int] = 12
    bed_occupancy: Optional[float] = 78.0
    resource_utilization: Optional[float] = 78.0
    appointment_count: Optional[int] = 45
    cancellation_count: Optional[int] = 2
    department_workload: Optional[float] = 72.0
    ambulance_arrivals: Optional[int] = 3

class LOSPredictionInput(BaseModel):
    age: float = Field(..., description="Patient age")
    gender: str = Field(..., description="Gender (Male/Female)")
    condition: str = Field(..., description="Medical Condition")
    procedure: str = Field(..., description="Medical Procedure")
    cost: Optional[float] = Field(8000.0, description="Observed/estimated treatment cost")

class CostPredictionInput(BaseModel):
    age: float = Field(..., description="Patient age")
    gender: str = Field(..., description="Gender (Male/Female)")
    condition: str = Field(..., description="Medical Condition")
    procedure: str = Field(..., description="Medical Procedure")
    length_of_stay: Optional[float] = Field(5.0, description="Length of stay in days")

class ReadmissionPredictionInput(BaseModel):
    age: float = Field(..., description="Patient age")
    gender: str = Field(..., description="Gender (Male/Female)")
    condition: str = Field(..., description="Medical Condition")
    procedure: str = Field(..., description="Medical Procedure")
    cost: Optional[float] = Field(8000.0, description="Treatment cost")
    length_of_stay: Optional[float] = Field(5.0, description="Length of stay in days")

class FilteredStatsInput(BaseModel):
    age_min: Optional[int] = None
    age_max: Optional[int] = None
    gender: Optional[str] = None
    condition: Optional[str] = None
    procedure: Optional[str] = None
    readmission: Optional[str] = None
    outcome: Optional[str] = None
    satisfaction: Optional[int] = None

# ─────────────────────────────────────────────────────────────────────────────
# Existing Operational Endpoints (Preserved)
# ─────────────────────────────────────────────────────────────────────────────

@app.get("/")
@app.get("/api/ai/health")
def health_check():
    return {
        "service": "Medora AI Python Engine",
        "status": "ONLINE",
        "ml_loaded": ml_service.is_loaded,
        "hospital_analytics_loaded": hospital_analytics_service.is_loaded,
        "hospital_ml_loaded": hospital_ml_service.is_trained,
        "metadata": ml_service.metadata
    }

@app.get("/api/ai/metrics")
def get_metrics_endpoint():
    return {
        "status": "SUCCESS",
        "is_loaded": ml_service.is_loaded,
        "metadata": ml_service.metadata
    }

@app.post("/api/ai/predict")
def predict_endpoint(data: OperationalInput):
    inputs = data.model_dump()
    return ml_service.predict(inputs)

@app.post("/api/ai/agents/analyze")
def agent_analyze_endpoint(data: OperationalInput):
    inputs = data.model_dump()
    return agent_orchestrator.run_workflow(inputs)

@app.post("/api/ai/explain")
def genai_explain_endpoint(data: OperationalInput):
    inputs = data.model_dump()
    workflow_res = agent_orchestrator.run_workflow(inputs)
    explanation = genai_service.generate_explanation(workflow_res["agent_outputs"], inputs)
    
    return {
        "workflow": workflow_res,
        "explanation": explanation["genai_explanation"]
    }

# ─────────────────────────────────────────────────────────────────────────────
# Hospital Analytics Dataset Endpoints (Actual Kaggle Dataset)
# ─────────────────────────────────────────────────────────────────────────────

@app.get("/api/hospital/kpi")
def get_hospital_kpi():
    return hospital_analytics_service.get_kpi_summary()

@app.get("/api/hospital/condition-distribution")
def get_condition_distribution():
    return hospital_analytics_service.get_condition_distribution()

@app.get("/api/hospital/procedure-distribution")
def get_procedure_distribution():
    return hospital_analytics_service.get_procedure_distribution()

@app.get("/api/hospital/cost-by-condition")
def get_cost_by_condition():
    return hospital_analytics_service.get_cost_by_condition()

@app.get("/api/hospital/cost-by-procedure")
def get_cost_by_procedure():
    return hospital_analytics_service.get_cost_by_procedure()

@app.get("/api/hospital/los-by-condition")
def get_los_by_condition():
    return hospital_analytics_service.get_los_by_condition()

@app.get("/api/hospital/los-by-procedure")
def get_los_by_procedure():
    return hospital_analytics_service.get_los_by_procedure()

@app.get("/api/hospital/readmission-distribution")
def get_readmission_distribution():
    return hospital_analytics_service.get_readmission_distribution()

@app.get("/api/hospital/outcome-distribution")
def get_outcome_distribution():
    return hospital_analytics_service.get_outcome_distribution()

@app.get("/api/hospital/satisfaction-distribution")
def get_satisfaction_distribution():
    return hospital_analytics_service.get_satisfaction_distribution()

@app.get("/api/hospital/age-groups")
def get_age_groups():
    return hospital_analytics_service.get_age_group_distribution()

@app.get("/api/hospital/gender-distribution")
def get_gender_distribution():
    return hospital_analytics_service.get_gender_distribution()

@app.get("/api/hospital/cost-vs-los")
def get_cost_vs_los():
    return hospital_analytics_service.get_cost_vs_los()

@app.get("/api/hospital/readmission-by-condition")
def get_readmission_by_condition():
    return hospital_analytics_service.get_readmission_by_condition()

@app.get("/api/hospital/readmission-by-procedure")
def get_readmission_by_procedure():
    return hospital_analytics_service.get_readmission_by_procedure()

@app.get("/api/hospital/satisfaction-by-outcome")
def get_satisfaction_by_outcome():
    return hospital_analytics_service.get_satisfaction_by_outcome()

@app.get("/api/hospital/los-by-outcome")
def get_los_by_outcome():
    return hospital_analytics_service.get_los_by_outcome()

@app.get("/api/hospital/cost-stats")
def get_cost_stats():
    return hospital_analytics_service.get_cost_stats()

@app.post("/api/hospital/filtered-stats")
def get_filtered_stats(filters: FilteredStatsInput):
    clean_filters = {k: v for k, v in filters.model_dump().items() if v is not None and v != "All"}
    return hospital_analytics_service.get_filtered_stats(clean_filters)

# ─────────────────────────────────────────────────────────────────────────────
# Machine Learning Inferences & Metrics
# ─────────────────────────────────────────────────────────────────────────────

@app.get("/api/hospital/ml/metrics")
def get_hospital_ml_metrics():
    return hospital_ml_service.get_model_metrics()

@app.post("/api/hospital/ml/predict-los")
def predict_los_endpoint(data: LOSPredictionInput):
    cost_val = data.cost if data.cost is not None else 8000.0
    return hospital_ml_service.predict_los(
        age=data.age,
        gender=data.gender,
        condition=data.condition,
        procedure=data.procedure,
        cost=cost_val
    )

@app.post("/api/hospital/ml/predict-cost")
def predict_cost_endpoint(data: CostPredictionInput):
    los_val = data.length_of_stay if data.length_of_stay is not None else 5.0
    return hospital_ml_service.predict_cost(
        age=data.age,
        gender=data.gender,
        condition=data.condition,
        procedure=data.procedure,
        los=los_val
    )

@app.post("/api/hospital/ml/predict-readmission")
def predict_readmission_endpoint(data: ReadmissionPredictionInput):
    cost_val = data.cost if data.cost is not None else 8000.0
    los_val = data.length_of_stay if data.length_of_stay is not None else 5.0
    return hospital_ml_service.predict_readmission(
        age=data.age,
        gender=data.gender,
        condition=data.condition,
        procedure=data.procedure,
        cost=cost_val,
        los=los_val
    )

# ─────────────────────────────────────────────────────────────────────────────
# Multi-Agent AI Workflow & Recommendations
# ─────────────────────────────────────────────────────────────────────────────

@app.get("/api/hospital/agents/run")
def run_hospital_agents():
    return hospital_agent_orchestrator.run_all()

@app.get("/api/hospital/recommendations")
def get_hospital_recommendations():
    workflow_res = hospital_agent_orchestrator.run_all()
    rec_agent_out = workflow_res.get("agent_outputs", {}).get("recommendation", {})
    return rec_agent_out

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
