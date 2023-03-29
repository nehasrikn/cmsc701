import java.util.Random;


public class TimeIt {
    /* Invoke it for bit vectors of various sizes, and plot
     the bit-vector size (say N) versus the time requried to do some fixed number of rank operations
     */

    Random rand = new Random();
    public int[] generateRandomBitVector(int n, double p) {
        int[] bitVector = new int[n];
        for (int i = 0; i < n; i++) {
            bitVector[i] = (rand.nextDouble() < p) ? 1 : 0;
        }
        return bitVector;
    }

    public int getRandomIndex(int n) {
        int low = 1;
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

    public static void main(String[] args) {

        int[] bitVectorSizes = {10, 100, 1000, 10000, 100000, 1000000, 10000000};
        int numRankOps = 100000;
        TimeIt timeIt = new TimeIt();

        for (int bitVectorSize : bitVectorSizes) {
            int[] bitVector = timeIt.generateRandomBitVector(bitVectorSize, 0.5);
            System.out.println("Number of bits: " + bitVector.length);
            // System.out.println("Overhead: " + timeIt.calculateOverhead(bitVector) + " bits");
            // timeIt.timeRank(bitVector, numRankOps);
            timeIt.timeSelect(bitVector, numRankOps);
            System.out.println();

        }
    }


}
