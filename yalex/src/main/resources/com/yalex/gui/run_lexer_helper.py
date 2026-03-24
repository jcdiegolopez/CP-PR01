# -*- coding: utf-8 -*-
"""Invocado por la GUI de YALex: carga el lexer generado y ejecuta tokenize_all sobre stdin UTF-8."""
import importlib.util
import sys


def main() -> None:
    if len(sys.argv) != 2:
        print("Uso: run_lexer_helper.py <ruta_lexer.py>", file=sys.stderr)
        sys.exit(2)
    path = sys.argv[1]
    spec = importlib.util.spec_from_file_location("_yalex_generated_lexer", path)
    if spec is None or spec.loader is None:
        print("No se pudo cargar el módulo desde: " + path, file=sys.stderr)
        sys.exit(2)
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    data = sys.stdin.read()
    try:
        if not hasattr(mod, "tokenize_all"):
            print("El módulo no define tokenize_all", file=sys.stderr)
            sys.exit(1)
        mod.tokenize_all(data)
    except Exception as e:
        print(type(e).__name__ + ": " + str(e), file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
