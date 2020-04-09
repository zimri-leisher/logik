package logik

/**
 * The Logik singleton object. The best way to parse a logical expression is using the [parse] method of this object.
 * This object also has various settings that modify the way Logik works. For example, [trueText] and [falseText] allow you to
 * modify what Logik prints when it encounters the boolean literals `true` or `false` (e.g. when printing our a truth table).
 */
object Logik {

    var trueText = "true"
    var falseText = "false"

    fun Boolean.logikString() = if(this) trueText else falseText

    /**
     * Parses some [text] as a logical expression and returns it as a [LogikStatement].
     * This is synonymous to instantiating a new [LogikStatement] with [text] as its constructor parameter.
     */
    fun parse(text: String) = LogikStatement(text)
}