package interfaces;

import core.objects.XmlObject;
import flags.RuleType;

public interface XmlTagRule <T extends RuleType> {
    T getRuleType();
    void execute(XmlObject currentObject);
}