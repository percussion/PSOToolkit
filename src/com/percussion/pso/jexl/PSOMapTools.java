package com.percussion.pso.jexl;

import static java.util.Arrays.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;

public class PSOMapTools extends PSJexlUtilBase {

    @IPSJexlMethod(description = "creates a map from a default map with a custom map overlayed", params =
    {
          @IPSJexlParam(name = "defaultOptions", description = "the map to be overlayed"),
          @IPSJexlParam(name = "customOptions", description = "the map to overlay on top of the previous argument")
    }, returns = "an overlayed map")
    public Map<String,Object> overlay(Map<String,Object> defaultOptions, Map<String,Object> customOptions) {
        Map<String,Object> m = new HashMap<String, Object>();
        if (defaultOptions != null)
            m.putAll(defaultOptions);
        if (customOptions != null)
            m.putAll(customOptions);
        return m;
    }
    
    public Object get(Map<String,Object> m, String key, Object d) {
        Object rvalue = m.get(key);
        return rvalue == null ? d : rvalue;
    }
    
    public Object getFirstDefined(Map<String,Object> m, List<String> keys, Object d) {
        for(String k : keys) {
            Object rvalue = m.get(k);
            if (rvalue != null) return rvalue;
        }
        return d;
    }
    
    public Object getFirstDefined(Map<String,Object> m, String keys, Object d) {
        return getFirstDefined(m, asList(keys.split(",")), d);
    }
    
    
    
    @IPSJexlMethod(description = "creates a map from a list of keys and list of values", params =
    {
          @IPSJexlParam(name = "keys", description = "list of strings"),
          @IPSJexlParam(name = "values", description = "list of objects")
    }, returns = "map")
    public Map<String,Object> create(List<String> keys, List<? extends Object> values) {
        if (keys == null) throw new IllegalArgumentException("Keys cannot be null");
        if (values == null) throw new IllegalArgumentException("Values cannot be null");
        //if (keys.size() < values.size()) throw new IllegalArgumentException("There cannot be more keys then values");
        Map<String, Object> m = new HashMap<String, Object>();
        for(int i = 0; i < keys.size(); i++) {
            String k = keys.get(i);
            Object v = i >= values.size() ? values.get(values.size() - 1) : values.get(i); 
            m.put(k, v);
        }
        return m;
    }
    
    //@IPSJexlMethod(description = "creates a map from a default map with a custom map overlayed", params = {}, returns = "a new map.")
    public Map<String,Object> create() {
        return new HashMap<String, Object>();
    }
}
