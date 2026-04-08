import java.util.*;

public class GerenciadorProcessos {

    private int proximoId = 1;
    private List<ProcessControlBlock> filaProntos = new ArrayList<>();
    private GerenteMemoria gm;


    public  GerenciadorProcessos(int numFrame, int tamPg){
        gm = new GerenteMemoria(numFrame, tamPg);
    }


    public boolean criaProcesso(int tamanhoPrograma) {

        if(tamanhoPrograma > 128) return false;

        ///  CARREGA PROGRAMA ( carrega pelo gerenciador de memoria paginado)
        ArrayList<Integer> paginasAlocadas = gm.aloca(tamanhoPrograma);

        // Verifica se foi possível alocar
        if(paginasAlocadas.isEmpty()) {
            System.out.println("Memória insuficiente!");
            return false;
        }

        ProcessControlBlock pcb = new ProcessControlBlock(proximoId++);

        // salva no PCB
        pcb.tabelaPaginas = paginasAlocadas;

        pcb.estado = "PRONTO";
        pcb.pc = 0;

        filaProntos.add(pcb);

        System.out.println("Processo criado: " + pcb.id);
        return true;
    }

    public void desaloca(ProcessControlBlock pcb) {
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
