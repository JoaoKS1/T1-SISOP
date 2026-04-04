import java.util.*;

public class GerenciadorProcessos {

    private int proximoId = 1;
    private List<ProcessControlBlock> filaProntos = new ArrayList<>();
    private GerenciadorMemoria gm;

    public GerenciadorProcessos(GerenciadorMemoria gm) {
        this.gm = gm;
    }

    public ProcessControlBlock criaProcesso(int tamanhoPrograma) {

        ProcessControlBlock pcb = new ProcessControlBlock(proximoId++);

        ArrayList<Integer> tabelaPaginas = gm.aloca(tamanhoPrograma);

        // verifica se conseguiu alocar
        if (tabelaPaginas == null) {
            System.out.println("Memória insuficiente!");
            return null;
        }

        // salva no PCB
        pcb.tabelaPaginas = tabelaPaginas;

        pcb.estado = "PRONTO";
        pcb.pc = 0;

        filaProntos.add(pcb);

        System.out.println("Processo criado: " + pcb.id);
        return pcb;
    }

    public void removeProcesso(ProcessControlBlock pcb) {
        gm.desaloca(pcb.tabelaPaginas);
        filaProntos.remove(pcb);

        System.out.println("Processo removido: " + pcb.id);
    }

    public void listarProcessos() {
        for (ProcessControlBlock pcb : filaProntos) {
            System.out.println("ID: " + pcb.id + " Estado: " + pcb.estado);
        }
    }
}
