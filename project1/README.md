### CMSC 701 Project 1: The Suffix Array

> What language have you written your solution in? <br>

My code is written in Java.

> What resources did you consult in working on this assignment?

I used this [implementation](https://github.com/v-v/karkkainen-sanders/tree/master/code/java/Bioinformatika/src) of the suffix array in this assignment. In order to stay consistent with their API, my code that reads in FASTA files is similar to their implementation.


In order to run the executables, you can execute them as bash scripts:
```
bash querysa results/influenza/influenza_sa_preftab_5.bin data/influenza/influenzaA_queries.fa_500 naive results/influenza/naive_k_5_q_500.out
```
