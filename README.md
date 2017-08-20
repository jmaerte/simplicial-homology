# Calculator for simplicial homology over Z.
1. [Input](#input)
2. [Output](#output)
3. [How it works](#work)
4. [Core Algorithm(Smith Normalform)](#core)
5. [Commands](#cmds)

## Input <a name="input"></a>
The program takes a file input. The file should contain a name for the complex and the supersets of it.
For example one would safe the comlex C = {Ø, {1}, {2}, {3}, {1,2}, {1,3}, {2,3}, {1,2,3}, {4}} as the following content of a text file:
C := [[1,2,3], [4]].<br>
The program will then generate all subsets of the 'supersets' itself.
## Output <a name="output"></a>
The program constructs a chain complex through defining C<sub>k</sub> as the set of all sets contained by the simplicial complex C with magnitude k+1.<br>
One says, that C<sub>k</sub> has dimension k. F.e. C<sub>-1</sub> = {Ø} is of dimension -1.<br>
Further we define boundary maps ∂<sub>k</sub>: C<sub>k</sub> -> C<sub>k-1</sub> through the following mapping rule:<br>
Let A={a<sub>0</sub>,...,a<sub>k</sub>} be an element of C<sub>k</sub>, then we formally consider e<sub>A</sub> a basis element of C<sub>k</sub>.<br>
Since we want ![equation](http://latex.codecogs.com/gif.latex?%5Cpartial_k%20%5Ccirc%20%5Cpartial_%7Bk-1%7D%20%3D%200) for every k > 0, we will set<br>
![equation](http://latex.codecogs.com/gif.latex?%5Cpartial_k%28e_A%29%20%3D%20%5Csum_%7Bi%20%3D%200%7D%5Ek%20%7B%28-1%29%5Ei%20e_%7BA%5Csetminus%5C%7Ba_i%5C%7D%7D%7D)
<br>
Furthermore we then define the k-th homology group as ![equation](http://latex.codecogs.com/gif.latex?H_k%28C%29%20%3D%20%7Bker%7E%5Cpartial_k%7D/%7Bim%7E%5Cpartial_%7Bk&plus;1%7D%7D)<br>
This groups are finitely generated free abelian groups in our case. The program prints out the isomorphic direct sum of powers of cyclic groups such as Z<sub>n</sub> or Z.<br>
## How it works <a name="work"></a>
We calculate the matrices of the boundary maps stated above.<br>
While doing that we can already process just generated rows, without having the full matrix saved. Every time we generate a (generally sparse) row vector of the matrix, we look up, if we have an index-intersection of the current vector with an already generated vector, having a trailing invertible in Z, which are +1 and -1 (meaning, that we have an index u, such that the u-th entry, let us call it x, in our current vector is not equal to 0. If we already have a vector with trailing invertible y at position u, we can add -y * x times the vector with trailing invertible at position u to the current vector and, through repeating this for every entry in the current vector, get a vector with only 0s in columns, where a trailing one in any earlier row exists).<br>
Now there are two cases:<br>
First: The current vector has a trailing invertible still. Then we put it to the vectors with this property, so that we can subtract it from the later on processed vectors. Since our goal is the reduced row echelon form, we will need to also subtract the latest processed vector from every previously processed vector.<br>
Second: The current vector has no trailing invertible (equivalently: the absolute value of the trailing non-zero value in vector is not 1). Then we add the vector to our remaining matrix. This one is going to be the hard part.<br>
While processing the matrix, we count how many vector has been added to the trailing invertible vectors. That is how many 1s we will certainly get in the smith normal form of our boundary matrix.<br>
After this first processing we go to the [Core Algorithm](#core) with our remaining matrix, because through the row echelon form, we can just eliminate the rows, which have a trailing invertible, columnwise and get the identity matrix in the top-left block of our smith normal form. The top-right and top-left blocks are 0 by definition of the smith normal form. The bottom-right block will become the smith normal form of the remaining matrix.<br>
## Core Algorithm(Smith Normalform) <a name="core"></a>
The long runtimes on bigger examples are mainly due to this algorithm.
### Proceeding
**Input**: Matrix with arbitrary structure.<br>
**Output**: invariant factors of input matrix.<br>
**Process**: *[Description here]*
## Commands <a name="cmd"></a>
### Program arguments
For initializing a program argument one needs to type -<command> <parameters> after the program call in command line.<br>
(Star after argument marks that it is necessary)

| Command       | Description   |
| ------------- | ------------- |
| **C** *path*:**string**\* | C: Complex. This command determines the file, where to take the complex data from.<br> NOTE: If your path contains spaces, please set double quotation marks ". |
### Flags
