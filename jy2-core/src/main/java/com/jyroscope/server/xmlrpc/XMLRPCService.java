package com.jyroscope.server.xmlrpc;

import java.io.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import com.jyroscope.server.http.*;



public class XMLRPCService implements HTTPService {
    
    private API api;
    
    public XMLRPCService(API api, boolean includeSystemAPI) {
        if (includeSystemAPI)
            this.api = new SystemAPI(api);
        else
            this.api = api;
    }
    
    @Override
    public void process(HTTPAction action) throws HTTPException {
        try {
            // Parse Input
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            
            // TODO This probably should be rethought so as to not go InputStream->StringBuffer->String->StringReader->InputSource
            Document doc = db.parse(new InputSource(new StringReader(action.getRequestBody())));
            Element root = doc.getDocumentElement();
            MethodCall call = DOMTranslator.parseMethodCallElement(root);
            
            // Process Method Call
            Method method = api.getMethod(call.getName());
            if (method == null)
                throw new XMLRPCException("Unsupported operation: " + call.getName());
            Object result = method.process(call.getParams());
            
            // Build Result
            Document out = db.newDocument();
            DOMTranslator.writeResponse(result, out);
            
            
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(out);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            transformer.transform(source, new StreamResult(baos));
            
            action.setContentType("text/xml");
            action.setResponseBody(baos.toByteArray());
            
        } catch (TransformerException te) {
            throw new HTTPException(500, "Internal Server Error", te);
        } catch (XMLRPCException xre) {
            throw new HTTPException(500, "Internal Server Error", xre);
        } catch (XMLParseException xrpe) {
            throw new HTTPException(400, "Bad Request", xrpe);
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
            throw new HTTPException(500, "Internal Server Error", pce);
        } catch (SAXException se) {
            throw new HTTPException(400, "Bad Request", se);
        } catch (IOException ioe) {
            throw new HTTPException(500, "Internal Server Error", ioe);
		} catch (TransformerFactoryConfigurationError | FactoryConfigurationError | Exception e) {
			throw new HTTPException(500, "Internal Server Error", e);
		}
    }

}
