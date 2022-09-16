package worksmobile.intern.apigateway.utils;

public class TypeChecker {

    public static boolean isInteger (String str) {
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isBoolean (String str) {
        return str.equals("true") || str.equals("false");
    }
}
