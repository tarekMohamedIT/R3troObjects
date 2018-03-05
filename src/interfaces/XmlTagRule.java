package interfaces;

import core.XmlObject;
import flags.RuleAction;

public interface XmlTagRule {
    RuleAction execute(XmlObject currentObject);

}
