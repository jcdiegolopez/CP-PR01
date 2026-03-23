package com.yalex;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.yalex.codegen.PythonCodeGen;
import com.yalex.yal.YalFile;
import com.yalex.yal.YalParser;

/**
 * Punto de entrada: {@code java -jar yalex.jar lexer.yal -o thelexer}
 * (genera {@code thelexer.py}).
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        // Lanzar Interfaz Gráfica si no hay argumentos (TC-01)
        if (args.length == 0) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                try {
                    javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {} // Fallback silencioso
                
                com.yalex.gui.YalexGui view = new com.yalex.gui.YalexGui();
                new com.yalex.gui.YalexController(view);
                view.setVisible(true);
            });
            return;
        }

        // Ejecución en Consola / CLI Normal (TC-04)
        if (args.length == 1 && isHelp(args[0])) {
            System.out.print(usage());
            return;
        }
        try {
            ParsedCli cli = parseCli(args);
            runGeneration(cli.input(), cli.output());
        } catch (IllegalArgumentException ex) {
            System.err.println(ex.getMessage());
            System.err.println();
            System.err.print(usage());
            System.exit(1);
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        } catch (Exception ex) {
            System.err.println("Error inesperado en CLI: " + ex.getMessage());
            System.exit(1);
        }
    }

    /**
     * Lógica central de ejecución, extraída para que la GUI no lance System.exit(1)
     */
    public static void runGeneration(Path input, Path output) throws IOException {
        YalFile yal = new YalParser().parse(input);
        PythonCodeGen.generate(yal, output);
        System.out.println("¡Generador ejecutado con Éxito!");
        System.out.println("Analizador en bruto: " + output.toAbsolutePath().normalize());
    }

    /**
     * Parsea la línea de comandos. Expuesto para pruebas (mismo paquete).
     *
     * <p>Formas aceptadas:
     * {@code yalex archivo.yal -o salida} o {@code yalex -o salida archivo.yal}
     */
    static ParsedCli parseCli(String[] args) {
        Objects.requireNonNull(args, "args");
        if (args.length < 3) {
            throw new IllegalArgumentException("faltan argumentos.");
        }
        String out = null;
        List<String> loose = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            if ("-o".equals(a) || "--output".equals(a)) {
                if (i + 1 >= args.length) {
                    throw new IllegalArgumentException("falta ruta tras -o");
                }
                if (out != null) {
                    throw new IllegalArgumentException("solo se permite un -o");
                }
                out = args[++i].trim();
                if (out.isEmpty()) {
                    throw new IllegalArgumentException("ruta de salida vacía");
                }
                continue;
            }
            if (a.startsWith("-")) {
                throw new IllegalArgumentException("opción desconocida: " + a);
            }
            loose.add(a);
        }
        if (out == null) {
            throw new IllegalArgumentException("falta -o <salida>");
        }
        if (loose.size() != 1) {
            throw new IllegalArgumentException("se espera exactamente un archivo .yal");
        }
        Path input = Path.of(loose.get(0));
        if (!Files.isRegularFile(input)) {
            throw new IllegalArgumentException("no es un archivo regular: " + input);
        }
        Path output = normalizeOutputPath(out);
        return new ParsedCli(input, output);
    }

    private static Path normalizeOutputPath(String out) {
        String s = out;
        if (!s.endsWith(".py")) {
            s = s + ".py";
        }
        return Path.of(s);
    }

    private static boolean isHelp(String a) {
        return "-h".equals(a) || "--help".equals(a) || "/?".equals(a);
    }

    private static String usage() {
        return """
                Uso: yalex <archivo.yal> -o <salida>
                     yalex -o <salida> <archivo.yal>

                <salida> es la ruta base del .py (se añade .py si no termina en .py).

                Opciones:
                  -h, --help    muestra esta ayuda
                """;
    }

    record ParsedCli(Path input, Path output) {
    }
}
