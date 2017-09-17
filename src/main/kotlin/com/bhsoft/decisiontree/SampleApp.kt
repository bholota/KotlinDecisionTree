package com.bhsoft.decisiontree

/**
 * Kotlin implementation of classification decision tree learning based on
 * https://github.com/random-forests/tutorials/blob/master/decision_tree.py
 */
class DecisionTree(val headers: Array<String>) {

    private var trainedTree: GraphNode? = null

    /**
     * Counts how many times particular class occurs in provided data table
     * @return map with label as key and count as value
     */
    fun classCount(rows: Array<Array<Value>>): MutableMap<String, Int> {
        var classCounter = mutableMapOf<String, Int>()
        for (row in rows) {
            val label = (row.last() as Text).text
            if (!classCounter.containsKey(label)) {
                classCounter[label] = 0
            }
            classCounter[label] = classCounter[label]!! + 1
        }
        return classCounter
    }

    /**
     * Splits data based on provided question
     * @return pair that contains matching and non matching elements arrays
     */
    fun split(rows: Array<Array<Value>>, question: Question): Pair<Array<Array<Value>>, Array<Array<Value>>> {
        var trueRows = mutableListOf<Array<Value>>()
        var falseRows = mutableListOf<Array<Value>>()
        for (row in rows) {
            if (question.match(row)) {
                trueRows.add(row)
            } else {
                falseRows.add(row)
            }
        }
        return Pair(trueRows.toTypedArray(), falseRows.toTypedArray())
    }

    /**
     * Information gain calculation with gini impurity
     * @return gini impurity for data
     */
    fun gini(left: Array<Array<Value>>, right: Array<Array<Value>>, currentUncertainty: Float): Float {
        val p = left.size.toFloat() / (left.size.toFloat() + right.size)
        return currentUncertainty - p * calculateImpurity(left) - (1 - p) * calculateImpurity(right)
    }

    /**
     * Calculate impurity for gini algorithm
     * @return impurity for data
     */
    private fun calculateImpurity(rows: Array<Array<Value>>): Float {
        val counts = classCount(rows)
        var impurity = 1f
        for (count in counts) {
            val probabilityLabel = count.value / rows.size.toFloat()
            impurity -= probabilityLabel * probabilityLabel
        }
        return impurity
    }

    /**
     * Find best split that will be best information gain
     * @return pair with info gain and question
     */
    fun findBestSplit(rows: Array<Array<Value>>): Pair<Float, Question?> {
        var bestGain = 0f
        var bestQuestion: Question? = null
        var currentUncertanity = calculateImpurity(rows)
        val featureCount = rows[0].size - 2 // last feature is label

        for (col in 0..featureCount) {
            val values = rows.map { it[col] }.distinct()

            for (v in values) {
                val question = Question(headers[col], col, v)
                val trueFalseRows = split(rows, question)

                if (trueFalseRows.first.isEmpty() or trueFalseRows.second.isEmpty()) {
                    continue
                }
                val gain = gini(trueFalseRows.first, trueFalseRows.second, currentUncertanity)
                if (gain >= bestGain) {
                    bestGain = gain
                    bestQuestion = question
                }
            }
        }
        return Pair(bestGain, bestQuestion)
    }

    /**
     * @return trained tree for provided data
     */
    fun trainTree(rows: Array<Array<Value>>): GraphNode {
        val (gain, question) = findBestSplit(rows)
        if (gain == 0f) {
            return Leaf(classCount(rows))
        }
        val (trueRows, falseRows) = split(rows, question!!)
        val trueBranch = trainTree(trueRows)
        val falseBranch = trainTree(falseRows)
        return Branch(question, trueBranch, falseBranch)
    }

    /**
     * Classify provided data row
     * @return predictions
     */
    fun classify(row: Array<Value>, node: GraphNode): MutableMap<String, Int> {
        return when (node) {
            is Leaf -> node.predictions
            is Branch -> {
                return if (node.question.match(row)) {
                    classify(row, node.trueBranch)
                } else {
                    classify(row, node.falseBranch)
                }
            }
        }
    }

    /**
     * Prints trained tree
     *
     * It will crash if there is no trained tree in instance
     * @see train
     * @see printTree
     * @throws NullPointerException
     */
    fun printTrainedTree(spacing: String = "") {
        printTree(trainedTree!!, spacing)
    }

    /**
     * Prints provided tree
     * @see printTrainedTree
     */
    fun printTree(node: GraphNode, spacing: String = "") {
        if (node is Leaf) {
            println(spacing + "Predict " + node.predictions)
            return
        }
        node as Branch
        println(spacing + node.question)
        println(spacing + "-->True:")
        printTree(node.trueBranch, spacing + "   ")
        println(spacing + "-->False:")
        printTree(node.falseBranch, spacing + "   ")
    }

    /**
     * Prints provided leaf data
     */
    fun printLeaf(frequencyOfOccurence: Map<String, Int>): Map<String, String> {
        val total = frequencyOfOccurence.size.toFloat()
        val probabilities: MutableMap<String, String> = mutableMapOf()
        for ((key, value) in frequencyOfOccurence) {
            probabilities[key] = (value.toFloat() / total * 100f).toInt().toString() + "%"
        }
        return probabilities
    }

    /**
     * Prints predictions for provided data with trained tree model
     */
    fun printPredictions(rows: Array<Array<Value>>) {
        for (row in rows) {
            println(printLeaf(classify(row, trainedTree!!)))
        }
    }

    /**
     * Trains internal tree model with provided data
     */
    fun train(trainingRows: Array<Array<Value>>) {
        trainedTree = trainTree(trainingRows)
    }
}


fun main(vararg args: String) {
    run {
        val headers = arrayOf("color", "diameter", "label")
        val trainData = arrayOf(
                arrayOf(Text("Green"), Numeric(3f), Text("Apple")),
                arrayOf(Text("Yellow"), Numeric(3f), Text("Apple")),
                arrayOf(Text("Red"), Numeric(1f), Text("Grape")),
                arrayOf(Text("Yellow"), Numeric(3f), Text("Lemon"))
        )
        val decisionTree = DecisionTree(headers)
        decisionTree.train(trainData)
        decisionTree.printTrainedTree()
        println("----")
        decisionTree.printPredictions(trainData)
    }

    run {

    }
}