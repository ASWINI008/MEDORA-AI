class GenAIService:
    def generate_explanation(self, agent_outputs, inputs):
        pred = agent_outputs.get("prediction", {})
        rec = agent_outputs.get("recommendation", {})
        flow = agent_outputs.get("patient_flow", {})
        staff = agent_outputs.get("staff", {})
        resource = agent_outputs.get("resource", {})
        amb = agent_outputs.get("ambulance", {})

        wait_time = pred.get("predicted_waiting_time_minutes", 30.0)
        risk = pred.get("congestion_risk", "MEDIUM")
        
        dept = inputs.get("department", "Emergency Department")

        # Synthesize structured natural language explanation
        problem = f"{dept} Congestion Analysis ({risk} Risk)"
        
        reason_parts = []
        if flow.get("metrics", {}).get("queue_length", 0) > 15:
            reason_parts.append(f"high patient queue ({flow.get('metrics', {}).get('queue_length', 0)} patients)")
        if staff.get("metrics", {}).get("staff_workload_percent", 0) > 70:
            reason_parts.append(f"elevated staff workload ({staff.get('metrics', {}).get('staff_workload_percent', 0)}%)")
        if resource.get("metrics", {}).get("available_beds", 0) <= 8:
            reason_parts.append(f"limited bed availability ({resource.get('metrics', {}).get('available_beds', 0)} beds)")
        if amb.get("metrics", {}).get("incoming_ambulance_count", 0) > 0:
            reason_parts.append(f"inbound ambulances ({amb.get('metrics', {}).get('incoming_ambulance_count', 0)} inbound)")

        reason = "Operational congestion is driven by " + (" combined with ".join(reason_parts) if reason_parts else "standard operational throughput requirements.")

        predicted_impact = f"Patient waiting duration is projected at ~{wait_time:.1f} minutes under current resource allocation."

        operational_recommendation = rec.get("recommendation", "Review staff allocation and available bed capacity according to hospital operational policy.")

        return {
            "genaiStatus": "GENAI_INTEGRATION_PENDING",
            "llm_connected": False,
            "genai_explanation": {
                "problem": problem,
                "reason": reason,
                "predicted_impact": predicted_impact,
                "recommendation": operational_recommendation,
                "disclaimer": "Operational decision-support only. Live LLM integration pending. Real LLM endpoint not connected yet; responses generated via multi-agent rule engine."
            }
        }

genai_service = GenAIService()

