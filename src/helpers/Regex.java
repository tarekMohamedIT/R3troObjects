package helpers;

public class Regex {
    public static String REGEX_TAG_OPEN = "(<((\\w|\\d)+?)([\\S\\s]*?)(>))|(<!--)";
    public static String REGEX_TAG_OPEN_CLOSED = "(<)([\\S\\s]*?)(/>)";
    public static String REGEX_TAG_CLOSE = "</((\\w|\\d)+?)>";
    public static String REGEX_TAG_ALL = "((<!--)|(-->))|(<(/?)([A-Za-z0-9]+?)(\\s(([A-Za-z\\-]+(\\s?=\\s?((\"[\\S\\s]*?\")|('[\\S\\s]*?'))\\s*?))|[A-Za-z\\-]+?))*?((\\s*?)/?>))";
    public static String REGEX_TAG_OPEN_ONLY = "link|meta|br|hr|img|input";
    public static String REGEX_TAG_NON_VOID = "span|div|script";
}
