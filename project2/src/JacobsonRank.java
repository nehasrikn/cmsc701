import edu.berkeley.cs.succinct.util.vector.IntVector;

import java.util.Arrays;

class Chunk {
    IntVector relativeRanks;
    IntVector subChunkIndices;
    int maxRelativeRank;

    public Chunk(int chunkStart, int chunkEnd, int[] bitVector, int subChunkSize) {
        int numSubChunks = (int) Math.ceil((double)(chunkEnd - chunkStart) / subChunkSize);

        relativeRanks = new IntVector(numSubChunks, 64);
        subChunkIndices = new IntVector(numSubChunks, 64);

        int relativeRank = 0;
        for (int i = 0; i < numSubChunks; i++) { // enumerate subchunks in bit
            int startOfSubChunk = i * subChunkSize + chunkStart;
            int endOfSubChunk = Math.min((i + 1) * subChunkSize + chunkStart, chunkEnd);

            int subChunkIndex = getSubChunkIndex(startOfSubChunk, endOfSubChunk, bitVector);
            subChunkIndices.add(i, subChunkIndex);

            int subChunkRank = subChunkRank(startOfSubChunk, endOfSubChunk, bitVector);
            relativeRank += subChunkRank;
            relativeRanks.add(i, relativeRank);
        }
        maxRelativeRank = relativeRank;
    }

    public static int getSubChunkIndex(int subChunkStart, int subChunkEnd, int[] bitVector) {
        int result = 0;
        int powerOfTwo = 1;
        for (int i = subChunkStart; i < subChunkEnd; i++) {
            result += bitVector[i] * powerOfTwo;
            powerOfTwo *= 2;
        }
        return result;

    }
    public static int subChunkRank(int subChunkStart, int subChunkEnd, int[] bitVector) {
        int rank = 0;
        for (int i = subChunkStart; i < subChunkEnd; i++) {
            rank += bitVector[i];
        }
        return rank;
    }

}


public class JacobsonRank {

    IntVector cumulativeRank;
    Chunk[] chunks;
    int chunkSize;
    int subChunkSize;

    public static void main(String[] args) {
       int[] x = {0,1,1,1,0, 1,0,1,1,1, 0,1};
       JacobsonRank j = new JacobsonRank();
       j.constructRankData(x);
       System.out.println(j.rank1(12));

       /* Ground Truth */
        int[] gt = new int[x.length];
        int rank = 0;
        for (int i = 0; i < x.length; i++) {
            rank += x[i];
            gt[i] = rank;
        }
        System.out.println(Arrays.toString(gt));

       /* Test */
        int[] test = new int[x.length];
        for (int i = 0; i < x.length; i++) {
            test[i] = j.rank1(i);
        }
        System.out.println(Arrays.toString(test));
        System.out.println(Arrays.equals(gt, test));


    }

    public static void printIntVector(IntVector v, int size) {
        for (int i = 0; i < size; i++) {
            System.out.print(v.get(i) + " ");
        }
        System.out.println();
    }



    public void constructRankData(int[] b) {
        int n = b.length;

        int log2n = (int) (Math.log(n) / Math.log(2));

        chunkSize = (int) Math.ceil(Math.pow(log2n, 2));
        System.out.println("chunkSize: " + chunkSize);
        subChunkSize = (int) Math.ceil(0.5 * Math.log(n) / Math.log(2));
        System.out.println("subChunkSize: " + subChunkSize);

        int numChunks = (int) Math.ceil((double) n / chunkSize);

        cumulativeRank = new IntVector(n, 64);
        chunks = new Chunk[numChunks];

        int rank = 0;
        for (int i = 0; i < numChunks; i++) {
            Chunk chunk = new Chunk(
                    i * chunkSize,
                    Math.min((i + 1) * chunkSize, n),
                    b,
                    subChunkSize
            );
            rank += chunk.maxRelativeRank;
            cumulativeRank.add(i, rank);
            chunks[i] = chunk;
        }
        printIntVector(cumulativeRank, numChunks);
    }

    public int rank1(int i) {
        int chunkNum = i / chunkSize;
        int rank = 0;
        if (chunkNum > 0) {
            rank += cumulativeRank.get(Math.max(0, chunkNum - 1));
        }
        Chunk chunk = chunks[chunkNum];
        int subChunkIndex = (i % chunkSize) / subChunkSize;
        if (subChunkIndex > 0) {
            rank += chunk.relativeRanks.get(subChunkIndex - 1);
        }
        int subChunk = chunk.subChunkIndices.get(subChunkIndex);
        int subChunkOffset = (i % chunkSize) % subChunkSize;
        int withinSubChunkRank = ((1 << (subChunkOffset + 1)) - 1) & subChunk;
        rank += Integer.bitCount(withinSubChunkRank);
        return rank;
    }

}
