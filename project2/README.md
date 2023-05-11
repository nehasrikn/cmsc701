# CMSC 701 Project 2: Implementing bitvector rank and select, and applying them to a sparse array

> What language have you written your solution in? <br>

My code is written in Java.

> What resources did you consult in working on this assignment?

I used this [implementation](https://github.com/amplab/succinct/blob/master/core/src/main/java/edu/berkeley/cs/succinct/util/vector/IntVector.java) of an underlying BitVector in this assignment.


### Rank + Select
You can utilize rank and select functionality by using the `JacobsonRank` class located in the file `JacobsonRank.java`.
```
int[] bitVector = {0,1,0,0,0,1}

JacobsonRank jacobsonRank = new JacobsonRank();
jacobsonRank.constructRankData(bitVector);

/* RANK */
jacobsonRank.rank1(getRandomIndex(bitVector.length));

/* SELECT */
jacobsonRank.select1(getRandomIndex(maxRank));
```

### SparseArray
The code for the SparseArray is located in `SparseArray.java`, along with the wrapper class that returns the elements (`SparseArrayQueryResult`). To build the SparseArray class: 

```
int[] randomBitVector = generateRandomBitVector(n, p);

SparseArray sparseArray = new SparseArray();
sparseArray.create(n);

for (int i = 0; i < n; i++) {
    if (randomBitVector[i] == 1) {
        sparseArray.append("hkjhkji", i);
    }
}
sparseArray.finalizeArray();

sparseArray.get_index_of(getRandomIndex(sparseArray.bitVectorSize));
sparseArray.num_elem_at(getRandomIndex(sparseArray.bitVectorSize));

```

### TimeIt
For all the plot values (i.e times and overheads), my testing code is located in the `TimeIt.java` class.
