import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import java.util.HashMap;
import java.util.function.Function;


public class BuildSA {
    public static void main(String[] args) {
        // Create a new instance of the SA class
        BuildSA sa = new BuildSA();
        String reference = sa.readFastaFile(System.getProperty("user.dir") + "/data/ecoli.fa");
        int[] suffixArray = sa.buildSuffixArray(reference);
        //BuildSA.printSuffixArray(suffixArray, reference);
        Map<String, int[]> prefixTable = sa.buildPrefixTableBinarySearch(reference, suffixArray, 2);
        System.out.println("Done.");
        for (String key : prefixTable.keySet()) {
            System.out.println(key + " " + Arrays.toString(prefixTable.get(key)));
        }

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

    public String readFastaFile(String filename) {
        // There could be multiple sequences in the file (header + sequence)
        String[] genomeSequences = null; 
        BufferedReader fileReader = null;
        int seqNumber = 0; // counter for sequences
        StringBuilder sb = null;

        try {
            fileReader = new BufferedReader(new FileReader(filename));
            String line = fileReader.readLine(); // first sequence's header
            // count the number of sequences
            while (line != null) {
                if (line.charAt(0) == '>') {
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
        genomeSequences[seqNumber] = sb.toString();
        return genomeSequences[0] + '$';
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
        Map<String, int[]> prefixTable = new TreeMap<String, int[]>();
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
        Map<String, int[]> prefixTable = new TreeMap<String, int[]>();
        int i = 0;
        while (i < suffixArray.length) {
            String suffix = reference.substring(suffixArray[i]);
            if (suffix.length() < k || (suffix.length() == k && suffix.charAt(suffix.length() - 1) == '$')) {
                i++;
                continue;
            }
            String prefix = suffix.substring(0, k);
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
            String midString = reference.substring(midSuffixIndex, midSuffixIndex + prefix.length());

            int prevSuffixIndex = suffixArray[mid - 1];
            String prevString = reference.substring(prevSuffixIndex, prevSuffixIndex + prefix.length());

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
