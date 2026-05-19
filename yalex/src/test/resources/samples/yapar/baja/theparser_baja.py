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
    return "\"pasar\""

def sem_4(stack):
    # T -> NUM
    return "\"numero\""

ACTION = {
    0: {"NUM": ('shift', 3)},
    1: {"$": ('accept',), "PLUS": ('shift', 4), "MINUS": ('shift', 5)},
    2: {"$": ('reduce', 3, "E", 1), "PLUS": ('reduce', 3, "E", 1), "MINUS": ('reduce', 3, "E", 1)},
    3: {"$": ('reduce', 4, "T", 1), "PLUS": ('reduce', 4, "T", 1), "MINUS": ('reduce', 4, "T", 1)},
    4: {"NUM": ('shift', 3)},
    5: {"NUM": ('shift', 3)},
    6: {"$": ('reduce', 1, "E", 3), "PLUS": ('reduce', 1, "E", 3), "MINUS": ('reduce', 1, "E", 3)},
    7: {"$": ('reduce', 2, "E", 3), "PLUS": ('reduce', 2, "E", 3), "MINUS": ('reduce', 2, "E", 3)},
}

GOTO = {
    0: {"T": 2, "E": 1},
    4: {"T": 6},
    5: {"T": 7},
}

# (prod_id, lhs, rhs_len, action_fn)
PRODUCTIONS = [
    (0, "S'", 1, None),
    (1, "E", 3, sem_1),
    (2, "E", 3, sem_2),
    (3, "E", 1, sem_3),
    (4, "T", 1, sem_4),
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
