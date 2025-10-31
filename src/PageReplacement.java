import java.util.*;
import java.util.function.Consumer;

public class PageReplacement {

    /***
     * Algoritmo First-In, First-Out (FIFO).
     * Substitui a pagina que esta ha mais tempo na memoria
     *
     * @param pages Lista de paginas
     * @param numFrames - Numero de frames de memoria disponiveis
     * @param log
     */
    public static void fifo(int[] pages, int numFrames, Consumer<String> log) {
        int faults = 0;
        Set<Integer> frames = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();

        for (int i = 0; i < pages.length; i++) {
            int page = pages[i];
            // verifica falta de pagina
            if (!frames.contains(page)) {
                faults++;
                log.accept(String.format("(FIFO) Acesso %d: (%d) -> MISS. Falta #%d", i, page, faults));

                // se memoria cheia, remove pagina mais antiga
                if (frames.size() == numFrames) {
                    int removed = queue.remove();
                    frames.remove(removed);
                    log.accept(String.format("(FIFO) Acesso %d: Memoria cheia. Removendo (%d)", i, removed));
                } else {
                    log.accept(String.format("(FIFO) Acesso %d: Memoria livre", i));
                }
                log.accept(String.format("(FIFO) Acesso %d: Inserindo (%d)", i, page));
                frames.add(page);
                queue.add(page);
            } else {
                log.accept(String.format("(FIFO) Acesso %d: (%d) -> HIT", i, page));
            }
            log.accept("(FIFO) Frames: " + framesToString(frames));
            log.accept("(FIFO) Fila: " + queue);
        }
        log.accept(String.format("(FIFO) Total de faltas: %d", faults));
    }

    /***
     * Algoritmo Least Recently Used (LRU).
     * Substitui a pagina que foi usada menos recentemente.
     *
     * @param pages Lista de paginas
     * @param numFrames - Numero de frames de memoria disponiveis
     * @param log
     */
    public static void lru(int[] pages, int numFrames,  Consumer<String> log) {
        int faults = 0;
        Set<Integer> frames = new HashSet<>();
        List<Integer> usage = new ArrayList<>();

        for (int i = 0; i < pages.length; i++) {
            int page = pages[i];
            // verifica falta de pagina
            if (!frames.contains(page)) {
                faults++;
                log.accept(String.format("(LRU) Acesso %d: (%d) -> MISS. Falta #%d", i, page, faults));

                // se memoria cheia, remove pagina usada menos recentemente
                if (frames.size() == numFrames) {
                    int removed = usage.removeFirst();
                    frames.remove(removed);
                    log.accept(String.format("(LRU) Acesso %d: Memoria cheia. Removendo (%d)", i, removed));
                } else {
                    log.accept(String.format("(LRU) Acesso %d: Memoria livre", i));
                }
                log.accept(String.format("(LRU) Acesso %d: Inserindo (%d)", i, page));
                frames.add(page);
                usage.add(page);
            } else {
                // pagina esta na memoria, atualiza a posicao na lista de uso
                usage.remove((Integer) page);
                usage.add(page);
                log.accept(String.format("(LRU) Acesso %d: (%d) -> HIT. Atualizando posicao", i, page));
            }
            log.accept("(LRU) Frames: " + framesToString(frames));
            log.accept("(LRU) Ordem: " + usage);
        }
        log.accept(String.format("(LRU) Total de faltas: %d", faults));
    }

    /***
     * Algoritmo Otimo.
     * Substitui a pagina que sera usada mais tarde no futuro.
     *
     * @param pages Lista de paginas
     * @param numFrames - Numero de frames de memoria disponiveis
     * @param log
     */
    public static void optimal(int[] pages, int numFrames, Consumer<String> log) {
        int faults = 0;
        List<Integer> frames = new ArrayList<>();

        for (int i = 0; i < pages.length; i++) {
            int page = pages[i];
            // verifica falta de pagina
            if (!frames.contains(page)) {
                faults++;
                log.accept(String.format("(Opt) Acesso %d: (%d) -> MISS. Falta #%d", i, page, faults));
                // se memoria cheia, procura a pagina com o uso futuro mais distante
                if (frames.size() == numFrames) {
                    int toRemove = -1;
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
                        log.accept(String.format("(Opt) Acesso %d: Proximo uso (%d) -> %d", i, framePage, nextUse));

                        // remove a pagina que nao sera mais usada
                        if (nextUse == Integer.MAX_VALUE) {
                            toRemove = framePage;
                            // encontrou a pagina a ser remvoida, sai do loop
                            break;
                        }

                        // atualiza a pagina com o proximo uso mais distante
                        if (nextUse > farthest) {
                            farthest = nextUse;
                            toRemove = framePage;
                        }
                    }

                    frames.remove((Integer) toRemove);
                    log.accept(String.format("(Opt) Acesso %d: Removendo (%d)", i, toRemove));
                }
                frames.add(page);
            } else {
                log.accept(String.format("(Opt) Acesso %d: (%d) -> HIT", i, page));
            }
            log.accept("(Opt) Frames: " + framesToString(frames));
        }
        log.accept(String.format("(Opt) Total de faltas: %d", faults));
    }

    /***
     * Algoritmo do Relogio (Algoritmo Segunda Chance).
     * Usa um bit de referencia (use bit) para dar uma segunda chance a uma pagina.
     *
     * @param pages Lista de paginas
     * @param numFrames - Numero de frames de memoria disponiveis
     * @param log
     */

    public static void clock(int[] pages, int numFrames, Consumer<String> log) {
        int faults = 0;
        // arraylist vai ser tratada como lista circular
        List<ClockEntry> frames = new ArrayList<>();
        // map pagina -> sua clockentry para facilitar busca
        Map<Integer, ClockEntry> map = new HashMap<>();
        // ponteiro
        int hand = 0;

        for (int i = 0; i < pages.length; i++) {
            int page = pages[i];

            // verifica se a pagina esta na memoria e atualiza o use bit
            if (map.containsKey(page)) {
                ClockEntry entry = map.get(page);
                entry.usebit = true;
                log.accept(String.format("(Clock) Acesso %d: (%d) -> HIT", i, page));
            }
            else {
                faults++;
                log.accept(String.format("(Clock) Acesso %d: (%d) -> MISS. Falta #%d", i, page, faults));

                // se memoria NAO cheia
                if (frames.size() < numFrames) {
                    ClockEntry newEntry = new ClockEntry(page);
                    frames.add(newEntry);
                    map.put(page, newEntry);
                    log.accept(String.format("(Clock) Acesso %d: Memoria livre. Inserindo (%d)", i, page));
                } else {
                    log.accept(String.format("(Clock) Acesso %d: Memoria cheia. Procurando substituicao", i));
                    // se memoria cheia, usa o ponteiro para remover
                    while (true) {
                        ClockEntry pageAtHand = frames.get(hand);

                        // verifica use bit da pagina sendo apontada
                        if (pageAtHand.usebit) {
                            // use bit == true -> segunda chance
                            log.accept(String.format("(Clock) Acesso %d: %s Segunda chance. Set USEBIT=false", i, pageAtHand));
                            pageAtHand.usebit = false;
                            // avanca ponteiro
                            hand = (hand + 1) % numFrames;
                        } else {
                            log.accept(String.format("(Clock) Acesso %d: %s Removendo", i, pageAtHand));
                            // use bit == false -> remove a pagina
                            map.remove(pageAtHand.page);

                            ClockEntry newEntry = new ClockEntry(page);
                            frames.set(hand, newEntry); // substituicao
                            map.put(page, newEntry);

                            // avanca ponteiro
                            hand =  (hand + 1) % numFrames;

                            log.accept(String.format("(Clock) Acesso %d: Inserindo (%d)", i, page));
                            break;
                        }
                    }
                }
            }
            log.accept(String.format("Frames: %s | Hand: %d",  frames, hand));
        }
        log.accept(String.format("(Clock) Total de faltas: %d", faults));
    }

    private static class ClockEntry {
        int page;
        boolean usebit;

        ClockEntry(int page) {
            this.page = page;
            this.usebit = true;
        }

        @Override
        public String toString() {
            return String.format("(%d:USEBIT=%s)", page, usebit ? "TRUE" : "FALSE");
        }
    }

    private static String framesToString(Collection<Integer> frames) {
        if (frames.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        Iterator<Integer> it = frames.iterator();
        sb.append("[");
        while (it.hasNext()) {
            sb.append(it.next());
            if (it.hasNext()) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}

