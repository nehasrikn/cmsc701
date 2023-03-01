import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.*;
import java.util.*;
import java.util.ArrayList;

import java.util.HashMap;
import java.io.Serializable;

import java.util.function.Function;

class SuffixArrayData implements Serializable {
    public int[] suffixArray;
    public Map<String, int[]> prefixTable;
    public int k;
    public String reference;
    SuffixArrayData(int[] sa, Map<String, int[]> pt, int k, String ref) {
        suffixArray = sa;
        prefixTable = pt;
        k = k;
        reference = ref;
    }
    public void serializeAndWriteToBinFile(String filename) {
        try {
            FileOutputStream fileOut = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(this);
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream byteObjOut = new ObjectOutputStream(fileOut);
            byteObjOut.writeObject(this);
            byte[] bytes = byteOut.toByteArray();
            out.close();
            fileOut.close();
            byteObjOut.close();
            byteOut.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static SuffixArrayData readBinFileAndDeserialize(String filename) {
        /* Read a serialized SuffixArrayData object from a file. */
        SuffixArrayData saData = null;
        try {
            FileInputStream fileStream = new FileInputStream(filename);
            ObjectInputStream objectStream = new ObjectInputStream(fileStream);
            saData = (SuffixArrayData) objectStream.readObject();
            objectStream.close();
            fileStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return saData;
    }
}


public class BuildSA {
    public static void main(String[] args) {
        /* PARSE ARGUMENTS */
        int k = 0;
        String referenceFile = "";
        String outputFile = "";

        if (args.length == 3) {
            k = Integer.parseInt(args[0]);
            referenceFile = args[1];
            outputFile = args[2];
        } else if (args.length == 2) {
            referenceFile = args[0];
            outputFile = args[1];
        } else {
            System.out.println("Usage: java BuildSA <k> <reference.fa> <output>");
            System.exit(1);
        }

        BuildSA sa = new BuildSA();
        String[][] reference = sa.readFastaFile(referenceFile, false);
        int[] suffixArray = sa.buildSuffixArray(reference[0][0]);
        Map<String, int[]> prefixTable = sa.buildPrefixTableBinarySearch(reference[0][0], suffixArray, k);

        SuffixArrayData saData = new SuffixArrayData(suffixArray, prefixTable, k, reference[0][0]);
        saData.serializeAndWriteToBinFile(outputFile);
    }

    public static void printSuffixArray(int[] suffixArray, String reference) {
        for (int j : suffixArray) {
            System.out.println(j + " " + reference.substring(j));
        }
    }
    public int[] referenceToInts(String reference) {
        char[] bases = reference.toCharArray();
        int[] baseInts = new int[bases.length];
        for (int i = 0; i < bases.length; i++) {
            baseInts[i] = bases[i] + 1;
        }
        return baseInts;
    }

    public Set<Integer> getUniqueBases(int[] baseInts) {
        Set<Integer> uniqueBases = new HashSet<>();
        for (int baseInt : baseInts) {
            uniqueBases.add(baseInt);
        }
        return uniqueBases;
    }

    public static String[][] readFastaFile(String filename, boolean appendSentinel) {
        // There could be multiple sequences in the file (header + sequence)
        String[] genomeSequences = null;
        ArrayList<String> sequenceHeaders = new ArrayList<String>();
        BufferedReader fileReader = null;
        int seqNumber = 0; // counter for sequences
        StringBuilder sb = null;

        try {
            fileReader = new BufferedReader(new FileReader(filename));
            String line = fileReader.readLine(); // first sequence's header
            // count the number of sequences
            while (line != null) {
                if (line.charAt(0) == '>') {
                    sequenceHeaders.add(line);
                    seqNumber++;
                }
                line = fileReader.readLine();
            }
            System.out.println("Number of sequences: " + seqNumber);
            genomeSequences = new String[seqNumber];

            // Actually read + store
            fileReader = new BufferedReader(new FileReader(filename));
            boolean genomeHeader = true;
            sb = new StringBuilder();
            seqNumber = 0;
            line = fileReader.readLine(); // header
            while (line != null) {
                if (line.charAt(0) == '>' && !genomeHeader) {
                    if (appendSentinel) {
                        sb.append('$');
                    }
                    genomeSequences[seqNumber] = sb.toString();
                    seqNumber++;
                    sb = new StringBuilder();
                } else if (line.charAt(0) != '>'){
                    sb.append(line);
                }
                genomeHeader = false;
                line = fileReader.readLine();
            }
            
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + filename);
        } catch (IOException e) {
            System.out.println("Error reading file: " + filename);
        } finally {
            try {
                fileReader.close();
            } catch (IOException | NullPointerException e) {
                System.out.println("Error closing file: " + filename);
            }
        }
        if (appendSentinel) {
            sb.append('$');
        }
        genomeSequences[seqNumber] = sb.toString();
        return new String[][]{genomeSequences, sequenceHeaders.toArray(new String[sequenceHeaders.size()])};
    }
    
    public int[] buildSuffixArray(String reference) {
        int[] baseInts = referenceToInts(reference);
        Set<Integer> uniqueBases = getUniqueBases(baseInts);

        int[] suffixArray = new int[baseInts.length];
		suffixArray = SuffixArray.constructSuffixArray(baseInts, Collections.max(uniqueBases) + 1);	
        return suffixArray;
    }

    public Map<String, int[]> buildPrefixTableLinear(String reference, int[] suffixArray, int k) {
        /*
        Build the prefix table atop the suffix array that jumps to the suffix array interval
        corresponding to any prefix of length k.
         */
        Map<String, int[]> prefixTable = new HashMap<String, int[]>();
        if (k == 0) {
            return prefixTable;
        }
        for (int i = 0; i < suffixArray.length; i++) {
            String suffix = reference.substring(suffixArray[i]);
            if (suffix.length() < k || (suffix.length() == k && suffix.charAt(suffix.length() - 1) == '$')) {
                continue;
            }
            String prefix = suffix.substring(0, k);
            int j = i + 1;
            while (j < suffixArray.length && reference.substring(suffixArray[j]).startsWith(prefix)) {
                j++;
            }
            int[] interval = {i, j};
            prefixTable.put(prefix, interval);
            i = j-1; // Skip to the end of the interval
        }
        return prefixTable;
    }

    public Map<String, int[]> buildPrefixTableBinarySearch(String reference, int[] suffixArray, int k) {
        /*
        Build the prefix table atop the suffix array that jumps to the suffix array interval
        corresponding to any prefix of length k.
         */
        Map<String, int[]> prefixTable = new HashMap<String, int[]>();
        if (k == 0) {
            return prefixTable;
        }
        int i = 0;
        while (i < suffixArray.length) {
            // String suffix = reference.substring(suffixArray[i]);
            if (suffixArray[i] + k > reference.length() || reference.charAt(suffixArray[i] + k - 1) == '$') {
                // suffix.length() < k || (suffix.length() == k && suffix.charAt(suffix.length() - 1) == '$')
                i++;
                continue;
            }
            // String prefix = suffix.substring(0, k);
            String prefix = reference.substring(suffixArray[i], suffixArray[i] + k);
            int j = i + 1;
            int firstNonPrefix = findIdenticalPrefixChunk(reference, suffixArray, prefix, j);
            if (firstNonPrefix == -1) {
                firstNonPrefix = j;
            }

            int[] interval = {i, firstNonPrefix};
            prefixTable.put(prefix, interval);
            i = firstNonPrefix; // Skip to the end of the interval
        }
        return prefixTable;
    }


    public static int findIdenticalPrefixChunk(String reference, int[] suffixArray, String prefix, int startIndex) {
        /*
        Find the first suffix that does not start with the prefix, starting the search from startIndex.
         */
        int low = startIndex;
        int high = suffixArray.length - 1;
        int mid;

        while (low <= high) {
            mid = low + (high - low) / 2;

            int midSuffixIndex = suffixArray[mid];
            String midString = reference.substring(midSuffixIndex, Math.min(reference.length(), midSuffixIndex + prefix.length()));

            int prevSuffixIndex = suffixArray[mid - 1];
            String prevString = reference.substring(prevSuffixIndex, Math.min(reference.length(), prevSuffixIndex + prefix.length()));

            if (!midString.equals(prefix) && (mid == 0 || prevString.equals(prefix))) {
                return mid;
            } else if (midString.equals(prefix)) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return -1; // no 1s found
    }



}
