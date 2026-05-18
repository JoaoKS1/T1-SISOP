import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GerenciadorMemoriaPaginado implements GerenciadorMemoria {

    private int tamPg;
    private int numFrames;
    private Map<Integer, ArrayList<Integer>> tabelaDeProcesso = new HashMap<>();
    private ArrayList<Boolean> frameUsados;
    private final Object lock = new Object();

    public GerenciadorMemoriaPaginado(int tamMem, int tamPg) {
        this.tamPg = tamPg;
        this.numFrames = tamMem / tamPg;

        frameUsados = new ArrayList<>(numFrames);
        for (int i = 0; i < numFrames; i++) {
            frameUsados.add(Boolean.FALSE);
        }
    }

    // Métdo não utilizado, vindo da interface
    @Override
    public ArrayList<Integer> aloca(int nroPalavras) {
        return null;
    }

    // Aloca memoria
    public ArrayList<Integer> aloca(int idProcesso, int numeroPalavras) {

        synchronized (lock) {
            //  Verifica quantidade de paginas necessarias para alocar programa, joganod pra cima
            int quantidadePaginas = (int) Math.ceil((double) numeroPalavras / tamPg);

            ArrayList<Integer> paginas = new ArrayList<>();

            // Verifica se há paginas livres disponiveis
            int livres = 0;
            for (Boolean paginasUsada : frameUsados) {
                if (!paginasUsada) {
                    livres++;
                }
            }

            // Caso não tenha paginas o suficiente, retorna como erro
            if (livres < quantidadePaginas) {
                return null;
            }

            // Aloca as paginas e define como true os frames usados, adicioando a pagia ao processo
            for (int i = 0; i < numFrames && paginas.size() < quantidadePaginas; i++) {
                if (!frameUsados.get(i)) {
                    frameUsados.set(i, true);
                    paginas.add(i);
                }
            }

            // Se a alocação foi menor que a quantiadade de paginas necessarias, desaloca e retorna null
            if (paginas.size() < quantidadePaginas) {

                for (int p : paginas) {
                    frameUsados.set(p, false);
                }
                return null;

            }

            // Indexa tabelas do processo
            tabelaDeProcesso.put(idProcesso, paginas);

            return paginas;

        }

    }


    @Override
    public void desaloca(int idProcesso) {

        synchronized (lock){

            ArrayList<Integer> frameProcesso = tabelaDeProcesso.remove(idProcesso);

            if (frameProcesso == null) return;

            Sistema.Word[] memoria = Sistema.sistemaAtual.hardWare.memoria.posicao;

            // Desalocando as posições alocadas
            for (int i = 0; i < frameProcesso.size(); i++) {
                int frame = frameProcesso.get(i);
                frameUsados.set(frame, false);

                int inicio = frame * this.tamPg;
                int fim = inicio + this.tamPg;

                for (int j = inicio; j < fim && j < memoria.length; j++) {
                    memoria[j] = Sistema.sistemaAtual.new Word(Sistema.Opcode.___, -1, -1, -1);
                }

            }

        }




    }

    public int getTamPg() {
        return tamPg;
    }
}
