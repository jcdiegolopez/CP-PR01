# theparser.py — generado automáticamente por YAPar
# No editar manualmente.

# Acciones semánticas
def sem_1(stack):
    # Program -> StmtList
    return "\"programa\""

def sem_2(stack):
    # StmtList -> StmtList Stmt
    return "\"lista_stmts\""

def sem_3(stack):
    # StmtList -> Stmt
    return "\"stmt_unico\""

def sem_4(stack):
    # Stmt -> AssignStmt
    return "\"stmt_asignacion\""

def sem_5(stack):
    # Stmt -> IfStmt
    return "\"stmt_if\""

def sem_6(stack):
    # Stmt -> WhileStmt
    return "\"stmt_while\""

def sem_7(stack):
    # Stmt -> ReturnStmt
    return "\"stmt_return\""

def sem_8(stack):
    # AssignStmt -> ID ASSIGN Expr SEMICOLON
    return "\"asignar\""

def sem_9(stack):
    # IfStmt -> IF LPAREN Expr RPAREN LBRACE StmtList RBRACE
    return "\"si_sin_else\""

def sem_10(stack):
    # IfStmt -> IF LPAREN Expr RPAREN LBRACE StmtList RBRACE ELSE LBRACE StmtList RBRACE
    return "\"si_con_else\""

def sem_11(stack):
    # WhileStmt -> WHILE LPAREN Expr RPAREN LBRACE StmtList RBRACE
    return "\"mientras\""

def sem_12(stack):
    # ReturnStmt -> RETURN Expr SEMICOLON
    return "\"retornar\""

def sem_13(stack):
    # Expr -> Expr PLUS Term
    return "\"suma\""

def sem_14(stack):
    # Expr -> Expr MINUS Term
    return "\"resta\""

def sem_15(stack):
    # Expr -> Term
    return "\"pasarExpr\""

def sem_16(stack):
    # Term -> Term TIMES Factor
    return "\"multiplicar\""

def sem_17(stack):
    # Term -> Term DIVIDE Factor
    return "\"dividir\""

def sem_18(stack):
    # Term -> Factor
    return "\"pasarTerm\""

def sem_19(stack):
    # Factor -> LPAREN Expr RPAREN
    return "\"agrupar\""

def sem_20(stack):
    # Factor -> ID
    return "\"identificador\""

def sem_21(stack):
    # Factor -> INT
    return "\"entero\""

def sem_22(stack):
    # Factor -> FLOAT
    return "\"flotante\""

ACTION = {
    0: {"RETURN": ('shift', 11), "WHILE": ('shift', 10), "ID": ('shift', 8), "IF": ('shift', 9)},
    1: {"$": ('accept',)},
    2: {"RETURN": ('shift', 11), "$": ('reduce', 1, "Program", 1), "WHILE": ('shift', 10), "ID": ('shift', 8), "IF": ('shift', 9)},
    3: {"RETURN": ('reduce', 3, "StmtList", 1), "$": ('reduce', 3, "StmtList", 1), "RBRACE": ('reduce', 3, "StmtList", 1), "WHILE": ('reduce', 3, "StmtList", 1), "ID": ('reduce', 3, "StmtList", 1), "IF": ('reduce', 3, "StmtList", 1)},
    4: {"RETURN": ('reduce', 4, "Stmt", 1), "$": ('reduce', 4, "Stmt", 1), "RBRACE": ('reduce', 4, "Stmt", 1), "WHILE": ('reduce', 4, "Stmt", 1), "ID": ('reduce', 4, "Stmt", 1), "IF": ('reduce', 4, "Stmt", 1)},
    5: {"RETURN": ('reduce', 5, "Stmt", 1), "$": ('reduce', 5, "Stmt", 1), "RBRACE": ('reduce', 5, "Stmt", 1), "WHILE": ('reduce', 5, "Stmt", 1), "ID": ('reduce', 5, "Stmt", 1), "IF": ('reduce', 5, "Stmt", 1)},
    6: {"RETURN": ('reduce', 6, "Stmt", 1), "$": ('reduce', 6, "Stmt", 1), "RBRACE": ('reduce', 6, "Stmt", 1), "WHILE": ('reduce', 6, "Stmt", 1), "ID": ('reduce', 6, "Stmt", 1), "IF": ('reduce', 6, "Stmt", 1)},
    7: {"RETURN": ('reduce', 7, "Stmt", 1), "$": ('reduce', 7, "Stmt", 1), "RBRACE": ('reduce', 7, "Stmt", 1), "WHILE": ('reduce', 7, "Stmt", 1), "ID": ('reduce', 7, "Stmt", 1), "IF": ('reduce', 7, "Stmt", 1)},
    8: {"ASSIGN": ('shift', 13)},
    9: {"LPAREN": ('shift', 14)},
    10: {"LPAREN": ('shift', 15)},
    11: {"FLOAT": ('shift', 21), "LPAREN": ('shift', 22), "ID": ('shift', 19), "INT": ('shift', 20)},
    12: {"RETURN": ('reduce', 2, "StmtList", 2), "$": ('reduce', 2, "StmtList", 2), "RBRACE": ('reduce', 2, "StmtList", 2), "WHILE": ('reduce', 2, "StmtList", 2), "ID": ('reduce', 2, "StmtList", 2), "IF": ('reduce', 2, "StmtList", 2)},
    13: {"FLOAT": ('shift', 21), "LPAREN": ('shift', 22), "ID": ('shift', 19), "INT": ('shift', 20)},
    14: {"FLOAT": ('shift', 21), "LPAREN": ('shift', 22), "ID": ('shift', 19), "INT": ('shift', 20)},
    15: {"FLOAT": ('shift', 21), "LPAREN": ('shift', 22), "ID": ('shift', 19), "INT": ('shift', 20)},
    16: {"SEMICOLON": ('shift', 28), "PLUS": ('shift', 26), "MINUS": ('shift', 27)},
    17: {"TIMES": ('shift', 29), "SEMICOLON": ('reduce', 15, "Expr", 1), "RPAREN": ('reduce', 15, "Expr", 1), "PLUS": ('reduce', 15, "Expr", 1), "MINUS": ('reduce', 15, "Expr", 1), "DIVIDE": ('shift', 30)},
    18: {"TIMES": ('reduce', 18, "Term", 1), "SEMICOLON": ('reduce', 18, "Term", 1), "RPAREN": ('reduce', 18, "Term", 1), "PLUS": ('reduce', 18, "Term", 1), "MINUS": ('reduce', 18, "Term", 1), "DIVIDE": ('reduce', 18, "Term", 1)},
    19: {"TIMES": ('reduce', 20, "Factor", 1), "SEMICOLON": ('reduce', 20, "Factor", 1), "RPAREN": ('reduce', 20, "Factor", 1), "PLUS": ('reduce', 20, "Factor", 1), "MINUS": ('reduce', 20, "Factor", 1), "DIVIDE": ('reduce', 20, "Factor", 1)},
    20: {"TIMES": ('reduce', 21, "Factor", 1), "SEMICOLON": ('reduce', 21, "Factor", 1), "RPAREN": ('reduce', 21, "Factor", 1), "PLUS": ('reduce', 21, "Factor", 1), "MINUS": ('reduce', 21, "Factor", 1), "DIVIDE": ('reduce', 21, "Factor", 1)},
    21: {"TIMES": ('reduce', 22, "Factor", 1), "SEMICOLON": ('reduce', 22, "Factor", 1), "RPAREN": ('reduce', 22, "Factor", 1), "PLUS": ('reduce', 22, "Factor", 1), "MINUS": ('reduce', 22, "Factor", 1), "DIVIDE": ('reduce', 22, "Factor", 1)},
    22: {"FLOAT": ('shift', 21), "LPAREN": ('shift', 22), "ID": ('shift', 19), "INT": ('shift', 20)},
    23: {"SEMICOLON": ('shift', 32), "PLUS": ('shift', 26), "MINUS": ('shift', 27)},
    24: {"RPAREN": ('shift', 33), "PLUS": ('shift', 26), "MINUS": ('shift', 27)},
    25: {"RPAREN": ('shift', 34), "PLUS": ('shift', 26), "MINUS": ('shift', 27)},
    26: {"FLOAT": ('shift', 21), "LPAREN": ('shift', 22), "ID": ('shift', 19), "INT": ('shift', 20)},
    27: {"FLOAT": ('shift', 21), "LPAREN": ('shift', 22), "ID": ('shift', 19), "INT": ('shift', 20)},
    28: {"RETURN": ('reduce', 12, "ReturnStmt", 3), "$": ('reduce', 12, "ReturnStmt", 3), "RBRACE": ('reduce', 12, "ReturnStmt", 3), "WHILE": ('reduce', 12, "ReturnStmt", 3), "ID": ('reduce', 12, "ReturnStmt", 3), "IF": ('reduce', 12, "ReturnStmt", 3)},
    29: {"FLOAT": ('shift', 21), "LPAREN": ('shift', 22), "ID": ('shift', 19), "INT": ('shift', 20)},
    30: {"FLOAT": ('shift', 21), "LPAREN": ('shift', 22), "ID": ('shift', 19), "INT": ('shift', 20)},
    31: {"RPAREN": ('shift', 39), "PLUS": ('shift', 26), "MINUS": ('shift', 27)},
    32: {"RETURN": ('reduce', 8, "AssignStmt", 4), "$": ('reduce', 8, "AssignStmt", 4), "RBRACE": ('reduce', 8, "AssignStmt", 4), "WHILE": ('reduce', 8, "AssignStmt", 4), "ID": ('reduce', 8, "AssignStmt", 4), "IF": ('reduce', 8, "AssignStmt", 4)},
    33: {"LBRACE": ('shift', 40)},
    34: {"LBRACE": ('shift', 41)},
    35: {"TIMES": ('shift', 29), "SEMICOLON": ('reduce', 13, "Expr", 3), "RPAREN": ('reduce', 13, "Expr", 3), "PLUS": ('reduce', 13, "Expr", 3), "MINUS": ('reduce', 13, "Expr", 3), "DIVIDE": ('shift', 30)},
    36: {"TIMES": ('shift', 29), "SEMICOLON": ('reduce', 14, "Expr", 3), "RPAREN": ('reduce', 14, "Expr", 3), "PLUS": ('reduce', 14, "Expr", 3), "MINUS": ('reduce', 14, "Expr", 3), "DIVIDE": ('shift', 30)},
    37: {"TIMES": ('reduce', 16, "Term", 3), "SEMICOLON": ('reduce', 16, "Term", 3), "RPAREN": ('reduce', 16, "Term", 3), "PLUS": ('reduce', 16, "Term", 3), "MINUS": ('reduce', 16, "Term", 3), "DIVIDE": ('reduce', 16, "Term", 3)},
    38: {"TIMES": ('reduce', 17, "Term", 3), "SEMICOLON": ('reduce', 17, "Term", 3), "RPAREN": ('reduce', 17, "Term", 3), "PLUS": ('reduce', 17, "Term", 3), "MINUS": ('reduce', 17, "Term", 3), "DIVIDE": ('reduce', 17, "Term", 3)},
    39: {"TIMES": ('reduce', 19, "Factor", 3), "SEMICOLON": ('reduce', 19, "Factor", 3), "RPAREN": ('reduce', 19, "Factor", 3), "PLUS": ('reduce', 19, "Factor", 3), "MINUS": ('reduce', 19, "Factor", 3), "DIVIDE": ('reduce', 19, "Factor", 3)},
    40: {"RETURN": ('shift', 11), "WHILE": ('shift', 10), "ID": ('shift', 8), "IF": ('shift', 9)},
    41: {"RETURN": ('shift', 11), "WHILE": ('shift', 10), "ID": ('shift', 8), "IF": ('shift', 9)},
    42: {"RETURN": ('shift', 11), "RBRACE": ('shift', 44), "WHILE": ('shift', 10), "ID": ('shift', 8), "IF": ('shift', 9)},
    43: {"RETURN": ('shift', 11), "RBRACE": ('shift', 45), "WHILE": ('shift', 10), "ID": ('shift', 8), "IF": ('shift', 9)},
    44: {"RETURN": ('reduce', 9, "IfStmt", 7), "$": ('reduce', 9, "IfStmt", 7), "RBRACE": ('reduce', 9, "IfStmt", 7), "ELSE": ('shift', 46), "WHILE": ('reduce', 9, "IfStmt", 7), "ID": ('reduce', 9, "IfStmt", 7), "IF": ('reduce', 9, "IfStmt", 7)},
    45: {"RETURN": ('reduce', 11, "WhileStmt", 7), "$": ('reduce', 11, "WhileStmt", 7), "RBRACE": ('reduce', 11, "WhileStmt", 7), "WHILE": ('reduce', 11, "WhileStmt", 7), "ID": ('reduce', 11, "WhileStmt", 7), "IF": ('reduce', 11, "WhileStmt", 7)},
    46: {"LBRACE": ('shift', 47)},
    47: {"RETURN": ('shift', 11), "WHILE": ('shift', 10), "ID": ('shift', 8), "IF": ('shift', 9)},
    48: {"RETURN": ('shift', 11), "RBRACE": ('shift', 49), "WHILE": ('shift', 10), "ID": ('shift', 8), "IF": ('shift', 9)},
    49: {"RETURN": ('reduce', 10, "IfStmt", 11), "$": ('reduce', 10, "IfStmt", 11), "RBRACE": ('reduce', 10, "IfStmt", 11), "WHILE": ('reduce', 10, "IfStmt", 11), "ID": ('reduce', 10, "IfStmt", 11), "IF": ('reduce', 10, "IfStmt", 11)},
}

GOTO = {
    0: {"Program": 1, "IfStmt": 5, "WhileStmt": 6, "AssignStmt": 4, "StmtList": 2, "ReturnStmt": 7, "Stmt": 3},
    2: {"IfStmt": 5, "WhileStmt": 6, "AssignStmt": 4, "ReturnStmt": 7, "Stmt": 12},
    40: {"IfStmt": 5, "WhileStmt": 6, "AssignStmt": 4, "StmtList": 42, "ReturnStmt": 7, "Stmt": 3},
    41: {"IfStmt": 5, "WhileStmt": 6, "AssignStmt": 4, "StmtList": 43, "ReturnStmt": 7, "Stmt": 3},
    42: {"IfStmt": 5, "WhileStmt": 6, "AssignStmt": 4, "ReturnStmt": 7, "Stmt": 12},
    43: {"IfStmt": 5, "WhileStmt": 6, "AssignStmt": 4, "ReturnStmt": 7, "Stmt": 12},
    11: {"Expr": 16, "Factor": 18, "Term": 17},
    13: {"Expr": 23, "Factor": 18, "Term": 17},
    14: {"Expr": 24, "Factor": 18, "Term": 17},
    47: {"IfStmt": 5, "WhileStmt": 6, "AssignStmt": 4, "StmtList": 48, "ReturnStmt": 7, "Stmt": 3},
    15: {"Expr": 25, "Factor": 18, "Term": 17},
    48: {"IfStmt": 5, "WhileStmt": 6, "AssignStmt": 4, "ReturnStmt": 7, "Stmt": 12},
    22: {"Expr": 31, "Factor": 18, "Term": 17},
    26: {"Factor": 18, "Term": 35},
    27: {"Factor": 18, "Term": 36},
    29: {"Factor": 37},
    30: {"Factor": 38},
}

# (prod_id, lhs, rhs_len, action_fn)
PRODUCTIONS = [
    (0, "S'", 1, None),
    (1, "Program", 1, sem_1),
    (2, "StmtList", 2, sem_2),
    (3, "StmtList", 1, sem_3),
    (4, "Stmt", 1, sem_4),
    (5, "Stmt", 1, sem_5),
    (6, "Stmt", 1, sem_6),
    (7, "Stmt", 1, sem_7),
    (8, "AssignStmt", 4, sem_8),
    (9, "IfStmt", 7, sem_9),
    (10, "IfStmt", 11, sem_10),
    (11, "WhileStmt", 7, sem_11),
    (12, "ReturnStmt", 3, sem_12),
    (13, "Expr", 3, sem_13),
    (14, "Expr", 3, sem_14),
    (15, "Expr", 1, sem_15),
    (16, "Term", 3, sem_16),
    (17, "Term", 3, sem_17),
    (18, "Term", 1, sem_18),
    (19, "Factor", 3, sem_19),
    (20, "Factor", 1, sem_20),
    (21, "Factor", 1, sem_21),
    (22, "Factor", 1, sem_22),
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
