import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class FileGenerator {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            throw new IllegalArgumentException("Wrong number of arguments");
        }

        int numPages = Integer.parseInt(args[0]);
        int max = Integer.parseInt(args[1]);
        String fileName = "pages" + numPages + ".txt";

        Path dataFolder = Path.of("data");
        Files.createDirectories(dataFolder);

        Path filePath = dataFolder.resolve(fileName);

        System.out.printf("Criando arquivo com %d numeros de 0 a %d\n", numPages, max);

        Random r = new Random();
        try (FileWriter w = new FileWriter(filePath.toFile())) {
            for (int i = 0; i < numPages; i++) {
                int n =  r.nextInt(max + 1);
                w.write(n + (i < numPages - 1 ? " " : ""));
            }
        }

        System.out.printf("Arquivo %s criado em '%s'", fileName, dataFolder.toAbsolutePath());
    }
}
