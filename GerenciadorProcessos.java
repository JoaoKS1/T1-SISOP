import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GerenciadorProcessos extends Thread {

    private int proximoId = 1;
    private final List<ProcessControlBlock> filaProntos;
    private ProcessControlBlock processoRodando;
    private final Object lock = new Object();

    public static Map<Integer, ProcessControlBlock> listaProcessBlock = new HashMap<>();

    private static GerenciadorMemoriaPaginado gmp;
    private final Sistema sistema;

    public GerenciadorProcessos(Sistema sistema) {
        this.sistema = sistema;
        this.filaProntos = sistema.sistemaOperacional.ready;

        int numFrames = (int) Math.ceil((double) Sistema.tamMem / Sistema.tamPg);
        gmp = new GerenciadorMemoriaPaginado(numFrames, Sistema.tamPg);
    }

    ///  Cria um processo no gerenciador de processo
    public boolean criaProcesso(String nomePrograma, Sistema.Word[] programa) {

        // Solicita alocação no gerenciador paginado
        ArrayList<Integer> paginasAlocadas = Sistema.gmp.aloca(proximoId, programa.length);

        if (paginasAlocadas == null) {
            System.out.println("Memoria insuficiente para criar o processo.");
            return false;
        }

        // Um pcb pra ele
        ProcessControlBlock pcb = new ProcessControlBlock(proximoId, nomePrograma, programa, paginasAlocadas, "PRONTO");

        // Carrega programa
        sistema.sistemaOperacional.utils.loadProgramPaged(programa, paginasAlocadas);

        // Seção crítica, impede que outra thread mexa nesse mesmo bloco do process block
        synchronized (lock) {
            listaProcessBlock.put(proximoId, pcb); // Salva processo e id dele
            filaProntos.add(pcb);
            System.out.println("Processo criado: " + pcb.id + " (" + nomePrograma + ")");
            proximoId++;
        }
        return true;
    }

    ///  Desalocar um processo
    public void desaloca(int id) {
        ProcessControlBlock pcb = listaProcessBlock.get(id);

        // Seção critica
        synchronized (lock) {
            if (pcb == null) {
                System.out.println("Processo " + id + " nao encontrado.");
                return;
            }

            // Desalocar páginas
            Sistema.gmp.desaloca(pcb.id);

            // Remove processo
            filaProntos.remove(pcb);


            if (processoRodando != null && processoRodando.id == id) {
                processoRodando = null;
                sistema.sistemaOperacional.running = null;
            }

            listaProcessBlock.remove(id);
            System.out.println("Processo removido: " + pcb.id);
        }
    }

    /// Listar processos do sitema
    public void listarProcessos() {
        synchronized (lock) {
            if (listaProcessBlock.isEmpty()) {
                System.out.println("Sem processos no sistema.");
                return;
            }

            for (Map.Entry<Integer, ProcessControlBlock> entry : listaProcessBlock.entrySet()) {
                ProcessControlBlock pcb = entry.getValue();
                String fila = (processoRodando != null && processoRodando.id == pcb.id) ? "RUNNING" : (filaProntos.contains(pcb) ? "READY" : "OUTRA");
                System.out.println("ID: " + pcb.id + " Programa: " + pcb.nomePrograma + " Estado: " + pcb.estado + " Fila: " + fila + " Paginas: " + pcb.tabelaPaginas);
            }
        }
    }


    // Dump da memoria dos processos
    public void dumpProcesso(int id) {
        ProcessControlBlock pcb;
        synchronized (lock) {
            pcb = listaProcessBlock.get(id);
        }

        if (pcb == null) {
            System.out.println("Processo " + id + " nao encontrado.");
            return;
        }

        System.out.println("PCB => id=" + pcb.id + ", programa=" + pcb.nomePrograma + ", estado=" + pcb.estado + ", pc=" + pcb.pc + ", tabelaPaginas=" + pcb.tabelaPaginas);

        int totalPalavras = pcb.imagemPrograma.length;
        for (int i = 0; i < totalPalavras; i++) {
            int pagina = i / Sistema.tamPg;
            int offset = i % Sistema.tamPg;
            int frame = pcb.tabelaPaginas.get(pagina);
            int enderecoFisico = frame * Sistema.tamPg + offset;
            System.out.print(enderecoFisico + ":  ");
            sistema.sistemaOperacional.utils.dump(sistema.hardWare.memoria.posicao[enderecoFisico]);
        }
    }

    ///  Para executar um processo
    public void executaProcesso(int id) {
        synchronized (lock) {
            ProcessControlBlock pcb = listaProcessBlock.get(id);

            if (pcb == null) {
                System.out.println("Processo " + id + " nao encontrado.");
                return;
            }

            if (!filaProntos.contains(pcb)) {
                filaProntos.add(pcb);
            }
            pcb.estado = "PRONTO";

        }

    }

    public void executaTodosEscalonados() {
        while (true) {
            ProcessControlBlock pcb = null;

            synchronized (lock) {
                if (!filaProntos.isEmpty()) {
                    pcb = filaProntos.remove(0);
                }

            }
            if (pcb == null) {
                executaFatia(pcb);
            }

            try {
                Thread.sleep(1);
            } catch (Exception e) {
            }
        }


    }

    public void passoEscalonadorContinuo() {
        ProcessControlBlock pcb;
        synchronized (lock) {
            if (!sistema.sistemaOperacional.escalonadorAtivo) {
                return;
            }
            if (processoRodando != null || filaProntos.isEmpty()) {
                return;
            }
            pcb = filaProntos.remove(0);
            processoRodando = pcb;
            sistema.sistemaOperacional.running = pcb;
            pcb.estado = "EXECUTANDO";
        }

        executaFatia(pcb);
    }

    private void executaFatia(ProcessControlBlock pcb) {


        synchronized (lock) {
            processoRodando = pcb;
            sistema.sistemaOperacional.running = pcb;
            pcb.estado = "EXECUTANDO";
        }


        sistema.hardWare.cpu.run(sistema.sistemaOperacional.delta);

        // Salvando resultado
        boolean terminouStop = sistema.hardWare.cpu.parouPorStop();


        synchronized (lock) {
            pcb.pc = sistema.hardWare.cpu.getPc();
            pcb.registradores = sistema.hardWare.cpu.getRegistradoresSnapshot();

            processoRodando = null;
            sistema.sistemaOperacional.running = null;


            if (terminouStop) {
                pcb.estado = "FINALIZADO";
                Sistema.gmp.desaloca(pcb.id);
                listaProcessBlock.remove(pcb.id);

                System.out.println("Processo finalizado e removido: " + pcb.id);
            } else {
                pcb.estado = "PRONTO";

                filaProntos.add(pcb);


            }

        }
    }

    public void executadoTudoEscalonador() {
        while (true) {
            ProcessControlBlock pcb;

            synchronized (lock) {
                if (filaProntos.isEmpty()) {

                    if (listaProcessBlock.isEmpty()) {
                        return;
                    }
                    continue;
                }

                pcb = filaProntos.remove(0);
            }

            executaFatia(pcb);

        }

    }

}

