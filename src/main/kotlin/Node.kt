package logik

sealed class Node(val token: Token) {
    abstract fun visit(context: VariableContext): Boolean
}

class AtomicPreoposition(token: Token) : Node(token) {
    override fun visit(context: VariableContext): Boolean {
        return context.getValue(this)
    }

    override fun toString(): String {
        return token.value
    }
}

sealed class UnaryOperator(token: Token, val arg: Node) : Node(token)

class Not(token: Token, arg: Node) : UnaryOperator(token, arg) {
    override fun visit(context: VariableContext): Boolean {
        return !arg.visit(context)
    }
}

class Literal(token: Token) : Node(token) {
    override fun visit(context: VariableContext): Boolean {
        return token.value.toBoolean()
    }
}

sealed class BinaryOperator(token: Token, val left: Node, val right: Node) : Node(token)

class Implies(token: Token, left: Node, right: Node) : BinaryOperator(token, left, right) {
    override fun visit(context: VariableContext): Boolean {
        val leftValue = left.visit(context)
        val rightValue = right.visit(context)
        return !leftValue || (leftValue && rightValue)
    }
}

class IfAndOnlyIf(token: Token, left: Node, right: Node) : BinaryOperator(token, left, right) {
    override fun visit(context: VariableContext): Boolean {
        val leftValue = left.visit(context)
        val rightValue = right.visit(context)
        return leftValue == rightValue
    }
}

class ExclusiveOr(token: Token, left: Node, right: Node) : BinaryOperator(token, left, right) {
    override fun visit(context: VariableContext): Boolean {
        val leftValue = left.visit(context)
        val rightValue = right.visit(context)
        return leftValue != rightValue
    }
}

class Or(token: Token, left: Node, right: Node) : BinaryOperator(token, left, right) {
    override fun visit(context: VariableContext): Boolean {
        val leftValue = left.visit(context)
        val rightValue = right.visit(context)
        return leftValue || rightValue
    }
}

class And(token: Token, left: Node, right: Node) : BinaryOperator(token, left, right) {
    override fun visit(context: VariableContext): Boolean {
        val leftValue = left.visit(context)
        val rightValue = right.visit(context)
        return leftValue && rightValue
    }
}
