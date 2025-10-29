import java.util.*;

public class PageReplacement {

    /***
     * Algoritmo First-In, First-Out (FIFO).
     * Substitui a pagina que esta ha mais tempo na memoria
     *
     * @param pages Lista de paginas
     * @param numFrames - Numero de frames de memoria disponiveis
     * @return Numero de faltas de pagina
     */
    public static int fifo(int[] pages, int numFrames) {
        int faults = 0;
        Set<Integer> frames = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();

        for (int page : pages) {
            // verifica falta de pagina
            if (!frames.contains(page)) {
                faults++;

                // se memoria cheia, remove pagina mais antiga
                if (frames.size() == numFrames) {
                    int pageToRemove = queue.remove();
                    frames.remove(pageToRemove);
                }

                frames.add(page);
                queue.add(page);
            }
        }
        return faults;
    }

    /***
     * Algoritmo Least Recently Used (LRU).
     * Substitui a pagina que foi usada menos recentemente.
     *
     * @param pages Lista de paginas
     * @param numFrames - Numero de frames de memoria disponiveis
     * @return Numero de faltas de pagina
     */
    public static int lru(int[] pages, int numFrames) {
        int faults = 0;
        Set<Integer> frames = new HashSet<>();
        List<Integer> lastUsed = new ArrayList<>();

        for (int page : pages) {
            // verifica falta de pagina
            if (!frames.contains(page)) {
                faults++;

                // se memoria cheia, remove pagina usada menos recentemente
                if (frames.size() == numFrames) {
                    int pageToRemove = lastUsed.removeFirst();
                    frames.remove(pageToRemove);
                }

                frames.add(page);
                lastUsed.add(page);
            } else {
                // pagina esta na memoria, atualiza a posicao na lista de uso
                lastUsed.remove((Integer) page);
                lastUsed.add(page);
            }
        }

        return faults;
    }

    /***
     * Algoritmo Otimo.
     * Substitui a pagina que sera usada mais tarde no futuro.
     *
     * @param pages Lista de paginas
     * @param numFrames - Numero de frames de memoria disponiveis
     * @return Numero de faltas de pagina
     */
    public static int optimal(int[] pages, int numFrames) {
        int faults = 0;
        List<Integer> frames = new ArrayList<>();

        for (int i = 0; i < pages.length; i++) {
            int page = pages[i];

            // verifica falta de pagina
            if (!frames.contains(page)) {
                faults++;
            }

            // se memoria cheia, procura a pagina com o uso futuro mais distante
            if (frames.size() == numFrames) {
                int pageToRemove = -1;
                int farthest = -1;

                for (int framePage : frames) {
                    // assume que nao sera usada novamente
                    int nextUse = Integer.MAX_VALUE;

                    // procura o proximo uso dessa pagina no futuro
                    for (int j = i + 1; j < pages.length; j++) {
                        if (pages[j] == framePage) {
                            nextUse = j;
                            break;
                        }
                    }

                    // remove a pagina que nao sera mais usada
                    if (nextUse == Integer.MAX_VALUE) {
                        pageToRemove = framePage;
                        // encontrou a pagina a ser remvoida, sai do loop
                        break;
                    }

                    // atualiza a pagina com o proximo uso mais distante
                    if (nextUse > farthest) {
                        farthest = nextUse;
                        pageToRemove = framePage;
                    }
                }

                frames.remove((Integer) pageToRemove);
            }

            frames.add(page);
        }

        return faults;
    }

    /***
     * Algoritmo do Relogio (Algoritmo Segunda Chance).
     * Usa um bit de referencia (use bit) para dar uma segunda chance a uma pagina.
     *
     * @param pages Lista de paginas
     * @param numFrames - Numero de frames de memoria disponiveis
     * @return Numero de faltas de pagina
     */

    public static int clock(int[] pages, int numFrames) {
        int faults = 0;
        // mapa para armazenar o use bit da pagina
        Map<Integer, Boolean> frames = new HashMap<>();
        // arraylist vai ser tratada como lista circular
        List<Integer> frameOrder = new ArrayList<>();

        int hand = 0;

        for (int page : pages) {
            // verifica se a pagina esta na memoria e atualiza o use bit
            if (frames.containsKey(page)) {
                frames.put(page, true);
            }
            else {
                faults++;

                // se memoria NAO cheia
                if (frames.size() < numFrames) {
                    frames.put(page, true);
                    frameOrder.add(page);
                } else {
                    // se memoria cheia, usa o ponteiro para remover
                    while (true) {
                        int pageAtHand = frameOrder.get(hand);

                        // verifica use bit da pagina sendo apontada
                        if (frames.get(pageAtHand)) {
                            // use bit == true -> segunda chance
                            frames.put(pageAtHand, false);
                            // avanco do ponteiro garantindo comportamento circular na arraylist
                            hand = (hand + 1) % numFrames;
                        } else {
                            // use bit == false -> remove a pagina
                            frames.remove(pageAtHand);
                            frames.put(page, true);
                            frameOrder.set(hand, page); // substituicao na lista

                            hand =  (hand + 1) % numFrames;
                            // encontrou a pagina a ser remvoida, sai do loop
                            break;
                        }
                    }
                }
            }
        }
        return faults;
    }
}

