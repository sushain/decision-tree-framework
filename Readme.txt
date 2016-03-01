Instructions: -


- There are 7 .java files in the 'Source' folder: -

1.) C5.java
2.) DataPoint.java
3.) DisplayProcessor.java
4.) DecisionTree.java
5.) InputProcessor.java
6.) TreeNode.java
7.) Utils.java


- Extract / Copy all the above files folder to a common folder / directory.

- Copy all the attribute (.names), training (.data / .dat) and test files (.test) to the above common folder.

- Compile C5.java using - javac C5.java

  This should generate the 7 .class files in the current directory.

- Run C5 using - java C5 <names-File> <training-Set-Filename> [testing-Set-Filename] [max-depth]

[Optional] - [testing-Set-Filename] - defaults to 1/3rd of training-file sampled randomly

[Optional] - [max-depth] - defaults to 3 | Enter a large value (like 100) to make it redundant

Note: In case you're not providing any of the optional parameters, you needn't worry about the order; as in whether [max-depth] can come as the 3rd argume
nt in case [testing-Set-Filename] isn't present. The framework will take care of this :-)