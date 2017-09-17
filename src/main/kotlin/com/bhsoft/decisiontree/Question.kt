package com.bhsoft.decisiontree


/**
 * Decision tree branch condition abstraction
 */
class Question(val header: String, val column: Int, val value: Value) {

    /**
     * Tests provided data against saved value
     * @return true if value and data are equal for Text,
     * greater or equal for Numeric.
     */
    fun match(data: Array<Value>): Boolean {
        val colData = data[column]
        return when (colData) {
            is Numeric -> colData.number >= (value as Numeric).number
            is Text -> colData.text == (value as Text).text
            is Missing -> false
        }
    }

    override fun toString(): String {
        val condition: String = when (value) {
            is Numeric -> ">= " + value.number
            is Text -> "== " + value.text
            is Missing -> "?"
        }
        return "Is $header $condition"
    }
}