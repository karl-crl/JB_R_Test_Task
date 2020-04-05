import java.lang.Integer.min
import java.util.function.BinaryOperator

fun simplifyBinaryConstExpression(expr: BinaryExpression): Expression {
    when (expr.expr1) {
        is ConstantExpression -> when(expr.expr2) {
            is ConstantExpression -> {
                val num1 =
                    if (expr.expr1.isNegate)
                        -expr.expr1.toString().drop(1).reversed().toLong()
                    else expr.expr1.toString().reversed().toLong()
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
        else -> return expr
    }
}

@Test
fun testSimplifyConstExpression() {
    assertEquals("4", simplifyBinaryConstExpression(stringToBinaryExpression("2+2")).toString())
}

val logicOperations = listOf('>', '<', '=')
val arithmeticOperation = listOf('+', '-', '*')
