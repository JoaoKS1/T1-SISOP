import java.util.ArrayList;

public class GerenciadorMemoriaPaginado implements GerenciadorMemoria {

    private int tamMem;
    private int tamPg;
    private int numFrames;
    private boolean[] framesLivres;

    public GerenciadorMemoriaPaginado(int tamMem, int tamPg) {
        this.tamMem = tamMem;
        this.tamPg = tamPg;
        this.numFrames = tamMem / tamPg;

        framesLivres = new boolean[numFrames];

        // inicialmente todos os frames estão livres
        for (int i = 0; i < numFrames; i++) {
            framesLivres[i] = true;
        }
    }

    @Override
    public ArrayList<Integer> aloca(int nroPalavras) {

        int paginasNecessarias = (int) Math.ceil((double) nroPalavras / tamPg);

        ArrayList<Integer> framesAlocados = new ArrayList<>();

        // procura frames livres
        for (int i = 0; i < numFrames && framesAlocados.size() < paginasNecessarias; i++) {
            if (framesLivres[i]) {
                framesLivres[i] = false;
                framesAlocados.add(i);
            }
        }

        // se não conseguiu todos, desfaz
        if (framesAlocados.size() < paginasNecessarias) {
            for (int frame : framesAlocados) {
                framesLivres[frame] = true;
            }
            return null;
        }

        return framesAlocados;
    }

    @Override
    public void desaloca(ArrayList<Integer> tabelaPaginas) {

        for (int frame : tabelaPaginas) {
            if (frame >= 0 && frame < numFrames) {
                framesLivres[frame] = true;
            }
        }
    }

    // (opcional) pra debug
    public void imprimeEstadoMemoria() {
        System.out.println("Estado dos frames:");
        for (int i = 0; i < numFrames; i++) {
            System.out.println("Frame " + i + ": " + (framesLivres[i] ? "Livre" : "Ocupado"));
        }
    }

    // getter útil pro resto do sistema
    public int getTamPg() {
        return tamPg;
    }
}
