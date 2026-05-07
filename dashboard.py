import streamlit as st
import pandas as pd
import pyodbc
import plotly.express as px

# 1. Configuração da Ligação à BD local (SQL Server LocalDB)
def init_connection():
    for server in ["(localdb)\\MSSQLLocalDB", ".\\SQLEXPRESS", "localhost"]:
        conn_str = f"DRIVER={{ODBC Driver 17 for SQL Server}};SERVER={server};DATABASE=DATABASE_51619;Trusted_Connection=yes;Encrypt=no;"
        try:
            return pyodbc.connect(conn_str, autocommit=True)
        except Exception as e:
            st.warning(f"Falha con {server}: {e}")
    st.error("Nenhuma conexão possível.")
    return None

conn = init_connection()
if conn is None:
    st.stop()

# 2. Configuração da Página
st.set_page_config(page_title="IoT Dashboard - Estações", layout="wide")
st.title("📊 Monitorização de Estações em Tempo Real")
st.sidebar.header("Filtros de Visualização")

# 3. Carregar Dados da View 3FN que criámos
@st.cache_data(ttl=10) # Atualiza a cada 10 segundos
def load_data():
    # Caso a view não exista (aqui criaremos uma view simples chamada v_dashboard_monitorizacao):
    create_view_sql = """
    IF OBJECT_ID('dbo.v_dashboard_monitorizacao', 'V') IS NULL
    BEGIN
        EXEC('CREATE VIEW dbo.v_dashboard_monitorizacao AS
        SELECT TOP 100
            d.ID_Data,
            d.Valor,
            d.Timestamp,
            s.Tipo_Sensor,
            s.Unidade_Medida,
            st.Localizacao AS Nome_Grupo
        FROM [Data] d
        JOIN Sensor s ON d.ID_Sensor = s.ID_Sensor
        JOIN Station st ON s.ID_Station = st.ID_Station
        JOIN [Group] g ON st.ID_Group = g.ID_Group
        ORDER BY d.Timestamp DESC')
    END
    """
    conn.execute(create_view_sql)

    query = "SELECT TOP 100 * FROM dbo.v_dashboard_monitorizacao ORDER BY Timestamp DESC"
    return pd.read_sql(query, conn)

df = load_data()

# 4. Filtros na Barra Lateral
grupos = df['Nome_Grupo'].unique()
grupo_sel = st.sidebar.multiselect("Selecionar Grupo(s):", grupos, default=grupos)

# Filtrar o DataFrame
df_filtered = df[df['Nome_Grupo'].isin(grupo_sel)]

# 5. Layout de Cartões (KPIs)
if not df_filtered.empty:
    st.subheader("Últimas Leituras")
    cols = st.columns(len(df_filtered['Tipo_Sensor'].unique()))
    
    for i, sensor in enumerate(df_filtered['Tipo_Sensor'].unique()):
        ultima_leitura = df_filtered[df_filtered['Tipo_Sensor'] == sensor].iloc[0]
        cols[i].metric(
            label=f"{sensor} ({ultima_leitura['Nome_Grupo']})", 
            value=f"{ultima_leitura['Valor']} {ultima_leitura['Unidade_Medida']}",
            delta=f"🕒 {ultima_leitura['Timestamp'].strftime('%H:%M:%S')}",
            delta_color="off"
        )

# 6. Gráficos Temporais
st.divider()
st.subheader("Histórico de Dados")

if not df_filtered.empty:
    fig = px.line(
        df_filtered, 
        x="Timestamp", 
        y="Valor", 
        color="Tipo_Sensor",
        facet_col="Nome_Grupo",
        title="Evolução dos Sensores por Estação",
        labels={"Valor": "Leitura", "Timestamp": "Hora"},
        template="plotly_dark"
    )
    st.plotly_chart(fig, use_container_width=True)

# 7. Tabela de Dados Brutos
with st.expander("Ver Tabela de Dados Completa"):
    st.dataframe(df_filtered, use_container_width=True)

# Botão para forçar atualização
if st.button('🔄 Atualizar Dados Manualmente'):
    st.rerun()