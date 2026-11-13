package com.veinminer.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON persistence DTO. Kept separate from the runtime domain model.
 */
public final class ConfigDocument {
    public static final int CURRENT_VERSION = 2;

    public int version = CURRENT_VERSION;
    public Map<String, Boolean> settings = new LinkedHashMap<>();
    public List<RuleDto> rules = new ArrayList<>();

    public static final class RuleDto {
        public String kind;
        public String id;
        public List<FacetDto> facets;
        public boolean enabled;
    }

    public static final class FacetDto {
        public String type;
        public String value;
    }
}
