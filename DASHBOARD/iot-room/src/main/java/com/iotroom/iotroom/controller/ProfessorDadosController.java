package com.iotroom.iotroom.controller;

import com.iotroom.iotroom.dto.ComparacaoFiltroDTO;
import com.iotroom.iotroom.dto.ComparacaoSerieDTO;
import com.iotroom.iotroom.security.AuthenticatedUser;
import com.iotroom.iotroom.service.ProfessorDadosService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/professor/dados")
public class ProfessorDadosController {

    private final ProfessorDadosService professorDadosService;

    public ProfessorDadosController(ProfessorDadosService professorDadosService) {
        this.professorDadosService = professorDadosService;
    }

    @GetMapping
    public String index(
            @RequestParam(required = false, defaultValue = "1") Integer numeroComparacoes,

            @RequestParam(required = false) Long grupoId1,
            @RequestParam(required = false) Long experienciaId1,
            @RequestParam(required = false) Long estacaoId1,
            @RequestParam(required = false, defaultValue = "TEMPERATURA") String tipoSensor1,
            @RequestParam(required = false, defaultValue = "50") Integer limite1,

            @RequestParam(required = false) Long grupoId2,
            @RequestParam(required = false) Long experienciaId2,
            @RequestParam(required = false) Long estacaoId2,
            @RequestParam(required = false, defaultValue = "TEMPERATURA") String tipoSensor2,
            @RequestParam(required = false, defaultValue = "50") Integer limite2,

            @RequestParam(required = false) Long grupoId3,
            @RequestParam(required = false) Long experienciaId3,
            @RequestParam(required = false) Long estacaoId3,
            @RequestParam(required = false, defaultValue = "TEMPERATURA") String tipoSensor3,
            @RequestParam(required = false, defaultValue = "50") Integer limite3,

            @RequestParam(required = false) Long grupoId4,
            @RequestParam(required = false) Long experienciaId4,
            @RequestParam(required = false) Long estacaoId4,
            @RequestParam(required = false, defaultValue = "TEMPERATURA") String tipoSensor4,
            @RequestParam(required = false, defaultValue = "50") Integer limite4,

            Model model,
            Authentication authentication
    ) {
        Long professorId = obterUtilizadorId(authentication);

        int totalComparacoes = normalizarNumeroComparacoes(numeroComparacoes);

        List<ComparacaoFiltroDTO> filtros = new ArrayList<>();

        filtros.add(new ComparacaoFiltroDTO(grupoId1, experienciaId1, estacaoId1, tipoSensor1, limite1));

        if (totalComparacoes >= 2) {
            filtros.add(new ComparacaoFiltroDTO(grupoId2, experienciaId2, estacaoId2, tipoSensor2, limite2));
        }

        if (totalComparacoes >= 3) {
            filtros.add(new ComparacaoFiltroDTO(grupoId3, experienciaId3, estacaoId3, tipoSensor3, limite3));
        }

        if (totalComparacoes >= 4) {
            filtros.add(new ComparacaoFiltroDTO(grupoId4, experienciaId4, estacaoId4, tipoSensor4, limite4));
        }

        List<ComparacaoSerieDTO> series = professorDadosService.comparar(professorId, filtros);

        model.addAttribute("numeroComparacoes", totalComparacoes);
        model.addAttribute("filtros", filtros);
        model.addAttribute("series", series);

        model.addAttribute("grupos", professorDadosService.listarGruposDoProfessor(professorId));
        model.addAttribute("experiencias", professorDadosService.listarExperienciasDoProfessor(professorId));
        model.addAttribute("estacoes", professorDadosService.listarEstacoesDoProfessor(professorId));

        model.addAttribute("paginaAtual", "dados");

        return "professor/dados/index";
    }

    private int normalizarNumeroComparacoes(Integer numeroComparacoes) {
        if (numeroComparacoes == null) {
            return 1;
        }

        if (numeroComparacoes < 1) {
            return 1;
        }

        if (numeroComparacoes > 4) {
            return 4;
        }

        return numeroComparacoes;
    }

    private Long obterUtilizadorId(Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        return user.getId();
    }
}