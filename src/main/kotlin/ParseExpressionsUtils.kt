fun stringToNumber(string: String) : Number {
    val reversed = string.reversed()
    var result: Number = Digit(reversed[0])
    for (i in 1 until reversed.length) {
        result = Digits(Digit(reversed[i]), result)
    }
    return result
}

fun stringToConstantExpression(string: String): ConstantExpression {
    return when (string[0]) {
        '-' -> ConstantExpression(true, stringToNumber(string.drop(1)))
        else -> ConstantExpression(false, stringToNumber(string))
    }
}

/**
 * @param string --- string without braces
 */
fun stringToBinaryExpression(string: String): BinaryExpression {
    return when {
        string.startsWith("element") ->
            BinaryExpression(
                Operation(string[7]),
                Element(),
                stringToExpression(string.drop(8)))
        string.endsWith("element") ->
            BinaryExpression(
                Operation(string[string.length - 8]),
                stringToExpression(string.dropLast(8)),
                Element())
        string[0] != '(' -> {
            val operation = string.drop(1).find { it in operationsCollection } ?: throw SyntaxException("")
            val pos = string.drop(1).indexOf(operation) + 1
            BinaryExpression(
                Operation(operation),
                stringToConstantExpression(string.slice(0 until pos)),
                stringToExpression(string.drop(pos+1)))
        }
        string.last() != ')' -> {
            val operation = string.drop(1).findLast { it in operationsCollection } ?: throw SyntaxException("")
            val pos = string.indexOf(operation)
            BinaryExpression(
                Operation(operation),
                stringToExpression(string.slice(0 until pos)),
                stringToConstantExpression(string.drop(pos+1)))
        }
        string[0] == '(' -> {
            var dykeNum = 0
            var pos = 0
            run loop@{
                string.forEach { it ->
                    pos++
                    if (it == '(') {
                        dykeNum++
                    } else if (it == ')') {
                        dykeNum--
                    }
                    if (dykeNum == 0) {
                        return@loop
                    }
                }
            }

            BinaryExpression(
                Operation(string[pos]),
                stringToExpression(string.slice(0 until pos)),
                stringToExpression(string.drop(pos+1)))
        }
        else -> throw SyntaxException("Wrong syntax")
    }
}

fun stringToExpression(string: String): Expression {
    return when {
        string == "element" -> Element()
        string[0] == '(' -> stringToBinaryExpression(string.drop(1).dropLast(1))
        else -> stringToConstantExpression(string)
    }
}

fun stringToCall(call: String): Call {
    val callWithoutSpaces = call.replace(" ", "")
    return when {
        callWithoutSpaces.startsWith("filter") -> {
            Call(true, stringToExpression(callWithoutSpaces.drop(7).dropLast(1)))
        }
        callWithoutSpaces.startsWith("map") -> {
            Call(false, stringToExpression(callWithoutSpaces.drop(4).dropLast(1)))
        }
        else -> {
            throw TypeException("Call should be 'filter' or 'map'")
        }
    }
}

/**
 * Replace 'element' entry in target with replacement
 */
fun replaceWith(target: Expression, replacement: Expression): Expression {
    return when (target) {
        is Element -> replacement
        is BinaryExpression -> BinaryExpression(
            target.operation,
            replaceWith(target.expr1, replacement),
            replaceWith(target.expr2, replacement))
        is ConstantExpression -> target
    }
}
