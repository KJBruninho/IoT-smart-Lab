package com.iotroom.iotroom.service;

import com.iotroom.iotroom.dto.ComparacaoFiltroDTO;
import com.iotroom.iotroom.dto.ComparacaoLeituraDTO;
import com.iotroom.iotroom.dto.ComparacaoLeituraProjection;
import com.iotroom.iotroom.dto.ComparacaoSerieDTO;
import com.iotroom.iotroom.model.Estacao;
import com.iotroom.iotroom.model.Experiencia;
import com.iotroom.iotroom.model.Grupo;
import com.iotroom.iotroom.repository.EstacaoRepository;
import com.iotroom.iotroom.repository.ExperienciaRepository;
import com.iotroom.iotroom.repository.GrupoRepository;
import com.iotroom.iotroom.repository.ProfessorDadosRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ProfessorDadosService {

	private final ProfessorDadosRepository professorDadosRepository;
	private final ExperienciaRepository experienciaRepository;
	private final GrupoRepository grupoRepository;
	private final EstacaoRepository estacaoRepository;
	
	public ProfessorDadosService(
	        ProfessorDadosRepository professorDadosRepository,
	        ExperienciaRepository experienciaRepository,
	        GrupoRepository grupoRepository,
	        EstacaoRepository estacaoRepository
	) {
	    this.professorDadosRepository = professorDadosRepository;
	    this.experienciaRepository = experienciaRepository;
	    this.grupoRepository = grupoRepository;
	    this.estacaoRepository = estacaoRepository;
	}

    public List<ComparacaoSerieDTO> comparar(
            Long professorId,
            List<ComparacaoFiltroDTO> filtros
    ) {
        List<ComparacaoSerieDTO> series = new ArrayList<>();

        int numero = 1;

        for (ComparacaoFiltroDTO filtro : filtros) {
            ComparacaoFiltroDTO filtroNormalizado = normalizarFiltro(filtro);

            List<ComparacaoLeituraProjection> projections =
                    professorDadosRepository.findLeiturasComparacao(
                            professorId,
                            filtroNormalizado.getGrupoId(),
                            filtroNormalizado.getExperienciaId(),
                            filtroNormalizado.getEstacaoId(),
                            filtroNormalizado.getTipoSensor(),
                            PageRequest.of(0, filtroNormalizado.getLimite())
                    );

            List<ComparacaoLeituraDTO> leituras = new ArrayList<>();

            for (ComparacaoLeituraProjection projection : projections) {
                leituras.add(new ComparacaoLeituraDTO(projection));
            }

            Collections.reverse(leituras);

            String nomeSerie = criarNomeSerie(numero, filtroNormalizado, leituras);

            series.add(new ComparacaoSerieDTO(
                    nomeSerie,
                    filtroNormalizado,
                    leituras
            ));

            numero++;
        }

        return series;
    }

    public List<Grupo> listarGruposDoProfessor(Long professorId) {
        return grupoRepository.findByProfessorIdOrderByCriadoEmDesc(professorId);
    }

    public List<Experiencia> listarExperienciasDoProfessor(Long professorId) {
        return experienciaRepository.findByCriadoPorIdOrderByCriadoEmDesc(professorId);
    }

    public List<Estacao> listarEstacoesDoProfessor(Long professorId) {
        return estacaoRepository.findEstacoesDoProfessor(professorId);
    }

    private ComparacaoFiltroDTO normalizarFiltro(ComparacaoFiltroDTO filtro) {
        if (filtro == null) {
            return new ComparacaoFiltroDTO(null, null, null, "TEMPERATURA", 50);
        }

        Long grupoId = filtro.getGrupoId();
        Long experienciaId = filtro.getExperienciaId();
        Long estacaoId = filtro.getEstacaoId();
        String tipoSensor = normalizarTipoSensor(filtro.getTipoSensor());
        Integer limite = normalizarLimite(filtro.getLimite());

        return new ComparacaoFiltroDTO(
                grupoId,
                experienciaId,
                estacaoId,
                tipoSensor,
                limite
        );
    }

    private String normalizarTipoSensor(String tipoSensor) {
        if (tipoSensor == null || tipoSensor.trim().isEmpty()) {
            return "TEMPERATURA";
        }

        String tipo = tipoSensor.trim().toUpperCase();

        if (!"TEMPERATURA".equals(tipo) && !"TDS".equals(tipo)) {
            return "TEMPERATURA";
        }

        return tipo;
    }

    private Integer normalizarLimite(Integer limite) {
        if (limite == null) {
            return 50;
        }

        if (limite < 5) {
            return 5;
        }

        if (limite > 500) {
            return 500;
        }

        return limite;
    }

    private String criarNomeSerie(
            int numero,
            ComparacaoFiltroDTO filtro,
            List<ComparacaoLeituraDTO> leituras
    ) {
        if (leituras == null || leituras.isEmpty()) {
            return "Comparação " + numero + " - " + filtro.getTipoSensor();
        }

        ComparacaoLeituraDTO primeira = leituras.get(0);

        return primeira.getExperienciaNome()
                + " / "
                + primeira.getEstacaoNome()
                + " / "
                + primeira.getTipoSensor();
    }
}