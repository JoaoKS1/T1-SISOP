import java.util.ArrayList;

public class GerenteMemoria implements GerenciadorMemoria{
    private static  double numFrames = 0;
    private static int tamPg;
    private static ArrayList<Boolean> paginasUsadas = new ArrayList<>(tamPg);
    private static int frame;
    private static GerenciadorMemoriaPaginado gmp = new GerenciadorMemoriaPaginado(Sistema.tamMem, Sistema.tamPg);
    private static ArrayList<Integer> paginasUsadasNoPrograma = new ArrayList<>();


    public GerenteMemoria(){
    }

    public static void defineValores(int numFrame, int tamPg) {
        GerenteMemoria.numFrames = numFrame;
        GerenteMemoria.tamPg = tamPg;
    }

    // T1-A1.2 Gerenciador de memória responsável por proucurar as páginas livres e fornecer o tamanho dos frames para o GM paginado
    @Override
    public ArrayList<Integer> aloca(int nroPalavrasASeremAlocadas) {
        paginasUsadasNoPrograma.clear();
        if (nroPalavrasASeremAlocadas < numFrames) {

            /// Pega prox valor mais alto se vier número double
            double qtnPaginas = Math.ceil((double) nroPalavrasASeremAlocadas / tamPg);

            /// Array que guarda quais páginas estão livres para serém usadas

            /// Contador de paginas
            int quantidadePáginasLivres = 0;

            /// No array de páginas, verifica quais ainda estão livres para serem usadas
            for (int i = 0; i < numFrames && paginasUsadasNoPrograma.size() < qtnPaginas; i++) {
                if (!paginasUsadas.get(i)) {
                    quantidadePáginasLivres++;
                    paginasUsadasNoPrograma.add(i);
                }
            }

            /// Verifca se a quantidade de páginas livre é suficiente para alocar o novo programa, retornando o número de páginas livre. NÃO ALOCA NADA ATÉ AQUI
            if (quantidadePáginasLivres >= qtnPaginas)  {
                gmp.aloca(frame, tamPg, paginasUsadasNoPrograma); /// Chama metodo que aloca paginação

                return paginasUsadasNoPrograma;
            }

        }

        return paginasUsadasNoPrograma;
    }



    /// NÃO É UTILIZADO
    @Override
    public ArrayList<Integer> aloca(int frame, int tamPg, ArrayList<Integer> paginasUsadas) {
        return null;
    }

    @Override
    public void traduzEndereco(int endereco, ArrayList<Integer> tabelaPaginas) {
        int pagina = endereco / tamPg;

        if(pagina >= paginasUsadas.size()){
            throw new RuntimeException("Acesso a inválido (posição inválida).");
        }

        int offset = endereco % tamPg;
        int frame = tabelaPaginas.get(pagina);
        int enderecoFisico = frame * tamPg + offset; // Feito para acessar a memória fisica, garante acessar a página correta do programa especificado



    }

    // T1-A1.2
    /// A partir da posição do array de paginas a serem liberadas, desaloca (define como falso) a posição referente a ele.
    @Override
    public void desaloca(ArrayList<Integer> pagianasASeremDesalocadas) {
        for (int index : pagianasASeremDesalocadas) {
            paginasUsadas.set(index, false); /// Apenas torna a página a qual o programa esta alocada como false, ou seja, ela está disponível para ser usada/sobescrita
        }
    }
}
