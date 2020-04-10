package com.cygns.logik

sealed class Node(val token: Token) {
    abstract fun visit(context: VariableContext): Boolean

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Node

        if (token != other.token) return false

        return true
    }

    override fun hashCode(): Int {
        return token.hashCode()
    }

    /**
     * Converts a [Node] into a representation that can be parsed by the LaTeX compiler
     */
    abstract fun toLaTeX(): String
}

class Variable(token: Token) : Node(token) {
    override fun visit(context: VariableContext): Boolean {
        return context.getValue(this)
    }

    override fun toLaTeX() = toString()

    override fun toString(): String {
        return token.value
    }
}

sealed class UnaryOperator(token: Token, val arg: Node) : Node(token) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as UnaryOperator

        if (arg != other.arg) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + arg.hashCode()
        return result
    }

    override fun toString(): String {
        return "(${token.value} $arg)"
    }
}

class Not(token: Token, arg: Node) : UnaryOperator(token, arg) {
    override fun visit(context: VariableContext): Boolean {
        return !arg.visit(context)
    }

    override fun toLaTeX(): String {
        return "\\lnot ${arg.toLaTeX()}"
    }
}

class Literal(token: Token) : Node(token) {
    override fun visit(context: VariableContext): Boolean {
        return token.value.toBoolean()
    }

    override fun toString(): String {
        return token.value
    }

    override fun toLaTeX() = toString()
}

sealed class BinaryOperator(token: Token, val left: Node, val right: Node) : Node(token) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as BinaryOperator

        if (left != other.left) return false
        if (right != other.right) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + left.hashCode()
        result = 31 * result + right.hashCode()
        return result
    }

    override fun toString(): String {
        return "(${token.value} $left $right)"
    }

}

class Implies(token: Token, left: Node, right: Node) : BinaryOperator(token, left, right) {
    override fun visit(context: VariableContext): Boolean {
        val leftValue = left.visit(context)
        val rightValue = right.visit(context)
        return !leftValue || (leftValue && rightValue)
    }

    override fun toLaTeX(): String {
        return "(${left.toLaTeX()} \\implies ${right.toLaTeX()})"
    }
}

class IfAndOnlyIf(token: Token, left: Node, right: Node) : BinaryOperator(token, left, right) {
    override fun visit(context: VariableContext): Boolean {
        val leftValue = left.visit(context)
        val rightValue = right.visit(context)
        return leftValue == rightValue
    }

    override fun toLaTeX(): String {
        return "(${left.toLaTeX()} \\iff ${right.toLaTeX()})"
    }
}

class ExclusiveOr(token: Token, left: Node, right: Node) : BinaryOperator(token, left, right) {
    override fun visit(context: VariableContext): Boolean {
        val leftValue = left.visit(context)
        val rightValue = right.visit(context)
        return leftValue != rightValue
    }

    override fun toLaTeX(): String {
        return "(${left.toLaTeX()} \\oplus ${right.toLaTeX()})"
    }
}

class Or(token: Token, left: Node, right: Node) : BinaryOperator(token, left, right) {
    override fun visit(context: VariableContext): Boolean {
        val leftValue = left.visit(context)
        val rightValue = right.visit(context)
        return leftValue || rightValue
    }

    override fun toLaTeX(): String {
        return "(${left.toLaTeX()} \\lor ${right.toLaTeX()})"
    }
}

class And(token: Token, left: Node, right: Node) : BinaryOperator(token, left, right) {
    override fun visit(context: VariableContext): Boolean {
        val leftValue = left.visit(context)
        val rightValue = right.visit(context)
        return leftValue && rightValue
    }

    override fun toLaTeX(): String {
        return "(${left.toLaTeX()} \\land ${right.toLaTeX()})"
    }
}

class Nand(token: Token, left: Node, right: Node) : BinaryOperator(token, left, right) {
    override fun visit(context: VariableContext): Boolean {
        val leftValue = left.visit(context)
        val rightValue = right.visit(context)
        return !(leftValue && rightValue)
    }

    override fun toLaTeX(): String {
        return "(${left.toLaTeX()}) nand (${right.toLaTeX()})"
    }
}