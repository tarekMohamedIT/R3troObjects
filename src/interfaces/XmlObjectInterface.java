package interfaces;

import abstraction.BaseTreeObject;

public interface XmlObjectInterface <T extends BaseTreeObject>
{
    String buildTagOpen();
    String buildTagClose();
    String buildTagOpenClose();
    String buildScript(int depthLevel);

}
