import uuid
import datetime
from app.services.ml_service import ml_service

class PatientFlowAgent:
    def analyze(self, inputs):
        department = inputs.get("department", "Emergency")
        queue_length = inputs.get("queue_length", inputs.get("queueLength", 10))
        waiting_time = inputs.get("avg_waiting_time", inputs.get("waiting_time", 25.0))
        arrival_rate = inputs.get("patient_arrival_rate", inputs.get("patient_flow", 20))

        if queue_length > 25 or arrival_rate > 35:
            queue_status = "HIGH QUEUE"
            insight = f"High patient queue in {department} ({queue_length} patients). Average wait time is {waiting_time} mins."
            bottleneck = "Triage velocity below incoming arrival rate"
        elif queue_length > 15:
            queue_status = "MODERATE QUEUE"
            insight = f"Moderate queue accumulation ({queue_length} patients). Average wait time is {waiting_time} mins."
            bottleneck = "Consultation throughput bottleneck"
        else:
            queue_status = "NORMAL QUEUE"
            insight = f"Patient flow operating smoothly with {queue_length} waiting."
            bottleneck = "None"

        return {
            "agent_name": "Patient Flow Agent",
            "department": department,
            "queue_status": queue_status,
            "waiting_time_insight": insight,
            "flow_bottleneck_indicators": bottleneck,
            "metrics": {
                "queue_length": queue_length,
                "waiting_time_minutes": waiting_time,
                "arrival_rate": arrival_rate
            }
        }

class StaffAgent:
    def analyze(self, inputs):
        department = inputs.get("department", "Emergency")
        doctors = inputs.get("available_doctors", inputs.get("availableDoctors", 4))
        nurses = inputs.get("available_nurses", inputs.get("nurses", 8))
        workload = inputs.get("department_workload", inputs.get("staff_workload", 60.0))

        if doctors <= 2 or nurses <= 4 or workload > 80.0:
            staffing_status = "LOW CAPACITY"
            insight = f"Critical staff constraint in {department}: {doctors} doctor(s), {nurses} nurse(s) on duty. Staff workload at {workload}%."
        elif doctors <= 3 or nurses <= 6 or workload > 70.0:
            staffing_status = "MODERATE CAPACITY"
            insight = f"Elevated staff workload in {department} ({workload}%). {doctors} doctors and {nurses} nurses available."
        else:
            staffing_status = "SUFFICIENT CAPACITY"
            insight = f"Staffing level optimal across {department} with workload at {workload}%."

        return {
            "agent_name": "Staff Agent",
            "department": department,
            "staffing_status": staffing_status,
            "workload_insight": insight,
            "metrics": {
                "available_doctors": doctors,
                "available_nurses": nurses,
                "staff_workload_percent": workload
            }
        }

class ResourceAgent:
    def analyze(self, inputs):
        beds = inputs.get("available_beds", inputs.get("beds", 10))
        rooms = inputs.get("available_rooms", inputs.get("rooms", 5))
        resources = inputs.get("available_resources", inputs.get("resources", "Sufficient"))
        utilization = inputs.get("resource_utilization", inputs.get("utilization", 70.0))

        if beds <= 3 or utilization > 90.0:
            resource_availability = "HIGH UTILIZATION"
            insight = f"Critical bed & resource pressure: {beds} bed(s), {rooms} room(s) available. Capacity utilization at {utilization}%."
        elif beds <= 8 or utilization > 80.0:
            resource_availability = "MODERATE UTILIZATION"
            insight = f"Resource buffer restricted: {beds} bed(s) available ({utilization}% utilization)."
        else:
            resource_availability = "SUFFICIENT CAPACITY"
            insight = f"Resource capacity is healthy with {beds} bed(s) available ({utilization}% utilization)."

        return {
            "agent_name": "Resource Agent",
            "resource_availability": resource_availability,
            "capacity_insight": insight,
            "metrics": {
                "available_beds": beds,
                "available_rooms": rooms,
                "resource_status": resources,
                "utilization_percent": utilization
            }
        }

class AppointmentAgent:
    def analyze(self, inputs):
        count = inputs.get("appointment_count", inputs.get("appointmentCount", 40))
        scheduled = inputs.get("scheduled_appointments", inputs.get("scheduled", 30))
        completed = inputs.get("completed_appointments", inputs.get("completed", 8))
        cancelled = inputs.get("cancelled_appointments", inputs.get("cancelled", 2))

        if count > 75 or scheduled > 50:
            pressure = "HIGH PRESSURE"
            insight = f"High scheduling volume ({count} appointments total, {scheduled} upcoming). Peak intake expected."
        elif count > 40:
            pressure = "MODERATE PRESSURE"
            insight = f"Moderate appointment volume ({count} total, {completed} completed, {cancelled} cancelled)."
        else:
            pressure = "BALANCED PRESSURE"
            insight = f"Standard appointment load ({count} appointments)."

        return {
            "agent_name": "Appointment Agent",
            "appointment_workload": f"{count} total ({scheduled} scheduled, {completed} completed, {cancelled} cancelled)",
            "scheduling_pressure": pressure,
            "metrics": {
                "appointment_count": count,
                "scheduled": scheduled,
                "completed": completed,
                "cancelled": cancelled
            }
        }

class AmbulanceAgent:
    def analyze(self, inputs):
        incoming = inputs.get("incoming_ambulance_count", inputs.get("incomingAmbulances", 2))
        eta = inputs.get("eta_minutes", inputs.get("eta", 12))
        ed_capacity = inputs.get("emergency_department_capacity", inputs.get("edCapacity", 85.0))

        if incoming >= 3 or (incoming >= 1 and ed_capacity >= 85.0):
            pressure = "HIGH OPERATIONAL PRESSURE"
            readiness = f"Incoming payload alert: {incoming} ambulance(s) inbound (ETA ~{eta} mins). ED capacity at {ed_capacity}%."
        elif incoming >= 1:
            pressure = "MODERATE OPERATIONAL PRESSURE"
            readiness = f"{incoming} inbound ambulance(s) with ETA of ~{eta} mins. ED capacity at {ed_capacity}%."
        else:
            pressure = "NORMAL OPERATIONAL PRESSURE"
            readiness = "No immediate high-priority inbound ambulance influx detected."

        return {
            "agent_name": "Ambulance Agent",
            "expected_operational_pressure": pressure,
            "intake_readiness": readiness,
            "metrics": {
                "incoming_ambulance_count": incoming,
                "eta_minutes": eta,
                "emergency_department_capacity": ed_capacity
            }
        }

class PredictionAgent:
    def analyze(self, inputs):
        ml_res = ml_service.predict(inputs)

        wait_time = ml_res.get("predicted_waiting_time_minutes", 30.0)
        risk = ml_res.get("congestion_risk", "MEDIUM")
        cluster = ml_res.get("workload_cluster", "MODERATE WORKLOAD")

        wait_insight = f"Model predicts average waiting time of {wait_time:.1f} minutes for incoming arrivals."
        congestion_insight = f"XGBoost classifier evaluates departmental congestion risk as {risk} (Cluster: {cluster})."

        return {
            "agent_name": "Prediction Agent",
            "waiting_time_prediction_interpretation": wait_insight,
            "congestion_interpretation": congestion_insight,
            "predicted_waiting_time_minutes": wait_time,
            "congestion_risk": risk,
            "workload_cluster": cluster,
            "model_status": ml_res.get("model_status", "ACTIVE")
        }

class RecommendationAgent:
    def analyze(self, flow_res, staff_res, resource_res, appt_res, amb_res, pred_res, department="Emergency Department"):
        queue_st = flow_res.get("queue_status", "NORMAL QUEUE")
        staff_st = staff_res.get("staffing_status", "SUFFICIENT CAPACITY")
        res_st = resource_res.get("resource_availability", "SUFFICIENT CAPACITY")
        risk_st = pred_res.get("congestion_risk", "LOW")
        amb_st = amb_res.get("expected_operational_pressure", "NORMAL OPERATIONAL PRESSURE")

        # Operational decision support synthesis
        if queue_st == "HIGH QUEUE" or staff_st == "LOW CAPACITY" or res_st == "HIGH UTILIZATION" or risk_st in ["HIGH", "CRITICAL"] or amb_st == "HIGH OPERATIONAL PRESSURE":
            priority = "HIGH"
            recommendation = (
                f"{department} operational load is high. "
                "Review staff allocation and available capacity according to hospital operational policy."
            )
            reasoning = (
                f"Synthesized Multi-Agent Trigger: Patient Flow ({queue_st}), Staffing ({staff_st}), "
                f"Resource ({res_st}), Ambulance ({amb_st}), ML Risk ({risk_st})."
            )
        elif queue_st == "MODERATE QUEUE" or staff_st == "MODERATE CAPACITY" or res_st == "MODERATE UTILIZATION" or risk_st == "MEDIUM":
            priority = "MEDIUM"
            recommendation = (
                f"{department} operating under moderate pressure. "
                "Monitor triage throughput and prepare float nursing support if arrivals spike."
            )
            reasoning = (
                f"Moderate indicators detected across Patient Flow ({queue_st}) and Staff Load ({staff_st})."
            )
        else:
            priority = "LOW"
            recommendation = (
                f"{department} operational metrics remain stable. "
                "Maintain standard department workflow and resource monitoring."
            )
            reasoning = "All 7 Multi-Agent metrics report normal operational limits."

        return {
            "agent_name": "Recommendation Agent",
            "priority": priority,
            "recommendation": recommendation,
            "reasoning": reasoning
        }

class AgentOrchestrator:
    def run_workflow(self, inputs):
        exec_id = str(uuid.uuid4())[:8]
        timestamp = datetime.datetime.now().isoformat()
        dept = inputs.get("department", "Emergency Department")

        # Instantiate 7 agents
        flow_agent = PatientFlowAgent()
        staff_agent = StaffAgent()
        resource_agent = ResourceAgent()
        appt_agent = AppointmentAgent()
        amb_agent = AmbulanceAgent()
        pred_agent = PredictionAgent()
        rec_agent = RecommendationAgent()

        # Execute multi-agent chain
        flow_res = flow_agent.analyze(inputs)
        staff_res = staff_agent.analyze(inputs)
        resource_res = resource_agent.analyze(inputs)
        appt_res = appt_agent.analyze(inputs)
        amb_res = amb_agent.analyze(inputs)
        pred_res = pred_agent.analyze(inputs)
        rec_res = rec_agent.analyze(flow_res, staff_res, resource_res, appt_res, amb_res, pred_res, department=dept)

        agents_status = [
            {"name": "Patient Flow Agent", "status": "ACTIVE", "last_run": timestamp},
            {"name": "Staff Agent", "status": "ACTIVE", "last_run": timestamp},
            {"name": "Resource Agent", "status": "ACTIVE", "last_run": timestamp},
            {"name": "Appointment Agent", "status": "ACTIVE", "last_run": timestamp},
            {"name": "Ambulance Agent", "status": "ACTIVE", "last_run": timestamp},
            {"name": "Prediction Agent", "status": "ACTIVE", "last_run": timestamp},
            {"name": "Recommendation Agent", "status": "ACTIVE", "last_run": timestamp}
        ]

        return {
            "execution_id": f"EXEC-{exec_id}",
            "timestamp": timestamp,
            "workflow_status": "COMPLETED",
            "ai_agent_engine_status": "ACTIVE",
            "agents": agents_status,
            "agent_outputs": {
                "patient_flow": flow_res,
                "staff": staff_res,
                "resource": resource_res,
                "appointment": appt_res,
                "ambulance": amb_res,
                "prediction": pred_res,
                "recommendation": rec_res
            }
        }

agent_orchestrator = AgentOrchestrator()

