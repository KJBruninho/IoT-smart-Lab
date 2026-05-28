async function carregarDados(url){const res=await fetch(url);return await res.json();}
function criarGrafico(canvasId,label,dados){const ctx=document.getElementById(canvasId);const labels=dados.map(d=>d.hora);const valores=dados.map(d=>d.valor);return new Chart(ctx,{type:"line",data:{labels:labels,datasets:[{label:label,data:valores,tension:.35,borderWidth:3,pointRadius:3}]},options:{responsive:true,plugins:{legend:{display:false}},scales:{y:{beginAtZero:false}}}})}
async function atualizarGraficos(){try{const temperatura=await carregarDados("/api/temperatura");const tds=await carregarDados("/api/tds");criarGrafico("temperaturaChart","Temperatura ºC",temperatura);criarGrafico("tdsChart","TDS ppm",tds)}catch(e){console.error("Erro ao carregar gráficos",e)}}
atualizarGraficos();
