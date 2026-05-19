package com.yapar.codegen;

import com.yapar.automaton.LR0Automaton;
import com.yapar.model.Grammar;
import com.yapar.model.Production;
import com.yapar.tables.ParseResult;
import com.yapar.tables.ParseTable;
import com.yapar.tables.SLRAction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

/**
 * Genera el archivo {@code theparser.py} con:
 * <ul>
 *   <li>Tablas ACTION y GOTO serializadas como dicts Python.</li>
 *   <li>Funciones de acción semántica para cada producción.</li>
 *   <li>Driver LR genérico con función pública {@code parse(tokens)}.</li>
 * </ul>
 *
 * El formato de las entradas en ACTION es:
 * <pre>
 *   ("shift", estado)
 *   ("reduce", prod_id, lhs, rhs_len)
 *   ("accept",)
 *   ("error",)
 * </pre>
 */
public final class ParserCodeGen {

    private ParserCodeGen() {}

    /**
     * Genera el archivo de salida en {@code outputPath}.
     *
     * @param result    tablas SLR(1) + conflictos
     * @param grammar   gramática original (para nombres de símbolos)
     * @param automaton autómata LR(0) (para producciones augmentadas y acciones semánticas)
     * @param outputPath ruta del archivo .py a crear
     */
    public static void generate(ParseResult result, Grammar grammar,
                                LR0Automaton automaton, Path outputPath) {
        Objects.requireNonNull(result,     "result no puede ser null");
        Objects.requireNonNull(grammar,    "grammar no puede ser null");
        Objects.requireNonNull(automaton,  "automaton no puede ser null");
        Objects.requireNonNull(outputPath, "outputPath no puede ser null");

        String source = buildSource(result.table(), automaton);
        try {
            Files.writeString(outputPath, source, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new RuntimeException("No se pudo escribir " + outputPath + ": " + ex.getMessage(), ex);
        }
    }

    // ── Construcción del fuente Python ────────────────────────────────────────

    private static String buildSource(ParseTable table, LR0Automaton automaton) {
        StringBuilder sb = new StringBuilder();

        appendHeader(sb);
        appendSemanticActions(sb, automaton);  // deben definirse antes de PRODUCTIONS
        appendActionTable(sb, table);
        appendGotoTable(sb, table);
        appendProductions(sb, automaton);
        appendDriver(sb);

        return sb.toString();
    }

    // ── Secciones ─────────────────────────────────────────────────────────────

    private static void appendHeader(StringBuilder sb) {
        sb.append("# theparser.py — generado automáticamente por YAPar\n");
        sb.append("# No editar manualmente.\n\n");
    }

    private static void appendActionTable(StringBuilder sb, ParseTable table) {
        sb.append("ACTION = {\n");
        for (Map.Entry<Integer, Map<String, SLRAction>> stateEntry
                : table.actionTable().entrySet()) {
            sb.append("    ").append(stateEntry.getKey()).append(": {");
            boolean first = true;
            for (Map.Entry<String, SLRAction> symEntry : stateEntry.getValue().entrySet()) {
                if (!first) sb.append(", ");
                sb.append(pyString(symEntry.getKey())).append(": ")
                  .append(actionToPython(symEntry.getValue()));
                first = false;
            }
            sb.append("},\n");
        }
        sb.append("}\n\n");
    }

    private static void appendGotoTable(StringBuilder sb, ParseTable table) {
        sb.append("GOTO = {\n");
        for (Map.Entry<Integer, Map<String, Integer>> stateEntry
                : table.gotoTable().entrySet()) {
            sb.append("    ").append(stateEntry.getKey()).append(": {");
            boolean first = true;
            for (Map.Entry<String, Integer> symEntry : stateEntry.getValue().entrySet()) {
                if (!first) sb.append(", ");
                sb.append(pyString(symEntry.getKey())).append(": ")
                  .append(symEntry.getValue());
                first = false;
            }
            sb.append("},\n");
        }
        sb.append("}\n\n");
    }

    private static void appendProductions(StringBuilder sb, LR0Automaton automaton) {
        sb.append("# (prod_id, lhs, rhs_len, action_fn)\n");
        sb.append("PRODUCTIONS = [\n");
        for (Production p : automaton.augmentedProductions()) {
            String actionFn = p.action().isEmpty() ? "None" : ("sem_" + p.id());
            sb.append("    (").append(p.id()).append(", ")
              .append(pyString(p.lhs())).append(", ")
              .append(p.rhs().size()).append(", ").append(actionFn).append("),\n");
        }
        sb.append("]\n\n");
    }

    private static void appendSemanticActions(StringBuilder sb, LR0Automaton automaton) {
        sb.append("# Acciones semánticas\n");
        for (Production p : automaton.augmentedProductions()) {
            if (p.action().isEmpty()) continue;
            sb.append("def sem_").append(p.id()).append("(stack):\n");
            sb.append("    # ").append(p.lhs()).append(" -> ")
              .append(p.rhs().isEmpty() ? "eps" : String.join(" ", p.rhs())).append("\n");
            sb.append("    return ").append(pyString(p.action())).append("\n\n");
        }
    }

    private static void appendDriver(StringBuilder sb) {
        sb.append("""
def parse(tokens):
    \"\"\"
    tokens: lista de (tipo, lexema) donde tipo es el nombre del terminal.
    Devuelve el resultado de la acción semántica de la producción raíz,
    o lanza SyntaxError si la entrada no es válida.
    \"\"\"
    tokens = list(tokens) + [('$', '$')]
    state_stack = [0]
    val_stack   = []
    i = 0

    while True:
        state  = state_stack[-1]
        tok_type, tok_val = tokens[i]
        action = ACTION.get(state, {}).get(tok_type)

        if action is None:
            raise SyntaxError(
                f"Error de sintaxis en token '{tok_type}' ('{tok_val}'), estado {state}")

        kind = action[0]

        if kind == 'shift':
            state_stack.append(action[1])
            val_stack.append(tok_val)
            i += 1

        elif kind == 'reduce':
            _, prod_id, lhs, rhs_len = action
            _, _, _, sem_fn = PRODUCTIONS[prod_id]
            args = val_stack[-rhs_len:] if rhs_len > 0 else []
            result = sem_fn(args) if sem_fn is not None else (args[0] if args else None)
            if rhs_len > 0:
                del state_stack[-rhs_len:]
                del val_stack[-rhs_len:]
            top = state_stack[-1]
            next_state = GOTO.get(top, {}).get(lhs)
            if next_state is None:
                raise SyntaxError(f"GOTO indefinido para ({top}, '{lhs}')")
            state_stack.append(next_state)
            val_stack.append(result)

        elif kind == 'accept':
            return val_stack[-1] if val_stack else None

        else:
            raise SyntaxError(
                f"Error en token '{tok_type}' ('{tok_val}'), estado {state}")
""");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String actionToPython(SLRAction action) {
        return switch (action) {
            case SLRAction.Shift s   -> "('shift', " + s.state() + ")";
            case SLRAction.Reduce r  -> "('reduce', " + r.production().id() + ", "
                                        + pyString(r.production().lhs()) + ", "
                                        + r.production().rhs().size() + ")";
            case SLRAction.Accept a  -> "('accept',)";
            case SLRAction.Error e   -> "('error',)";
        };
    }

    private static String pyString(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
