/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.umk.mat.imare.io;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 *
 * @author Bartek
 */
public abstract class Config {

    private static final String configFileName = "imareconfig.xml";
    private static Document cfgDoc;
    private static File cfgFile;
    private static Element paramTemplate;
    private static Element valueTemplate;

    public static void init() throws IOException {
        cfgFile = new File(configFileName);
        cfgDoc = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            boolean setDefaults = false;
            if (!cfgFile.exists()) {
                cfgFile.createNewFile();
                cfgDoc = db.newDocument();
                cfgDoc.setXmlVersion("1.0");
                cfgDoc.appendChild(cfgDoc.createElement("configuration"));
                Element params = cfgDoc.createElement("params");
                cfgDoc.getFirstChild().appendChild(params);
                setDefaults = true;
                save();
            } else {
                cfgDoc = db.parse(cfgFile);
            }
            // szablon dla parametru
            paramTemplate = cfgDoc.createElement("param");
            Element ptn = cfgDoc.createElement("name");
            ptn.setTextContent("Example name");
            paramTemplate.appendChild(ptn);
            Element ptvs = cfgDoc.createElement("values");
            valueTemplate = cfgDoc.createElement("value");
            valueTemplate.setTextContent("Example");
            ptvs.appendChild(valueTemplate);
            paramTemplate.appendChild(ptvs);
            if (setDefaults) {
                // tu wpisuj wartosci domyslne configa
                write("ShowStartupScreen", "1");
                write("RecentItemsNumber", "5");
                write("RecentAudioFiles", "(puste)");
                write("RecentProjects", "(puste)");
                save();
            }

        } catch (java.io.IOException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);

        } catch (Exception ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void reset() {
        File f = new File(configFileName);
        f.delete();
        try {
            init();
        } catch (IOException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static NodeList getValues(String param) {
        NodeList values;
        NodeList params = cfgDoc.getElementsByTagName("param");
        for (int i = 0; i < params.getLength(); i++) {
            if (params.item(i).getFirstChild().getTextContent().equals(param)) {
                values = params.item(i).getLastChild().getChildNodes();
                return values;
            }
        }
        return null;
    }

    private static Element getValuesNode(String param) {
        Element values;
        NodeList params = cfgDoc.getElementsByTagName("param");
        for (int i = 0; i < params.getLength(); i++) {
            if (params.item(i).getFirstChild().getTextContent().equals(param)) {
                values = (Element)params.item(i).getLastChild();
                return values;
            }
        }
        return null;
    }

    public static void load() throws IOException {
        try {
            cfgFile = new File(configFileName);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            cfgDoc = db.parse(cfgFile);
        } catch (Exception ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void save() {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            Source src = new DOMSource(cfgDoc);
            Result dst = new StreamResult(cfgFile);
            transformer.transform(src, dst);
        } catch (TransformerException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void write(String param, String value) {
        NodeList params = cfgDoc.getFirstChild().getFirstChild().getChildNodes();
        for (int i = 0; i < params.getLength(); i++) {
            if (params.item(i).getFirstChild().getTextContent().equals(param)) {
                params.item(i).getLastChild().getFirstChild().setTextContent(value);
                save();
                return;
            }
        }
        Element p = (Element)paramTemplate.cloneNode(true);
        p.getFirstChild().setTextContent(param);
        p.getLastChild().getFirstChild().setTextContent(value);
        cfgDoc.getFirstChild().getFirstChild().appendChild(p);
        save();
    }

    public static void write(String param, String[] values) {
        NodeList params = cfgDoc.getFirstChild().getFirstChild().getChildNodes();
        for (int i = 0; i < params.getLength(); i++) {
            if (params.item(i).getFirstChild().getTextContent().equals(param)) {
                Element p = (Element) params.item(i).getLastChild().getFirstChild().cloneNode(true);
                for (String value : values) {
                    Element pp = (Element) p.cloneNode(true);
                    pp.setTextContent(value);
                    params.item(i).getLastChild().appendChild(pp);
                }
                save();
                return;
            }
        }
        Element p = (Element)paramTemplate.cloneNode(true);
        p.getFirstChild().setTextContent(param);
        p.getLastChild().getFirstChild().setTextContent(values[0]);
        cfgDoc.getFirstChild().getFirstChild().appendChild(p);
        save();
    }

    public static void pushBack(String param, String value) {
        Element values = getValuesNode(param);
        if (values != null) {
            Element newValue = (Element)valueTemplate.cloneNode(true);
            newValue.setTextContent(value);
            values.insertBefore(newValue, null);
            save();
        }
    }

    public static String popBack(String param) {
        NodeList values = getValues(param);
        String ret = "";
        if (values != null) {
            ret = values.item(values.getLength() - 1).getTextContent();
            values.item(0).getParentNode().removeChild(values.item(values.getLength() - 1));
        }
        save();
        return ret;
    }

    public static void pushFront(String param, String value) {
        Element values = getValuesNode(param);
        if (values != null) {
            Element newValue = (Element)valueTemplate.cloneNode(true);
            Element before;
            newValue.setTextContent(value);
            NodeList valueList = getValues(param);
            if (valueList.getLength() == 0) {
                before = null;
            } else {
                before = (Element)valueList.item(0);
            }
            values.insertBefore(newValue, before);
        }
        save();
    }

    public static String popFront(String param) {
        NodeList values = getValues(param);
        String ret = "";
        if (values != null) {
            ret = values.item(0).getTextContent();
            values.item(0).getParentNode().removeChild(values.item(0));
        }
        save();
        return ret;
    }

    public static int getValueCount(String param) {
        NodeList values = getValues(param);
        if (values != null) {
            return values.getLength();
        }
        return -1;
    }

    public static String[] read(String param) {
        String[] ret = null;
        NodeList values = getValues(param);
        if (values != null) {
            ret = new String[values.getLength()];
            for (int j = 0; j < values.getLength(); j++) {
                ret[j] = values.item(j).getTextContent();
            }
            return ret;
        }
        ret = new String[1];
        ret[0] = "0";
        return ret;
    }

    public static void removeAt(String param, int pos) {
        NodeList values = getValues(param);
        if (pos >= 0 || pos < values.getLength()) {
            Element bye = (Element)values.item(pos);
            bye.getParentNode().removeChild(bye);
            save();
        }
    }

    public static void removeValue(String param, String value) {
        NodeList values = getValues(param);
        for (int i = 0; i < values.getLength(); i++) {
            if (values.item(i).getTextContent().equals(value)) {
                values.item(i).getParentNode().removeChild(values.item(i));
                save();
                return;
            }
        }
    }

}
