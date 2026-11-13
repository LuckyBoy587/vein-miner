package com.veinminer.classification;

import com.veinminer.config.model.FacetType;
import com.veinminer.config.model.TargetKind;
import com.veinminer.config.model.TargetSelector;
import com.veinminer.config.model.TargetSelectors;

import java.util.List;
import java.util.Objects;

/**
 * A family of tools sharing a tool type, for example all pickaxes.
 */
public record ToolFamily(String id, String displayName, List<ToolEntry> entries) {
    public ToolFamily {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(displayName, "displayName");
        entries = List.copyOf(entries);
    }

    /**
     * Broad selector matching every tool in this family.
     */
    public TargetSelector selector() {
        return TargetSelectors.of(TargetKind.TOOL)
                .facet(FacetType.TOOL_TYPE, id)
                .build();
    }
}
