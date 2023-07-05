package katana.model.token;

public enum TokenType {
    LEFT_PAREN, // '('
    RIGHT_PAREN, // ')'
    LEFT_BRACE, // '{'
    RIGHT_BRACE, // '}'
    COMMA, // ','
    DOT, // '.'
    MINUS, // '-'
    PLUS, // '+'
    SEMICOLON, // ';'
    SLASH, // '/'
    STAR, // '*'

    BANG, // '!'
    BANG_EQUAL, // '!='
    EQUAL, // '='
    EQUAL_EQUAL, // '=='
    GREATER, // '>'
    GREATER_EQUAL, // '>='
    LESS, // '<'
    LESS_EQUAL, // '<='
    AND, // '&&'
    OR, // '||'

    IDENTIFIER,
    STRING,
    NUMBER,

    // keyword
    IF,
    ELSE,
    WHILE,
    FOR,
    NULL,
    PRINT,
    RETURN,
    THIS,
    SUPER,
    TRUE,
    FALSE,
    VAR,
    CLASS,
    FUN,
    EXTENDS,

    EOF
}
