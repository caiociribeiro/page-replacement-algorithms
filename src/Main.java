import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Wrong number of arguments");
        }

        String filePath = args[0];
        int NUM_FRAMES = Integer.parseInt(args[1]);

        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        String[] pageStrings = content.split(" ");
        int[] pages = new int[pageStrings.length];
        for (int i = 0; i < pageStrings.length; i++) {
            pages[i] = Integer.parseInt(pageStrings[i]);
        }

        System.out.println("\nSIMULADOR DE SUBSTITUICAO DE PAGINAS");
        System.out.println("--".repeat(25) + "\n");
        System.out.printf("Lendo arquivo '%s'\n", filePath);
        System.out.printf("%-20s - %10d\n", "Numero de frames", NUM_FRAMES);
        System.out.printf("%-20s - %10d\n", "Numero de paginas", pages.length);
        System.out.println();
        System.out.println("--".repeat(25) + "\n");

        int fifo = PageReplacement.fifo(pages, NUM_FRAMES);
        int lru = PageReplacement.lru(pages, NUM_FRAMES);
        int optimal = PageReplacement.optimal(pages, NUM_FRAMES);
        int clock = PageReplacement.clock(pages, NUM_FRAMES);

        System.out.printf("%-20s - %10d faltas de página%n", "Metodo FIFO", fifo);
        System.out.printf("%-20s - %10d faltas de página%n", "Metodo LRU", lru);
        System.out.printf("%-20s - %10d faltas de página%n", "Metodo Otimo", optimal);
        System.out.printf("%-20s - %10d faltas de página%n", "Metodo Relogio", clock);
    }
}