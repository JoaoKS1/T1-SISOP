public class ComandosTerminal {

    public static void showCommandsTerminal() {
        System.out.println("\n===== TERMINAL =====");

        System.out.println("- new <nomeDePrograma> ");
        // Cria um processo na memória, solicita alocação ao GM,
        // cria PCB, configura partição/tabela de páginas e adiciona na lista de prontos.
        // Retorna um ID único do processo.

        System.out.println("- rm <id>");
        // Remove o processo com o ID informado do sistema,
        // independente de já ter executado ou não.

        System.out.println("- ps");
        // Lista todos os processos existentes no sistema. (EXECUTANDO)

        System.out.println("- dump <id>");
        // Mostra o conteúdo do PCB e da memória do processo com o ID informado.

        System.out.println("- dumpM <inicio, fim>");
        // Mostra o conteúdo da memória entre as posições início e fim,
        // independente de qual processo pertence.

        System.out.println("- exec <id>");
        // Executa o processo com o ID informado.
        // Se não existir, deve retornar erro.

        System.out.println("- traceOn");
        // Ativa o modo de execução detalhado (CPU imprime cada instrução executada).

        System.out.println("- traceOff");
        // Desativa o modo de execução detalhado.

        System.out.println("- exit");
        // Encerra o sistema.

    }


}
