import java.util.Random;


public class TimeIt {
    /* Invoke it for bit vectors of various sizes, and plot
     the bit-vector size (say N) versus the time requried to do some fixed number of rank operations
     */

    Random rand = new Random();
    public int[] generateRandomBitVector(int n, double p) {
        int[] bitVector = new int[n];
        for (int i = 0; i < n; i++) {
            bitVector[i] = (rand.nextDouble() <= p) ? 1 : 0;
        }
        return bitVector;
    }

    public int getRandomIndex(int n) {
        int low = 3;
        int high = n - 1;
        return rand.nextInt(high-low) + low;
    }

    public long timeRank(int[] bitVector, int numRankOps) {
        JacobsonRank jacobsonRank = new JacobsonRank();
        jacobsonRank.constructRankData(bitVector);

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < numRankOps; i++) {
            jacobsonRank.rank1(getRandomIndex(bitVector.length));
        }
        long endTime = System.currentTimeMillis();
        System.out.print("Bit vector size: " + bitVector.length + " | Num rank ops: " + numRankOps + " | ");
        System.out.println("Time taken: " + (endTime - startTime));
        return endTime - startTime;
    }

    public static int sumArray(int[] arr) {
        int sum = 0;
        for (int i = 0; i < arr.length; i++) {
            sum += arr[i];
        }
        return sum;
    }

    public long timeSelect(int[] bitVector, int numSelectOps) {
        JacobsonRank jacobsonRank = new JacobsonRank();
        jacobsonRank.constructRankData(bitVector);

        int maxRank = sumArray(bitVector);

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < numSelectOps; i++) {
            jacobsonRank.select1(getRandomIndex(maxRank));
        }
        long endTime = System.currentTimeMillis();
        System.out.print("Bit vector size: " + bitVector.length + " | Num select ops: " + numSelectOps + " | ");
        System.out.println("Time taken: " + (endTime - startTime));
        return endTime - startTime;
    }

    public int calculateOverhead(int[] bitVector) {
        JacobsonRank jacobsonRank = new JacobsonRank();
        jacobsonRank.constructRankData(bitVector);
        return jacobsonRank.overhead();
    }

    public SparseArray generateRandomSparseArray(int n, double p) {
        int[] randomBitVector = generateRandomBitVector(n, p);

        SparseArray sparseArray = new SparseArray();
        sparseArray.create(n);
        for (int i = 0; i < n; i++) {
            if (randomBitVector[i] == 1) {
                sparseArray.append("hkjhkji", i);
            }
        }
        sparseArray.finalizeArray();
        return sparseArray;
    }

    public double timeSparseArrayGetAtIndex(SparseArray sparseArray, int numOps) {

        long startTime = System.nanoTime();
        for (int i = 0; i < numOps; i++) {
            //sparseArray.num_elem_at(getRandomIndex(sparseArray.bitVectorSize));
            sparseArray.get_index_of(getRandomIndex(sparseArray.bitVectorSize));
        }
        long endTime = System.nanoTime();

        System.out.print("Bit vector size: " + sparseArray.bitVectorSize + " | Num get at rank ops: " + numOps + " | ");
        System.out.println("Time taken: " + ((endTime - startTime)/1e6));
        return (endTime - startTime)/1e6;
    }

    public void timeSparseArray() {
        double[] sparsities = {0.01, 0.05, 0.1, 0.5};
        int[] bitVectorSizes = {10000}; // {100, 1000, 10000, 100000, 1000000}; //{10000};
        int numOps = 100000;
        for (double sparsity: sparsities) {
            // System.out.println("Sparsity: " + sparsity);
            for (int bitVectorSize : bitVectorSizes) {
                System.out.println("Sparsity: " + sparsity + " | Bit vector size: " + bitVectorSize);
                SparseArray sparseArray = generateRandomSparseArray(bitVectorSize, sparsity);
                timeSparseArrayGetAtIndex(sparseArray, numOps);
            }
            System.out.println();
        }
    }

    public void timeJacobsonRankSelect() {
        int[] bitVectorSizes = {10, 100, 1000, 10000, 100000, 1000000, 10000000};
        int numRankOps = 100000;
        for (int bitVectorSize : bitVectorSizes) {
            int[] bitVector = generateRandomBitVector(bitVectorSize, 0.5);
            System.out.println("Number of bits: " + bitVector.length);
            // System.out.println("Overhead: " + timeIt.calculateOverhead(bitVector) + " bits");
            // timeIt.timeRank(bitVector, numRankOps);
            // timeSelect(bitVector, numRankOps);
            System.out.println();

        }
    }

    public void measureOverheadVsSparsity() {

        double[] sparsities = {0.01, 0.05, 0.1, 0.5};
        for (double sparsity: sparsities) {
            System.out.println("Sparsity: " + sparsity);
            SparseArray sparseArray = generateRandomSparseArray(10000, sparsity);
            System.out.println("Overhead: " + sparseArray.overhead() + " bits");
            System.out.println("Naive Overhead: " + sparseArray.overheadNoSparsity() + " bits");
            System.out.println();
        }
    }

    public void measureOverheadVsBitVectorSize() {
        int[] bitVectorSizes = {10, 100, 1000, 10000, 100000, 1000000, 10000000};

        for (int bitVectorSize : bitVectorSizes) {
            System.out.println("Bit Vector Size: " + bitVectorSize);
            SparseArray sparseArray = generateRandomSparseArray(bitVectorSize, 0.1);
            System.out.println("Overhead: " + sparseArray.overhead() + " bits");
            System.out.println("Naive Overhead: " + sparseArray.overheadNoSparsity() + " bits");
            System.out.println();
        }
    }

    public static void main(String[] args) {

        TimeIt timeIt = new TimeIt();

        // timeIt.timeJacobsonRankSelect();
        // timeIt.timeSparseArray();

        // timeIt.measureOverheadVsSparsity();

        timeIt.measureOverheadVsBitVectorSize();

    }


}
