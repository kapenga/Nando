# Nando
Nando is capable of doing Boolean Rule Induction and Binary Rule based Machine Learning
#
#### Warning: The application is still in beta and a work in progress.

### How it works
The Nand class has a static function 'expand' that can be fed with a number of BitArrays that act as columns in a truth table.
The result is the root of a boolean expression tree. Every branch node is simply a nand operator.
Every leaf represents one of the input columns of the truth table. (A leaf node can be an inverted (not) column.)

### Usage
There are a few tests in main.java available for use.
Right now there are a few magic variables to play around with. On average the default values seems to lead to good results.
Limit the maximum depth of the tree and the number of 'expands' in case of Binary Rule based Machine Learning to avoid overfitting.

### Why Nand?
1. The boolean Nand operator is functional complete. So is the nor operator but nand is choosen.
2. The initial version of the algorithm supported the entire spectrum of boolean operations. It turned out that for big boolean expressions (>1k operators) the advantage of supporting a wide range of operators disappears.
3. If everything is a nand operations only the leaf nodes are of interest. There is less information to store. Trees are more easy to compare.

### What's the advantage above other algorithms?
As soon as I find similar algorithms I will compare things.

### How it works (in depth)
The central mechanic is a recursive function.
This function starts with the root node. The node is initialised with the (possibly inverted) input column that has the greatest similarity (best score) with the target column.
The scoring function is a bitcount on the resulting column after the following operation: (inputColumn ^ targetColumn) & careColumn.
The careColumn is initialised with all values set to true.

Later more...
