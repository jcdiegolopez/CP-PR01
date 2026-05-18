package com.yapar;

import com.yapar.analysis.FirstFollowCalculator;
import com.yapar.automaton.LR0Automaton;
import com.yapar.automaton.LR0AutomatonBuilder;
import com.yapar.bridge.YalexBridge;
import com.yapar.model.Grammar;
import com.yapar.model.Production;
import com.yapar.yalp.YalpFile;
import com.yapar.yalp.YalpParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Punto de entrada de YAPar.
 *
 * <pre>
 *   Uso: yapar parser.yalp -l lexer.yal -o theparser
 * </pre>
 *
 * Pipeline:
 * <ol>
 *   <li>Parsear .yalp → {@link YalpFile}  (Workstream A)</li>
 *   <li>Validar tokens con el .yal via {@link YalexBridge}  (Workstream A)</li>
 *   <li>Construir {@link Grammar}  (Workstream A)</li>
 *   <li>FIRST/FOLLOW + autómata LR(0)  (Workstream B — pendiente)</li>
 *   <li>Tablas SLR(1) + generación de theparser  (Workstream C — pendiente)</li>
 * </ol>
 */
public final class YaparMain {

    private YaparMain() {}

    public static void main(String[] args) {
        if (args.length == 0 || isHelp(args[0])) {
            System.out.print(usage());
            return;
        }
        try {
            ParsedCli cli = parseCli(args);
            runPipeline(cli);
        } catch (IllegalArgumentException ex) {
            System.err.println("[YAPAR] Error: " + ex.getMessage());
            System.err.println();
            System.err.print(usage());
            System.exit(1);
        } catch (Exception ex) {
            System.err.println("[YAPAR] Error inesperado: " + ex.getMessage());
            System.exit(1);
        }
    }

    /**
     * Ejecuta el pipeline completo. Expuesto para pruebas.
     */
    /**
     * Resultado del pipeline completo hasta el final del Workstream B.
     * Dev 3 (Workstream C) extiende esto con {@code ParseResult} cuando esté listo.
     */
    public record PipelineResult(Grammar grammar, LR0Automaton automaton,
                                  Map<String, Set<String>> followSets) {}

    public static PipelineResult runPipeline(ParsedCli cli) {
        Objects.requireNonNull(cli, "cli no puede ser null");

        // ── Paso 1: Parsear .yalp ─────────────────────────────────────────────
        YalpFile yalpFile = new YalpParser().parse(cli.yalp());
        System.out.println("[YAPAR] .yalp parseado: " +
            yalpFile.getDeclaredTokens().size() + " terminal(es), " +
            yalpFile.getProductions().size() + " producción(es) distintas.");

        // ── Paso 2: Bridge con YALex (opcional) ───────────────────────────────
        if (cli.yal() != null) {
            YalexBridge.BridgeResult bridge =
                YalexBridge.analyze(cli.yal(), yalpFile.getDeclaredTokens());
            bridge.warnings().forEach(System.err::println);
            System.out.println("[YAPAR] Bridge .yal: " +
                bridge.letNames().size() + " let(s), " +
                bridge.rulePatterns().size() + " patrón(es) en reglas.");
        }

        // ── Paso 3: Construir Grammar (Workstream A) ──────────────────────────
        Grammar grammar = Grammar.build(yalpFile);
        printGrammarSummary(grammar);

        // ── Paso 4: FIRST/FOLLOW + Autómata LR(0) (Workstream B) ─────────────
        FirstFollowCalculator calc = new FirstFollowCalculator(grammar);
        Map<String, Set<String>> followSets = calc.getFollowSets();
        System.out.println("[YAPAR] Conjuntos FIRST/FOLLOW calculados.");
        followSets.forEach((nt, set) ->
            System.out.println("  FOLLOW(" + nt + ") = " + set));

        LR0AutomatonBuilder builder = new LR0AutomatonBuilder(grammar);
        LR0Automaton automaton = builder.build();
        System.out.println("[YAPAR] Autómata LR(0): " +
            automaton.stateCount() + " estados, " +
            automaton.transitionCount() + " transiciones.");

        // ── Paso 5: Tablas SLR(1) + generación theparser (Workstream C) ───────
        System.out.println("[YAPAR] Tablas SLR(1) + generación theparser → pendiente (Workstream C).");

        return new PipelineResult(grammar, automaton, followSets);
    }

    private static void printGrammarSummary(Grammar grammar) {
        System.out.println("[YAPAR] Gramática construida:");
        System.out.println("  Símbolo inicial : " + grammar.startSymbol());
        System.out.println("  No-terminales   : " + grammar.nonTerminals());
        System.out.println("  Terminales      : " + grammar.terminals());
        System.out.println("  Producciones    : " + grammar.productions().size());
        for (Production p : grammar.productions()) {
            System.out.println("    " + p);
        }
    }

    // ── Parseo de la línea de comandos ────────────────────────────────────────

    public static ParsedCli parseCli(String[] args) {
        Objects.requireNonNull(args, "args no puede ser null");

        Path yalp   = null;
        Path yal    = null;
        String outStr = null;

        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            switch (a) {
                case "-l", "--lexer" -> {
                    if (i + 1 >= args.length)
                        throw new IllegalArgumentException("Falta la ruta tras " + a);
                    if (yal != null)
                        throw new IllegalArgumentException("Solo se permite un " + a);
                    yal = Path.of(args[++i]);
                    if (!Files.isRegularFile(yal))
                        throw new IllegalArgumentException("No es un archivo regular: " + yal);
                }
                case "-o", "--output" -> {
                    if (i + 1 >= args.length)
                        throw new IllegalArgumentException("Falta la ruta tras -o");
                    if (outStr != null)
                        throw new IllegalArgumentException("Solo se permite un -o");
                    outStr = args[++i].trim();
                    if (outStr.isEmpty())
                        throw new IllegalArgumentException("Ruta de salida vacía");
                }
                default -> {
                    if (a.startsWith("-"))
                        throw new IllegalArgumentException("Opción desconocida: " + a);
                    if (yalp != null)
                        throw new IllegalArgumentException("Solo se permite un archivo .yalp");
                    yalp = Path.of(a);
                    if (!Files.isRegularFile(yalp))
                        throw new IllegalArgumentException("No es un archivo regular: " + yalp);
                }
            }
        }

        if (yalp == null)   throw new IllegalArgumentException("Falta el archivo .yalp");
        if (outStr == null) throw new IllegalArgumentException("Falta -o <salida>");

        Path output = normalizeOutput(outStr);
        return new ParsedCli(yalp, yal, output);
    }

    private static Path normalizeOutput(String out) {
        return out.endsWith(".py") ? Path.of(out) : Path.of(out + ".py");
    }

    private static boolean isHelp(String a) {
        return "-h".equals(a) || "--help".equals(a) || "/?".equals(a);
    }

    private static String usage() {
        return """
                Uso: yapar <parser.yalp> [-l <lexer.yal>] -o <salida>

                  <parser.yalp>     especificación de la gramática
                  -l <lexer.yal>    (opcional) archivo YALex para validación cruzada de tokens
                  -o <salida>       ruta base del parser generado (se añade .py si falta)

                Opciones:
                  -h, --help        muestra esta ayuda
                """;
    }

    public record ParsedCli(Path yalp, Path yal, Path output) {}
}
