package logik

import java.lang.StringBuilder

class TruthTable(val statement: LogikStatement) {

    val entries: Map<VariableContext, Boolean>

    init {
        val variableCount = statement.variables.size
        val possibilities = arrayOfNulls<Array<Boolean>>(Math.pow(2.0, variableCount.toDouble()).toInt())
        for (possibiltyIndex in possibilities.indices) {
            val subPossibilities = arrayOfNulls<Boolean>(variableCount)
            for (variableIndex in statement.variables.indices) {
                val switchFrequency = 1 / (Math.pow(2.0, (variableIndex + 1).toDouble()))
                val period = ((Math.pow(2.0, variableCount.toDouble()).toInt() * switchFrequency).toInt())
                subPossibilities[variableIndex] = (possibiltyIndex % (2 * period)) / period < 1
            }
            possibilities[possibiltyIndex] = subPossibilities.requireNoNulls()
        }
        possibilities.requireNoNulls()
        val contexts = mutableListOf<VariableContext>()
        for (possibility in possibilities) {
            val context = VariableContext(statement)
            for ((index, value) in possibility!!.withIndex()) {
                val prep = statement.variables[index]
                context.setValue(prep, value)
            }
            contexts.add(context)
        }
        entries = contexts.map { it to statement.evaluate(it) }.toMap()
    }

    override fun toString(): String {
        val builder = StringBuilder()
        builder.appendln("input: " + statement.text)
        for(row in entries) {
            builder.append(row.key.toString() + " --> " + row.value.toString())
            builder.append('\n')
        }
        return builder.toString()
    }
}