# theparser.py — generado automáticamente por YAPar
# No editar manualmente.

# Acciones semánticas
def sem_1(stack):
    # E -> E PLUS T
    return "\"suma\""

def sem_2(stack):
    # E -> E MINUS T
    return "\"resta\""

def sem_3(stack):
    # E -> T
    return "\"pasarE\""

def sem_4(stack):
    # T -> T TIMES F
    return "\"multiplicacion\""

def sem_5(stack):
    # T -> T DIVIDE F
    return "\"division\""

def sem_6(stack):
    # T -> T MOD F
    return "\"modulo\""

def sem_7(stack):
    # T -> F
    return "\"pasarT\""

def sem_8(stack):
    # F -> LPAREN E RPAREN
    return "\"agrupacion\""

def sem_9(stack):
    # F -> NUM
    return "\"numero\""

ACTION = {
    0: {"NUM": ('shift', 4), "LPAREN": ('shift', 5)},
    1: {"$": ('accept',), "PLUS": ('shift', 6), "MINUS": ('shift', 7)},
    2: {"MOD": ('shift', 10), "$": ('reduce', 3, "E", 1), "TIMES": ('shift', 8), "RPAREN": ('reduce', 3, "E", 1), "PLUS": ('reduce', 3, "E", 1), "MINUS": ('reduce', 3, "E", 1), "DIVIDE": ('shift', 9)},
    3: {"MOD": ('reduce', 7, "T", 1), "$": ('reduce', 7, "T", 1), "TIMES": ('reduce', 7, "T", 1), "RPAREN": ('reduce', 7, "T", 1), "PLUS": ('reduce', 7, "T", 1), "MINUS": ('reduce', 7, "T", 1), "DIVIDE": ('reduce', 7, "T", 1)},
    4: {"MOD": ('reduce', 9, "F", 1), "$": ('reduce', 9, "F", 1), "TIMES": ('reduce', 9, "F", 1), "RPAREN": ('reduce', 9, "F", 1), "PLUS": ('reduce', 9, "F", 1), "MINUS": ('reduce', 9, "F", 1), "DIVIDE": ('reduce', 9, "F", 1)},
    5: {"NUM": ('shift', 4), "LPAREN": ('shift', 5)},
    6: {"NUM": ('shift', 4), "LPAREN": ('shift', 5)},
    7: {"NUM": ('shift', 4), "LPAREN": ('shift', 5)},
    8: {"NUM": ('shift', 4), "LPAREN": ('shift', 5)},
    9: {"NUM": ('shift', 4), "LPAREN": ('shift', 5)},
    10: {"NUM": ('shift', 4), "LPAREN": ('shift', 5)},
    11: {"RPAREN": ('shift', 17), "PLUS": ('shift', 6), "MINUS": ('shift', 7)},
    12: {"MOD": ('shift', 10), "$": ('reduce', 1, "E", 3), "TIMES": ('shift', 8), "RPAREN": ('reduce', 1, "E", 3), "PLUS": ('reduce', 1, "E", 3), "MINUS": ('reduce', 1, "E", 3), "DIVIDE": ('shift', 9)},
    13: {"MOD": ('shift', 10), "$": ('reduce', 2, "E", 3), "TIMES": ('shift', 8), "RPAREN": ('reduce', 2, "E", 3), "PLUS": ('reduce', 2, "E", 3), "MINUS": ('reduce', 2, "E", 3), "DIVIDE": ('shift', 9)},
    14: {"MOD": ('reduce', 4, "T", 3), "$": ('reduce', 4, "T", 3), "TIMES": ('reduce', 4, "T", 3), "RPAREN": ('reduce', 4, "T", 3), "PLUS": ('reduce', 4, "T", 3), "MINUS": ('reduce', 4, "T", 3), "DIVIDE": ('reduce', 4, "T", 3)},
    15: {"MOD": ('reduce', 5, "T", 3), "$": ('reduce', 5, "T", 3), "TIMES": ('reduce', 5, "T", 3), "RPAREN": ('reduce', 5, "T", 3), "PLUS": ('reduce', 5, "T", 3), "MINUS": ('reduce', 5, "T", 3), "DIVIDE": ('reduce', 5, "T", 3)},
    16: {"MOD": ('reduce', 6, "T", 3), "$": ('reduce', 6, "T", 3), "TIMES": ('reduce', 6, "T", 3), "RPAREN": ('reduce', 6, "T", 3), "PLUS": ('reduce', 6, "T", 3), "MINUS": ('reduce', 6, "T", 3), "DIVIDE": ('reduce', 6, "T", 3)},
    17: {"MOD": ('reduce', 8, "F", 3), "$": ('reduce', 8, "F", 3), "TIMES": ('reduce', 8, "F", 3), "RPAREN": ('reduce', 8, "F", 3), "PLUS": ('reduce', 8, "F", 3), "MINUS": ('reduce', 8, "F", 3), "DIVIDE": ('reduce', 8, "F", 3)},
}

GOTO = {
    0: {"T": 2, "E": 1, "F": 3},
    5: {"T": 2, "E": 11, "F": 3},
    6: {"T": 12, "F": 3},
    7: {"T": 13, "F": 3},
    8: {"F": 14},
    9: {"F": 15},
    10: {"F": 16},
}

# (prod_id, lhs, rhs_len, action_fn)
PRODUCTIONS = [
    (0, "S'", 1, None),
    (1, "E", 3, sem_1),
    (2, "E", 3, sem_2),
    (3, "E", 1, sem_3),
    (4, "T", 3, sem_4),
    (5, "T", 3, sem_5),
    (6, "T", 3, sem_6),
    (7, "T", 1, sem_7),
    (8, "F", 3, sem_8),
    (9, "F", 1, sem_9),
]

def parse(tokens):
    """
    tokens: lista de (tipo, lexema) donde tipo es el nombre del terminal.
    Devuelve el resultado de la acción semántica de la producción raíz,
    o lanza SyntaxError si la entrada no es válida.
    """
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
