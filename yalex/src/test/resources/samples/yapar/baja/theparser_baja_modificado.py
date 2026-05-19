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
    # T -> F
    return "\"pasarT\""

def sem_6(stack):
    # F -> NUM
    return "\"numero\""

ACTION = {
    0: {"NUM": ('shift', 4)},
    1: {"$": ('accept',), "PLUS": ('shift', 5), "MINUS": ('shift', 6)},
    2: {"$": ('reduce', 3, "E", 1), "TIMES": ('shift', 7), "PLUS": ('reduce', 3, "E", 1), "MINUS": ('reduce', 3, "E", 1)},
    3: {"$": ('reduce', 5, "T", 1), "TIMES": ('reduce', 5, "T", 1), "PLUS": ('reduce', 5, "T", 1), "MINUS": ('reduce', 5, "T", 1)},
    4: {"$": ('reduce', 6, "F", 1), "TIMES": ('reduce', 6, "F", 1), "PLUS": ('reduce', 6, "F", 1), "MINUS": ('reduce', 6, "F", 1)},
    5: {"NUM": ('shift', 4)},
    6: {"NUM": ('shift', 4)},
    7: {"NUM": ('shift', 4)},
    8: {"$": ('reduce', 1, "E", 3), "TIMES": ('shift', 7), "PLUS": ('reduce', 1, "E", 3), "MINUS": ('reduce', 1, "E", 3)},
    9: {"$": ('reduce', 2, "E", 3), "TIMES": ('shift', 7), "PLUS": ('reduce', 2, "E", 3), "MINUS": ('reduce', 2, "E", 3)},
    10: {"$": ('reduce', 4, "T", 3), "TIMES": ('reduce', 4, "T", 3), "PLUS": ('reduce', 4, "T", 3), "MINUS": ('reduce', 4, "T", 3)},
}

GOTO = {
    0: {"T": 2, "E": 1, "F": 3},
    5: {"T": 8, "F": 3},
    6: {"T": 9, "F": 3},
    7: {"F": 10},
}

# (prod_id, lhs, rhs_len, action_fn)
PRODUCTIONS = [
    (0, "S'", 1, None),
    (1, "E", 3, sem_1),
    (2, "E", 3, sem_2),
    (3, "E", 1, sem_3),
    (4, "T", 3, sem_4),
    (5, "T", 1, sem_5),
    (6, "F", 1, sem_6),
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
