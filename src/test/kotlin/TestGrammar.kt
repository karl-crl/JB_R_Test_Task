import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.lang.Exception
import kotlin.random.Random

class TestGrammar {
    fun generateConstExpression(): ConstantExpression {
        return stringToConstantExpression(Random.nextLong().toString())
    }

    fun generateBinaryExpression(expr1: Expression, expr2: Expression): BinaryExpression {
        return BinaryExpression(Operation(operationsCollection.shuffled().take(1)[0]), expr1, expr2)
    }

    fun generateSimpleBinaryExpression(): BinaryExpression {
        var expr1 = if (Random.nextBoolean()) Element() else generateConstExpression()
        var expr2 = if (Random.nextBoolean()) Element() else generateConstExpression()
        return generateBinaryExpression(expr1, expr2)
    }

    fun generateExpression(): Expression {
        //with propabibily 1/2 return simple expression
        if (Random.nextInt(0,1) == 0) {
            return generateSimpleBinaryExpression()
        }
        return generateBinaryExpression(generateExpression(), generateExpression())
    }

    @Test
    fun testStringToNumber() {
        var number: String
        for (i in 0..1000) {
            number = Random.nextLong(0, Long.MAX_VALUE).toString()
            assertEquals(number, stringToNumber(number).toString())
        }
    }

    @Test(expected = SyntaxException::class)
    fun testWrongStringToNumber() {
        stringToConstantExpression("-123e4")
        stringToConstantExpression("-1234T")
        stringToConstantExpression("r123e4")
        stringToConstantExpression("123-4")
        stringToConstantExpression("+1234")
    }

    @Test
    fun testStringToExpression() {
        var expression: String
        for (i in 0..1000) {
            expression = generateExpression().toString()
            assertEquals(expression, stringToExpression(expression).toString())
        }
    }

    @Test
    fun testParser() {
        CallChainParser.simplify = false
        var expression = "filter{(element>10)}%>%filter{(element<20)}"
        var expected = "filter{((element>10)&(element<20))}%>%map{element}"
        assertEquals(expected, CallChainParser.rewriteCallChain(expression))
        expression = "map{(element+10)}%>%filter{(element>10)}%>%map{(element*element)}"
        expected = "filter{((element+10)>10)}%>%map{((element+10)*(element+10))}"
        assertEquals(expected, CallChainParser.rewriteCallChain(expression))
        expression = "map{(element+10)}%>%filter{(element>10)}%>%map{(element*(element+20))}"
        expected = "filter{((element+10)>10)}%>%map{((element+10)*((element+10)+20))}"
        assertEquals(expected, CallChainParser.rewriteCallChain(expression))
        expression = "map{(element+10)}%>%filter{(element>10)}%>%map{(element-30)}%>%filter{((element*element)<100)}"
        expected = "filter{(((element+10)>10)&((((element+10)-30)*((element+10)-30))<100))}%>%map{((element+10)-30)}"
        assertEquals(expected, CallChainParser.rewriteCallChain(expression))
    }

    @Test
    fun testSimplifyConstExpression() {
        assertEquals("4", simplifyBinaryConstExpression(stringToBinaryExpression("2+2")).toString())
        assertEquals("4", simplifyBinaryConstExpression(stringToBinaryExpression("8-4")).toString())
        assertEquals("4", simplifyBinaryConstExpression(stringToBinaryExpression("-4+8")).toString())
        assertEquals("4", simplifyBinaryConstExpression(stringToBinaryExpression("8-4")).toString())
        assertEquals("4", simplifyBinaryConstExpression(stringToBinaryExpression("2*2")).toString())
        assertEquals("4", simplifyBinaryConstExpression(stringToBinaryExpression("-2*-2")).toString())
        assertEquals("22", simplifyBinaryConstExpression(stringToBinaryExpression("20+2")).toString())
        assertEquals("22", simplifyBinaryConstExpression(stringToBinaryExpression("2+20")).toString())
    }

    @Test
    fun testSimplifyParser() {
        CallChainParser.simplify = true
        var expression = "filter{(element>10)}%>%filter{(element<20)}"
        var expected = "filter{((element>10)&(element<20))}%>%map{element}"
        assertEquals(expected, CallChainParser.rewriteCallChain(expression))
        expression = "map{(element+10)}%>%filter{(element>10)}%>%map{(element*element)}"
        expected = "filter{((element+10)>10)}%>%map{((element+10)*(element+10))}"
        assertEquals(expected, CallChainParser.rewriteCallChain(expression))
        expression = "map{(element+10)}%>%filter{(element>10)}%>%map{(element*(element+20))}"
        expected = "filter{((element+10)>10)}%>%map{((element+10)*(element+30))}"
        assertEquals(expected, CallChainParser.rewriteCallChain(expression))
        expression = "map{(element+10)}%>%filter{(element>10)}%>%map{(element-30)}%>%filter{((element*element)<100)}"
        expected = "filter{(((element+10)>10)&(((element-20)*(element-20))<100))}%>%map{(element-20)}"
        assertEquals(expected, CallChainParser.rewriteCallChain(expression))
    }
}