package wjtoth.cyclicstablematching;

/**
 * Created by justin on 02/08/16.
 */
public class PermutationArray {
    int[] array;

    public PermutationArray(int[] array) {
        this.array = array;
    }

    @Override
    public boolean equals(Object obj) {
        if(getClass() != obj.getClass()) {
            return false;
        }
        PermutationArray permutationArray = (PermutationArray)obj;
        int[] objArray = permutationArray.getArray();
        if(array.length != objArray.length) {
            return false;
        }
        for(int i = 0; i<array.length; ++i) {
            if(array[i] != objArray[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
       int result = 1;
        for(int i = 0; i<array.length; ++i) {
            result = 37*result + array[i];
        }
        return result;
    }

    public int[] getArray() {
        return array;
    }
}
