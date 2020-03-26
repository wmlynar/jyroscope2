package com.jyroscope.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.jy2.mapper.QdoxAnnotations;
import com.thoughtworks.qdox.model.JavaAnnotatedElement;
import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaConstructor;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaMember;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;

public class BeanType {
    
    static final String GET_PREFIX = "get";
    static final String SET_PREFIX = "set";
    static final String IS_PREFIX = "is";
    
    public static enum MemberType {
        FIELD, GETTER, SETTER
    }
    
    public static class Property {
        private String name;
		private JavaClass type;
        private MemberType memberType;
		private JavaMember member;

		public Property(String name, JavaClass type, MemberType memberType, JavaMember member) {
            this.name = name;
            this.type = type;
            this.memberType = memberType;
            this.member = member;
        }

		public JavaClass getType() {
            return type;
        }

		public JavaMember getMember() {
            return member;
        }
        
    }
    
	private static HashMap<JavaClass, BeanType> cache = new HashMap<>();

	public static BeanType load(JavaClass type) throws ConversionException {
		BeanType beanType = cache.get(type);
        if (beanType == null)
			cache.put(type, beanType = new BeanType(type));
        return beanType;
        
    }
        

	private JavaClass type;
    private String name;
    private Map<String,List<Property>> properties;
    private Map<String,String> remapped;
    private List<Property> transients;
    
	private BeanType(JavaClass type) throws ConversionException {
		this.type = type;
		this.properties = new HashMap<>();
		this.remapped = new HashMap<>();
		this.transients = new ArrayList<>();

		this.name = type.getCanonicalName();
		if (this.name == null)
			throw new ConversionException("Class " + type.getName() + " does not have a canonical name");

		JavaConstructor constructor = type.getConstructor(null);
		if (constructor == null && !type.getConstructors().isEmpty()) {
			throw new ConversionException("Class " + type.getName() + " does not have a default constructor");
		}

		for (JavaField field : type.getFields()) {
			addProperty(new Property(field.getName(), field.getType(), MemberType.FIELD, field));
		}

		for (JavaMethod method : type.getMethods()) {
			// handle
			if (method != null && !method.isStatic()
					&& !method.getDeclaringClass().getFullyQualifiedName().equals("java.lang.Object")) {
				String methodName = method.getName();
				List<JavaParameter> args = method.getParameters();
				JavaClass returnType = method.getReturns();

				if (args.size() == 0 && !returnType.getFullyQualifiedName().equals("void")) {
					if (methodName.startsWith(GET_PREFIX) && methodName.length() > 3) {
						String propertyName = Character.toLowerCase(methodName.charAt(GET_PREFIX.length()))
								+ methodName.substring(GET_PREFIX.length() + 1);
						addProperty(new Property(propertyName, returnType, MemberType.GETTER, method));
					} else if (methodName.startsWith(IS_PREFIX) && methodName.length() > 2
							&& returnType.getFullyQualifiedName().equals("boolean")) {
						String propertyName = Character.toLowerCase(methodName.charAt(IS_PREFIX.length()))
								+ methodName.substring(IS_PREFIX.length() + 1);
						addProperty(new Property(propertyName, returnType, MemberType.GETTER, method));
					}
				} else if (methodName.startsWith(SET_PREFIX) && methodName.length() > 3 && args.size() == 1
						&& returnType.getFullyQualifiedName().equals("void")) {
					String propertyName = Character.toLowerCase(methodName.charAt(SET_PREFIX.length()))
							+ methodName.substring(SET_PREFIX.length() + 1);
					addProperty(new Property(propertyName, args.get(0).getJavaClass(), MemberType.SETTER, method));
				}
			}
		}

		// Remove the transients
		for (Property trans : transients)
			properties.remove(trans.name);
		// Do the remapping:
		HashMap<String, List<Property>> newProperties = new HashMap<>();
		// first, remove the old mappings
		for (Map.Entry<String, String> entry : remapped.entrySet()) {
			List<Property> property = properties.remove(entry.getKey());
			if (property != null)
				newProperties.put(entry.getValue(), property);
		}
		// then, add the new mappings
		for (Map.Entry<String, List<Property>> entry : newProperties.entrySet()) {
			if (properties.containsKey(entry.getKey()))
				throw new ConversionException("Name " + entry.getKey() + " set with @Name on " + type.getName()
						+ " conflicts with existing property");
			else
				properties.put(entry.getKey(), entry.getValue());
		}
	}

    private void addProperty(Property property) {
		if (property.member instanceof JavaAnnotatedElement) {
			JavaAnnotatedElement annotated = (JavaAnnotatedElement) property.member;
			JavaAnnotation propertyName = null;
			JavaAnnotation hide = null;
			for (JavaAnnotation a : annotated.getAnnotations()) {
				if (a.getType().getFullyQualifiedName().equals(QdoxAnnotations.NAME)) {
					propertyName = a;
				}
				if (a.getType().getFullyQualifiedName().equals(QdoxAnnotations.HIDE)) {
					hide = a;
				}
			}
            if (propertyName != null) {
				String name = propertyName.getProperty("value").toString();
				name = name.substring(1, name.length() - 1);
				remapped.put(property.name, name);
            }
			if (hide != null)
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
