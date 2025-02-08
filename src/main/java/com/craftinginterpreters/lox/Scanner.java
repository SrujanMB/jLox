package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.TokenType.*;

class Scanner {
  private final String source;
  private final List<Token> tokens = new ArrayList<>();

  private int start = 0; // Start of the lexeme being considered
  private int current = 0; // Current character of the lexeme being considered
  private int line = 1;

  Scanner(String scanner) {
    this.source = source;
  }

  List<Token> scanTokens() {
    while (!isAtEnd()) {
      // We are at the beginning of the next lexeme.
      start = current;
      scanToken();
    }

    // Adding an extra EOF token at the end to make
    // stuff in the parser easier?
    tokens.add(new Token(EOF, "", null, line));
    return tokens;
  }

  private void scanToken() {
    // Scanning single character tokens:
    char c = advance();
    switch (c) {
      case '(':
        addToken(LEFT_PAREN);
        break;
      case ')':
        addToken(RIGHT_PAREN);
        break;
      case '{':
        addToken(LEFT_BRACE);
        break;
      case '}':
        addToken(RIGHT_BRACE);
        break;
      case ',':
        addToken(COMMA);
        break;
      case '.':
        addToken(DOT);
        break;
      case '-':
        addToken(MINUS);
        break;
      case '+':
        addToken(PLUS);
        break;
      case ';':
        addToken(SEMICOLON);
        break;
      case '*':
        addToken(STAR);
        break;
      // Division or Comments?
      case '/':
        if (match('/')) {
          // A comments goes till the end of the line.
          while (peek() != '\n' && !isAtEnd())
            advance(); // Ignore contents.
        } else {
          addToken(SLASH);
        }
        break;

      // Double character matches:
      case '!':
        addToken(match('=') ? BANG_EQUAL : BANG);
        break;
      case '=':
        addToken(match('=') ? EQUAL_EQUAL : EQUAL);
        break;
      case '<':
        addToken(match('=') ? LESS_EQUAL : LESS);
        break;
      case '>':
        addToken(match('=') ? GREATER_EQUAL : GREATER);
        break;

      // Go over other less meaningfull characters:
      case ' ':
      case '\r':
      case '\t':
        // Ignore whitespace.
        break;
      case '\n':
        line++;
        break;

      // Literals
      case '"':
        string();
        break;
      default:
        if (isDigit(c)) {
          number();
        } else {
          // Here to report any invalid character the scanner finds
          // instead of just discarding it and moving on:
          Lox.error(line, "Unexpected character.");
        }
        break;
    }
  }

  // Literal detection:
  private void string() {
    while (peek() != '"' && !isAtEnd()) {
      if (peek() == '\n')
        line++;
      advance();

      if (isAtEnd()) {
        Lox.error(line, "Unterminated string.");
        return;
      }
    }

    // The closing ".
    advance();

    // Could add escape sequence support somewhere here...

    // Trim the surrounding qoutes.
    String value = source.substring(start + 1, current - 1);
    addToken(STRING, value);
  }

  private void number() {
    while (isDigit(peek()))
      advance();

    // Look for a fractional part.
    if (peek() == '.' && isDigit(peekNext())) {
      // Consume the "."
      advance();

      while (isDigit(peek()))
        advance();
    }

    addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
  }

  // Helper functions:
  private boolean match(char expected) {
    // (If used after advance()) this function
    // checks the next character.
    if (isAtEnd())
      return false;
    if (source.charAt(current) != expected)
      return false;

    current++;
    return true;
  }

  private char peek() {
    // Lookahead without consuming the character:
    if (isAtEnd())
      return '\0';
    return source.charAt(current);
  }

  private char peekNext() {
    if (current + 1 >= source.length())
      return '\0';
    return source.charAt(current + 1);
  }

  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private boolean isAtEnd() {
    return current >= source.length();
  }

  private char advance() {
    return source.charAt(current++);
  }

  private void addToken(TokenType type) {
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }
}
