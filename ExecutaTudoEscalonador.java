import java.util.List;

public class ExecutaTudoEscalonador extends Thread{
    public static class ExecutaTudo(List<ProcessControlBlock> filaProntos,ProcessControlBlock processoRodando{
        while (true) {
            ProcessControlBlock pcb;
            synchronized (lock) {
                if (filaProntos.isEmpty()) {
                    System.out.println("Todos os processos finalizados.");
                    break;
                }
                //tirar esse if será ?
                if (processoRodando != null) {
                    break;
                }
                pcb = filaProntos.remove(0);
            }

            executaFatia(pcb);
        }
    }


}
