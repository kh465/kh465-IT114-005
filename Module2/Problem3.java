package Module2; // Important: the package corresponds to the folder it resides in
import java.util.Arrays;

// usage
// compile: javac Module2/Problem3.java
// run: java Module2.Problem3

public class Problem3 {
    public static void main(String[] args) {
        //Don't edit anything here
        Integer[] a1 = new Integer[]{-1, -2, -3, -4, -5, -6, -7, -8, -9, -10};
        Integer[] a2 = new Integer[]{-1, 1, -2, 2, 3, -3, -4, 5};
        Double[] a3 = new Double[]{-0.01, -0.0001, -.15};
        String[] a4 = new String[]{"-1", "2", "-3", "4", "-5", "5", "-6", "6", "-7", "7"};
        
        bePositive(a1);
        bePositive(a2);
        bePositive(a3);
        bePositive(a4);
    }
    // <T> turns this into a generic so it can take in any datatype, it'll be passed as an Object so casting is required
    static <T> void bePositive(T[] arr){
        System.out.println("Processing Array:" + Arrays.toString(arr));
        //your code should set the indexes of this array
        Object[] output = new Object[arr.length];
        //hint: use the arr variable; don't diretly use the a1-a4 variables
        //TODO convert each value to positive
        //kh465 September 21st, 2024
        for (int i = 0; i < arr.length; i++) //looping the entire length of arr to set output array to arr array values
            output[i] = arr[i]; //setting output array to the same values as arr array so nulls are overwritten
        for (int i = 0; i < arr.length; i++)
            if (arr.getClass().getSimpleName().equals("String[]")) //this gets the class type, its simplified name, and checks to see if it equals to "String[]".
            //if it does, then *in theory* it should have parsed the String output[i] to an int, gotten the absolute value of output[i] and set that value to output[i]
            //but this does not work for some reason.
            {
                //output[i] = Math.abs(Integer.parseInt(output[i])); //left the code that does not work, hopefully i can either figure it out or have it explained to me
            }
            else if (arr.getClass().getSimpleName().equals("Integer[]")) //this gets the class type, its simplified name, and checks to see if it equals to "Integer[]"
            //if it is, we enter the loop, get the absolute value of arr[i] (casted as an int) and set that value to output[i]
            {
                output[i] = Math.abs((int)arr[i]); //explained on line 38/39. this fails if not casted as an int, unsure why
            }
            else //if both checks above fail, assume the array arr is a double and execute this statement. get the absolute value of arr[i] (casted as a double) and set it
            //to output[i]. this would fail with any other array type, but for the purpose of this assignment i think this is okay.
            {
                output[i] = Math.abs((double)arr[i]); //explained on line 43/44. this also fails if not casted as a double, still unsure why
            }
        //set the result to the proper index of the output array and maintain the original data type
        //hint: don't forget to handle the data types properly, the result datatype should be the same as the original datatype
        
        //end edit section

        StringBuilder sb = new StringBuilder();
        for(Object i : output){
            if(sb.length() > 0){
                sb.append(",");
            }
            sb.append(String.format("%s (%s)", i, i.getClass().getSimpleName().substring(0,1)));
        }
        System.out.println("Result: " + sb.toString());
    }
}
