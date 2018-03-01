package exceptions;

/**
 * Created by tarek on 6/29/17.
 */
public class AttributeNotExsistedException extends RuntimeException {
    public AttributeNotExsistedException() {
        super("This attribute is not found, use addAttribute() method to add this attribute");
    }

    public AttributeNotExsistedException(String s) {
        super(s);
    }

    public AttributeNotExsistedException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public AttributeNotExsistedException(Throwable throwable) {
        super("This attribute is not found, use addAttribute() method to add this attribute", throwable);
    }

    public AttributeNotExsistedException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
