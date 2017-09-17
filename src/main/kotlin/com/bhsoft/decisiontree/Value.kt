package com.bhsoft.decisiontree

/**
 * Abstraction that allows to work on text or float based features
 */
sealed class Value
data class Numeric(val number: Float) : Value()
data class Text(val text: String) : Value()
class Missing(): Value()