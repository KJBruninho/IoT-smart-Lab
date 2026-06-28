package com.iotroom.iotroom.service.aluno;

import com.iotroom.iotroom.dto.aluno.AlunoLeituraDTO;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.AxisCrosses;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.BarDirection;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.MarkerStyle;
import org.apache.poi.xddf.usermodel.chart.XDDFBarChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFLineChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AlunoExcelExportService {

    private static final DateTimeFormatter DATA_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public byte[] gerarExcel(List<AlunoLeituraDTO> leituras) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Estilos estilos = criarEstilos(workbook);

            XSSFSheet sheetLeituras = workbook.createSheet("Leituras");
            XSSFSheet sheetEstatisticas = workbook.createSheet("Estatisticas");
            XSSFSheet sheetGraficosEstatisticos = workbook.createSheet("Graficos Estatisticos");
            XSSFSheet sheetGraficosSensores = workbook.createSheet("Graficos Sensores");
            XSSFSheet sheetDadosGraficos = workbook.createSheet("_dados_graficos");

            escreverLeituras(sheetLeituras, leituras, estilos);

            List<EstatisticaSerie> estatisticas = calcularEstatisticas(leituras);
            escreverEstatisticas(sheetEstatisticas, estatisticas, estilos);
            escreverGraficosEstatisticos(sheetGraficosEstatisticos, sheetEstatisticas, estatisticas, estilos);

            Map<String, List<SerieGraficoAluno>> seriesPorTipo = criarSeriesGraficoAluno(leituras);
            escreverGraficosSensores(sheetGraficosSensores, sheetDadosGraficos, seriesPorTipo, estilos);

            workbook.setSheetHidden(workbook.getSheetIndex(sheetDadosGraficos), true);
            workbook.setActiveSheet(workbook.getSheetIndex(sheetLeituras));

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível gerar o ficheiro Excel.", e);
        }
    }

    private void escreverLeituras(XSSFSheet sheet, List<AlunoLeituraDTO> leituras, Estilos estilos) {
        String[] headers = {
                "Data",
                "Grupo",
                "Experiência",
                "Estação",
                "Device ID",
                "Sensor",
                "Tipo",
                "Valor",
                "Unidade"
        };

        Row header = sheet.createRow(0);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(estilos.header);
        }

        int rowIndex = 1;

        for (AlunoLeituraDTO leitura : leituras) {
            Row row = sheet.createRow(rowIndex++);

            row.createCell(0).setCellValue(leitura.dataRegisto() != null ? leitura.dataRegisto().format(DATA_FORMATTER) : "");
            row.createCell(1).setCellValue(texto(leitura.grupoNome()));
            row.createCell(2).setCellValue(texto(leitura.experienciaNome()));
            row.createCell(3).setCellValue(texto(leitura.estacaoNome()));
            row.createCell(4).setCellValue(texto(leitura.deviceId()));
            row.createCell(5).setCellValue(texto(leitura.sensorNome()));
            row.createCell(6).setCellValue(texto(leitura.tipoSensor()));

            Cell valorCell = row.createCell(7);
            valorCell.setCellValue(numero(leitura.valor()));
            valorCell.setCellStyle(estilos.numero);

            row.createCell(8).setCellValue(texto(leitura.unidade()));
        }

        sheet.createFreezePane(0, 1);
        sheet.setAutoFilter(new CellRangeAddress(0, Math.max(0, rowIndex - 1), 0, headers.length - 1));
        ajustarColunas(sheet, headers.length);
    }

    private List<EstatisticaSerie> calcularEstatisticas(List<AlunoLeituraDTO> leituras) {
        Map<String, Acumulador> acumuladores = new LinkedHashMap<>();

        for (AlunoLeituraDTO leitura : leituras) {
            String tipo = texto(leitura.tipoSensor());
            String serie = nomeSerieAluno(leitura);
            String experiencia = texto(leitura.experienciaNome());
            String unidade = texto(leitura.unidade());

            String chave = tipo + "|" + leitura.sensorId() + "|" + leitura.estacaoId() + "|" + leitura.experienciaId() + "|" + unidade;

            Acumulador acumulador = acumuladores.computeIfAbsent(
                    chave,
                    key -> new Acumulador(tipo, serie, experiencia, unidade)
            );

            acumulador.adicionar(numero(leitura.valor()));
        }

        List<EstatisticaSerie> resultado = new ArrayList<>();

        for (Acumulador acumulador : acumuladores.values()) {
            resultado.add(acumulador.toEstatistica());
        }

        return resultado;
    }

    private void escreverEstatisticas(XSSFSheet sheet, List<EstatisticaSerie> estatisticas, Estilos estilos) {
        String[] headers = {
                "Tipo",
                "Série",
                "Experiência",
                "Unidade",
                "N",
                "Mínimo",
                "Média",
                "Máximo",
                "Desvio padrão"
        };

        Row header = sheet.createRow(0);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(estilos.header);
        }

        int rowIndex = 1;

        for (EstatisticaSerie estatistica : estatisticas) {
            Row row = sheet.createRow(rowIndex++);

            row.createCell(0).setCellValue(estatistica.tipo());
            row.createCell(1).setCellValue(estatistica.serie());
            row.createCell(2).setCellValue(estatistica.experiencia());
            row.createCell(3).setCellValue(estatistica.unidade());
            row.createCell(4).setCellValue(estatistica.quantidade());

            criarNumero(row, 5, estatistica.minimo(), estilos.numero);
            criarNumero(row, 6, estatistica.media(), estilos.numero);
            criarNumero(row, 7, estatistica.maximo(), estilos.numero);
            criarNumero(row, 8, estatistica.desvioPadrao(), estilos.numero);
        }

        sheet.createFreezePane(0, 1);
        sheet.setAutoFilter(new CellRangeAddress(0, Math.max(0, rowIndex - 1), 0, headers.length - 1));
        ajustarColunas(sheet, headers.length);
    }

    private void escreverGraficosEstatisticos(
            XSSFSheet sheetGraficos,
            XSSFSheet sheetEstatisticas,
            List<EstatisticaSerie> estatisticas,
            Estilos estilos
    ) {
        Row title = sheetGraficos.createRow(0);
        Cell titleCell = title.createCell(0);
        titleCell.setCellValue("Gráficos estatísticos");
        titleCell.setCellStyle(estilos.titulo);

        Row info = sheetGraficos.createRow(1);
        info.createCell(0).setCellValue("Estatísticas calculadas com todos os dados filtrados exportados, sem limite visual da página.");

        if (estatisticas.isEmpty()) {
            Row empty = sheetGraficos.createRow(3);
            empty.createCell(0).setCellValue("Sem dados para gerar gráficos estatísticos.");
            return;
        }

        criarGraficoBarras(
                sheetGraficos,
                sheetEstatisticas,
                "Média por série",
                1,
                estatisticas.size(),
                6,
                0,
                3,
                12,
                22
        );

        criarGraficoBarras(
                sheetGraficos,
                sheetEstatisticas,
                "Quantidade de leituras por série",
                1,
                estatisticas.size(),
                4,
                0,
                24,
                12,
                43
        );

        criarGraficoBarras(
                sheetGraficos,
                sheetEstatisticas,
                "Desvio padrão por série",
                1,
                estatisticas.size(),
                8,
                13,
                3,
                25,
                22
        );

        sheetGraficos.setColumnWidth(0, 24 * 256);
        sheetGraficos.setColumnWidth(1, 24 * 256);
        sheetGraficos.setColumnWidth(2, 24 * 256);
    }

    private Map<String, List<SerieGraficoAluno>> criarSeriesGraficoAluno(List<AlunoLeituraDTO> leituras) {
        Map<String, LinkedHashMap<String, SerieGraficoAlunoBuilder>> acumuladores = new LinkedHashMap<>();

        for (AlunoLeituraDTO leitura : leituras) {
            String tipo = textoOuPadrao(leitura.tipoSensor(), "SENSOR");
            String unidade = texto(leitura.unidade());

            LinkedHashMap<String, SerieGraficoAlunoBuilder> seriesDoTipo = acumuladores.computeIfAbsent(
                    tipo,
                    key -> new LinkedHashMap<>()
            );

            String chaveSerie = leitura.sensorId()
                    + "|"
                    + leitura.estacaoId()
                    + "|"
                    + leitura.experienciaId();

            SerieGraficoAlunoBuilder builder = seriesDoTipo.computeIfAbsent(
                    chaveSerie,
                    key -> new SerieGraficoAlunoBuilder(tipo, nomeSerieAluno(leitura), unidade)
            );

            builder.pontos.add(new PontoGraficoAluno(
                    leitura.dataRegisto() != null ? leitura.dataRegisto().format(DATA_FORMATTER) : "",
                    numero(leitura.valor())
            ));
        }

        Map<String, List<SerieGraficoAluno>> resultado = new LinkedHashMap<>();

        for (Map.Entry<String, LinkedHashMap<String, SerieGraficoAlunoBuilder>> entry : acumuladores.entrySet()) {
            List<SerieGraficoAluno> series = new ArrayList<>();

            for (SerieGraficoAlunoBuilder builder : entry.getValue().values()) {
                series.add(new SerieGraficoAluno(
                        builder.tipo,
                        builder.nome,
                        builder.unidade,
                        builder.pontos
                ));
            }

            resultado.put(entry.getKey(), series);
        }

        return resultado;
    }

    private void escreverGraficosSensores(
            XSSFSheet sheetGraficos,
            XSSFSheet sheetDados,
            Map<String, List<SerieGraficoAluno>> seriesPorTipo,
            Estilos estilos
    ) {
        Row title = sheetGraficos.createRow(0);
        Cell titleCell = title.createCell(0);
        titleCell.setCellValue("Gráficos dos sensores");
        titleCell.setCellStyle(estilos.titulo);

        Row info = sheetGraficos.createRow(1);
        info.createCell(0).setCellValue("Estes gráficos recriam os gráficos da área Aluno com todos os dados filtrados exportados, sem aplicar limite visual.");

        if (seriesPorTipo.isEmpty()) {
            Row empty = sheetGraficos.createRow(3);
            empty.createCell(0).setCellValue("Sem dados para gerar gráficos dos sensores.");
            return;
        }

        int dadosStartRow = 0;
        int chartTopRow = 3;

        for (Map.Entry<String, List<SerieGraficoAluno>> entry : seriesPorTipo.entrySet()) {
            String tipo = entry.getKey();
            List<SerieGraficoAluno> series = entry.getValue();

            if (series.isEmpty()) {
                continue;
            }

            BlocoDadosGrafico bloco = escreverBlocoDadosGrafico(sheetDados, tipo, series, dadosStartRow);

            criarGraficoLinhasSensor(
                    sheetGraficos,
                    sheetDados,
                    tituloGraficoSensor(tipo, series),
                    bloco.firstDataRow(),
                    bloco.lastDataRow(),
                    bloco.firstSeriesColumn(),
                    bloco.lastSeriesColumn(),
                    0,
                    chartTopRow,
                    18,
                    chartTopRow + 20
            );

            dadosStartRow = bloco.nextStartRow();
            chartTopRow += 23;
        }

        sheetGraficos.setColumnWidth(0, 24 * 256);
        sheetGraficos.setColumnWidth(1, 24 * 256);
        sheetGraficos.setColumnWidth(2, 24 * 256);
    }

    private BlocoDadosGrafico escreverBlocoDadosGrafico(
            XSSFSheet sheet,
            String tipo,
            List<SerieGraficoAluno> series,
            int startRow
    ) {
        Row title = sheet.createRow(startRow);
        title.createCell(0).setCellValue(tipo);

        Row header = sheet.createRow(startRow + 1);
        header.createCell(0).setCellValue("Leitura");

        for (int i = 0; i < series.size(); i++) {
            header.createCell(i + 1).setCellValue(series.get(i).nome());
        }

        int maxPontos = 0;

        for (SerieGraficoAluno serie : series) {
            maxPontos = Math.max(maxPontos, serie.pontos().size());
        }

        for (int pontoIndex = 0; pontoIndex < maxPontos; pontoIndex++) {
            Row row = sheet.createRow(startRow + 2 + pontoIndex);
            row.createCell(0).setCellValue(pontoIndex + 1);

            for (int serieIndex = 0; serieIndex < series.size(); serieIndex++) {
                SerieGraficoAluno serie = series.get(serieIndex);

                if (pontoIndex < serie.pontos().size()) {
                    row.createCell(serieIndex + 1).setCellValue(serie.pontos().get(pontoIndex).valor());
                }
            }
        }

        return new BlocoDadosGrafico(
                startRow + 2,
                startRow + 1 + maxPontos,
                1,
                series.size(),
                startRow + maxPontos + 4
        );
    }

    private void criarGraficoLinhasSensor(
            XSSFSheet sheetGraficos,
            XSSFSheet sheetDados,
            String titulo,
            int firstDataRow,
            int lastDataRow,
            int firstSeriesColumn,
            int lastSeriesColumn,
            int col1,
            int row1,
            int col2,
            int row2
    ) {
        if (lastDataRow < firstDataRow || lastSeriesColumn < firstSeriesColumn) {
            return;
        }

        XSSFDrawing drawing = sheetGraficos.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, col1, row1, col2, row2);

        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText(titulo);
        chart.setTitleOverlay(false);

        chart.getOrAddLegend().setPosition(LegendPosition.BOTTOM);

        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        bottomAxis.setTitle("Número da leitura");

        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle("Valor");
        leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);

        XDDFChartData data = chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);

        XDDFDataSource<Double> categorias = XDDFDataSourcesFactory.fromNumericCellRange(
                sheetDados,
                new CellRangeAddress(firstDataRow, lastDataRow, 0, 0)
        );

        int headerRow = firstDataRow - 1;

        for (int column = firstSeriesColumn; column <= lastSeriesColumn; column++) {
            XDDFNumericalDataSource<Double> valores = XDDFDataSourcesFactory.fromNumericCellRange(
                    sheetDados,
                    new CellRangeAddress(firstDataRow, lastDataRow, column, column)
            );

            XDDFChartData.Series serie = data.addSeries(categorias, valores);
            String nomeSerie = sheetDados.getRow(headerRow).getCell(column).getStringCellValue();
            serie.setTitle(nomeSerie, null);

            if (serie instanceof XDDFLineChartData.Series) {
                XDDFLineChartData.Series linha = (XDDFLineChartData.Series) serie;
                linha.setSmooth(false);
                linha.setMarkerStyle(MarkerStyle.NONE);
            }
        }

        chart.plot(data);
    }

    private void criarGraficoBarras(
            XSSFSheet sheetGraficos,
            XSSFSheet sheetEstatisticas,
            String titulo,
            int firstDataRow,
            int lastDataRow,
            int valueColumn,
            int col1,
            int row1,
            int col2,
            int row2
    ) {
        XSSFDrawing drawing = sheetGraficos.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, col1, row1, col2, row2);

        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText(titulo);
        chart.setTitleOverlay(false);

        chart.getOrAddLegend().setPosition(LegendPosition.BOTTOM);

        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        bottomAxis.setTitle("Série");

        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle("Valor");
        leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);

        XDDFChartData data = chart.createData(ChartTypes.BAR, bottomAxis, leftAxis);
        XDDFBarChartData barData = (XDDFBarChartData) data;
        barData.setBarDirection(BarDirection.COL);

        XDDFDataSource<String> categorias = XDDFDataSourcesFactory.fromStringCellRange(
                sheetEstatisticas,
                new CellRangeAddress(firstDataRow, lastDataRow, 1, 1)
        );

        XDDFNumericalDataSource<Double> valores = XDDFDataSourcesFactory.fromNumericCellRange(
                sheetEstatisticas,
                new CellRangeAddress(firstDataRow, lastDataRow, valueColumn, valueColumn)
        );

        XDDFChartData.Series serie = data.addSeries(categorias, valores);
        serie.setTitle(titulo, null);

        chart.plot(data);
    }

    private Estilos criarEstilos(XSSFWorkbook workbook) {
        CreationHelper creationHelper = workbook.getCreationHelper();

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        CellStyle header = workbook.createCellStyle();
        header.setFont(headerFont);
        header.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
        header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        header.setBorderBottom(BorderStyle.THIN);

        Font tituloFont = workbook.createFont();
        tituloFont.setBold(true);
        tituloFont.setFontHeightInPoints((short) 16);

        CellStyle titulo = workbook.createCellStyle();
        titulo.setFont(tituloFont);

        CellStyle numero = workbook.createCellStyle();
        numero.setDataFormat(creationHelper.createDataFormat().getFormat("0.00"));

        return new Estilos(header, titulo, numero);
    }

    private void criarNumero(Row row, int coluna, double valor, CellStyle style) {
        Cell cell = row.createCell(coluna);
        cell.setCellValue(valor);
        cell.setCellStyle(style);
    }

    private void ajustarColunas(XSSFSheet sheet, int totalColunas) {
        for (int i = 0; i < totalColunas; i++) {
            sheet.autoSizeColumn(i);

            int larguraAtual = sheet.getColumnWidth(i);
            int larguraMaxima = 42 * 256;

            if (larguraAtual > larguraMaxima) {
                sheet.setColumnWidth(i, larguraMaxima);
            }
        }
    }

    private String tituloGraficoSensor(String tipo, List<SerieGraficoAluno> series) {
        String unidade = "";

        for (SerieGraficoAluno serie : series) {
            if (serie.unidade() != null && !serie.unidade().isBlank()) {
                unidade = serie.unidade();
                break;
            }
        }

        return unidade.isBlank() ? tipo : tipo + " (" + unidade + ")";
    }

    private String nomeSerieAluno(AlunoLeituraDTO leitura) {
        return textoOuPadrao(leitura.sensorNome(), "Sensor")
                + " · "
                + textoOuPadrao(leitura.estacaoNome(), "Estação");
    }

    private String texto(String valor) {
        return valor != null ? valor : "";
    }

    private String textoOuPadrao(String valor, String padrao) {
        return valor != null && !valor.isBlank() ? valor : padrao;
    }

    private double numero(BigDecimal valor) {
        return valor != null ? valor.doubleValue() : 0.0;
    }

    private record Estilos(
            CellStyle header,
            CellStyle titulo,
            CellStyle numero
    ) {
    }

    private record EstatisticaSerie(
            String tipo,
            String serie,
            String experiencia,
            String unidade,
            long quantidade,
            double minimo,
            double media,
            double maximo,
            double desvioPadrao
    ) {
    }

    private record PontoGraficoAluno(
            String label,
            double valor
    ) {
    }

    private record SerieGraficoAluno(
            String tipo,
            String nome,
            String unidade,
            List<PontoGraficoAluno> pontos
    ) {
    }

    private record BlocoDadosGrafico(
            int firstDataRow,
            int lastDataRow,
            int firstSeriesColumn,
            int lastSeriesColumn,
            int nextStartRow
    ) {
    }

    private static class SerieGraficoAlunoBuilder {
        private final String tipo;
        private final String nome;
        private final String unidade;
        private final List<PontoGraficoAluno> pontos = new ArrayList<>();

        private SerieGraficoAlunoBuilder(String tipo, String nome, String unidade) {
            this.tipo = tipo;
            this.nome = nome;
            this.unidade = unidade;
        }
    }

    private static class Acumulador {
        private final String tipo;
        private final String serie;
        private final String experiencia;
        private final String unidade;

        private long quantidade = 0;
        private double soma = 0;
        private double somaQuadrados = 0;
        private double minimo = Double.MAX_VALUE;
        private double maximo = -Double.MAX_VALUE;

        private Acumulador(String tipo, String serie, String experiencia, String unidade) {
            this.tipo = tipo;
            this.serie = serie;
            this.experiencia = experiencia;
            this.unidade = unidade;
        }

        private void adicionar(double valor) {
            quantidade++;
            soma += valor;
            somaQuadrados += valor * valor;
            minimo = Math.min(minimo, valor);
            maximo = Math.max(maximo, valor);
        }

        private EstatisticaSerie toEstatistica() {
            double media = quantidade > 0 ? soma / quantidade : 0;
            double variancia = quantidade > 0 ? (somaQuadrados / quantidade) - (media * media) : 0;
            double desvioPadrao = Math.sqrt(Math.max(0, variancia));

            return new EstatisticaSerie(
                    tipo,
                    serie,
                    experiencia,
                    unidade,
                    quantidade,
                    minimo == Double.MAX_VALUE ? 0 : minimo,
                    media,
                    maximo == -Double.MAX_VALUE ? 0 : maximo,
                    desvioPadrao
            );
        }
    }
}
