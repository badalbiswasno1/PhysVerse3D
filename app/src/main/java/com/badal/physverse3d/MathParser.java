package com.badal.physverse3d;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MathParser {
    private final String expr;
    private int pos;
    private Map<String, Double> variables;

    private static final Set<String> FUNCTIONS = new HashSet<>();
    static {
        FUNCTIONS.add("sin"); FUNCTIONS.add("cos"); FUNCTIONS.add("tan");
        FUNCTIONS.add("sqrt"); FUNCTIONS.add("log"); FUNCTIONS.add("ln");
        FUNCTIONS.add("abs"); FUNCTIONS.add("exp");
    }
    private static final Set<String> CONSTANTS = new HashSet<>();
    static {
        CONSTANTS.add("pi"); CONSTANTS.add("e");
    }

    public MathParser(String expression) {
        this.expr = expression.replaceAll("\\s+", "");
    }

    public double evaluate(Map<String, Double> vars) {
        this.variables = vars;
        this.pos = 0;
        return parseExpression();
    }

    public static Set<String> extractVariables(String expression) {
        Set<String> vars = new HashSet<>();
        Matcher m = Pattern.compile("[a-zA-Z]+").matcher(expression);
        while (m.find()) {
            String token = m.group();
            if (!FUNCTIONS.contains(token) && !CONSTANTS.contains(token)) {
                vars.add(token);
            }
        }
        return vars;
    }

    private char peek() { return pos < expr.length() ? expr.charAt(pos) : '\0'; }
    private char next() { return expr.charAt(pos++); }

    private double parseExpression() {
        double value = parseTerm();
        while (peek() == '+' || peek() == '-') {
            char op = next();
            double rhs = parseTerm();
            value = (op == '+') ? value + rhs : value - rhs;
        }
        return value;
    }

    private double parseTerm() {
        double value = parsePower();
        while (peek() == '*' || peek() == '/') {
            char op = next();
            double rhs = parsePower();
            value = (op == '*') ? value * rhs : value / rhs;
        }
        return value;
    }

    private double parsePower() {
        double value = parseUnary();
        if (peek() == '^') {
            next();
            double exponent = parsePower();
            value = Math.pow(value, exponent);
        }
        return value;
    }

    private double parseUnary() {
        if (peek() == '-') { next(); return -parseUnary(); }
        if (peek() == '+') { next(); return parseUnary(); }
        return parsePrimary();
    }

    private double parsePrimary() {
        if (peek() == '(') {
            next();
            double value = parseExpression();
            if (peek() == ')') next();
            return value;
        }
        if (Character.isDigit(peek()) || peek() == '.') {
            return parseNumber();
        }
        if (Character.isLetter(peek())) {
            String name = parseIdentifier();
            if (peek() == '(') {
                next();
                double arg = parseExpression();
                if (peek() == ')') next();
                return applyFunction(name, arg);
            }
            return resolveNameToValue(name);
        }
        throw new RuntimeException("Unexpected char at " + pos);
    }

    private double parseNumber() {
        int start = pos;
        while (pos < expr.length() && (Character.isDigit(peek()) || peek() == '.')) pos++;
        return Double.parseDouble(expr.substring(start, pos));
    }

    private String parseIdentifier() {
        int start = pos;
        while (pos < expr.length() && Character.isLetter(peek())) pos++;
        return expr.substring(start, pos);
    }

    private double applyFunction(String name, double arg) {
        switch (name) {
            case "sin": return Math.sin(arg);
            case "cos": return Math.cos(arg);
            case "tan": return Math.tan(arg);
            case "sqrt": return Math.sqrt(arg);
            case "log": return Math.log10(arg);
            case "ln": return Math.log(arg);
            case "abs": return Math.abs(arg);
            case "exp": return Math.exp(arg);
            default: throw new RuntimeException("Unknown function: " + name);
        }
    }

    private double resolveNameToValue(String name) {
        if (name.equals("pi")) return Math.PI;
        if (name.equals("e")) return Math.E;
        if (variables != null && variables.containsKey(name)) return variables.get(name);
        throw new RuntimeException("Unknown variable: " + name);
    }
}
