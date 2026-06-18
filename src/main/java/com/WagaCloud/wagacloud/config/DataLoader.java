package com.WagaCloud.wagacloud.config;

import com.WagaCloud.wagacloud.model.Armazenamento;
import com.WagaCloud.wagacloud.model.Usuario;
import com.WagaCloud.wagacloud.model.VirtualMachine;
import com.WagaCloud.wagacloud.model.enums.NivelAcesso;
import com.WagaCloud.wagacloud.model.enums.TipoDisco;
import com.WagaCloud.wagacloud.repository.UsuarioRepository;
import com.WagaCloud.wagacloud.service.RecursoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RecursoService recursoService;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            System.out.println("[DataLoader] Banco já populado — pulando carga inicial.");
            return;
        }

        System.out.println("[DataLoader] Banco vazio — inserindo dados iniciais...");

        Usuario admin = criarUsuario("Administrador", "admin@cloud.com", "123", NivelAcesso.ADMIN);
        Usuario maria = criarUsuario("Maria Silva", "maria@cloud.com", "123", NivelAcesso.COMUM);
        Usuario joao = criarUsuario("João Souza", "joao@cloud.com", "123", NivelAcesso.COMUM);

        recursoService.criarVM(admin.getId(), montarVM("web-server", 4, 8, "Ubuntu 22.04"));
        recursoService.criarVM(maria.getId(), montarVM("banco-dados", 8, 16, "Debian 12"));
        recursoService.criarVM(joao.getId(), montarVM("app-backend", 2, 4, "Ubuntu 22.04"));

        recursoService.criarStorage(maria.getId(),
                montarDisco("backup-mensal", 200, 0, TipoDisco.HDD));

        System.out.println("[DataLoader] Carga inicial concluída: "
                + usuarioRepository.count() + " usuários, 3 VMs (com disco padrão) e 1 disco solto.");
    }

    private Usuario criarUsuario(String nome, String email, String senha, NivelAcesso nivel) {
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setEmail(email);
        u.setSenha(senha);
        u.setNivelAcesso(nivel);
        return usuarioRepository.save(u);
    }

    private VirtualMachine montarVM(String nome, int cpu, int ramGB, String so) {
        VirtualMachine vm = new VirtualMachine();
        vm.setNome(nome);
        vm.setQtdCpu(cpu);
        vm.setMemoriaRamGB(ramGB);
        vm.setSo(so);
        return vm;
    }

    private Armazenamento montarDisco(String nome, int capacidadeGB, int usadoGB, TipoDisco tipo) {
        Armazenamento disco = new Armazenamento();
        disco.setNome(nome);
        disco.setCapacidadeGB(capacidadeGB);
        disco.setUsadoGB(usadoGB);
        disco.setTipoDisco(tipo);
        return disco;
    }
}
