package com.jyroscope.server.xmlrpc;

import java.io.*;
import java.net.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.w3c.dom.*;
import org.xml.sax.*;

public class XMLRPCClient {
    
    private static final int TIMEOUT_TIME = 5000;
    
    URI server;
    
    public XMLRPCClient(URI server) {
        this.server = server;
    }
    
    public Object call(MethodCall call) throws IOException, XMLRPCException {
        return call(call.getName(), call.getParams());
    }
    
    public Object call(String name, XMLRPCArray params) throws IOException, XMLRPCException {
        
        HttpURLConnection connection = (HttpURLConnection)server.toURL().openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "text/xml");
        connection.setConnectTimeout(TIMEOUT_TIME);
        
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document out = db.newDocument();
            DOMTranslator.writeRequest(name, params, out);
            
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(out);
            
            connection.connect();
            transformer.transform(source, new StreamResult(connection.getOutputStream()));
            
            
            Document in = db.parse(new InputSource(connection.getInputStream()));
            Element root = in.getDocumentElement();
            Object response = DOMTranslator.parseMethodResponse(root);
            
            if (response instanceof XMLRPCFault) {
                XMLRPCFault fault = (XMLRPCFault)response;
                throw new XMLRPCException(fault.toString());
            }
            
            return response;
        } catch (IOException ie) {
            throw new IOException("XML-RPC client cannot connect to " + server, ie);
        } catch (TransformerException te) {
            throw new IOException(te);
        } catch (SAXException se) {
            throw new IOException(se);
        } catch (XMLParseException pe) {
            throw new IOException(pe);
        } catch (ParserConfigurationException pce) {
            throw new IOException(pce);
        }
    }

}
