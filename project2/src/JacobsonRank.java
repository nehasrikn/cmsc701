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
       // System.out.println(j.rank1(6));
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
        int rank = cumulativeRank.get((i / chunkSize) -1 );
        Chunk chunk = chunks[i / chunkSize];
        rank += chunk.relativeRanks.get(i % chunkSize - 1);

        chunk.subChunkIndices.get(i % chunkSize );

        int subChunkIndex = i % chunkSize - 1;

        return 0;
    }

}