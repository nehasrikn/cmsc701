import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


class SparseArrayQueryResult {
    String elem = null;
    public void setElem(String elem) {
        this.elem = elem;
    }
}

public class SparseArray {

    JacobsonRank jacobsonRank;
    int bitVectorSize;
    ArrayList<String> elements;
    ArrayList<Integer> positions;
    public static void main(String[] args) {
        SparseArray sparseArray = new SparseArray();
        sparseArray.create(13);
        sparseArray.append("foo", 1);
        sparseArray.append("bar", 5);
        sparseArray.append("baz", 9);
        sparseArray.append("bar", 12);



        sparseArray.finalizeArray();

        SparseArrayQueryResult e = new SparseArrayQueryResult();
        System.out.println(sparseArray.get_index_of(3));

        sparseArray.get_at_rank(4, e);
        System.out.println(e.elem);

        sparseArray.get_at_index(10, e);
        System.out.println(e.elem);
    }

    public void create(int size) {
        /* Creates an empty sparse array of length size (the size of the underlying bitvector you will create). */
        elements = new ArrayList<>();
        positions = new ArrayList<>();
        bitVectorSize = size;
    }

    public void append(String elem, int pos) {
        /* Appends the element elem at index pos of the sparse array.
         */
        if (pos >= bitVectorSize || pos < 0) {
            throw new IllegalArgumentException("pos must be less than size");
        }
        elements.add(elem);
        positions.add(pos);
    }

    public void finalizeArray() {
        /* Finalizes the sparse array. This method should be called after all elements have been appended.
         * This method should create the underlying bitvector and store it in the bitVector field.
         */
        int[] bitVector = new int[bitVectorSize];
        for (int pos : positions) {
            bitVector[pos] = 1;
        }
        System.out.println("SparseArray BitVector: " + Arrays.toString(bitVector));
        jacobsonRank = new JacobsonRank();
        jacobsonRank.constructRankData(bitVector);
    }

    public boolean get_at_rank(int r, SparseArrayQueryResult elem) {
        /* This function places a reference to the r-th present item in the array in the reference elem.
        It returns true if there was >= r items in the sparse array and false otherwise
         */
        if (r < 0) {
            throw new IllegalArgumentException("rank must be greater than 0");
        } else if (r > bitVectorSize) {
            return false;
        } else {
            elem.setElem(elements.get(r - 1));
            return true;
        }
    }

    public boolean get_at_index(int idx, SparseArrayQueryResult elem) {
        /* This function looks at the r-th index in the sparse bitvector; if that bit is 1,
        it fetches the corresponding value and binds it to the reference elem and returns true,
        if that bit is a 0, it simply returns false.
         */
        if (idx < 0 || idx >= bitVectorSize) {
            throw new IllegalArgumentException("idx must be between 0 and size");
        }
        return get_at_rank(jacobsonRank.rank1(idx) - 1, elem);
    }

    public int get_index_of(int r) {
        /* This function takes as its argument a rank r and returns the index in the sparse array where the
        r-th present element appears.
         */
        if (r < 0) {
            throw new IllegalArgumentException("rank must be greater than 0");
        } else if (r > elements.size()) {
            return -1;
        }
        int pos = jacobsonRank.select1(r-1) + 1;
        return pos;
    }

    public int num_elem_at(int i) {
        /* This function returns the count of present elements (1s in the bit vector) up to and
        including index r. This is just rank on the bitvector, but it is inclusive rather than exclusive of index r
         */
        if (i < 0 || i >= bitVectorSize) {
            throw new IllegalArgumentException("i must be between 0 and size");
        }
        return jacobsonRank.rank1(i);

    }

    public int size() {
        return bitVectorSize;
    }
    public int num_elem() {
        return elements.size();
    }

    public void save() {
    }

    public void load(String fname) {

    }

}
