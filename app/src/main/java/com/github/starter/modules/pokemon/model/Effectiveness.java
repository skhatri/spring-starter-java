package com.github.starter.modules.pokemon.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Effectiveness {
    private final String typeName;
    private final List<String> noEffect;             // m0 - 0x damage (no effect)
    private final List<String> doubleResistant;     // m025 - 0.25x damage (double resistance)
    private final List<String> notVeryEffective;    // m05 - 0.5x damage (not very effective)
    private final List<String> neutral;             // m1 - 1x damage (neutral)
    private final List<String> effective;           // m2 - 2x damage (super effective)
    private final List<String> superEffective;      // m4 - 4x damage (double super effective)

    public Effectiveness(String typeName, List<String> noEffect, List<String> doubleResistant, List<String> notVeryEffective, 
                        List<String> neutral, List<String> effective, List<String> superEffective) {
        this.typeName = typeName;
        this.noEffect = noEffect;
        this.doubleResistant = doubleResistant;
        this.notVeryEffective = notVeryEffective;
        this.neutral = neutral;
        this.effective = effective;
        this.superEffective = superEffective;
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
        
        
        if (arrayValue instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) arrayValue;
            return list;
        }
        
        if (arrayValue instanceof String) {
            String arrayStr = (String) arrayValue;
            
            if (arrayStr.equals("{}") || arrayStr.isEmpty()) {                
                return Collections.emptyList();
            }
            
            if (arrayStr.startsWith("{") && arrayStr.endsWith("}")) {
                String content = arrayStr.substring(1, arrayStr.length() - 1);
                
                if (content.isEmpty()) {
                    return Collections.emptyList();
                }
                
                String[] elements = content.split(",");
                return new ArrayList<>(Arrays.asList(elements));
            }
        }
        
        // Handle arrays directly
        if (arrayValue.getClass().isArray()) {
            Object[] array = (Object[]) arrayValue;
            List<String> result = new ArrayList<>(array.length);
            for (Object item : array) {
                if (item != null) {
                    result.add(item.toString());
                }
            }
            return result;
        }
        
        return Collections.emptyList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Effectiveness)) return false;
        Effectiveness that = (Effectiveness) o;
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
        return noEffect;
    }

    public List<String> getDoubleResistant() {
        return doubleResistant;
    }

    public List<String> getNotVeryEffective() {
        return notVeryEffective;
    }

    public List<String> getNeutral() {
        return neutral;
    }

    public List<String> getEffective() {
        return effective;
    }

    public List<String> getSuperEffective() {
        return superEffective;
    }
} 