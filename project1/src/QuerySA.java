import java.util.Arrays;
public class QuerySA {
    public static void main(String[] args) {
        // "abracadabradad$";

        BuildSA sa = new BuildSA();
        QuerySA qs = new QuerySA();

        String reference = sa.readFastaFile(System.getProperty("user.dir") + "/data/ecoli.fa");
        int[] suffixArray = sa.buildSuffixArray(reference);
        System.out.println("Done reading in.");
        // BuildSA.printSuffixArray(suffixArray, reference);

        System.out.println(Arrays.toString(qs.simpleAccelQuery("AAGTATTGGC", suffixArray, reference)));

//        var tick = System.currentTimeMillis();
//        for (int i = 0; i < 10000; i++) {
//            qs.naiveQuery("CGCTTTCTCGGCAACAGTTTTACCATATTATCTCGACTTCCGGTGGTAATGCCGGGTTGTCACTGGAGATTCATCCGCACATGTTACGCCATTCGTGTGG", suffixArray, reference);
//        }
//        var tock = System.currentTimeMillis();
//        System.out.println("Naive query took " + (tock - tick) + " ms.");
    }
    public int getMismatchIndex(String reference, int index, String query, int offset) {
        /* Find the index of the first mismatch between query and reference, starting at index in reference. */
        int i = offset;
        for (; i < Math.min(query.length(), reference.length() - index); i++) {
            if (query.charAt(i) != reference.charAt(index + i)) {
                return i;
            }
        }
        return i;
    }
    public int strComp(String reference, int index, String query, int offset) {
        /* Compare query to reference, starting at index in reference.
        Returns 0 if equal, -1 if query is less than reference, 1 if query is greater than reference. */
        int mismatchIndex = getMismatchIndex(reference, index, query, offset);
        if (mismatchIndex == query.length()) {
            return 0;
        }
        if (index + mismatchIndex >= reference.length()) {
            return -1;
        }
        return Character.compare(reference.charAt(index + mismatchIndex), query.charAt(mismatchIndex));
    }

    public int binarySearchLB(String query, int[] suffixArray, String reference) {
        int left = 0;
        int right = suffixArray.length - 1;
        int pivot = -1;
        while (left < right) {
            pivot = left + (right - left) / 2;
            int comp = strComp(reference, suffixArray[pivot], query, 0);
            if (comp >= 0) {
                right = pivot;
            } else {
                left = pivot + 1;
            }
        }
        return left;
    }

    public int binarySearchUB(String query, int[] suffixArray, String reference) {
        int left = 0;
        int right = suffixArray.length - 1;
        int pivot = -1;
        while (left < right) {
            pivot = left + (right - left) / 2;
            int comp = strComp(reference, suffixArray[pivot], query, 0);
            if (comp > 0) {
                right = pivot;
            } else {
                left = pivot + 1;
            }
        }
        return left;
    }


    public int[] naiveQuery(String query, int[] suffixArray, String reference) {
        var lower = binarySearchLB(query, suffixArray, reference);
        var upper = binarySearchUB(query, suffixArray, reference);
        int[] results = new int[upper - lower];
        for (int i = lower; i < upper; i++) {
            results[i - lower] = suffixArray[i];
        }
        Arrays.sort(results);
        return results;
    }

    public int[] simpleAccelQuery(String query, int[] suffixArray, String reference) {
        int left = 0;
        int right = suffixArray.length - 1;
        int pivot;
        int comp;
        int minLCPSkip;

        int lcpLeftQuery = getMismatchIndex(reference, suffixArray[left], query, 0);
        int lcpRightQuery = getMismatchIndex(reference, suffixArray[right], query, 0);

        while (left < right) {
            pivot = left + (right - left) / 2;
            minLCPSkip = Math.min(lcpLeftQuery, lcpRightQuery);

            comp = strComp(reference, suffixArray[pivot], query, minLCPSkip);
            if (comp >= 0) {
                right = pivot;
                lcpRightQuery = getMismatchIndex(reference, suffixArray[right], query, minLCPSkip);

            } else {
                left = pivot + 1;
                lcpLeftQuery = getMismatchIndex(reference, suffixArray[left], query, minLCPSkip);

            }
        }
        int lower_bound = left; // now this stores what i want
        
        right = suffixArray.length - 1;
        pivot = -1;
        lcpRightQuery = getMismatchIndex(reference, suffixArray[right], query, 0);

        while (left < right) {
            pivot = left + (right - left) / 2;
            minLCPSkip = Math.min(lcpLeftQuery, lcpRightQuery);
            comp = strComp(reference, suffixArray[pivot], query, 0);
            if (comp > 0) {
                right = pivot;
                lcpRightQuery = getMismatchIndex(reference, suffixArray[right], query, minLCPSkip);
            } else {
                left = pivot + 1;
                lcpLeftQuery = getMismatchIndex(reference, suffixArray[left], query, minLCPSkip);
            }
        }
        int upper_bound = left; // now this stores what i want

        int[] results = new int[upper_bound - lower_bound];
        for (int i = lower_bound; i < upper_bound; i++) {
            results[i - lower_bound] = suffixArray[i];
        }
        Arrays.sort(results);
        return results;

    }


    
}