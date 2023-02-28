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

        // System.out.println(qs.compareStrings("abraka", reference, 0));

        System.out.println(Arrays.toString(qs.naiveQuery("CGCTTTCTCGGCAACAGTTTTACCATATTATCTCGACTTCCGGTGGTAATGCCGGGTTGTCACTGGAGATTCATCCGCACATGTTACGCCATTCGTGTGG", suffixArray, reference)));

    }

    public int compareStrings(String reference, int index, String query) {
        /* Compare query to reference, starting at index in reference.
        Returns 0 if equal, -1 if query is less than reference, 1 if query is greater than reference. */
        int qIndex = 0;
        int rIndex = index;
        for (int i = 0; i < Math.min(query.length(), reference.length() - index); i++) {
            if (query.charAt(qIndex) < reference.charAt(rIndex)) {
                return 1;
            } else if (query.charAt(qIndex) > reference.charAt(rIndex)) {
                return -1;
            }
            qIndex++;
            rIndex++;
        }
        if (query.length() <= reference.length() - index) {
            return 0;
        } else {
            return -1;
        }
    }

    public int binarySearchLB(String query, int[] suffixArray, String reference) {
        int left = 0;
        int right = suffixArray.length - 1;
        int pivot = -1;
        while (left < right) {
            pivot = left + (right - left) / 2;
            int comp = compareStrings(reference, suffixArray[pivot], query);
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
            int comp = compareStrings(reference, suffixArray[pivot], query);
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


    
}