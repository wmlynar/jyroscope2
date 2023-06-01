package com.jyroscope.core.local.types;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import com.jyroscope.annotations.Message;
import com.jyroscope.base.local.types.IdentityTypeConverter;
import com.jyroscope.core.types.BeanType;
import com.jyroscope.types.ConversionException;
import com.jyroscope.core.types.MethodBuilder;
import com.jyroscope.core.types.SourceLoader;
import com.jyroscope.types.TypeConverter;
import com.jyroscope.core.types.TypeConverterBuilder;
import com.jyroscope.core.types.TypeConverterFactory;
import com.jyroscope.core.types.TypeConverterHelperCollection;

public class JavaTypeConverterFactory implements TypeConverterFactory {

	private static final Logger LOG = Logger.getLogger(JavaTypeConverterFactory.class.getCanonicalName());
	
    private static final String copyArray = "#1[] #t = #2; #= t";
    private static final String startArrayReadLoop = "#1[] #t = new #1[#2.length]; for (int i=0; i<#t.length; i++) { #= #t";
    private static final String getArrayLoop = "#1[i]";
    private static final String setArrayReadLoop = "#1[i] = #2;";
    private static final String closeArrayReadLoop = "} #= #1";
    
    private final TypeConverterHelperCollection<Class<?>,Class<?>> converters;
    
    public JavaTypeConverterFactory() {
        converters = new TypeConverterHelperCollection<>();
        converters.add(new JavaConvertPrimitive());
        converters.add(new JavaConvertString());
        converters.add(new JavaConvertArray());
    }
        
    @Override
    public <S,D> TypeConverter<S,D> get(Class<? extends S> from, Class<? extends D> to) throws ConversionException {
        // No need to convert between identical types
        if (to.isAssignableFrom(from))
            return (TypeConverter<S, D>)new IdentityTypeConverter<>();
        
        // Check names are the same
        Message fromMessage = from.getAnnotation(Message.class);
        Message toMessage = to.getAnnotation(Message.class);
        
        if (fromMessage != null 
            && toMessage != null 
            && fromMessage.value() != null 
            && !fromMessage.value().equals(toMessage.value()))
                throw new ConversionException("Cannot convert from " + from.getName() + " (" + fromMessage.value() + ") to " + to.getName() + " (" + toMessage.value() +"): the type names do not match");
        
        return (TypeConverter<S,D>)readJava(from, to);
    }
    
    public <S,D> TypeConverter<S,D> readJava(Class<? extends S> from, Class<? extends D> to) throws ConversionException {
        BeanType fromType = BeanType.load(from);
        BeanType toType = BeanType.load(to);
        TypeConverterBuilder typeConverterBuilder = new TypeConverterBuilder(fromType.getName(), toType.getName());
        String result = convertJava(typeConverterBuilder, null, fromType.getName(), typeConverterBuilder.getInput(), from, to);
        typeConverterBuilder.setOutput(result);
        String newClass = typeConverterBuilder.getName();
        String newSource = typeConverterBuilder.getSource();
        LOG.finest(newSource);
        return (TypeConverter<S, D>)SourceLoader.create(newClass, newSource);
    }
    
    private String convertJava(TypeConverterBuilder builder, MethodBuilder currentMethod, String currentContext, String source, Class<?> from, Class<?> to) throws ConversionException {
        if (from.equals(to))
            return source;
        
        String reader = converters.getReader(from, to);
        if (reader != null)
            return currentMethod.apply(reader, source);
        
        else if (from.isArray() && to.isArray()) {
            Class<?> fromElement = from.getComponentType();
            Class<?> toElement = from.getComponentType();
            String fromElementTypeName = fromElement.getCanonicalName();
            String toElementTypeName = toElement.getCanonicalName();
            if (toElementTypeName != null) {
                String arrayCopy = currentMethod.apply(copyArray, fromElementTypeName, source);
                String result = currentMethod.apply(startArrayReadLoop, toElementTypeName, arrayCopy);
                String loopVar = currentMethod.apply(getArrayLoop, arrayCopy);
                currentMethod.add(currentMethod.apply(setArrayReadLoop, loopVar, convertJava(builder, currentMethod, currentContext + "[]", loopVar, fromElement, toElement)));
                return currentMethod.apply(closeArrayReadLoop, result);
            }
        }
        
        else {
        
            BeanType fromType;
            BeanType toType;
            try {
                fromType = BeanType.load(from);
                toType = BeanType.load(to);
            } catch (ConversionException ce) {
                throw new ConversionException("No cast from " + from.getName() + " to " + to.getName() + " for " + currentContext);
            }
            
            MethodBuilder method = builder.createMethod(toType.getName(), fromType.getName());
            method.setResult(method.apply("new #1()", toType.getName()));
            for (String name : fromType.getProperties().keySet()) {
                BeanType.Property getter = fromType.getGetter(name);
                BeanType.Property setter = toType.getSetter(name);
                if (getter != null) {
                    if (setter != null) {
                        Class<?> sourceType = getter.getType();
                        Class<?> targetType = setter.getType();
                        String getterTemplate = createGetterTemplate(getter);
                        String setterTemplate = createSetterTemplate(setter);
                        
                        String input = method.apply(getterTemplate, method.getArg(0));
                        
                        String read = convertJava(builder, method, currentContext + "." + name, input, sourceType, targetType);
                        method.add(method.apply(setterTemplate, method.getResult(), read));
                    } else {
						LOG.warning("No setter for " + currentContext + "." + name + ", skipping");
                    }
                }
            }
            return method.getInvocation(source);
            
        }
        
        throw new ConversionException("No cast from " + from.getName() + " to " + to.getName() + " for " + currentContext);
        
    }
        
    public String createSetterTemplate(BeanType.Property setter) {
        Member member = setter.getMember();
        if (member instanceof Method) {
            return "#1." + member.getName() + "(#2);";
        } else if (member instanceof Field) {
            return "#1." + member.getName() + " = #2;";
        } else
            throw new IllegalArgumentException("Unrecognized member type: " + member.getClass());
    }
    
    public String createGetterTemplate(BeanType.Property getter) {
        Member member = getter.getMember();
        if (member instanceof Method) {
            return "#1." + member.getName() + "()";
        } else if (member instanceof Field) {
            return "#1." + member.getName();
        } else
            throw new IllegalArgumentException("Unrecognized member type: " + member.getClass());
    }
    
}
