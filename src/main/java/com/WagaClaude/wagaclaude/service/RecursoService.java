package com.WagaClaude.wagaclaude.service;

import com.WagaClaude.wagaclaude.model.Armazenamento;
import com.WagaClaude.wagaclaude.model.Recurso;
import com.WagaClaude.wagaclaude.model.Usuario;
import com.WagaClaude.wagaclaude.model.VirtualMachine;
import com.WagaClaude.wagaclaude.model.enums.TipoDisco;
import com.WagaClaude.wagaclaude.repository.ArmazenamentoRepository;
import com.WagaClaude.wagaclaude.repository.UsuarioRepository;
import com.WagaClaude.wagaclaude.repository.VirtualMachineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecursoService {

    @Autowired
    private VirtualMachineRepository vmRepository;

    @Autowired
    private ArmazenamentoRepository armazenamentoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AutenticacaoService autenticacaoService;

    // Capacidade (GB) do disco padrão criado junto com toda VM nova.
    private static final int CAPACIDADE_DISCO_PADRAO = 50;

    /**
     * Cria uma VM para o usuário e já anexa um disco padrão a ela.
     */
    @Transactional
    public VirtualMachine criarVM(Integer usuarioId, VirtualMachine vm) {
        Usuario dono = buscarUsuario(usuarioId);

        if (vm.getNome() == null || vm.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome da VM é obrigatório");
        }
        if (vm.getQtdCpu() <= 0) {
            throw new IllegalArgumentException("A quantidade de CPU deve ser maior que zero");
        }
        if (vm.getMemoriaRamGB() <= 0) {
            throw new IllegalArgumentException("A memória RAM deve ser maior que zero");
        }
        if (vm.getSo() == null || vm.getSo().isBlank()) {
            throw new IllegalArgumentException("O sistema operacional é obrigatório");
        }

        vm.setDono(dono);
        VirtualMachine vmSalva = vmRepository.save(vm);

        // Disco padrão que acompanha toda VM nova.
        Armazenamento discoPadrao = new Armazenamento();
        discoPadrao.setNome("Disco padrão - " + vmSalva.getNome());
        discoPadrao.setCapacidadeGB(CAPACIDADE_DISCO_PADRAO);
        discoPadrao.setUsadoGB(0);
        discoPadrao.setTipoDisco(TipoDisco.SSD);
        discoPadrao.setDono(dono);
        discoPadrao.setVmAnexada(vmSalva);
        armazenamentoRepository.save(discoPadrao);

        return vmSalva;
    }

    /**
     * Cria um disco avulso (não anexado a nenhuma VM) para o usuário.
     */
    public Armazenamento criarStorage(Integer usuarioId, Armazenamento disco) {
        Usuario dono = buscarUsuario(usuarioId);

        if (disco.getNome() == null || disco.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome do disco é obrigatório");
        }
        if (disco.getCapacidadeGB() <= 0) {
            throw new IllegalArgumentException("A capacidade deve ser maior que zero");
        }
        if (disco.getTipoDisco() == null) {
            throw new IllegalArgumentException("O tipo do disco é obrigatório");
        }
        if (disco.getUsadoGB() < 0 || disco.getUsadoGB() > disco.getCapacidadeGB()) {
            throw new IllegalArgumentException("O espaço usado é inválido");
        }

        disco.setDono(dono);
        disco.setVmAnexada(null);
        return armazenamentoRepository.save(disco);
    }

    /**
     * Anexa um disco solto a uma VM.
     */
    public Armazenamento anexarDisco(Integer discoId, Integer vmId) {
        Armazenamento disco = buscarDisco(discoId);
        VirtualMachine vm = buscarVM(vmId);

        if (disco.getVmAnexada() != null) {
            throw new IllegalArgumentException("Este disco já está anexado a uma VM");
        }
        if (!disco.getDono().getId().equals(vm.getDono().getId())) {
            throw new IllegalArgumentException("Disco e VM devem pertencer ao mesmo usuário");
        }

        disco.setVmAnexada(vm);
        return armazenamentoRepository.save(disco);
    }

    /**
     * Desanexa um disco da VM em que estava.
     */
    public Armazenamento desanexarDisco(Integer discoId) {
        Armazenamento disco = buscarDisco(discoId);

        if (disco.getVmAnexada() == null) {
            throw new IllegalArgumentException("Este disco não está anexado a nenhuma VM");
        }

        disco.setVmAnexada(null);
        return armazenamentoRepository.save(disco);
    }

    /**
     * Lista os recursos (VMs + discos) visíveis para o usuário.
     * ADMIN (permissão VER_TODOS_RECURSOS) enxerga os recursos de todo mundo;
     * usuário COMUM enxerga apenas os seus.
     */
    public List<Recurso> listarRecursos(Integer usuarioId) {
        Usuario solicitante = buscarUsuario(usuarioId);

        List<Recurso> recursos = new ArrayList<>();
        if (autenticacaoService.temPermissao(solicitante, "VER_TODOS_RECURSOS")) {
            recursos.addAll(vmRepository.findAll());
            recursos.addAll(armazenamentoRepository.findAll());
        } else {
            recursos.addAll(vmRepository.findByDono(solicitante));
            recursos.addAll(armazenamentoRepository.findByDono(solicitante));
        }
        return recursos;
    }

    /**
     * Deleta um recurso (VM ou disco) pelo id. Ao deletar uma VM, seus discos
     * são desanexados (viram discos soltos) em vez de apagados.
     *
     * Só ADMIN pode deletar (permissão DELETAR_RECURSO) — usuário COMUM recebe
     * {@link AcessoNegadoException}.
     */
    @Transactional
    public void deletarRecurso(Integer id, Integer usuarioId) {
        Usuario solicitante = buscarUsuario(usuarioId);
        if (!autenticacaoService.temPermissao(solicitante, "DELETAR_RECURSO")) {
            throw new AcessoNegadoException("Apenas administradores podem deletar recursos");
        }

        VirtualMachine vm = vmRepository.findById(id).orElse(null);
        if (vm != null) {
            for (Armazenamento disco : vm.getDiscos()) {
                disco.setVmAnexada(null);
                armazenamentoRepository.save(disco);
            }
            vmRepository.delete(vm);
            return;
        }

        if (armazenamentoRepository.existsById(id)) {
            armazenamentoRepository.deleteById(id);
            return;
        }

        throw new IllegalArgumentException("Recurso não encontrado: id " + id);
    }

    // ---- helpers ----

    private Usuario buscarUsuario(Integer usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: id " + usuarioId));
    }

    private VirtualMachine buscarVM(Integer vmId) {
        return vmRepository.findById(vmId)
                .orElseThrow(() -> new IllegalArgumentException("VM não encontrada: id " + vmId));
    }

    private Armazenamento buscarDisco(Integer discoId) {
        return armazenamentoRepository.findById(discoId)
                .orElseThrow(() -> new IllegalArgumentException("Disco não encontrado: id " + discoId));
    }
}
