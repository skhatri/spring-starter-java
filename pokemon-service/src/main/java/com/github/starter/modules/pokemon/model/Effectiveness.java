package com.github.starter.modules.pokemon.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Effectiveness {
    private final String typeName;
    private final List<String> noEffect;             
    private final List<String> doubleResistant;     
    private final List<String> notVeryEffective;    
    private final List<String> neutral;             
    private final List<String> effective;           
    private final List<String> superEffective;      

    public Effectiveness(String typeName, List<String> noEffect, List<String> doubleResistant, List<String> notVeryEffective, 
                        List<String> neutral, List<String> effective, List<String> superEffective) {
        this.typeName = typeName;
        this.noEffect = noEffect != null ? Collections.unmodifiableList(List.copyOf(noEffect)) : Collections.emptyList();
        this.doubleResistant = doubleResistant != null ? Collections.unmodifiableList(List.copyOf(doubleResistant)) : Collections.emptyList();
        this.notVeryEffective = notVeryEffective != null ? Collections.unmodifiableList(List.copyOf(notVeryEffective)) : Collections.emptyList();
        this.neutral = neutral != null ? Collections.unmodifiableList(List.copyOf(neutral)) : Collections.emptyList();
        this.effective = effective != null ? Collections.unmodifiableList(List.copyOf(effective)) : Collections.emptyList();
        this.superEffective = superEffective != null ? Collections.unmodifiableList(List.copyOf(superEffective)) : Collections.emptyList();
    }

    public static Effectiveness fromReadable(io.r2dbc.spi.Readable kv) {
        String typeName = (String) kv.get("type_name");
        
        List<String> noEffect = parsePostgresArray(kv.get("m_0"));
        List<String> doubleResistant = parsePostgresArray(kv.get("m_025"));
        List<String> notVeryEffective = parsePostgresArray(kv.get("m_05"));
        List<String> neutral = parsePostgresArray(kv.get("m_1"));
        List<String> effective = parsePostgresArray(kv.get("m_2"));
        List<String> superEffective = parsePostgresArray(kv.get("m_4"));
        
        return new Effectiveness(typeName, noEffect, doubleResistant, notVeryEffective, neutral, effective, superEffective);
    }
    
    private static List<String> parsePostgresArray(Object arrayValue) {
        if (arrayValue == null) {
            return Collections.emptyList();
        }
        
        if (arrayValue instanceof String arrayStr) {
            String cleanStr = arrayStr.substring(1, arrayStr.length() - 1);
            if (cleanStr.isEmpty()) {
                return Collections.emptyList();
            }
            return Collections.unmodifiableList(Arrays.asList(cleanStr.split(",")));
        }
        
        if (arrayValue instanceof String[] array) {
            return Collections.unmodifiableList(Arrays.asList(array));
        }
        
        if (arrayValue instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) arrayValue;
            return Collections.unmodifiableList(list);
        }
        
        throw new IllegalArgumentException("Unsupported array type: " + arrayValue.getClass());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Effectiveness that)) return false;
        return Objects.equals(typeName, that.typeName) &&
                Objects.equals(noEffect, that.noEffect) &&
                Objects.equals(doubleResistant, that.doubleResistant) &&
                Objects.equals(notVeryEffective, that.notVeryEffective) &&
                Objects.equals(neutral, that.neutral) &&
                Objects.equals(effective, that.effective) &&
                Objects.equals(superEffective, that.superEffective);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeName, noEffect, doubleResistant, notVeryEffective, neutral, effective, superEffective);
    }

    public String getTypeName() {
        return typeName;
    }

    public List<String> getNoEffect() {
        return Collections.unmodifiableList(noEffect);
    }

    public List<String> getDoubleResistant() {
        return Collections.unmodifiableList(doubleResistant);
    }

    public List<String> getNotVeryEffective() {
        return Collections.unmodifiableList(notVeryEffective);
    }

    public List<String> getNeutral() {
        return Collections.unmodifiableList(neutral);
    }

    public List<String> getEffective() {
        return Collections.unmodifiableList(effective);
    }

    public List<String> getSuperEffective() {
        return Collections.unmodifiableList(superEffective);
    }
} 