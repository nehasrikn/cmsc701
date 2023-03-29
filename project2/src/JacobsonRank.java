import edu.berkeley.cs.succinct.util.vector.IntVector;

import java.io.*;
import java.util.Arrays;

class Chunk {
    IntVector relativeRanks;
    IntVector subChunkIndices;
    int maxRelativeRank;

    public Chunk(int chunkStart, int chunkEnd, int[] bitVector, int subChunkSize) {
        int numSubChunks = (int) Math.ceil((double) (chunkEnd - chunkStart) / subChunkSize);

        relativeRanks = new IntVector(numSubChunks, (int) Math.ceil(Math.log(chunkEnd-chunkStart+1) / Math.log(2)));
        subChunkIndices = new IntVector(numSubChunks, subChunkSize);

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


public class JacobsonRank  implements Serializable {

    IntVector cumulativeRank;
    Chunk[] chunks;
    int chunkSize;
    int subChunkSize;
    int n;

    public static void printIntVector(IntVector v, int size) {
        for (int i = 0; i < size; i++) {
            System.out.print(v.get(i) + " ");
        }
        System.out.println();
    }

    public int overhead() {
        int integers = 3 * Integer.SIZE;
        int cumulativeRankSize = cumulativeRank.overhead();
        int chunksSize = 0;
        for (Chunk chunk : chunks) {
            chunksSize += chunk.relativeRanks.overhead();
            chunksSize += chunk.subChunkIndices.overhead();
            chunksSize += Integer.SIZE;
        }
        return integers + cumulativeRankSize + chunksSize;
    }

    public void constructRankData(int[] b) {
        n = b.length;

        double log2n = Math.log(n) / Math.log(2);

        chunkSize = (int) Math.ceil(Math.pow(log2n, 2));
        subChunkSize = (int) Math.ceil(0.5 * log2n);

        int numChunks = (int) Math.ceil((double) n / chunkSize);

        cumulativeRank = new IntVector(n, (int) log2n);
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

         // System.out.println("chunkSize: " + chunkSize);
         // System.out.println("subChunkSize: " + subChunkSize);
         // System.out.println("numChunks: " + numChunks);
    }

    public int rank1(int i) {
        /* Gets the rank of the ith bit in the bit vector */

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

    public int select1(int rank) {
        /* Gets the index of the rankth 1 in the bit vector */
        int lo = 0;
        int hi = n - 1;

        if (rank1(hi) == rank) {
            return hi;
        } else if (rank1(hi) < rank) {
            return -1;
        }

        while (lo < hi) {
            int mid = (lo + hi) / 2;
            int midRank = rank1(mid);
            if (midRank > rank) {
                hi = mid;
            } else {
                lo = mid + 1;
            }
        }
        return lo - 1;
    }

    public void save(String fname) {
        /* Saves the rank data structure for this bit vector to the file fname (your bit vector should also have a save() function). */
        try {
            FileOutputStream fileOut = new FileOutputStream(fname);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(this);
            fileOut.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JacobsonRank load(String fname) {
        /* Loads the rank data structure for this bit vector from the file fname (your bit vector should also have a load() function). */
        try {
            FileInputStream fileIn = new FileInputStream(fname);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            JacobsonRank rank = (JacobsonRank) in.readObject();
            fileIn.close();
            in.close();
            return rank;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
