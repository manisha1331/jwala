package com.siemens.cto.aem.domain.model.rule.group;

import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.rule.Rule;
import com.siemens.cto.aem.domain.model.rule.identifier.IdentifierRule;

public class GroupIdRule extends IdentifierRule<Group> implements Rule {

    public GroupIdRule(final Identifier<Group> theId) {
        super(theId,
              AemFaultType.GROUP_NOT_SPECIFIED,
              "Group Id was not specified");
    }
}
