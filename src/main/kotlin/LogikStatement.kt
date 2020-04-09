package logik

class VariableContext(val statement: LogikStatement, private val values: MutableMap<AtomicPreoposition, Boolean>) {

    constructor(statement: LogikStatement) : this(
        statement,
        statement.variables.map { it to true }.toMap().toMutableMap()
    )

    constructor(statement: LogikStatement, pairs: Collection<Pair<String, Boolean>>) : this(statement) {
        for ((key, value) in pairs) {
            val prep = statement.variables.firstOrNull { it.token.value == key }
                ?: throw EvaluationException("Atomic preposition $key is not defined for statement $statement")
            values[prep] = value
        }
    }

    constructor(other: VariableContext) : this(
        other.statement,
        other.values.map { Pair(it.key, it.value) }.toMap().toMutableMap()
    )

    fun getValue(prep: AtomicPreoposition) =
        values[prep] ?: throw EvaluationException("Atomic preposition $prep is not defined for statement $statement")

    fun setValue(prep: AtomicPreoposition, value: Boolean) {
        values[prep] = value
    }

    override fun toString(): String {
        return values.entries.joinToString(separator = " | ") { it.key.token.value + " = " + it.value.toString() + if (it.value) " " else "" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VariableContext

        if (values != other.values) return false

        return true
    }

    override fun hashCode(): Int {
        return values.hashCode()
    }
}

class LogikStatement internal constructor(val text: String) {

    val variables = mutableListOf<AtomicPreoposition>()

    var variableContext: VariableContext

    private val tokens: Array<Token>
    private var currentIndex = 0

    private var currentToken: Token
    private val baseNode: Node

    init {
        val tokenList = mutableListOf<Token>()
        // parenthesis
        var editedText = text.replace("(", "( ").replace(")", " )")
        // operators
        editedText = editedText.replace("!", " ! ")
        for (word in editedText.split(" ").mapNotNull { if (it.all { char -> char == ' ' }) null else it.trim() }) {
            val matchingToken = TokenType.values().firstOrNull { it.regex.matchEntire(word) != null }
            if (matchingToken != null) {
                tokenList.add(Token(matchingToken, word))
            } else {
                /*
                error = RoutingLanguageParseError(
                    text.indexOf(word),
                    word.length,
                    "Unknown word '$word', try adding spaces around it"
                )
                 */
                throw EvaluationException("Invalid token '$word'")
            }
        }
        tokens = tokenList.toTypedArray()
        currentToken = tokens[0]
        baseNode = parse()
        variableContext = VariableContext(this)
    }

    fun evaluate(context: VariableContext = variableContext) = baseNode.visit(context)

    fun evaluate(variables: Map<String, Boolean>): Boolean =
        evaluate(VariableContext(this, variables.map { Pair(it.key, it.value) }))

    fun evaluate(vararg variables: Pair<String, Boolean>) = evaluate(variables.toMap())


    private fun parse() = nextExpression(OperatorPrecedence.LOWEST)

    private fun eat(requiredType: TokenType) {
        if (currentToken.type == requiredType) {
            if (currentIndex == tokens.lastIndex) {
                return
            }
            currentIndex += 1
            currentToken = tokens[currentIndex]
        } else {
            throw EvaluationException("Required $requiredType, but it was ${currentToken.type} at word index $currentIndex")
        }
    }

    private fun nextExpression(precedence: OperatorPrecedence): Node {
        var node = if (precedence == OperatorPrecedence.HIGHEST) {
            nextFactor()
        } else {
            nextExpression(precedence + 1)
        }
        while (currentToken.type.precedence == precedence) {
            val token = currentToken
            if (token.type.category == TokenCategory.OP_BINARY_INFIX) {
                eat(token.type)
                val leftArg = node
                val rightArg =
                    if (precedence == OperatorPrecedence.HIGHEST) nextFactor() else nextExpression(precedence + 1)
                node = token.toNode(leftArg, rightArg)
            } else if (token.type.category == TokenCategory.OP_UNARY_LEFT) {
                eat(token.type)
                val arg = node
                node = token.toNode(arg)
            }
        }
        return node
    }

    private fun nextFactor(): Node {
        // a factor is an part that could begin an expression
        val token = currentToken
        if (token.type.category == TokenCategory.LITERAL) {
            eat(token.type)
            return Literal(token)
        } else if (token.type.category == TokenCategory.VARIABLE) {
            eat(token.type)
            val prep = AtomicPreoposition(token)
            variables.add(prep)
            return prep
        } else if (token.type.category == TokenCategory.OP_UNARY_RIGHT) {
            if (currentIndex != tokens.lastIndex) {
                eat(token.type)
                return token.toNode(
                    if (token.type.precedence == OperatorPrecedence.HIGHEST)
                        nextFactor() else
                        nextExpression(token.type.precedence + 1)
                )
            } else {
                throw EvaluationException("Expected an expression at word index $currentIndex, but the text ended")
            }
        } else if (token.type == TokenType.OPEN_PAREN) {
            if (currentIndex != tokens.lastIndex) {
                eat(TokenType.OPEN_PAREN)
                val node = nextExpression(OperatorPrecedence.LOWEST)
                eat(TokenType.CLOSE_PAREN)
                return node
            } else {
                throw EvaluationException("Expected an expression at word index $currentIndex, but the text ended")
            }
        }
        throw EvaluationException("Incorrectly placed token $currentToken at word index $currentIndex")
    }
}