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
            int rank = jacobsonRank.rank1(getRandomIndex(bitVector.length));
        }
        long endTime = System.currentTimeMillis();
        System.out.print("Bit vector size: " + bitVector.length + " | Num rank ops: " + numRankOps + " | ");
        System.out.println("Time taken: " + (endTime - startTime));
        return endTime - startTime;
    }

    public static void main(String[] args) {
        TimeIt timeIt = new TimeIt();
        int[] bitVector = timeIt.generateRandomBitVector(1000000, 0.5);
        timeIt.timeRank(bitVector, 100000);
    }


}
