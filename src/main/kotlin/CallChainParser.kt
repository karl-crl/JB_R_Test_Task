/**
 * Rewrite call chain to one filter call and one map call
 */
class CallChainParser {
    companion object {
        val pipeSymb: String = "%>%"
        var simplify = false

        fun split(callChain: String): List<String> {
            return callChain.split(pipeSymb)
        }

        fun rewriteCallChain(callChain: String): String {
            val calls = split(callChain).map { stringToCall(it) }
            val filterCalls = mutableListOf<Expression>()
            val mapCalls = mutableListOf<Expression>()
            var currentState: Expression = Element()

            calls.forEach { it ->
                when {
                    it.isFilter -> filterCalls.add(replaceWith(it.expression, currentState))
                    else -> currentState = replaceWith(it.expression, currentState)
                }
            }
            var filter: String
            if (filterCalls.isEmpty()) {
                filter = ""
            } else {
                var filterExpr = filterCalls[0]
                for (i in 1 until filterCalls.size) {
                    filterExpr = BinaryExpression(Operation('&'), filterExpr, filterCalls[i])
                }
                filter = if (simplify) {
                    when(filterExpr) {
                        is BinaryExpression -> simplify2(filterExpr).toString()
                        else -> filterExpr.toString()
                    }
                } else {
                    filterExpr.toString()
                }
            }
            val map = if (simplify) {
                when (currentState) {
                    is BinaryExpression -> simplify2(currentState as BinaryExpression).toString()
                    else -> currentState.toString()
                }
            } else {
                currentState.toString()
            }

            return "filter{$filter}%>%map{${map}}"
        }
    }
}