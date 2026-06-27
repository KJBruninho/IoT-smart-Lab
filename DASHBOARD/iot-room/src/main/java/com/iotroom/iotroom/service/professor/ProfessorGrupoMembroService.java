package com.iotroom.iotroom.service.professor;

import com.iotroom.iotroom.dto.professor.GrupoMembroDTO;
import com.iotroom.iotroom.dto.professor.GrupoMembroFormDTO;
import com.iotroom.iotroom.dto.professor.GrupoMembroProjection;
import com.iotroom.iotroom.model.*;
import com.iotroom.iotroom.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProfessorGrupoMembroService {

    private final GrupoRepository grupoRepository;
    private final UtilizadorRepository utilizadorRepository;
    private final RoleGrupoRepository roleGrupoRepository;
    private final UtilizadorGrupoRepository utilizadorGrupoRepository;

    public ProfessorGrupoMembroService(
            GrupoRepository grupoRepository,
            UtilizadorRepository utilizadorRepository,
            RoleGrupoRepository roleGrupoRepository,
            UtilizadorGrupoRepository utilizadorGrupoRepository
    ) {
        this.grupoRepository = grupoRepository;
        this.utilizadorRepository = utilizadorRepository;
        this.roleGrupoRepository = roleGrupoRepository;
        this.utilizadorGrupoRepository = utilizadorGrupoRepository;
    }

    public List<GrupoMembroDTO> listarMembros(Long grupoId, Long professorId) {
        return listarMembros(grupoId, professorId, false);
    }

    public List<GrupoMembroDTO> listarMembros(Long grupoId, Long utilizadorId, boolean admin) {
        obterGrupo(grupoId, utilizadorId, admin);

        List<GrupoMembroProjection> projections =
                utilizadorGrupoRepository.listarMembrosDoGrupo(grupoId);

        List<GrupoMembroDTO> membros = new ArrayList<>();

        for (GrupoMembroProjection projection : projections) {
            membros.add(new GrupoMembroDTO(projection));
        }

        return membros;
    }

    public List<Utilizador> listarUtilizadoresDisponiveis(Long grupoId, Long professorId) {
        return listarUtilizadoresDisponiveis(grupoId, professorId, false);
    }

    public List<Utilizador> listarUtilizadoresDisponiveis(
            Long grupoId,
            Long utilizadorId,
            boolean admin
    ) {
        obterGrupo(grupoId, utilizadorId, admin);

        return utilizadorRepository.findUtilizadoresDisponiveisParaGrupo(grupoId);
    }

    public List<RoleGrupo> listarRoles() {
        return roleGrupoRepository.findAllByOrderByNomeAsc();
    }

    @Transactional
    public void adicionarMembro(Long grupoId, Long professorId, GrupoMembroFormDTO form) {
        adicionarMembro(grupoId, professorId, false, form);
    }

    @Transactional
    public void adicionarMembro(
            Long grupoId,
            Long utilizadorId,
            boolean admin,
            GrupoMembroFormDTO form
    ) {
        obterGrupo(grupoId, utilizadorId, admin);

        if (form == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formulário inválido.");
        }

        if (form.getUtilizadorId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "É obrigatório selecionar um utilizador.");
        }

        if (form.getRoleGrupoId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "É obrigatório selecionar uma role.");
        }

        utilizadorRepository.findById(form.getUtilizadorId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Utilizador não encontrado."
                ));

        roleGrupoRepository.findById(form.getRoleGrupoId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Role não encontrada."
                ));

        boolean jaExiste = utilizadorGrupoRepository.existsByIdUtilizadorIdAndIdGrupoId(
                form.getUtilizadorId(),
                grupoId
        );

        if (jaExiste) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Este utilizador já pertence ao grupo."
            );
        }

        UtilizadorGrupo membro = new UtilizadorGrupo();
        membro.setId(new UtilizadorGrupoId(form.getUtilizadorId(), grupoId));
        membro.setRoleGrupoId(form.getRoleGrupoId());
        membro.setCriadoEm(LocalDateTime.now());

        utilizadorGrupoRepository.save(membro);
    }

    @Transactional
    public void alterarRole(
            Long grupoId,
            Long utilizadorId,
            Long professorId,
            Long novaRoleGrupoId
    ) {
        alterarRole(grupoId, utilizadorId, professorId, false, novaRoleGrupoId);
    }

    @Transactional
    public void alterarRole(
            Long grupoId,
            Long membroUtilizadorId,
            Long utilizadorId,
            boolean admin,
            Long novaRoleGrupoId
    ) {
        obterGrupo(grupoId, utilizadorId, admin);

        if (novaRoleGrupoId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "É obrigatório selecionar uma role.");
        }

        UtilizadorGrupo membro = utilizadorGrupoRepository
                .findById(new UtilizadorGrupoId(membroUtilizadorId, grupoId))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Membro não encontrado neste grupo."
                ));

        RoleGrupo novaRole = roleGrupoRepository.findById(novaRoleGrupoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Role não encontrada."
                ));

        validarNaoRemoveUltimoOwner(grupoId, membro.getRoleGrupoId(), novaRole.getNome());

        membro.setRoleGrupoId(novaRoleGrupoId);

        utilizadorGrupoRepository.save(membro);
    }

    @Transactional
    public void removerMembro(Long grupoId, Long utilizadorId, Long professorId) {
        removerMembro(grupoId, utilizadorId, professorId, false);
    }

    @Transactional
    public void removerMembro(
            Long grupoId,
            Long membroUtilizadorId,
            Long utilizadorId,
            boolean admin
    ) {
        obterGrupo(grupoId, utilizadorId, admin);

        UtilizadorGrupo membro = utilizadorGrupoRepository
                .findById(new UtilizadorGrupoId(membroUtilizadorId, grupoId))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Membro não encontrado neste grupo."
                ));

        validarNaoRemoveUltimoOwner(grupoId, membro.getRoleGrupoId(), null);

        utilizadorGrupoRepository.deleteByIdUtilizadorIdAndIdGrupoId(membroUtilizadorId, grupoId);
    }

    public GrupoMembroFormDTO criarFormVazio() {
        return new GrupoMembroFormDTO();
    }

    private Grupo obterGrupo(Long grupoId, Long utilizadorId, boolean admin) {
        if (admin) {
            return grupoRepository.findById(grupoId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Grupo não encontrado."
                    ));
        }

        return grupoRepository.findByIdAndProfessorId(grupoId, utilizadorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Grupo não encontrado para este professor."
                ));
    }

    private void validarNaoRemoveUltimoOwner(
            Long grupoId,
            Long roleAtualId,
            String novaRoleNome
    ) {
        RoleGrupo owner = roleGrupoRepository.findByNome("OWNER")
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Role OWNER não existe."
                ));

        boolean membroAtualEhOwner = owner.getId().equals(roleAtualId);
        boolean vaiContinuarOwner = "OWNER".equalsIgnoreCase(novaRoleNome);

        if (!membroAtualEhOwner || vaiContinuarOwner) {
            return;
        }

        long totalOwners = utilizadorGrupoRepository.countByIdGrupoIdAndRoleGrupoId(
                grupoId,
                owner.getId()
        );

        if (totalOwners <= 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não é possível remover ou alterar o último OWNER do grupo."
            );
        }
    }
}