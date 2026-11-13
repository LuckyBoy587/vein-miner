package com.veinminer.config.model;

import java.util.Locale;
import java.util.Objects;

/**
 * Broad category of a configuration target.
 *
 * <p>This is intentionally an open value type rather than an enum. New entity
 * categories can be introduced by adding a constant (or by constructing a new
 * kind from an addon) without changing the configuration store or selectors.</p>
 */
public record TargetKind(String id) {
    public static final TargetKind TOOL = new TargetKind("tool");
    public static final TargetKind BLOCK = new TargetKind("block");
    public static final TargetKind ITEM = new TargetKind("item");
    public static final TargetKind ENTITY = new TargetKind("entity");

    public TargetKind {
        Objects.requireNonNull(id, "id");
        id = id.trim().toLowerCase(Locale.ROOT);
        if (id.isEmpty()) {
            throw new IllegalArgumentException("Target kind id must not be empty");
        }
    }

    @Override
    public String toString() {
        return id;
    }
}
