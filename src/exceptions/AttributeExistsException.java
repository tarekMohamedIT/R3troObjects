package exceptions;

/**
 * Created by tarek on 6/29/17.
 */
public class AttributeExistsException extends RuntimeException {
    public AttributeExistsException() {
        super("This attribute already exists, use modifyAttribute() method to modify an existed attribute");
    }

    public AttributeExistsException(String s) {
        super(s);
    }

    public AttributeExistsException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public AttributeExistsException(Throwable throwable) {
        super("This attribute already exists, use modifyAttribute() method to modify an existed attribute", throwable);
    }

    public AttributeExistsException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
