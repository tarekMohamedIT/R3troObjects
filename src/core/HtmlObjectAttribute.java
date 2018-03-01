package core;

/**
 * Created by tarek on 6/29/17.
 */
public class HtmlObjectAttribute {
    String name;
    String value;

    public HtmlObjectAttribute(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
