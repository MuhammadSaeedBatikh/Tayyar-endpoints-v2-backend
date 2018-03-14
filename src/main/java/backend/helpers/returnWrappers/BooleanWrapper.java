package backend.helpers.returnWrappers;

/**
 * Created by Muhammad Saeed on 3/13/2017.
 */
public class BooleanWrapper {
    public boolean booleanValue;
    public int flag;
    public String message = "";

    public BooleanWrapper(boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public BooleanWrapper(boolean booleanValue, String message) {
        this.booleanValue = booleanValue;
        this.message = message;
    }

    public BooleanWrapper(boolean booleanValue, int flag, String message) {
        this.booleanValue = booleanValue;
        this.flag = flag;
        this.message = message;
    }

    @Override
    public String toString() {
        return "BooleanWrapper{" +
                "booleanValue=" + booleanValue +
                ", flag=" + flag +
                ", message='" + message + '\'' +
                '}';
    }
}
