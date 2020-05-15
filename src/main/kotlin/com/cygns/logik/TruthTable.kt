package com.cygns.logik

import com.cygns.logik.Logik.logikString
import java.lang.Integer.max

/**
 * A class which stores in [mapping] every possible combination of truth values of [Variable]s for a given [statement], along with the
 * corresponding truth value of the statement. [mapping] will be populated at instantiation with 2^n entries, where n is the
 * number of variables in the [statement].
 */
class TruthTable internal constructor(val statement: LogikStatement) {

    /**
     * The entries of ([VariableAssignment], [Boolean]) pairs which make up every possible combination of truth values of
     * [Variable]s in the [statement], and the corresponding truth value of the overall [statement]
     */
    val mapping: Map<VariableAssignment, Array<Boolean>>


    var showSubExpressions = Logik.defaultShowSubExpressions
    var onlyShowTrueExpressions = Logik.defaultOnlyShowTrueExpressions

    init {
        val variableCount = statement.variables.size
        val possibilities = arrayOfNulls<Array<Boolean>>(Math.pow(2.0, variableCount.toDouble()).toInt())
        for (possibiltyIndex in possibilities.indices) {
            val subPossibilities = arrayOfNulls<Boolean>(variableCount)
            for (variableIndex in statement.variables.indices) {
                val switchFrequency = 1 / (Math.pow(2.0, (variableIndex + 1).toDouble()))
                val period = ((possibilities.size * switchFrequency).toInt())
                subPossibilities[variableIndex] = (possibiltyIndex % (2 * period)) / period < 1
            }
            possibilities[possibiltyIndex] = subPossibilities.requireNoNulls()
        }
        possibilities.requireNoNulls()
        val contexts = mutableListOf<VariableAssignment>()
        for (possibility in possibilities) {
            val context = VariableAssignment(statement)
            for ((index, value) in possibility!!.withIndex()) {
                val prep = statement.variables[index]
                context.setValue(prep, value)
            }
            contexts.add(context)
        }
        mapping = contexts.map { context ->
            Pair(
                context,
                arrayOf(*statement.subExpressions.map { it.visit(context) }.toTypedArray(), statement.evaluate(context))
            )
        }.toMap()
    }

    /**
     * Creates a LaTeX table with the variables, expressions and sub expressions on the first row and all of their respective
     * values for each possibility on the second row. Can be configured with [showSubExpressions] and [onlyShowTrueExpressions].
     */
    fun toLaTeX(): String {
        val builder = java.lang.StringBuilder()
        // beginner of table
        builder.appendln("\\[")
        builder.append("\\begin{array}{")
        for(variable in statement.variables) {
            builder.append("c|")
        }
        builder.deleteCharAt(builder.lastIndex)
        builder.append("?{2mm}")
        if(showSubExpressions) {
            for(subExpr in statement.subExpressions) {
                builder.append("c|")
            }
        }
        builder.appendln("c}")
        // first row: variable names, expr text
        for(variable in statement.variables) {
            builder.append(" ${variable.toLaTeX()} &")
        }
        if(showSubExpressions) {
            for(subExpr in statement.subExpressions) {
                builder.append(" ${subExpr.toLaTeX()} &")
            }
        }
        builder.appendln("${statement.baseNode.toLaTeX()} \\\\ \\hline")
        for((context, values) in mapping) {
            if(onlyShowTrueExpressions && !values.last()) {
                continue
            }
            for(variable in statement.variables) {
                val value = context.getValue(variable)
                builder.append(" ${value.logikString()} &")
            }
            if(showSubExpressions) {
                for(index in statement.subExpressions.indices) {
                    val value = values[index]
                    builder.append(" ${value.logikString()} &")
                }
            }
            builder.appendln(statement.evaluate(context).logikString() + " \\\\")
        }
        builder.appendln("\\end{array}\n\\]")
        return builder.toString()
    }

    override fun toString(): String {
        val booleanSize = max(Logik.trueText.length, Logik.falseText.length)
        val builder = StringBuilder()
        for (variable in statement.variables) {
            builder.append("| ${variable.token.value}".padEnd(3 + booleanSize))
        }
        if (showSubExpressions) {
            for (subExpression in statement.subExpressions) {
                builder.append("| ${subExpression} ")
            }
        }
        builder.appendln("| ${statement.text} |")
        for ((context, values) in mapping) {
            if(onlyShowTrueExpressions && !values.last()) {
                continue
            }
            for (variable in statement.variables) {
                val variableValue = context.getValue(variable)
                builder.append("| ${variableValue.logikString()}".padEnd(3 + booleanSize))
            }
            if (showSubExpressions) {
                for (index in values.indices) {
                    val value = values[index]
                    if (index == values.lastIndex) {
                        builder.append("| ${value.logikString()}".padEnd(statement.text.length + 2) + " ")
                    } else {
                        val subExprString = statement.subExpressions[index].toString()
                        builder.append("| ${value.logikString()}".padEnd(subExprString.length + 2) + " ")
                    }
                }
                builder.append("|")
            } else {
                builder.append("| ${values.last().logikString()}".padEnd(statement.text.length + 2) + " |")
            }
            builder.appendln()
        }
        return builder.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TruthTable
        val theseEntries = mapping.entries.toList()
        val otherEntries = other.mapping.entries.toList()
        for (i in theseEntries.indices) {
            val thisEntry = theseEntries[i]
            val otherEntry = otherEntries[i]
            if (thisEntry.key != otherEntry.key) {
                return false
            }
            if (!thisEntry.value.contentEquals(otherEntry.value)) {
                return false
            }
        }

        return true
    }

    override fun hashCode(): Int {
        return mapping.hashCode()
    }
}