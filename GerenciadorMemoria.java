import java.util.ArrayList;

public interface GerenciadorMemoria {

    // Retorna true e o vetor que em que página o frame será alocado
    ArrayList<Integer> aloca(int nroPalavras);


    // Libera frames alocados
    void desaloca(int idProcesso);


}
