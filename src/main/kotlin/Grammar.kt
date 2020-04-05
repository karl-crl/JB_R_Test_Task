//TODO: add private set to all variables
/**
 * <digit> | <digit> <number>
 **/
sealed class Number
class Digit(val digit: Char) : Number() {
    init {
        if (digit < '0' || digit > '9') {
            throw SyntaxException("Digit should be between 0..9")
        }
    }

    override fun toString(): String {
        return "$digit"
    }
}
class Digits(val digit: Digit, val digits: Number) : Number() {
    override fun toString(): String {
        return "${digit.toString()}${digits.toString()}"
    }
}

val operationsCollection = listOf('+', '-', '*', '>', '<', '=', '&', '|')
class Operation(val operation: Char) {
    init {
        if (!(operation in operationsCollection)) {
            throw SyntaxException("Parser does not support operator $operation")
        }
    }
    override fun toString(): String {
        return "$operation"
    }
}

/**
 * <constant-expression> ::= “-” <number> | <number>
 * <binary-expression> ::= “(” <expression> <operation> <expression> “)”
 * <expression> ::= “element” | <constant-expression> | <binary-expression>
 **/
sealed class Expression
class Element() : Expression() {
    override fun toString(): String {
        return "element"
    }
}
class ConstantExpression(val isNegate: Boolean, val number: Number) : Expression() {
    override fun toString(): String {
        if (isNegate) {
            return "-$number"
        } else {
            return number.toString()
        }
    }
}
class BinaryExpression(val operation: Operation, val expr1: Expression, val expr2: Expression) : Expression() {
    override fun toString(): String {
        return "(${expr1.toString()}${operation.toString()}${expr2.toString()})"
    }
}

/**
 * <map-call> ::= “map{” <expression> “}”
 * <filter-call> ::= “filter{” <expression> “}”
 * <call> ::= <map-call> | <filter-call>
 **/
class Call(val isFilter: Boolean, val expression: Expression)