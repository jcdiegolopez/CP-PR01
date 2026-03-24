package com.yalex.gui;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Ejecuta el lexer Python generado sobre un texto de entrada (para la pestaña "Probar lexer").
 */
public final class LexerTestRunner {

    public record Result(int exitCode, String stdout, String stderr, String pythonCommand) {
        public boolean isPythonMissing() {
            return exitCode == -2;
        }
    }

    private LexerTestRunner() {
    }

    /**
     * Intenta ejecutar {@code tokenize_all} del módulo generado leyendo la entrada desde stdin UTF-8.
     */
    public static Result run(Path lexerPy, String input) throws IOException, InterruptedException {
        Objects.requireNonNull(lexerPy, "lexerPy");
        Objects.requireNonNull(input, "input");
        if (!Files.isRegularFile(lexerPy)) {
            return new Result(-1, "", "No existe el archivo generado: " + lexerPy, "");
        }

        List<String> prefix = resolvePythonPrefix();
        if (prefix == null) {
            return new Result(-2, "", "No se encontró Python en el PATH (prueba: python, python3 o py -3).", "");
        }

        Path helper = extractHelperScript();
        try {
            List<String> cmd = new ArrayList<>(prefix);
            cmd.add(helper.toAbsolutePath().toString());
            cmd.add(lexerPy.toAbsolutePath().toString());

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(false);
            Process p = pb.start();

            String toSend = input;
            if (!toSend.isEmpty() && toSend.charAt(0) == '\uFEFF') {
                toSend = toSend.substring(1);
            }
            byte[] inBytes = toSend.getBytes(StandardCharsets.UTF_8);
            p.getOutputStream().write(inBytes);
            p.getOutputStream().close();

            String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            String err = new String(p.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);

            boolean finished = p.waitFor(60, TimeUnit.SECONDS);
            if (!finished) {
                p.destroyForcibly();
                return new Result(-1, out, err + "\n[Timeout 60s]", String.join(" ", prefix));
            }
            int code = p.exitValue();
            return new Result(code, out, err, String.join(" ", prefix));
        } finally {
            Files.deleteIfExists(helper);
        }
    }

    private static List<String> resolvePythonPrefix() {
        String[][] trials = {
                {"python"},
                {"python3"},
                {"py", "-3"}
        };
        for (String[] trial : trials) {
            try {
                List<String> cmd = new ArrayList<>(Arrays.asList(trial));
                cmd.add("--version");
                Process p = new ProcessBuilder(cmd).start();
                boolean ok = p.waitFor(5, TimeUnit.SECONDS) && p.exitValue() == 0;
                if (ok) {
                    return Arrays.asList(trial);
                }
            } catch (Exception ignored) {
                // siguiente candidato
            }
        }
        return null;
    }

    private static Path extractHelperScript() throws IOException {
        String resource = "/com/yalex/gui/run_lexer_helper.py";
        try (InputStream in = LexerTestRunner.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IOException("Recurso no encontrado: " + resource);
            }
            Path tmp = Files.createTempFile("yalex_run_lexer_", ".py");
            tmp.toFile().deleteOnExit();
            Files.copy(in, tmp, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return tmp;
        }
    }
}
