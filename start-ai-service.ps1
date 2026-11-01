Write-Host "Starting Medora AI Python FastAPI Service..." -ForegroundColor Green
cd ai-service
python -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
