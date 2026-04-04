import java.util.ArrayList;

public class ProcessControlBlock {
    public int id;
    public ArrayList<Integer> tabelaPaginas;
    public String estado;
    public int pc; // program counter

    public ProcessControlBlock(int id) {
        this.id = id;
        this.estado = "NOVO";
        this.pc = 0;
    } 
}

   

