import java.lang.reflect.Array;
import java.util.ArrayList;

public class GerenciadorMemoriaPaginado implements GerenciadorMemoria {

    private int tamMem;
    private int tamPg;
    private int numFrames;
    private static int initFrame = 0;
    private static int finalFrame = 0;
    private static ArrayList<Integer> framesAlocados;


    public GerenciadorMemoriaPaginado(int tamMem, int tamPg) {
        this.tamMem = tamMem;
        this.tamPg = tamPg;
        this.numFrames = tamMem / tamPg;

        framesAlocados = new ArrayList<>(numFrames);
    }

    public GerenciadorMemoriaPaginado() {

    }

    @Override
    public ArrayList<Integer> aloca(int nroPalavras) {
        return null;
    }


    /// Refatorado: Como no nosso gerenciador do sistema já calculamos se é possível alocar ou não, a lógica foi removida.
    /// Agora aqui só alocamos onde o programa começa, e retorna a posição dele na tabela de paginação
    @Override
    public ArrayList<Integer> aloca(int frame, int tamPg, ArrayList<Integer> paginasUsadas) {

        ArrayList<Integer> framesAlocadosProPrograma = new ArrayList<>();

        for (Integer paginasUsada : paginasUsadas) {
            initFrame = paginasUsada * tamPg;
            ///finalFrame = initFrame + tamPg - 1; não utilizado porém formula para saber onde programa acaba

            framesAlocados.add(initFrame);
            framesAlocadosProPrograma.add(initFrame);
        }

        return framesAlocadosProPrograma;
    }

    @Override
    public void traduzEndereco(int endereco, ArrayList<Integer> tabelaPaginas) {
        ArrayList<int[]> enderecoTraduzido = new ArrayList<>();
        for (Integer pagina : tabelaPaginas) {
            int enderecoNovo = (int) Math.ceil(pagina / tamPg); /// Acha a página desejada -> Pega maior valor se divisão for quebrada
            int offset = pagina % tamPg; /// Deslocamento dentro da pagina
            int[] enderecoCompleto = {enderecoNovo, offset};
            enderecoTraduzido.add(enderecoCompleto);
        }
    }

    @Override
    public void desaloca(ArrayList<Integer> tabelaPaginas) {
        for (int frame : tabelaPaginas) {
            if (frame >= 0 && frame < numFrames) {
                framesAlocados.set(frame, null);
            }
        }
    }

    // (opcional) pra debug
    public void imprimeEstadoMemoria() {
        System.out.println("Estado dos frames:");
        for (int i = 0; i < numFrames; i++) {
            System.out.println("Frame " + i + ": " + (framesAlocados.get(i) == null ? "Livre" : "Ocupado"));
        }
    }



    // getter útil pro resto do sistema
    public int getTamPg() {
        return tamPg;
    }
}
