package com.veinminer.config.model;

import net.minecraft.resources.Identifier;

import java.util.Objects;

/**
 * Selects one concrete registry entry.
 */
public record ExactTargetSelector(TargetKind kind, Identifier id) implements TargetSelector {
    public ExactTargetSelector {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(id, "id");
    }

    @Override
    public boolean matches(ClassifiedTarget target) {
        return target.kind().equals(kind) && target.id().equals(id);
    }

    @Override
    public int specificity() {
        return Integer.MAX_VALUE;
    }

    @Override
    public String toString() {
        return kind.id() + ":" + id;
    }
}
