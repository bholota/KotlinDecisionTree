package com.bhsoft.decisiontree

/**
 * Simple graph implementation to distinct branches and leafs
 */
sealed class GraphNode

data class Branch(val question: Question, val trueBranch: GraphNode, val falseBranch: GraphNode) : GraphNode()
data class Leaf(val predictions: MutableMap<String, Int>) : GraphNode()