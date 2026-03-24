# YALex — Generador de analizadores léxicos (CC3071)

YALex lee un archivo `.yal` (especificación de tokens con expresiones regulares al estilo Lex), construye **DFA por el método directo** (árbol sintáctico → nullable / firstpos / lastpos / followpos → DFA → minimización), y genera un **analizador léxico en Python** (`.py`) que aplica **lexema más largo** y **prioridad por orden de reglas**.

El motor **no** usa librerías de expresiones regulares del lenguaje destino: el reconocimiento va por **tablas de un DFA**.

---

## Requisitos

| Herramienta | Uso |
|-------------|-----|
| **JDK 21** | Compilar y ejecutar el generador (Java). |
| **Maven 3.x** | Compilar, empaquetar y pruebas. |
| **Python 3** (opcional) | Solo para **ejecutar** el `.py` generado (según lo que importes en el `{ header }` del `.yal`). |

---

## Cómo compilar

Desde la carpeta del módulo `yalex`:

```bash
cd yalex
mvn -q compile
```

Empaquetar JAR ejecutable:

```bash
mvn -q package
```

El JAR queda en `yalex/target/yalex-1.0-SNAPSHOT.jar`.

---

## Cómo ejecutar — solo terminal (CLI)

Sintaxis (equivalente a `yalex lexer.yal -o thelexer` del enunciado):

Tras `mvn compile` (o `mvn package`):

```bash
cd yalex
java -cp target/classes com.yalex.Main src/test/resources/samples/simple.yal -o output/mi_lexer
```

Con el JAR empaquetado:

```bash
java -jar target/yalex-1.0-SNAPSHOT.jar src/test/resources/samples/simple.yal -o output/mi_lexer
```

Se crea `mi_lexer.py` (si `-o` no termina en `.py`, se añade `.py`).

**Ayuda:**

```bash
java -cp target/classes com.yalex.Main --help
```

**Nota:** Si ejecutás `Main` **sin argumentos**, no entra al CLI: se abre la **interfaz gráfica** (ver siguiente sección).

---

## Cómo ejecutar — interfaz gráfica (GUI)

1. Compilá antes: `mvn -q compile` o `mvn -q package`.
2. Lanzá el programa **sin argumentos** para abrir la ventana Swing (estilo “IDE” con editor, terminal embebida y pestañas):

Tras `mvn compile`:

```bash
cd yalex
java -cp target/classes com.yalex.Main
```

(Sin argumentos: solo abre la GUI.)

3. En la GUI:
   - **+ Open Folder / .yal**: elegí un `.yal`.
   - Editá si querés; al **Run** se guarda el archivo y se genera **al lado del `.yal`** un `<mismo_nombre>.py`.
   - Verás salida en el panel tipo terminal, una pestaña con el **Python generado** y otra con el **diagrama del DFA combinado** (zoom con rueda, arrastrar para mover).

La lógica de generación es la misma que en CLI: `Main.runGeneration` → `YalParser` → `PythonCodeGen.generate`.

---

## Pruebas automáticas (JUnit)

```bash
cd yalex
mvn test
```

---

## Estructura relevante del código

| Parte | Paquetes / archivos |
|-------|----------------------|
| Entrada `.yal` | `com.yalex.yal` (`YalLexer`, `YalParser`, `YalFile`) |
| Modelo | `com.yalex.model` (`LetDefinition`, `Rule`, `RuleSet`) |
| Regexp → AST | `com.yalex.regex` |
| DFA directo + minimizar | `com.yalex.automata.*` |
| Salida Python | `com.yalex.codegen.PythonCodeGen`, `LetExpander` |
| CLI + arranque GUI | `com.yalex.Main` |
| GUI | `com.yalex.gui` (`YalexGui`, `YalexController`, `DfaGraphPanel`, `ConsoleOutputStream`) |
| DFA combinado (vista grafo) | `com.yalex.automata.dfa.CombinedDfaBuilder` |

---

## Ejemplos incluidos

En `yalex/src/test/resources/samples/`:

- `simple.yal` — caso pequeño (espacios, números, `+`, `eof`).
- `with_lets.yal` — `let` e identificadores.
- `full_example.yal` — más operadores y clase negada.

Opcionalmente hay scripts `.py` de apoyo en la misma carpeta para probar el lexer generado con stubs de tokens.

---

## Curso

Proyecto académico — **Diseño de lenguajes de programación (CC3071)**, Universidad del Valle de Guatemala.
