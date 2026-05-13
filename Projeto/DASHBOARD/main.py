from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Optional
import pyodbc
from datetime import datetime

app = FastAPI(title="IoT Gateway API - DATABASE_51619")

# --- CONFIGURAÇÃO DA CONEXÃO SQL SERVER ---
def get_db_connection():
    try:
        # Ajusta o SERVER se necessário (Ex: '.\SQLEXPRESS' ou '.')
        conn_str = (
            "DRIVER={ODBC Driver 17 for SQL Server};"
            "SERVER=(localdb)\\MSSQLLocalDB;"
            "DATABASE=DATABASE_51619;"
            "Trusted_Connection=yes;"
            "Encrypt=no;"
        )
        return pyodbc.connect(conn_str)
    except Exception as e:
        print(f"Erro de conexão: {e}")
        return None

# --- MODELOS DE DADOS (Pydantic) ---
class ReadingCreate(BaseModel):
    valor: float
    id_sensor: int

class ReadingResponse(BaseModel):
    id_data: int
    valor: float
    timestamp: datetime
    id_sensor: int

# --- ENDPOINTS ---

@app.get("/")
def read_root():
    return {"status": "Online", "message": "API de Monitorização IoT"}

# 1. Receber dados do Raspberry Pi (POST)
@app.post("/api/sensor-data", status_code=201)
async def receive_sensor_data(data: ReadingCreate):
    conn = get_db_connection()
    if not conn:
        raise HTTPException(status_code=500, detail="Erro ao ligar à base de dados")
    
    try:
        cursor = conn.cursor()
        query = "INSERT INTO [Data] (Valor, ID_Sensor) VALUES (?, ?)"
        cursor.execute(query, (data.valor, data.id_sensor))
        conn.commit()
        return {"message": "Leitura registada com sucesso"}
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))
    finally:
        conn.close()

# 2. Listar últimas leituras para o Frontend (GET)
@app.get("/api/readings", response_model=List[ReadingResponse])
async def get_readings(limit: int = 20):
    conn = get_db_connection()
    if not conn:
        raise HTTPException(status_code=500, detail="Erro de BD")
    
    try:
        cursor = conn.cursor()
        # Query para buscar as últimas leituras
        query = f"SELECT TOP ({limit}) ID_Data, Valor, Timestamp, ID_Sensor FROM [Data] ORDER BY Timestamp DESC"
        cursor.execute(query)
        
        rows = cursor.fetchall()
        results = []
        for row in rows:
            results.append({
                "id_data": row[0],
                "valor": row[1],
                "timestamp": row[2],
                "id_sensor": row[3]
            })
        return results
    finally:
        conn.close()

# 3. Endpoint para ver status dos sensores
@app.get("/api/sensors-status")
@app.get("/api/sensor-status")
async def get_sensors():
    conn = get_db_connection()
    if not conn:
        raise HTTPException(status_code=500, detail="Erro ao ligar à base de dados")
    try:
        cursor = conn.cursor()
        cursor.execute(
            "SELECT s.ID_Sensor, st.Nome_Tipo, st.Unidade_Medida "
            "FROM Sensor s "
            "JOIN SensorType st ON s.ID_SensorType = st.ID_SensorType"
        )
        sensors = [{"id": r[0], "tipo": r[1], "unidade": r[2]} for r in cursor.fetchall()]
        return sensors
    finally:
        conn.close()

if __name__ == "__main__":
    import uvicorn
    # Corre a API em http://localhost:8000
    uvicorn.run(app, host="0.0.0.0", port=8000)