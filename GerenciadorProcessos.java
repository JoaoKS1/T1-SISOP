import java.util.*;

public class GerenciadorProcessos {

    private int proximoId = 1;
    private List<ProcessControlBlock> filaProntos = new ArrayList<>();
    public static Map<Integer, ProcessControlBlock> listaProcessBlock = new HashMap<>();

    private GerenteMemoria gm = new GerenteMemoria();


    public  GerenciadorProcessos(int numFrame, int tamPg){
        gm.defineValores(numFrame, tamPg);
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

        // Cria e salva o pcb

        ProcessControlBlock pcb = new ProcessControlBlock(proximoId, paginasAlocadas, "PRONTO" );
        listaProcessBlock.put(proximoId, pcb);
        proximoId++;
        filaProntos.add(pcb);

        System.out.println("Processo criado: " + pcb.id);
        return true;
    }

    public void desaloca(int id) {
        ProcessControlBlock pcb = listaProcessBlock.get(id);
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
