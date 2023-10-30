## Basic neural network try

### Operating principle

This is a multi-layer, simple or convolutional, neural network system. It works with weight for "axons" defined as **Byte**.
So, unlike classical NN, it does not operate classical scalar product between vectors, but  
$$
V_1.V_2 \eq \sqrt( (1 \over n)*\sum {k=1}^n v1_k*v2_k) 
$$

in order to keep results near inside byte limits. (sqrt of absolute value, with sign preserved)

### Tested

Tested with mnist base (for number), with more than 99% of accuracy.


Tested with point in space (determine if in sphere or not for example)
