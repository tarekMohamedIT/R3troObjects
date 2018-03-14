package interfaces;

public interface XmlObjectInterface
{
    String buildTagOpen();
    String buildTagClose();
    String buildTagOpenClose();
    String buildScript(int depthLevel);

}
