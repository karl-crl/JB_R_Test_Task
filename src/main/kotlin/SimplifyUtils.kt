import java.lang.Integer.min
import java.util.function.BinaryOperator

fun simplifyBinaryConstExpression(expr: BinaryExpression): Expression {
    when (expr.expr1) {
        is ConstantExpression -> when(expr.expr2) {
            is ConstantExpression -> {
                val num1 =
                    if (expr.expr1.isNegate)
                        -expr.expr1.toString().drop(1).toLong()
                    else expr.expr1.toString().toLong()
                val num2 =
                    if (expr.expr2.isNegate)
                        -expr.expr2.toString().drop(1).toLong()
                    else expr.expr2.toString().toLong()
                val operator: BinaryOperator<Long>
                when (expr.operation.operation) {
                    '+' -> {
                        operator = BinaryOperator { x, y -> x + y }
                    }
                    '-' -> {
                        operator = BinaryOperator { x, y -> x - y }
                    }
                    '*' -> {
                        operator = BinaryOperator { x, y -> x * y }
                    }
                    else -> return expr
                }
                return stringToConstantExpression(operator.apply(num1, num2).toString())
            }
            else -> return expr
        }
        else ->  return expr
    }
}

fun simplify2(expr: BinaryExpression): Expression {
    val expr1 = when {
        expr.expr1 is BinaryExpression -> simplify2(expr.expr1)
        else -> expr.expr1
    }

    val expr2 = when {
        expr.expr2 is BinaryExpression -> simplify2(expr.expr2)
        else -> expr.expr2
    }


    //check if we can simplify second const expression
    when (expr2) {
        is ConstantExpression ->
            if (expr2.isNegate && expr.operation.operation == '+') {
                return simplify2(BinaryExpression(
                    Operation('-'),
                    expr.expr1,
                    ConstantExpression(false, expr2.number)))
            } else if (expr2.isNegate && expr.operation.operation == '-') {
                return simplify2(BinaryExpression(
                    Operation('+'),
                    expr.expr1,
                    ConstantExpression(false, expr2.number)))
            }
    }

    when (expr1) {
        is Element -> return BinaryExpression(expr.operation, expr1, expr2)
        is ConstantExpression -> when (expr2) {
            is Element -> return BinaryExpression(expr.operation, expr1, expr2)
            is ConstantExpression -> return simplifyBinaryConstExpression(expr)
            is BinaryExpression -> {
                if (expr2.expr1 is Element) {
                    if (expr.operation.operation == '+' && expr2.operation.operation in listOf('+', '-')){
                        return simplify2(BinaryExpression(
                            expr.operation,
                            Element(),
                            BinaryExpression(expr2.operation,expr1, expr2.expr2)))
                    } else if (expr.operation.operation == '-' && expr2.operation.operation == '+') {
                        return simplify2(BinaryExpression(
                            Operation('-'),
                            BinaryExpression(Operation('-'),expr1, expr2.expr2),
                            Element()))
                    } else if (expr.operation.operation == '-' && expr2.operation.operation == '-') {
                        return simplify2(BinaryExpression(
                            Operation('-'),
                            BinaryExpression(Operation('+'),expr1, expr2.expr2),
                            Element()))
                    } else {
                        return BinaryExpression(expr.operation, expr1, expr2)
                    }
                } else if (expr2.expr2 is Element) {
                    if (expr.operation.operation == '+' && expr2.operation.operation in listOf('+', '-')){
                        return simplify2(BinaryExpression(
                            expr2.operation,
                            BinaryExpression(Operation('+'),expr1, expr2.expr2),
                            Element()))
                    } else if (expr.operation.operation == '-' && expr2.operation.operation == '+') {
                        return BinaryExpression(
                            Operation('-'),
                            simplify2(BinaryExpression(Operation('-'),expr1, expr2.expr2)),
                            Element())
                    } else if (expr.operation.operation == '-' && expr2.operation.operation == '-') {
                        return simplify2(BinaryExpression(
                            Operation('+'),
                            BinaryExpression(Operation('-'),expr1, expr2.expr2),
                            Element()))
                    } else {
                        return BinaryExpression(expr.operation, expr1, expr2)
                    }
                } else {
                    return BinaryExpression(expr.operation, expr1, expr2)
                }
            }
        }
        is BinaryExpression -> when (expr2) {
            is Element -> return BinaryExpression(expr.operation, expr1, expr2)
            is ConstantExpression -> {
                if (expr1.expr1 is Element) {
                    if (expr.operation.operation == '+' && expr1.operation.operation in listOf('+', '-')){
                        return simplify2(BinaryExpression(
                            Operation('+'),
                            Element(),
                            BinaryExpression(expr1.operation,expr2, expr1.expr2)))
                    } else if (expr.operation.operation == '-' && expr1.operation.operation == '+') {
                        return simplify2(BinaryExpression(
                            Operation('+'),
                            Element(),
                            BinaryExpression(Operation('-'),expr1.expr2, expr2)))
                    } else if (expr.operation.operation == '-' && expr1.operation.operation == '-') {
                        return simplify2(BinaryExpression(
                            Operation('-'),
                            Element(),
                            BinaryExpression(Operation('+'),expr1.expr2, expr2)))
                    } else {
                        return BinaryExpression(expr.operation, expr1, expr2)
                    }
                } else if (expr1.expr2 is Element) {
                    if (expr.operation.operation in listOf('+', '-') && expr1.operation.operation in listOf('+', '-')){
                        return simplify2(BinaryExpression(
                            expr1.operation,
                            BinaryExpression(expr.operation,expr2, expr1.expr2),
                            Element()))
                    } else {
                        return BinaryExpression(expr.operation, expr1, expr2)
                    }
                } else {
                    return BinaryExpression(expr.operation, expr1, expr2)
                }
            }
            is BinaryExpression -> return BinaryExpression(expr.operation, expr1, expr2)
        }
    }
}
