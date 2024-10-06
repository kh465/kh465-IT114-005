package Module2; // Important: the package corresponds to the folder it resides in
import java.util.Arrays;

// usage
// compile: javac Module2/Problem1.java
// run: java Module2.Problem1

public class Problem1 {
    public static void main(String[] args) {
        //Don't edit anything here
        int[] a1 = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int[] a2 = new int[]{0, 1, 3, 5, 7, 9, 2, 4, 6, 8, 10};
        int[] a3 = new int[]{10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
        int[] a4 = new int[]{0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 10, 10};
        
        processArray(a1);
        processArray(a2);
        processArray(a3);
        processArray(a4);
    }
    static void processArray(int[] arr){
        System.out.println("Processing Array:" + Arrays.toString(arr));
        System.out.println("Odds output:");
        //hint: use the arr variable; don't diretly use the a1-a4 variables
        // Goal: output only add *values* of each passed array
        //TODO add/edit code here
        //kh465 September 21st, 2024
        for (int i = 0; i < arr.length; i++) //for loop of the length of arr
        {
            if (arr[i] % 2 == 1) //performing modulo on each index of arr. evens return 0, odds return 1. if 1 is returned, print it
                System.out.print(arr[i] + " "); //same as comment above. print it, add a space for clarity
        }
        //end add/edit section
        System.out.println();
        System.out.println("End process");
    }
    
}