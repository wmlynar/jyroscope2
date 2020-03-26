package com.jyroscope.server.xmlrpc;

import java.text.*;
import java.util.*;

import javax.xml.bind.*;

import org.w3c.dom.*;
import org.w3c.dom.Element;

public class DOMTranslator {
    
    public static MethodCall parseMethodCallElement(Element root) throws XMLParseException {
        if (!"methodCall".equals(root.getNodeName()))
            throw new XMLParseException("<" + root.getNodeName() + "> encountered but <methodCall> expected", root);
        
        MethodCall methodCall = new MethodCall(getChild(root, "methodName", true).getTextContent());
        Element params = getChild(root, "params", false);
        if (params != null) {
            for (Element param : getChildren(params, "param")) {
                Element value = getChild(param, "value", true);
                methodCall.addParam(parseValueElement(value));
            }
        }
        return methodCall;
    }
    
    public static Object parseMethodResponse(Element root) throws XMLParseException {
        if (!"methodResponse".equals(root.getNodeName()))
            throw new XMLParseException("<" + root.getNodeName() + "> encountered but <methodResponse> expected", root);

        Element fault = getChild(root, "fault", false);
        if (fault != null) {
            Element value = getChild(fault, "value", true);
            Object struct = parseValueElement(value);
            if (struct instanceof XMLRPCStruct) {
                XMLRPCStruct map = (XMLRPCStruct)struct;
                if (map.containsKey("faultCode") && (map.get("faultCode") instanceof Integer) && map.containsKey("faultString") && (map.get("faultString") instanceof String))
                    return new XMLRPCFault((Integer)map.get("faultCode"), (String)map.get("faultString"));
                else
                    throw new XMLParseException("Invalid format for returned <fault>", fault);
            } else
                throw new XMLParseException("Invalid format for returned <fault>", fault);
        } else {
            Element params = getChild(root, "params", true);
            Element param = getChild(params, "param", true);
            Element value = getChild(param, "value", true);
            return parseValueElement(value);
        }
    }
    
    public static Object parseValueElement(Element value) throws XMLParseException {
        Element child = getChild(value, null, false);
        String type = "string";
        String content;
        if (child == null)
            content = value.getTextContent();
        else {
            content = child.getTextContent();
            type = child.getNodeName();
        }
        
        if ("i4".equals(type) || "int".equals(type)) {
            try {
                return Integer.parseInt(content);
            } catch (NumberFormatException nfe) {
                throw new XMLParseException("Bad number format: "+ content, value);
            }
            
        } else if ("boolean".equals(type)) {
            try {
                return Integer.parseInt(content) != 0;
            } catch (NumberFormatException nfe) {
                throw new XMLParseException("Bad boolean format: "+ content, value);
            }
            
        } else if ("string".equals(type)) {
            return content;
            
        } else if ("double".equals(type)) {
            try {
                return Double.parseDouble(content);
            } catch (NumberFormatException nfe) {
                throw new XMLParseException("Bad number format: "+ content, value);
            }
            
        } else if ("dateTime.iso8601".equals(type)) {
            try {
                SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss");
                return df.parse(content);
            } catch (ParseException pe) {
                throw new XMLParseException("Bad date format: "+ content, value);
            }
            
        } else if ("base64".equals(type)) {
            try {
                return DatatypeConverter.parseBase64Binary(content);
            } catch (IllegalArgumentException iae) {
                throw new XMLParseException("Bad base64 format: "+ content, value);
            }
            
        } else if ("struct".equals(type)) {
            XMLRPCStruct struct = new XMLRPCStruct();
            for (Element member : getChildren(child, "member")) {
                String name = getChild(member, "name", true).getTextContent();
                Element structItem = getChild(member, "value", true);
                struct.put(name, parseValueElement(structItem));
            }
            return struct;
            
        } else if ("array".equals(type)) {
            Element data = getChild(child, "data", true);
            XMLRPCArray array = new XMLRPCArray();
            for (Element arrayItem : getChildren(data, "value"))
                array.add(parseValueElement(arrayItem));
            return array;
            
        } else {
            throw new XMLParseException("Unrecognized datatype " + type, value);
        }
    }
    
    private static Iterable<Element> getChildren(Element element, String name) {
        ArrayList<Element> children = new ArrayList<Element>();
        NodeList nl = element.getChildNodes();
        for (int i=0; i<nl.getLength(); i++) {
            Node child = nl.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element)child;
                if (name.equals(childElement.getNodeName())) {
                    children.add(childElement);
                }
            }
        }
        return children;
    }
    
    private static Element getChild(Element element, String name, boolean required) throws XMLParseException {
        Element result = null;
        NodeList nl = element.getChildNodes();
        for (int i=0; i<nl.getLength(); i++) {
            Node child = nl.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element)child;
                if (name == null || name.equals(childElement.getNodeName())) {
                    if (result == null)
                        result = childElement;
                    else
                        throw new XMLParseException("Too many <" + name + ">s", element);
                }
            }
        }
        if (result == null && required)
            throw new XMLParseException("Missing <" + name + ">s", element);
        
        return result;
    }
    
    private static Element createChild(String name, Document doc, Node target) {
        Element child = doc.createElement(name);
        target.appendChild(child);
        return child;
    }
    
    private static void createChild(String name, String text, Document doc, Node target) {
        Element child = doc.createElement(name);
        target.appendChild(child);
        child.appendChild(doc.createTextNode(text));
    }
    
    public static void writeResponse(Object message, Document doc) throws XMLRPCException {
        Element param = createChild("param", doc, createChild("params", doc, createChild("methodResponse", doc, doc)));
        writeValue(message, doc, param);
    }
    
    public static void writeRequest(String method, XMLRPCArray parameters, Document doc) throws XMLRPCException {
        Element methodCall = createChild("methodCall", doc, doc);
        createChild("methodName", method, doc, methodCall);
        Element params = createChild("params", doc, methodCall);
        for (Object value : parameters) {
            Element param = createChild("param", doc, params);
            writeValue(value, doc, param);
        }
    }
    
    public static void writeValue(Object value, Document doc, Element target) throws XMLRPCException {
        Element container = doc.createElement("value");
        target.appendChild(container);
        if (value instanceof String)
            createChild("string", (String)value, doc, container);
        else if (value instanceof Integer)
            createChild("int", String.valueOf(value), doc, container);
        else if (value instanceof Boolean)
            createChild("boolean", ((Boolean)value).booleanValue() ? "1" : "0", doc, container);
        else if (value instanceof Double) {
            createChild("double", String.valueOf(value), doc, container);
        } else if (value instanceof Date) {
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss");
            createChild("dateTime.iso8601", df.format((Date)value), doc, container);
        } else if (value instanceof byte[]) {
            // TODO implement base64 tag
            throw new XMLRPCException("Base64 responses are not yet supported");
        } else if (value instanceof Collection) {
            Element arrayData = createChild("data", doc, createChild("array", doc, container));
            for (Object item : ((Collection<?>)value))
                writeValue(item, doc, arrayData);
            if (!arrayData.hasChildNodes())
                arrayData.setTextContent("\r\n");
        } else if (value instanceof Map) {
            Element struct = createChild("struct", doc, container);
            for (Map.Entry<?,?> entry : ((Map<?,?>)value).entrySet()) {
                Element member = createChild("member", doc, struct);
                createChild("name", String.valueOf(entry.getKey()), doc, member);
                writeValue(entry.getValue(), doc, member);
            }
            if (!struct.hasChildNodes())
                struct.setTextContent("\r\n");
        } else {
            throw new XMLRPCException("Could not generate result for type " + (value == null ? "null" : value.getClass().toString()));
        }
    }

}
