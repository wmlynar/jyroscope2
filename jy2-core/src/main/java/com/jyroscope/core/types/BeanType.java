package com.jyroscope.core.types;

import com.jyroscope.annotations.*;
import com.jyroscope.types.ConversionException;

import java.lang.reflect.*;
import java.util.*;

public class BeanType {
    
    static final String GET_PREFIX = "get";
    static final String SET_PREFIX = "set";
    static final String IS_PREFIX = "is";
    
    public static enum MemberType {
        FIELD, GETTER, SETTER
    }
    
    public static class Property {
        private String name;
        private Class<?> type;
        private MemberType memberType;
        private Member member;

        public Property(String name, Class<?> type, MemberType memberType, Member member) {
            this.name = name;
            this.type = type;
            this.memberType = memberType;
            this.member = member;
        }

        public Class<?> getType() {
            return type;
        }

        public Member getMember() {
            return member;
        }
        
    }
    
    private static HashMap<Class<?>,BeanType> cache = new HashMap<>();
    
    public static BeanType load(Class<?> type) throws ConversionException {
        BeanType beanType = cache.get(type);
        if (beanType == null)
            cache.put(type, beanType = new BeanType(type));
        return beanType;

    }
        
    private Class<?> type;
    private String name;
    private Map<String,List<Property>> properties;
    private Map<String,String> remapped;
    private List<Property> transients;
    
    private BeanType(Class<?> type) throws ConversionException {
        this.type = type;
        this.properties = new HashMap<>();
        this.remapped = new HashMap<>();
        this.transients = new ArrayList<>();
        
        this.name = type.getCanonicalName();
        if (this.name == null)
            throw new ConversionException("Class " + type.getName() + " does not have a canonical name");
        
        try {
            type.getConstructor();
        } catch (NoSuchMethodException | SecurityException ex) {
            throw new ConversionException("Class " + type.getName() + " does not have a default constructor");
        }
        
        for (Field field : type.getFields()) {
            addProperty(new Property(field.getName(), field.getType(), MemberType.FIELD, field));
        }
        
        for (Method method : type.getMethods()) {
            // handle
            if (method != null && !Modifier.isStatic(method.getModifiers()) && !Object.class.equals(method.getDeclaringClass())) {
                String methodName = method.getName();
                Class<?>[] args = method.getParameterTypes();
                Class<?> returnType = method.getReturnType();
                
                if (args.length == 0 && !void.class.equals(returnType)) {
                    if (methodName.startsWith(GET_PREFIX) && methodName.length() > 3) {
                        String propertyName = Character.toLowerCase(methodName.charAt(GET_PREFIX.length())) + methodName.substring(GET_PREFIX.length() + 1);
                        addProperty(new Property(propertyName, returnType, MemberType.GETTER, method));
                    } else if (methodName.startsWith(IS_PREFIX) && methodName.length() > 2 && boolean.class.equals(returnType)) {
                        String propertyName = Character.toLowerCase(methodName.charAt(IS_PREFIX.length())) + methodName.substring(IS_PREFIX.length() + 1);
                        addProperty(new Property(propertyName, returnType, MemberType.GETTER, method));
                    }
                } else if (methodName.startsWith(SET_PREFIX) && methodName.length() > 3 && args.length == 1 && void.class.equals(returnType)) {
                    String propertyName = Character.toLowerCase(methodName.charAt(SET_PREFIX.length())) + methodName.substring(SET_PREFIX.length() + 1);
                    addProperty(new Property(propertyName, args[0], MemberType.SETTER, method));
                }
            }
        }

        // Remove the transients
        for (Property trans : transients)
            properties.remove(trans.name);
        // Do the remapping:
        HashMap<String,List<Property>> newProperties = new HashMap<>();
        // first, remove the old mappings
        for (Map.Entry<String,String> entry : remapped.entrySet()) {
            List<Property> property = properties.remove(entry.getKey());
            if (property != null)
                newProperties.put(entry.getValue(), property);
        }
        // then, add the new mappings
        for (Map.Entry<String,List<Property>> entry : newProperties.entrySet()) {
            if (properties.containsKey(entry.getKey()))
                throw new ConversionException("Name " + entry.getKey() + " set with @Name on " + type.getName() + " conflicts with existing property");
            else
                properties.put(entry.getKey(), entry.getValue());
        }
    }
    
    private void addProperty(Property property) {
        if (property.member instanceof AnnotatedElement) {
            AnnotatedElement annotated = (AnnotatedElement)property.member;
            Name propertyName = annotated.getAnnotation(Name.class);
            if (propertyName != null) {
                remapped.put(property.name, propertyName.value());
            }
            if (annotated.isAnnotationPresent(Hide.class))
                transients.add(property);
        }
        List<Property> props = properties.get(property.name);
        if (props == null)
            properties.put(property.name, props = new ArrayList<>());
        props.add(property);
    }
    
    private Property get(String property, MemberType memberType) {
        List<Property> props = properties.get(property);
        if (props == null)
            return null;
        for (Property prop : props)
            if (prop.memberType == memberType)
                return prop;
        return null;
    }
    
    public Property getGetter(String property) throws ConversionException {
        Property getter = get(property, MemberType.GETTER);
        Property field = get(property, MemberType.FIELD);
        if (getter == null && field == null)
            return null;
        if (getter != null && field != null)
            throw new ConversionException("Getter for property " + property + " is ambiguous: get method and a public field present");
        
        return getter == null ? field : getter;
    }
    
    public Property getSetter(String property) throws ConversionException {
        Property setter = get(property, MemberType.SETTER);
        Property field = get(property, MemberType.FIELD);
        if (setter == null && field == null)
            return null;
        if (setter != null && field != null)
            throw new ConversionException("Setter for property " + property + " is ambiguous: set method and public field present");
        
        return setter == null ? field : setter;    
    }    
    
    
    public Map<String,List<Property>> getProperties() {
        return properties;
    }
    
    public String getName() {
        return name;
    }
    
    

}
