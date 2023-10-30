## Basic neural network try

### Operating principle

This is a multi-layer, simple or convolutional, neural network system. It works with weight for "axons" defined as **Byte**.
So, unlike classical NN, it does not operate classical scalar product between vectors, but  
```math
X \cdot Y = \sqrt( {1 \over n}*\sum_{k=1}^n x_k*y_k)$$
```
in order to keep results near inside byte limits. (sqrt of absolute value, with sign preserved)

### Tested

Tested with mnist base (for number), with more than 99% of accuracy.


Tested with point in space (determine if in sphere or not for example)
