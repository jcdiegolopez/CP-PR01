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
    # Stmt -> ForStmt
    return "\"stmt_for\""

def sem_9(stack):
    # Stmt -> PrintStmt
    return "\"stmt_print\""

def sem_10(stack):
    # AssignStmt -> ID ASSIGN Expr SEMICOLON
    return "\"asignar\""

def sem_11(stack):
    # IfStmt -> IF LPAREN Expr RPAREN LBRACE StmtList RBRACE
    return "\"si_sin_else\""

def sem_12(stack):
    # IfStmt -> IF LPAREN Expr RPAREN LBRACE StmtList RBRACE ELSE LBRACE StmtList RBRACE
    return "\"si_con_else\""

def sem_13(stack):
    # WhileStmt -> WHILE LPAREN Expr RPAREN LBRACE StmtList RBRACE
    return "\"mientras\""

def sem_14(stack):
    # ReturnStmt -> RETURN Expr SEMICOLON
    return "\"retornar\""

def sem_15(stack):
    # ForStmt -> FOR ID ASSIGN Expr TO Expr LBRACE StmtList RBRACE
    return "\"para\""

def sem_16(stack):
    # PrintStmt -> PRINT LPAREN Expr RPAREN SEMICOLON
    return "\"imprimir\""

def sem_17(stack):
    # Expr -> Expr PLUS Term
    return "\"suma\""

def sem_18(stack):
    # Expr -> Expr MINUS Term
    return "\"resta\""

def sem_19(stack):
    # Expr -> Term
    return "\"pasarExpr\""

def sem_20(stack):
    # Term -> Term TIMES Factor
    return "\"multiplicar\""

def sem_21(stack):
    # Term -> Term DIVIDE Factor
    return "\"dividir\""

def sem_22(stack):
    # Term -> Factor
    return "\"pasarTerm\""

def sem_23(stack):
    # Factor -> LPAREN Expr RPAREN
    return "\"agrupar\""

def sem_24(stack):
    # Factor -> ID
    return "\"identificador\""

def sem_25(stack):
    # Factor -> INT
    return "\"entero\""

def sem_26(stack):
    # Factor -> FLOAT
    return "\"flotante\""

ACTION = {
    0: {"PRINT": ('shift', 15), "RETURN": ('shift', 13), "FOR": ('shift', 14), "WHILE": ('shift', 12), "ID": ('shift', 10), "IF": ('shift', 11)},
    1: {"$": ('accept',)},
    2: {"PRINT": ('shift', 15), "RETURN": ('shift', 13), "$": ('reduce', 1, "Program", 1), "FOR": ('shift', 14), "WHILE": ('shift', 12), "ID": ('shift', 10), "IF": ('shift', 11)},
    3: {"PRINT": ('reduce', 3, "StmtList", 1), "RETURN": ('reduce', 3, "StmtList", 1), "$": ('reduce', 3, "StmtList", 1), "RBRACE": ('reduce', 3, "StmtList", 1), "FOR": ('reduce', 3, "StmtList", 1), "WHILE": ('reduce', 3, "StmtList", 1), "ID": ('reduce', 3, "StmtList", 1), "IF": ('reduce', 3, "StmtList", 1)},
    4: {"PRINT": ('reduce', 4, "Stmt", 1), "RETURN": ('reduce', 4, "Stmt", 1), "$": ('reduce', 4, "Stmt", 1), "RBRACE": ('reduce', 4, "Stmt", 1), "FOR": ('reduce', 4, "Stmt", 1), "WHILE": ('reduce', 4, "Stmt", 1), "ID": ('reduce', 4, "Stmt", 1), "IF": ('reduce', 4, "Stmt", 1)},
    5: {"PRINT": ('reduce', 5, "Stmt", 1), "RETURN": ('reduce', 5, "Stmt", 1), "$": ('reduce', 5, "Stmt", 1), "RBRACE": ('reduce', 5, "Stmt", 1), "FOR": ('reduce', 5, "Stmt", 1), "WHILE": ('reduce', 5, "Stmt", 1), "ID": ('reduce', 5, "Stmt", 1), "IF": ('reduce', 5, "Stmt", 1)},
    6: {"PRINT": ('reduce', 6, "Stmt", 1), "RETURN": ('reduce', 6, "Stmt", 1), "$": ('reduce', 6, "Stmt", 1), "RBRACE": ('reduce', 6, "Stmt", 1), "FOR": ('reduce', 6, "Stmt", 1), "WHILE": ('reduce', 6, "Stmt", 1), "ID": ('reduce', 6, "Stmt", 1), "IF": ('reduce', 6, "Stmt", 1)},
    7: {"PRINT": ('reduce', 7, "Stmt", 1), "RETURN": ('reduce', 7, "Stmt", 1), "$": ('reduce', 7, "Stmt", 1), "RBRACE": ('reduce', 7, "Stmt", 1), "FOR": ('reduce', 7, "Stmt", 1), "WHILE": ('reduce', 7, "Stmt", 1), "ID": ('reduce', 7, "Stmt", 1), "IF": ('reduce', 7, "Stmt", 1)},
    8: {"PRINT": ('reduce', 8, "Stmt", 1), "RETURN": ('reduce', 8, "Stmt", 1), "$": ('reduce', 8, "Stmt", 1), "RBRACE": ('reduce', 8, "Stmt", 1), "FOR": ('reduce', 8, "Stmt", 1), "WHILE": ('reduce', 8, "Stmt", 1), "ID": ('reduce', 8, "Stmt", 1), "IF": ('reduce', 8, "Stmt", 1)},
    9: {"PRINT": ('reduce', 9, "Stmt", 1), "RETURN": ('reduce', 9, "Stmt", 1), "$": ('reduce', 9, "Stmt", 1), "RBRACE": ('reduce', 9, "Stmt", 1), "FOR": ('reduce', 9, "Stmt", 1), "WHILE": ('reduce', 9, "Stmt", 1), "ID": ('reduce', 9, "Stmt", 1), "IF": ('reduce', 9, "Stmt", 1)},
    10: {"ASSIGN": ('shift', 17)},
    11: {"LPAREN": ('shift', 18)},
    12: {"LPAREN": ('shift', 19)},
    13: {"FLOAT": ('shift', 25), "LPAREN": ('shift', 26), "ID": ('shift', 23), "INT": ('shift', 24)},
    14: {"ID": ('shift', 27)},
    15: {"LPAREN": ('shift', 28)},
    16: {"PRINT": ('reduce', 2, "StmtList", 2), "RETURN": ('reduce', 2, "StmtList", 2), "$": ('reduce', 2, "StmtList", 2), "RBRACE": ('reduce', 2, "StmtList", 2), "FOR": ('reduce', 2, "StmtList", 2), "WHILE": ('reduce', 2, "StmtList", 2), "ID": ('reduce', 2, "StmtList", 2), "IF": ('reduce', 2, "StmtList", 2)},
    17: {"FLOAT": ('shift', 25), "LPAREN": ('shift', 26), "ID": ('shift', 23), "INT": ('shift', 24)},
    18: {"FLOAT": ('shift', 25), "LPAREN": ('shift', 26), "ID": ('shift', 23), "INT": ('shift', 24)},
    19: {"FLOAT": ('shift', 25), "LPAREN": ('shift', 26), "ID": ('shift', 23), "INT": ('shift', 24)},
    20: {"SEMICOLON": ('shift', 34), "PLUS": ('shift', 32), "MINUS": ('shift', 33)},
    21: {"TIMES": ('shift', 35), "SEMICOLON": ('reduce', 19, "Expr", 1), "RPAREN": ('reduce', 19, "Expr", 1), "TO": ('reduce', 19, "Expr", 1), "LBRACE": ('reduce', 19, "Expr", 1), "PLUS": ('reduce', 19, "Expr", 1), "MINUS": ('reduce', 19, "Expr", 1), "DIVIDE": ('shift', 36)},
    22: {"TIMES": ('reduce', 22, "Term", 1), "SEMICOLON": ('reduce', 22, "Term", 1), "RPAREN": ('reduce', 22, "Term", 1), "TO": ('reduce', 22, "Term", 1), "LBRACE": ('reduce', 22, "Term", 1), "PLUS": ('reduce', 22, "Term", 1), "MINUS": ('reduce', 22, "Term", 1), "DIVIDE": ('reduce', 22, "Term", 1)},
    23: {"TIMES": ('reduce', 24, "Factor", 1), "SEMICOLON": ('reduce', 24, "Factor", 1), "RPAREN": ('reduce', 24, "Factor", 1), "TO": ('reduce', 24, "Factor", 1), "LBRACE": ('reduce', 24, "Factor", 1), "PLUS": ('reduce', 24, "Factor", 1), "MINUS": ('reduce', 24, "Factor", 1), "DIVIDE": ('reduce', 24, "Factor", 1)},
    24: {"TIMES": ('reduce', 25, "Factor", 1), "SEMICOLON": ('reduce', 25, "Factor", 1), "RPAREN": ('reduce', 25, "Factor", 1), "TO": ('reduce', 25, "Factor", 1), "LBRACE": ('reduce', 25, "Factor", 1), "PLUS": ('reduce', 25, "Factor", 1), "MINUS": ('reduce', 25, "Factor", 1), "DIVIDE": ('reduce', 25, "Factor", 1)},
    25: {"TIMES": ('reduce', 26, "Factor", 1), "SEMICOLON": ('reduce', 26, "Factor", 1), "RPAREN": ('reduce', 26, "Factor", 1), "TO": ('reduce', 26, "Factor", 1), "LBRACE": ('reduce', 26, "Factor", 1), "PLUS": ('reduce', 26, "Factor", 1), "MINUS": ('reduce', 26, "Factor", 1), "DIVIDE": ('reduce', 26, "Factor", 1)},
    26: {"FLOAT": ('shift', 25), "LPAREN": ('shift', 26), "ID": ('shift', 23), "INT": ('shift', 24)},
    27: {"ASSIGN": ('shift', 38)},
    28: {"FLOAT": ('shift', 25), "LPAREN": ('shift', 26), "ID": ('shift', 23), "INT": ('shift', 24)},
    29: {"SEMICOLON": ('shift', 40), "PLUS": ('shift', 32), "MINUS": ('shift', 33)},
    30: {"RPAREN": ('shift', 41), "PLUS": ('shift', 32), "MINUS": ('shift', 33)},
    31: {"RPAREN": ('shift', 42), "PLUS": ('shift', 32), "MINUS": ('shift', 33)},
    32: {"FLOAT": ('shift', 25), "LPAREN": ('shift', 26), "ID": ('shift', 23), "INT": ('shift', 24)},
    33: {"FLOAT": ('shift', 25), "LPAREN": ('shift', 26), "ID": ('shift', 23), "INT": ('shift', 24)},
    34: {"PRINT": ('reduce', 14, "ReturnStmt", 3), "RETURN": ('reduce', 14, "ReturnStmt", 3), "$": ('reduce', 14, "ReturnStmt", 3), "RBRACE": ('reduce', 14, "ReturnStmt", 3), "FOR": ('reduce', 14, "ReturnStmt", 3), "WHILE": ('reduce', 14, "ReturnStmt", 3), "ID": ('reduce', 14, "ReturnStmt", 3), "IF": ('reduce', 14, "ReturnStmt", 3)},
    35: {"FLOAT": ('shift', 25), "LPAREN": ('shift', 26), "ID": ('shift', 23), "INT": ('shift', 24)},
    36: {"FLOAT": ('shift', 25), "LPAREN": ('shift', 26), "ID": ('shift', 23), "INT": ('shift', 24)},
    37: {"RPAREN": ('shift', 47), "PLUS": ('shift', 32), "MINUS": ('shift', 33)},
    38: {"FLOAT": ('shift', 25), "LPAREN": ('shift', 26), "ID": ('shift', 23), "INT": ('shift', 24)},
    39: {"RPAREN": ('shift', 49), "PLUS": ('shift', 32), "MINUS": ('shift', 33)},
    40: {"PRINT": ('reduce', 10, "AssignStmt", 4), "RETURN": ('reduce', 10, "AssignStmt", 4), "$": ('reduce', 10, "AssignStmt", 4), "RBRACE": ('reduce', 10, "AssignStmt", 4), "FOR": ('reduce', 10, "AssignStmt", 4), "WHILE": ('reduce', 10, "AssignStmt", 4), "ID": ('reduce', 10, "AssignStmt", 4), "IF": ('reduce', 10, "AssignStmt", 4)},
    41: {"LBRACE": ('shift', 50)},
    42: {"LBRACE": ('shift', 51)},
    43: {"TIMES": ('shift', 35), "SEMICOLON": ('reduce', 17, "Expr", 3), "RPAREN": ('reduce', 17, "Expr", 3), "TO": ('reduce', 17, "Expr", 3), "LBRACE": ('reduce', 17, "Expr", 3), "PLUS": ('reduce', 17, "Expr", 3), "MINUS": ('reduce', 17, "Expr", 3), "DIVIDE": ('shift', 36)},
    44: {"TIMES": ('shift', 35), "SEMICOLON": ('reduce', 18, "Expr", 3), "RPAREN": ('reduce', 18, "Expr", 3), "TO": ('reduce', 18, "Expr", 3), "LBRACE": ('reduce', 18, "Expr", 3), "PLUS": ('reduce', 18, "Expr", 3), "MINUS": ('reduce', 18, "Expr", 3), "DIVIDE": ('shift', 36)},
    45: {"TIMES": ('reduce', 20, "Term", 3), "SEMICOLON": ('reduce', 20, "Term", 3), "RPAREN": ('reduce', 20, "Term", 3), "TO": ('reduce', 20, "Term", 3), "LBRACE": ('reduce', 20, "Term", 3), "PLUS": ('reduce', 20, "Term", 3), "MINUS": ('reduce', 20, "Term", 3), "DIVIDE": ('reduce', 20, "Term", 3)},
    46: {"TIMES": ('reduce', 21, "Term", 3), "SEMICOLON": ('reduce', 21, "Term", 3), "RPAREN": ('reduce', 21, "Term", 3), "TO": ('reduce', 21, "Term", 3), "LBRACE": ('reduce', 21, "Term", 3), "PLUS": ('reduce', 21, "Term", 3), "MINUS": ('reduce', 21, "Term", 3), "DIVIDE": ('reduce', 21, "Term", 3)},
    47: {"TIMES": ('reduce', 23, "Factor", 3), "SEMICOLON": ('reduce', 23, "Factor", 3), "RPAREN": ('reduce', 23, "Factor", 3), "TO": ('reduce', 23, "Factor", 3), "LBRACE": ('reduce', 23, "Factor", 3), "PLUS": ('reduce', 23, "Factor", 3), "MINUS": ('reduce', 23, "Factor", 3), "DIVIDE": ('reduce', 23, "Factor", 3)},
    48: {"TO": ('shift', 52), "PLUS": ('shift', 32), "MINUS": ('shift', 33)},
    49: {"SEMICOLON": ('shift', 53)},
    50: {"PRINT": ('shift', 15), "RETURN": ('shift', 13), "FOR": ('shift', 14), "WHILE": ('shift', 12), "ID": ('shift', 10), "IF": ('shift', 11)},
    51: {"PRINT": ('shift', 15), "RETURN": ('shift', 13), "FOR": ('shift', 14), "WHILE": ('shift', 12), "ID": ('shift', 10), "IF": ('shift', 11)},
    52: {"FLOAT": ('shift', 25), "LPAREN": ('shift', 26), "ID": ('shift', 23), "INT": ('shift', 24)},
    53: {"PRINT": ('reduce', 16, "PrintStmt", 5), "RETURN": ('reduce', 16, "PrintStmt", 5), "$": ('reduce', 16, "PrintStmt", 5), "RBRACE": ('reduce', 16, "PrintStmt", 5), "FOR": ('reduce', 16, "PrintStmt", 5), "WHILE": ('reduce', 16, "PrintStmt", 5), "ID": ('reduce', 16, "PrintStmt", 5), "IF": ('reduce', 16, "PrintStmt", 5)},
    54: {"PRINT": ('shift', 15), "RETURN": ('shift', 13), "RBRACE": ('shift', 57), "FOR": ('shift', 14), "WHILE": ('shift', 12), "ID": ('shift', 10), "IF": ('shift', 11)},
    55: {"PRINT": ('shift', 15), "RETURN": ('shift', 13), "RBRACE": ('shift', 58), "FOR": ('shift', 14), "WHILE": ('shift', 12), "ID": ('shift', 10), "IF": ('shift', 11)},
    56: {"LBRACE": ('shift', 59), "PLUS": ('shift', 32), "MINUS": ('shift', 33)},
    57: {"PRINT": ('reduce', 11, "IfStmt", 7), "RETURN": ('reduce', 11, "IfStmt", 7), "$": ('reduce', 11, "IfStmt", 7), "RBRACE": ('reduce', 11, "IfStmt", 7), "FOR": ('reduce', 11, "IfStmt", 7), "ELSE": ('shift', 60), "WHILE": ('reduce', 11, "IfStmt", 7), "ID": ('reduce', 11, "IfStmt", 7), "IF": ('reduce', 11, "IfStmt", 7)},
    58: {"PRINT": ('reduce', 13, "WhileStmt", 7), "RETURN": ('reduce', 13, "WhileStmt", 7), "$": ('reduce', 13, "WhileStmt", 7), "RBRACE": ('reduce', 13, "WhileStmt", 7), "FOR": ('reduce', 13, "WhileStmt", 7), "WHILE": ('reduce', 13, "WhileStmt", 7), "ID": ('reduce', 13, "WhileStmt", 7), "IF": ('reduce', 13, "WhileStmt", 7)},
    59: {"PRINT": ('shift', 15), "RETURN": ('shift', 13), "FOR": ('shift', 14), "WHILE": ('shift', 12), "ID": ('shift', 10), "IF": ('shift', 11)},
    60: {"LBRACE": ('shift', 62)},
    61: {"PRINT": ('shift', 15), "RETURN": ('shift', 13), "RBRACE": ('shift', 63), "FOR": ('shift', 14), "WHILE": ('shift', 12), "ID": ('shift', 10), "IF": ('shift', 11)},
    62: {"PRINT": ('shift', 15), "RETURN": ('shift', 13), "FOR": ('shift', 14), "WHILE": ('shift', 12), "ID": ('shift', 10), "IF": ('shift', 11)},
    63: {"PRINT": ('reduce', 15, "ForStmt", 9), "RETURN": ('reduce', 15, "ForStmt", 9), "$": ('reduce', 15, "ForStmt", 9), "RBRACE": ('reduce', 15, "ForStmt", 9), "FOR": ('reduce', 15, "ForStmt", 9), "WHILE": ('reduce', 15, "ForStmt", 9), "ID": ('reduce', 15, "ForStmt", 9), "IF": ('reduce', 15, "ForStmt", 9)},
    64: {"PRINT": ('shift', 15), "RETURN": ('shift', 13), "RBRACE": ('shift', 65), "FOR": ('shift', 14), "WHILE": ('shift', 12), "ID": ('shift', 10), "IF": ('shift', 11)},
    65: {"PRINT": ('reduce', 12, "IfStmt", 11), "RETURN": ('reduce', 12, "IfStmt", 11), "$": ('reduce', 12, "IfStmt", 11), "RBRACE": ('reduce', 12, "IfStmt", 11), "FOR": ('reduce', 12, "IfStmt", 11), "WHILE": ('reduce', 12, "IfStmt", 11), "ID": ('reduce', 12, "IfStmt", 11), "IF": ('reduce', 12, "IfStmt", 11)},
}

GOTO = {
    64: {"PrintStmt": 9, "IfStmt": 5, "WhileStmt": 6, "AssignStmt": 4, "ReturnStmt": 7, "ForStmt": 8, "Stmt": 16},
    32: {"Factor": 22, "Term": 43},
    0: {"Program": 1, "PrintStmt": 9, "IfStmt": 5, "WhileStmt": 6, "AssignStmt": 4, "StmtList": 2, "ReturnStmt": 7, "ForStmt": 8, "Stmt": 3},
    33: {"Factor": 22, "Term": 44},
    2: {"PrintStmt": 9, "IfStmt": 5, "WhileStmt": 6, "AssignStmt": 4, "ReturnStmt": 7, "ForStmt": 8, "Stmt": 16},
    35: {"Factor": 45},
    36: {"Factor": 46},
    38: {"Expr": 48, "Factor": 22, "Term": 21},
    13: {"Expr": 20, "Factor": 22, "Term": 21},
    17: {"Expr": 29, "Factor": 22, "Term": 21},
    50: {"PrintStmt": 9, "IfStmt": 5, "WhileStmt": 6, "AssignStmt": 4, "StmtList": 54, "ReturnStmt": 7, "ForStmt": 8, "Stmt": 3},
    18: {"Expr": 30, "Factor": 22, "Term": 21},
    51: {"PrintStmt": 9, "IfStmt": 5, "WhileStmt": 6, "AssignStmt": 4, "StmtList": 55, "ReturnStmt": 7, "ForStmt": 8, "Stmt": 3},
    19: {"Expr": 31, "Factor": 22, "Term": 21},
    52: {"Expr": 56, "Factor": 22, "Term": 21},
    54: {"PrintStmt": 9, "IfStmt": 5, "WhileStmt": 6, "AssignStmt": 4, "ReturnStmt": 7, "ForStmt": 8, "Stmt": 16},
    55: {"PrintStmt": 9, "IfStmt": 5, "WhileStmt": 6, "AssignStmt": 4, "ReturnStmt": 7, "ForStmt": 8, "Stmt": 16},
    26: {"Expr": 37, "Factor": 22, "Term": 21},
    59: {"PrintStmt": 9, "IfStmt": 5, "WhileStmt": 6, "AssignStmt": 4, "StmtList": 61, "ReturnStmt": 7, "ForStmt": 8, "Stmt": 3},
    28: {"Expr": 39, "Factor": 22, "Term": 21},
    61: {"PrintStmt": 9, "IfStmt": 5, "WhileStmt": 6, "AssignStmt": 4, "ReturnStmt": 7, "ForStmt": 8, "Stmt": 16},
    62: {"PrintStmt": 9, "IfStmt": 5, "WhileStmt": 6, "AssignStmt": 4, "StmtList": 64, "ReturnStmt": 7, "ForStmt": 8, "Stmt": 3},
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
    (8, "Stmt", 1, sem_8),
    (9, "Stmt", 1, sem_9),
    (10, "AssignStmt", 4, sem_10),
    (11, "IfStmt", 7, sem_11),
    (12, "IfStmt", 11, sem_12),
    (13, "WhileStmt", 7, sem_13),
    (14, "ReturnStmt", 3, sem_14),
    (15, "ForStmt", 9, sem_15),
    (16, "PrintStmt", 5, sem_16),
    (17, "Expr", 3, sem_17),
    (18, "Expr", 3, sem_18),
    (19, "Expr", 1, sem_19),
    (20, "Term", 3, sem_20),
    (21, "Term", 3, sem_21),
    (22, "Term", 1, sem_22),
    (23, "Factor", 3, sem_23),
    (24, "Factor", 1, sem_24),
    (25, "Factor", 1, sem_25),
    (26, "Factor", 1, sem_26),
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
