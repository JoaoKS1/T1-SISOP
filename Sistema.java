import java.util.ArrayList;

public class Sistema implements GerenciadorMemoria {

    // T1-A Criaçao das variaveis de Gerenciamento de memoria - tamMem e tamPg
    public int tamMem;
    public int tamPg;
    public int numFrames;
    public ArrayList<Boolean> paginasUsadas = new ArrayList<>(tamPg);


    @Override
    public ArrayList<Integer> aloca(int nroPalavrasASeremAlocadas) {
        if (nroPalavrasASeremAlocadas < numFrames) {
            int qtnPaginas = nroPalavrasASeremAlocadas / tamPg;
            ArrayList<Integer> paginasLivres = new ArrayList<>();
            int qtnTrue = 0;

            // Verifica quantas páginas há disponíveis para alocar
            for (int i = 0; i < qtnPaginas; i++) {
                if (paginasUsadas.get(i) == false) {
                    qtnTrue++;
                    paginasLivres.add(i);
                }
            }

            // Verifica se o número de paginas livres é maior ou igual ao número de páginas requisitas, caso não, retorna vazio
            if (qtnTrue >= qtnPaginas) return paginasLivres;

            return null;
            // devolve vetor -> tabela de paginas do processo  (convencionar). Deve existir função de carga que receve um nome de programa e uma tabela de paginas, lê as páginas e carrega na memoria --> esse função interna possui uma costante do tamanho do frame da página, quando carga sabe o tamanhoo e a tabela, ela pode carregar --- > O gerente processos tem que criar (retorna processo) e remover (remove processo), cria instancia de PCB {id, tabela de páginas, estado de execução....}
            // página é por processo -> tabela de página temos a página inicial e a final que será utilziado para desalocar o programa
            // SO tem que saber o estado de cada quadro, saber quem esta livre e quem esta ocupado (gerente de memoria precisa saber)
        }
        return null;
    }

    // A partir da posição do array de paginas a serem liberadas, desaloca (define como falso) a posição referente a ele.
    @Override
    public void desaloca(ArrayList<Integer> pagianasASeremDesalocadas) {
        for (int index : pagianasASeremDesalocadas) {
            paginasUsadas.set(index, false);
        }
    }


    public class Memory {
        public Word[] posicao; // pos[i] é a posição i da memória. cada posição é uma palavra.

        public Memory(int size) {
            posicao = new Word[size];
            for (int i = 0; i < posicao.length; i++) {
                posicao[i] = new Word(Opcode.___, -1, -1, -1);
            }
            ; // cada posicao da memoria inicializada
        }
    }

    public class Word { // cada posicao da memoria tem uma instrucao (ou um dado)
        public Opcode opcode; // código de operação
        public int registradorA; // indice do primeiro registrador da operacao (Rs ou Rd cfe opcode na tabela)
        public int registradorB; // indice do segundo registrador da operacao (Rc ou Rs cfe operacao)
        public int parametro; // parametro para instrucao (k ou A cfe operacao), ou o dado, se opcode = DADO

        public Word(Opcode _opcode, int _registradorA, int _registradorB, int _parametro) {
            opcode = _opcode;
            registradorA = _registradorA;
            registradorB = _registradorB;
            parametro = _parametro;
        }
    }

    // Definição da CPU

    public enum Opcode {
        DATA, ___, // se memoria nesta posicao tem um dado, usa DATA, se nao usada e NULO ___
        JMP, JMPI, JMPIG, JMPIL, JMPIE, // desvios
        JMPIM, JMPIGM, JMPILM, JMPIEM,
        JMPIGK, JMPILK, JMPIEK, JMPIGT,
        ADDI, SUBI, ADD, SUB, MULT, // matematicos
        LDI, LDD, STD, LDX, STX, MOVE, // movimentacao
        SYSCALL, STOP // chamada de sistema e parada
    }

    public enum Interrupts { // possiveis interrupcoes que esta CPU gera
        noInterrupt, intEnderecoInvalido, intInstrucaoInvalida, intOverflow;
    }

    public class CPU {
        private int maxInt; // valores maximo e minimo para inteiros nesta cpu
        private int minInt;

        // T1-A Metodo de traducao de endereço

        public int traduzEndereco(int enderecoLogico) {
            int pagina = enderecoLogico / tamPg;
            int offset = enderecoLogico % tamPg;

            int frame = pagina; // mapeamento direto

            return frame * tamPg + offset;
        }

        // CONTEXTO da CPU ...
        private int pc; // ... composto de program counter,
        private Word instructionRegister; // instruction register,
        private int[] registradores; // registradores da CPU
        private Interrupts interrupcoes; // durante instrucao, interrupcao pode ser sinalizada

        // FIM CONTEXTO DA CPU: tudo que precisa sobre o estado de um processo para
        // executa-lo
        // nas proximas versoes isto pode modificar

        private Word[] memoriaFisica; // array de memória "física", CPU tem uma ref a memoria para acessar

        private InterruptHandling desvioRotinasInt; // significa desvio para rotinas de tratamento de Int - se int
        // ligada, desvia
        private SysCallHandling sysCall; // significa desvio para tratamento de chamadas de sistema

        private boolean cpuStop; // flag para parar CPU - caso de interrupcao que acaba o processo, ou chamada

        // auxilio aa depuração
        private boolean debug; // se true entao mostra cada instrucao em execucao
        private Utilities dump; // para debug (dump)

        public CPU(Memory _memoria, boolean _debug) { // ref a MEMORIA passada na criacao da CPU
            maxInt = 32767; // capacidade de representacao modelada
            minInt = -32767; // se exceder deve gerar interrupcao de overflow
            memoriaFisica = _memoria.posicao; // usa o atributo 'memoriaFisica' para acessar a memoria, só para ficar
            // mais
            // pratico
            registradores = new int[10]; // aloca o espaço dos registradores - regs 8 e 9 usados somente para IO

            debug = _debug; // se true, print da instrucao em execucao

        }

        public void setAddressOfHandlers(InterruptHandling _iterruptHandling, SysCallHandling _sysCall) {
            desvioRotinasInt = _iterruptHandling; // aponta para rotinas de tratamento de interrupções
            sysCall = _sysCall; // aponta para rotinas de tratamento de chamadas de sistema
        }

        public void setUtilities(Utilities _utilities) {
            dump = _utilities; // aponta para rotinas utilitárias - fazer dump da memória na tela
        }

        // verificação de enderecamento
        private boolean legal(int endereco) { // todo acesso a memoria tem que ser verificado se é válido -
            // aqui no caso se o endereco é um endereco valido em toda memoria
            if (endereco >= 0 && endereco < memoriaFisica.length) {
                return true;
            } else {
                interrupcoes = Interrupts.intEnderecoInvalido; // se nao for liga interrupcao no meio da exec da
                // instrucao
                return false;
            }
        }

        private boolean testOverflow(int value) { // toda operacao matematica deve avaliar se ocorre overflow
            if ((value < minInt) || (value > maxInt)) {
                interrupcoes = Interrupts.intOverflow; // se houver liga interrupcao no meio da exec da instrucao
                return false;
            }
            ;
            return true;
        }

        public void setContext(int _pc) { // usado para setar o contexto da cpu para rodar um processo
            // [ nesta versao é somente colocar o PC na posicao 0 ]
            pc = _pc; // pc cfe endereco logico
            interrupcoes = Interrupts.noInterrupt; // reset da interrupcao registrada
        }

        public void run() { // execucao da CPU supoe que o contexto da CPU, vide acima,
            // esta devidamente setado
            cpuStop = false;
            while (!cpuStop) { // ciclo de instrucoes. acaba cfe resultado da exec da instrucao, veja cada

                // FASE DE FETCH
                if (legal(pc)) { // pc valido

                    // local de accesso a memoria - FETCH

                    // T1-A Implementando a tradução no Fetch da memoria
                    int enderecoFisico = traduzEndereco(pc);
                    instructionRegister = memoriaFisica[enderecoFisico];

                    // guarda em ir
                    // resto é dump de debug
                    if (debug) {
                        System.out.print("regs: ");
                        for (int i = 0; i < 10; i++) {
                            System.out.print(" r[" + i + "]:" + registradores[i]);
                        }
                        ;
                        System.out.println();
                    }
                    if (debug) {
                        System.out.print("pc: " + pc + " exec: ");
                        dump.dump(instructionRegister);
                    }

                    // FASE DE EXECUCAO DA INSTRUCAO CARREGADA NO instructionRegister
                    // T1-A implementa a tradução nas intruçoes que acessam a memoriaFisica
                    // T1-A adição de validação dos endereços fisicos
                    switch (instructionRegister.opcode) { // conforme o opcode (código de operação) executa

                        case LDI: // Rd ← k veja a tabela de instrucoes do HW simulado para entender a semantica
                            // da instrucao
                            registradores[instructionRegister.registradorA] = instructionRegister.parametro;
                            pc++;
                            break;
                        case LDD: // Rd <- [A]
                            int endFisico = traduzEndereco(instructionRegister.parametro);
                            if (legal(endFisico)) {

                                registradores[instructionRegister.registradorA] = memoriaFisica[endFisico].parametro;
                                pc++;
                            }
                            break;
                        case LDX: // RD <- [RS]

                            int endLogico = registradores[instructionRegister.registradorB];
                            endFisico = traduzEndereco(endLogico);

                            if (legal(endFisico)) {
                                registradores[instructionRegister.registradorA] = memoriaFisica[endFisico].parametro;
                                pc++;
                            }
                            break;
                        case STD: // [A] ← Rs
                            endFisico = traduzEndereco(instructionRegister.parametro);
                            if (legal(endFisico)) {

                                memoriaFisica[endFisico].opcode = Opcode.DATA;
                                memoriaFisica[endFisico].parametro = registradores[instructionRegister.registradorA];
                                pc++;
                                if (debug) {
                                    System.out.print("                                  ");
                                    dump.dump(instructionRegister.parametro, instructionRegister.parametro + 1);
                                }
                            }
                            break;
                        case STX: // [Rd] ←Rs

                            endLogico = registradores[instructionRegister.registradorA];
                            endFisico = traduzEndereco(endLogico);

                            if (legal(endFisico)) {

                                memoriaFisica[endFisico].opcode = Opcode.DATA;
                                memoriaFisica[endFisico].parametro = registradores[instructionRegister.registradorB];
                                pc++;
                            }
                            ;
                            break;
                        case MOVE: // RD <- RS
                            registradores[instructionRegister.registradorA] = registradores[instructionRegister.registradorB];
                            pc++;
                            break;
                        // Instrucoes Aritmeticas
                        case ADD: // Rd ← Rd + Rs
                            registradores[instructionRegister.registradorA] = registradores[instructionRegister.registradorA]
                                    + registradores[instructionRegister.registradorB];
                            testOverflow(registradores[instructionRegister.registradorA]);
                            pc++;
                            break;
                        case ADDI: // Rd ← Rd + k
                            registradores[instructionRegister.registradorA] = registradores[instructionRegister.registradorA]
                                    + instructionRegister.parametro;
                            testOverflow(registradores[instructionRegister.registradorA]);
                            pc++;
                            break;
                        case SUB: // Rd ← Rd - Rs
                            registradores[instructionRegister.registradorA] = registradores[instructionRegister.registradorA]
                                    - registradores[instructionRegister.registradorB];
                            testOverflow(registradores[instructionRegister.registradorA]);
                            pc++;
                            break;
                        case SUBI: // RD <- RD - k // NOVA
                            registradores[instructionRegister.registradorA] = registradores[instructionRegister.registradorA]
                                    - instructionRegister.parametro;
                            testOverflow(registradores[instructionRegister.registradorA]);
                            pc++;
                            break;
                        case MULT: // Rd <- Rd * Rs
                            registradores[instructionRegister.registradorA] = registradores[instructionRegister.registradorA]
                                    * registradores[instructionRegister.registradorB];
                            testOverflow(registradores[instructionRegister.registradorA]);
                            pc++;
                            break;

                        // Instrucoes JUMP
                        case JMP: // PC <- k
                            pc = instructionRegister.parametro;
                            break;
                        case JMPIM: // PC <- [A]
                            endFisico = traduzEndereco(instructionRegister.parametro);
                            if (legal(endFisico)) {
                                pc = memoriaFisica[endFisico].parametro;
                            }
                            break;
                        case JMPIG: // If Rc > 0 Then PC ← Rs Else PC ← PC +1
                            if (registradores[instructionRegister.registradorB] > 0) {
                                pc = registradores[instructionRegister.registradorA];
                            } else {
                                pc++;
                            }
                            break;
                        case JMPIGK: // If RC > 0 then PC <- k else PC++
                            if (registradores[instructionRegister.registradorB] > 0) {
                                pc = instructionRegister.parametro;
                            } else {
                                pc++;
                            }
                            break;
                        case JMPILK: // If RC < 0 then PC <- k else PC++
                            if (registradores[instructionRegister.registradorB] < 0) {
                                pc = instructionRegister.parametro;
                            } else {
                                pc++;
                            }
                            break;
                        case JMPIEK: // If RC = 0 then PC <- k else PC++
                            if (registradores[instructionRegister.registradorB] == 0) {
                                pc = instructionRegister.parametro;
                            } else {
                                pc++;
                            }
                            break;
                        case JMPIL: // if Rc < 0 then PC <- Rs Else PC <- PC +1
                            if (registradores[instructionRegister.registradorB] < 0) {
                                pc = registradores[instructionRegister.registradorA];
                            } else {
                                pc++;
                            }
                            break;
                        case JMPIE: // If Rc = 0 Then PC <- Rs Else PC <- PC +1
                            if (registradores[instructionRegister.registradorB] == 0) {
                                pc = registradores[instructionRegister.registradorA];
                            } else {
                                pc++;
                            }
                            break;
                        case JMPIGM: // If RC > 0 then PC <- [A] else PC++
                            endFisico = traduzEndereco(instructionRegister.parametro);
                            if (legal(endFisico)) {
                                if (registradores[instructionRegister.registradorB] > 0) {
                                    pc = memoriaFisica[endFisico].parametro;
                                } else {
                                    pc++;
                                }
                            }
                            break;
                        case JMPILM: // If RC < 0 then PC <- k else PC++
                            if (registradores[instructionRegister.registradorB] < 0) {
                                endFisico = traduzEndereco(instructionRegister.parametro);
                                if (legal(endFisico)) {
                                    pc = memoriaFisica[endFisico].parametro;
                                }
                            } else {
                                pc++;
                            }
                            break;
                        case JMPIEM: // If RC = 0 then PC <- k else PC++
                            if (registradores[instructionRegister.registradorB] == 0) {
                                endFisico = traduzEndereco(instructionRegister.parametro);
                                if (legal(endFisico)) {
                                    pc = memoriaFisica[endFisico].parametro;
                                }
                            } else {
                                pc++;
                            }
                            break;
                        case JMPIGT: // If RS>RC then PC <- k else PC++
                            if (registradores[instructionRegister.registradorA] > registradores[instructionRegister.registradorB]) {
                                pc = instructionRegister.parametro;
                            } else {
                                pc++;
                            }
                            break;

                        case DATA: // pc está sobre área supostamente de dados
                            interrupcoes = Interrupts.intInstrucaoInvalida;
                            break;

                        // Chamadas de sistema
                        case SYSCALL:
                            sysCall.handle(); // aqui desvia para rotina de chamada de sistema, no momento so temos IO
                            pc++;
                            break;

                        case STOP: // por enquanto, para execucao
                            sysCall.stop();
                            cpuStop = true;
                            break;

                        // Inexistente
                        default:
                            interrupcoes = Interrupts.intInstrucaoInvalida;
                            break;
                    }
                }
                // VERIFICA INTERRUPÇÃO !!! - TERCEIRA FASE DO CICLO DE INSTRUÇÕES
                if (interrupcoes != Interrupts.noInterrupt) { // existe interrupção
                    desvioRotinasInt.handle(interrupcoes); // desvia para rotina de tratamento - esta rotina é do SO
                    cpuStop = true; // nesta versao, para a CPU
                }
            } // FIM DO CICLO DE UMA INSTRUÇÃO
        }
    }
    // CPU - fim

    // Maquina Virtual - Hardware
    public class HardWare {
        public Memory memoria;
        public CPU cpu;

        public HardWare(int tamMem) {
            memoria = new Memory(tamMem);
            cpu = new CPU(memoria, true); // true liga debug
        }
    }

    // Hardware - fim

    // SW - inicio - Sistema Operacional

    // Interrupcoes- rotinas de tratamento
    public class InterruptHandling {
        private HardWare hardWare; // referencia ao hw se tiver que setar algo

        public InterruptHandling(HardWare _hardware) {
            hardWare = _hardware;
        }

        public void handle(Interrupts interrupcoes) {
            // apenas avisa - todas interrupcoes neste momento finalizam o programa
            System.out.println("Interrupcao " + interrupcoes + "   pc: " + hardWare.cpu.pc);
        }
    }

    // Chamada de Sistema - rotinas de tratamento

    public class SysCallHandling {
        private HardWare hardWare; // referencia ao hw se tiver que setar algo

        public SysCallHandling(HardWare _hardware) {
            hardWare = _hardware;
        }

        public void stop() { // chamada de sistema indicando final de programa
            System.out.println("SYSCALL STOP");
        }

        public void handle() { // chamada de sistema
            // suporta somente IO, com parametros
            // registradores[8] = in ou out e registradores[9] endereco do inteiro
            System.out
                    .println("SYSCALL pars:  " + hardWare.cpu.registradores[8] + " / " + hardWare.cpu.registradores[9]);

            if (hardWare.cpu.registradores[8] == 1) {
                // leitura

            } else if (hardWare.cpu.registradores[8] == 2) {
                // escrita - escreve o conteuodo da memoria na posicao dada em registradores[9]
                System.out.println("OUT:   " + hardWare.memoria.posicao[hardWare.cpu.registradores[9]].parametro);
            } else {
                System.out.println("  PARAMETRO INVALIDO");
            }
        }
    }

    // Ultilitarios do Sistema
    // - load é invocado a partir de requisição do usuário

    // carga na memória
    public class Utilities {
        private HardWare hardWare;

        public Utilities(HardWare _hardware) {
            hardWare = _hardware;
        }

        private void loadProgram(Word[] posicao) {
            Word[] memoria = hardWare.memoria.posicao;
            for (int i = 0; i < posicao.length; i++) {
                memoria[i].opcode = posicao[i].opcode;
                memoria[i].registradorA = posicao[i].registradorA;
                memoria[i].registradorB = posicao[i].registradorB;
                memoria[i].parametro = posicao[i].parametro;
            }
        }

        // dump da memória
        public void dump(Word w) { // funcoes de DUMP nao existem em hardware - colocadas aqui para facilidade
            System.out.print("[ ");
            System.out.print(w.opcode);
            System.out.print(", ");
            System.out.print(w.registradorA);
            System.out.print(", ");
            System.out.print(w.registradorB);
            System.out.print(", ");
            System.out.print(w.parametro);
            System.out.println("  ] ");
        }

        public void dump(int inicio, int fim) {
            Word[] memoria = hardWare.memoria.posicao; // m[] é o array de posições memória do hw
            for (int i = inicio; i < fim; i++) {
                System.out.print(i);
                System.out.print(":  ");
                dump(memoria[i]);
            }
        }

        private void loadAndExec(Word[] p) {
            loadProgram(p); // carga do programa na memoria
            System.out.println("---------------------------------- programa carregado na memoria");
            dump(0, p.length); // dump da memoria nestas posicoes
            hardWare.cpu.setContext(0); // seta pc para endereço 0 - ponto de entrada dos programas
            System.out.println("---------------------------------- inicia execucao ");
            hardWare.cpu.run(); // cpu roda programa ate parar
            System.out.println("---------------------------------- memoria após execucao ");
            dump(0, p.length); // dump da memoria com resultado
        }
    }

    public class SistemaOperacional {
        public InterruptHandling iterruptHandling;
        public SysCallHandling sysCallHandling;
        public Utilities utils;

        public SistemaOperacional(HardWare hardWare) {
            iterruptHandling = new InterruptHandling(hardWare); // rotinas de tratamento de int
            sysCallHandling = new SysCallHandling(hardWare); // chamadas de sistema
            hardWare.cpu.setAddressOfHandlers(iterruptHandling, sysCallHandling);
            utils = new Utilities(hardWare);
        }
    }

    // Sistema

    public HardWare hardWare;
    public SistemaOperacional sistemaOperacional;
    public Programs programas;

    // Criação da Maquina Virtual
    // T1-A Implementação do Gerente de Memoria - Adiçao do tamPg e numFrames
    public Sistema(int tamMem, int tamPag) {

        this.tamMem = tamMem;
        this.tamPg = tamPag;

        // T1-A Cálculo do número de frames da memória
        this.numFrames = tamMem / tamPg;

        hardWare = new HardWare(tamMem); // memoria do HW tem tamMem palavras
        sistemaOperacional = new SistemaOperacional(hardWare);
        hardWare.cpu.setUtilities(sistemaOperacional.utils); // permite cpu fazer dump de memoria ao avancar
        programas = new Programs();
    }

    // inicialisação e run da Maquina Virtual

    public void run() {

        sistemaOperacional.utils.loadAndExec(programas.retrieveProgram("fatorialV2"));

        // sistemaOperacional.utils.loadAndExec(progs.retrieveProgram("fatorial"));
        // fibonacci10,
        // fibonacci10v2,
        // progMinimo,
        // fatorialWRITE, // saida
        // fibonacciREAD, // entrada
        // PB
        // PC, // bubble sort
    }
    // Fim do Sistema

    // Instancia e testa sistema
    public static void main(String args[]) {
        Sistema sistema = new Sistema(1024, 8);
        sistema.run();
    }

    // Programas - não fazem parte do sistema
    // Esta classe representa programas armazenados (como se estivessem em disco)
    // que podem ser carregados para a memória (load faz isto)

    public class Program {
        public String name;
        public Word[] image;

        public Program(String name, Word[] image) {
            this.name = name;
            this.image = image;
        }
    }

    public class Programs {

        public Word[] retrieveProgram(String programaName) {
            for (Program programa : progs) {
                if (programa != null && programa.name == programaName)
                    return programa.image;
            }
            return null;
        }

        public Program[] progs = {
                new Program("fatorial",
                        new Word[]{
                                // este fatorial so aceita valores positivos. nao pode ser zero
                                // linha coment
                                new Word(Opcode.LDI, 0, -1, 7), // 0 r0 é valor a calcular fatorial
                                new Word(Opcode.LDI, 1, -1, 1), // 1 r1 é 1 para multiplicar (por r0)
                                new Word(Opcode.LDI, 6, -1, 1), // 2 r6 é 1 o decremento
                                new Word(Opcode.LDI, 7, -1, 8), // 3 r7 tem posicao 8 para fim do programa
                                new Word(Opcode.JMPIE, 7, 0, 0), // 4 se r0=0 pula para r7(=8)
                                new Word(Opcode.MULT, 1, 0, -1), // 5 r1 = r1 * r0 (r1 acumula o produto por cada termo)
                                new Word(Opcode.SUB, 0, 6, -1), // 6 r0 = r0 - r6 (r6=1) decrementa r0 para proximo
                                // termo
                                new Word(Opcode.JMP, -1, -1, 4), // 7 vai p posicao 4
                                new Word(Opcode.STD, 1, -1, 10), // 8 coloca valor de r1 na posição 10
                                new Word(Opcode.STOP, -1, -1, -1), // 9 stop
                                new Word(Opcode.DATA, -1, -1, -1) // 10 ao final o valor está na posição 10 da memória
                        }),

                new Program("fatorialV2",
                        new Word[]{
                                new Word(Opcode.LDI, 0, -1, 5), // numero para colocar na memoria, ou pode ser lido
                                new Word(Opcode.STD, 0, -1, 19),
                                new Word(Opcode.LDD, 0, -1, 19),
                                new Word(Opcode.LDI, 1, -1, -1),
                                new Word(Opcode.LDI, 2, -1, 13), // SALVAR POS STOP
                                new Word(Opcode.JMPIL, 2, 0, -1), // caso negativo pula pro STD
                                new Word(Opcode.LDI, 1, -1, 1),
                                new Word(Opcode.LDI, 6, -1, 1),
                                new Word(Opcode.LDI, 7, -1, 13),
                                new Word(Opcode.JMPIE, 7, 0, 0), // POS 9 pula para STD (Stop-1)
                                new Word(Opcode.MULT, 1, 0, -1),
                                new Word(Opcode.SUB, 0, 6, -1),
                                new Word(Opcode.JMP, -1, -1, 9), // pula para o JMPIE
                                new Word(Opcode.STD, 1, -1, 18),
                                new Word(Opcode.LDI, 8, -1, 2), // escrita
                                new Word(Opcode.LDI, 9, -1, 18), // endereco com valor a escrever
                                new Word(Opcode.SYSCALL, -1, -1, -1),
                                new Word(Opcode.STOP, -1, -1, -1), // POS 17
                                new Word(Opcode.DATA, -1, -1, -1), // POS 18
                                new Word(Opcode.DATA, -1, -1, -1)} // POS 19
                ),

                new Program("progMinimo",
                        new Word[]{
                                new Word(Opcode.LDI, 0, -1, 999),
                                new Word(Opcode.STD, 0, -1, 8),
                                new Word(Opcode.STD, 0, -1, 9),
                                new Word(Opcode.STD, 0, -1, 10),
                                new Word(Opcode.STD, 0, -1, 11),
                                new Word(Opcode.STD, 0, -1, 12),
                                new Word(Opcode.STOP, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1), // 7
                                new Word(Opcode.DATA, -1, -1, -1), // 8
                                new Word(Opcode.DATA, -1, -1, -1), // 9
                                new Word(Opcode.DATA, -1, -1, -1), // 10
                                new Word(Opcode.DATA, -1, -1, -1), // 11
                                new Word(Opcode.DATA, -1, -1, -1), // 12
                                new Word(Opcode.DATA, -1, -1, -1) // 13
                        }),

                new Program("fibonacci10",
                        new Word[]{ // mesmo que prog exemplo, so que usa r0 no lugar de r8
                                new Word(Opcode.LDI, 1, -1, 0),
                                new Word(Opcode.STD, 1, -1, 20),
                                new Word(Opcode.LDI, 2, -1, 1),
                                new Word(Opcode.STD, 2, -1, 21),
                                new Word(Opcode.LDI, 0, -1, 22),
                                new Word(Opcode.LDI, 6, -1, 6),
                                new Word(Opcode.LDI, 7, -1, 31),
                                new Word(Opcode.LDI, 3, -1, 0),
                                new Word(Opcode.ADD, 3, 1, -1),
                                new Word(Opcode.LDI, 1, -1, 0),
                                new Word(Opcode.ADD, 1, 2, -1),
                                new Word(Opcode.ADD, 2, 3, -1),
                                new Word(Opcode.STX, 0, 2, -1),
                                new Word(Opcode.ADDI, 0, -1, 1),
                                new Word(Opcode.SUB, 7, 0, -1),
                                new Word(Opcode.JMPIG, 6, 7, -1),
                                new Word(Opcode.STOP, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1), // POS 20
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1) // ate aqui - serie de fibonacci ficara armazenada
                        }),

                new Program("fibonacci10v2",
                        new Word[]{ // mesmo que prog exemplo, so que usa r0 no lugar de r8
                                new Word(Opcode.LDI, 1, -1, 0),
                                new Word(Opcode.STD, 1, -1, 20),
                                new Word(Opcode.LDI, 2, -1, 1),
                                new Word(Opcode.STD, 2, -1, 21),
                                new Word(Opcode.LDI, 0, -1, 22),
                                new Word(Opcode.LDI, 6, -1, 6),
                                new Word(Opcode.LDI, 7, -1, 31),
                                new Word(Opcode.MOVE, 3, 1, -1),
                                new Word(Opcode.MOVE, 1, 2, -1),
                                new Word(Opcode.ADD, 2, 3, -1),
                                new Word(Opcode.STX, 0, 2, -1),
                                new Word(Opcode.ADDI, 0, -1, 1),
                                new Word(Opcode.SUB, 7, 0, -1),
                                new Word(Opcode.JMPIG, 6, 7, -1),
                                new Word(Opcode.STOP, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1), // POS 20
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1) // ate aqui - serie de fibonacci ficara armazenada
                        }),
                new Program("fibonacciREAD",
                        new Word[]{
                                // mesmo que prog exemplo, so que usa r0 no lugar de r8
                                new Word(Opcode.LDI, 8, -1, 1), // leitura
                                new Word(Opcode.LDI, 9, -1, 55), // endereco a guardar o tamanho da serie de fib a gerar
                                // - pode ser de 1 a 20
                                new Word(Opcode.SYSCALL, -1, -1, -1),
                                new Word(Opcode.LDD, 7, -1, 55),
                                new Word(Opcode.LDI, 3, -1, 0),
                                new Word(Opcode.ADD, 3, 7, -1),
                                new Word(Opcode.LDI, 4, -1, 36), // posicao para qual ira pular (stop) *
                                new Word(Opcode.LDI, 1, -1, -1), // caso negativo
                                new Word(Opcode.STD, 1, -1, 41),
                                new Word(Opcode.JMPIL, 4, 7, -1), // pula pra stop caso negativo *
                                new Word(Opcode.JMPIE, 4, 7, -1), // pula pra stop caso 0
                                new Word(Opcode.ADDI, 7, -1, 41), // fibonacci + posição do stop
                                new Word(Opcode.LDI, 1, -1, 0),
                                new Word(Opcode.STD, 1, -1, 41), // 25 posicao de memoria onde inicia a serie de
                                // fibonacci gerada
                                new Word(Opcode.SUBI, 3, -1, 1), // se 1 pula pro stop
                                new Word(Opcode.JMPIE, 4, 3, -1),
                                new Word(Opcode.ADDI, 3, -1, 1),
                                new Word(Opcode.LDI, 2, -1, 1),
                                new Word(Opcode.STD, 2, -1, 42),
                                new Word(Opcode.SUBI, 3, -1, 2), // se 2 pula pro stop
                                new Word(Opcode.JMPIE, 4, 3, -1),
                                new Word(Opcode.LDI, 0, -1, 43),
                                new Word(Opcode.LDI, 6, -1, 25), // salva posição de retorno do loop
                                new Word(Opcode.LDI, 5, -1, 0), // salva tamanho
                                new Word(Opcode.ADD, 5, 7, -1),
                                new Word(Opcode.LDI, 7, -1, 0), // zera (inicio do loop)
                                new Word(Opcode.ADD, 7, 5, -1), // recarrega tamanho
                                new Word(Opcode.LDI, 3, -1, 0),
                                new Word(Opcode.ADD, 3, 1, -1),
                                new Word(Opcode.LDI, 1, -1, 0),
                                new Word(Opcode.ADD, 1, 2, -1),
                                new Word(Opcode.ADD, 2, 3, -1),
                                new Word(Opcode.STX, 0, 2, -1),
                                new Word(Opcode.ADDI, 0, -1, 1),
                                new Word(Opcode.SUB, 7, 0, -1),
                                new Word(Opcode.JMPIG, 6, 7, -1), // volta para o inicio do loop
                                new Word(Opcode.STOP, -1, -1, -1), // POS 36
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1), // POS 41
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1)
                        }),
                new Program("PB",
                        new Word[]{
                                // dado um inteiro em alguma posição de memória,
                                // se for negativo armazena -1 na saída; se for positivo responde o fatorial do
                                // número na saída
                                new Word(Opcode.LDI, 0, -1, 7), // numero para colocar na memoria
                                new Word(Opcode.STD, 0, -1, 50),
                                new Word(Opcode.LDD, 0, -1, 50),
                                new Word(Opcode.LDI, 1, -1, -1),
                                new Word(Opcode.LDI, 2, -1, 13), // SALVAR POS STOP
                                new Word(Opcode.JMPIL, 2, 0, -1), // caso negativo pula pro STD
                                new Word(Opcode.LDI, 1, -1, 1),
                                new Word(Opcode.LDI, 6, -1, 1),
                                new Word(Opcode.LDI, 7, -1, 13),
                                new Word(Opcode.JMPIE, 7, 0, 0), // POS 9 pula pra STD (Stop-1)
                                new Word(Opcode.MULT, 1, 0, -1),
                                new Word(Opcode.SUB, 0, 6, -1),
                                new Word(Opcode.JMP, -1, -1, 9), // pula para o JMPIE
                                new Word(Opcode.STD, 1, -1, 15),
                                new Word(Opcode.STOP, -1, -1, -1), // POS 14
                                new Word(Opcode.DATA, -1, -1, -1) // POS 15
                        }),
                new Program("PC",
                        new Word[]{
                                // Para um N definido (10 por exemplo)
                                // o programa ordena um vetor de N números em alguma posição de memória;
                                // ordena usando bubble sort
                                // loop ate que não swap nada
                                // passando pelos N valores
                                // faz swap de vizinhos se da esquerda maior que da direita
                                new Word(Opcode.LDI, 7, -1, 5), // TAMANHO DO BUBBLE SORT (N)
                                new Word(Opcode.LDI, 6, -1, 5), // aux N
                                new Word(Opcode.LDI, 5, -1, 46), // LOCAL DA MEMORIA
                                new Word(Opcode.LDI, 4, -1, 47), // aux local memoria
                                new Word(Opcode.LDI, 0, -1, 4), // colocando valores na memoria
                                new Word(Opcode.STD, 0, -1, 46),
                                new Word(Opcode.LDI, 0, -1, 3),
                                new Word(Opcode.STD, 0, -1, 47),
                                new Word(Opcode.LDI, 0, -1, 5),
                                new Word(Opcode.STD, 0, -1, 48),
                                new Word(Opcode.LDI, 0, -1, 1),
                                new Word(Opcode.STD, 0, -1, 49),
                                new Word(Opcode.LDI, 0, -1, 2),
                                new Word(Opcode.STD, 0, -1, 50), // colocando valores na memoria até aqui - POS 13
                                new Word(Opcode.LDI, 3, -1, 25), // Posicao para pulo CHAVE 1
                                new Word(Opcode.STD, 3, -1, 99),
                                new Word(Opcode.LDI, 3, -1, 22), // Posicao para pulo CHAVE 2
                                new Word(Opcode.STD, 3, -1, 98),
                                new Word(Opcode.LDI, 3, -1, 38), // Posicao para pulo CHAVE 3
                                new Word(Opcode.STD, 3, -1, 97),
                                new Word(Opcode.LDI, 3, -1, 25), // Posicao para pulo CHAVE 4 (não usada)
                                new Word(Opcode.STD, 3, -1, 96),
                                new Word(Opcode.LDI, 6, -1, 0), // r6 = r7 - 1 POS 22
                                new Word(Opcode.ADD, 6, 7, -1),
                                new Word(Opcode.SUBI, 6, -1, 1), // ate aqui
                                new Word(Opcode.JMPIEM, -1, 6, 97), // CHAVE 3 para pular quando r7 for 1 e r6 0 para
                                // interomper o loop de vez do programa
                                new Word(Opcode.LDX, 0, 5, -1), // r0 e ra pegando valores das posições da memoria POS
                                // 26
                                new Word(Opcode.LDX, 1, 4, -1),
                                new Word(Opcode.LDI, 2, -1, 0),
                                new Word(Opcode.ADD, 2, 0, -1),
                                new Word(Opcode.SUB, 2, 1, -1),
                                new Word(Opcode.ADDI, 4, -1, 1),
                                new Word(Opcode.SUBI, 6, -1, 1),
                                new Word(Opcode.JMPILM, -1, 2, 99), // LOOP chave 1 caso neg procura prox
                                new Word(Opcode.STX, 5, 1, -1),
                                new Word(Opcode.SUBI, 4, -1, 1),
                                new Word(Opcode.STX, 4, 0, -1),
                                new Word(Opcode.ADDI, 4, -1, 1),
                                new Word(Opcode.JMPIGM, -1, 6, 99), // LOOP chave 1 POS 38
                                new Word(Opcode.ADDI, 5, -1, 1),
                                new Word(Opcode.SUBI, 7, -1, 1),
                                new Word(Opcode.LDI, 4, -1, 0), // r4 = r5 + 1 POS 41
                                new Word(Opcode.ADD, 4, 5, -1),
                                new Word(Opcode.ADDI, 4, -1, 1), // ate aqui
                                new Word(Opcode.JMPIGM, -1, 7, 98), // LOOP chave 2
                                new Word(Opcode.STOP, -1, -1, -1), // POS 45
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1),
                                new Word(Opcode.DATA, -1, -1, -1)
                        })
        };
    }
}