import java.util.Arrays;
public class QuerySA {
    public static void main(String[] args) {
        // "abracadabradad$";

        BuildSA sa = new BuildSA();
        QuerySA qs = new QuerySA();

        String reference =  sa.readFastaFile(System.getProperty("user.dir") + "/data/ecoli.fa");
        int[] suffixArray = sa.buildSuffixArray(reference);
        // System.out.println(Arrays.toString(suffixArray));

        System.out.println(qs.naiveQuery("AAGCTAAGCA", suffixArray, reference));

    }

    public boolean naiveQuery(String query, int[] suffixArray, String reference) {
        int left = 0;
        int right = suffixArray.length - 1;
        while (left <= right) {
            int pivot = left + (right - left) / 2;
            String comp = reference.substring(suffixArray[pivot]);
            int lexicographicComparison = query.compareTo(comp);
            if (lexicographicComparison == 0 || comp.startsWith(query)) {
                return true;
            } else if (lexicographicComparison > 0) {
                left = pivot + 1;
            } else {
                right = pivot - 1;
            }
        }
        return false;
    }




    
}