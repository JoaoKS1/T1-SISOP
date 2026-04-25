import java.util.ArrayList;
import java.util.Map;

public class ProcessControlBlock {
    public int id;
    public ArrayList<Integer> tabelaPaginas;
    public String estado;
    public int pc; // program counter

    public ProcessControlBlock(int id, ArrayList<Integer> paginasAlocadas, String pronto) {
        this.id = id;
        this.estado = pronto;
        this.pc = 0;
        tabelaPaginas = paginasAlocadas;
    }
}

   

