package pl.edu.agh.gpsdosimeter;

import android.util.Log;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class FileManager {
    public final String configName = "config.xml";
    private final String defaultConfig = "<?xml version=\"1.0\" encoding=\"utf-8\"?><config><working_file path=\"test.xml\"/><add_comments value=\"true\"/><unsafe_level value=\"31.57\"/></config>";

    class AppConfig {
        private String workingFilePath = "";
        private boolean addComments = false;
        private float unsafeLevel = 31.57f;
        public AppConfig(String _workingFilePath, String _addComments, float _unsafeLevel) {
            this.workingFilePath = _workingFilePath;
            if (_addComments.contains("true"))
            {
                this.addComments = true;
            } else {
                this.addComments = false;
            }
            this.unsafeLevel = _unsafeLevel;
        }

        public void setWorkingFilePath(String _workingFilePath)
        {
            this.workingFilePath = _workingFilePath;
        }

        public void setAddComments(boolean _addComments)
        {
            this.addComments = _addComments;
        }

        public void setUnsafeLevel(String _unsafeLevel)
        {
            Log.d("Parse", _unsafeLevel);
            try {
                this.unsafeLevel = Float.parseFloat(_unsafeLevel);
            } catch (java.lang.NumberFormatException e)
            {
                this.unsafeLevel = 0.0f;
            }

        }

        public String getWorkingFileName()
        {
            return workingFilePath;
        }
        public boolean getAddComments()
        {
            return addComments;
        }
        public float getUnsafeLevel() {return unsafeLevel;}
        public void saveConfig(String path)
        {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = null;
            try {
                dBuilder = dbFactory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                throw new RuntimeException(e);
            }
            Document doc = dBuilder.newDocument();
            // root element
            Element rootElement = doc.createElement("config");
            doc.appendChild(rootElement);

            Element working_file = doc.createElement("working_file");
            Attr attrType = doc.createAttribute("path");
            attrType.setValue(workingFilePath);
            working_file.setAttributeNode(attrType);
            rootElement.appendChild(working_file);

            Element add_comments = doc.createElement("add_comments");
            Attr attrType2 = doc.createAttribute("value");
            attrType2.setValue(String.valueOf(this.addComments));
            add_comments.setAttributeNode(attrType2);
            rootElement.appendChild(add_comments);

            Element unsafe_Level = doc.createElement("unsafe_level");
            Attr attrType3 = doc.createAttribute("value");
            attrType3.setValue(String.valueOf(this.unsafeLevel));
            unsafe_Level.setAttributeNode(attrType3);
            rootElement.appendChild(unsafe_Level);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = null;
            try {
                transformer = transformerFactory.newTransformer();
            } catch (TransformerConfigurationException e) {
                throw new RuntimeException(e);
            }
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(path));
            try {
                transformer.transform(source, result);
            } catch (TransformerException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean createCleanConfig (String filepath)
    {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(filepath));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        try {
            writer.write(defaultConfig);
        } catch (IOException e) {
            try {
                writer.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
            return false;
        }

        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    protected AppConfig createAppConfig(String _workingFilePath, String _addComments, String _unsafeLevel)
    {
        return new AppConfig(_workingFilePath, _addComments, Float.parseFloat(_unsafeLevel));
    }
    protected AppConfig createAppConfig()
    {
        return new AppConfig("", "", 31.57f);
    }

    protected List<Measurement> parseMeasurements(String filepath)
    {
        Log.d("DEBUG", "Parsing measurements");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return null;
        }
        List<Measurement> measurements = new ArrayList<Measurement>();
        File measurementsFile = new File(filepath);
        Document measDoc;
        try {
            measDoc = builder.parse(measurementsFile);
        } catch (SAXException | IOException e) {
            e.printStackTrace();
            return null;
        }

        Element measRoot = measDoc.getDocumentElement();
        NodeList mNodes = measRoot.getElementsByTagName("measurement");
        for (int i = 0; i < mNodes.getLength(); i++)
        {
            measurements.add (new Measurement(((Element)mNodes.item(i)).getAttribute("gps"), Integer.valueOf(((Element)mNodes.item(i)).getAttribute("radiation")), ((Element)mNodes.item(i)).getAttribute("date"), ((Element)mNodes.item(i)).getAttribute("comment")));
        }
        return measurements;
    }

    AppConfig loadAppConfig(String _configPath)
    {
        File configFile = new File(_configPath);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return null;
        }

        Document config;
        try {
            config = builder.parse(configFile);
        } catch (SAXException | IOException e) {
            e.printStackTrace();
            return null;
        }
        Element confRoot = config.getDocumentElement();
        Element workingFile = (Element)confRoot.getElementsByTagName("working_file").item(0);
        Element addComments = (Element)confRoot.getElementsByTagName("add_comments").item(0);
        Element unsafeLevel = (Element)confRoot.getElementsByTagName("unsafe_level").item(0);

        return createAppConfig(workingFile.getAttribute("path"), addComments.getAttribute("value"), unsafeLevel.getAttribute("value"));
    }

    public List<Measurement> loadMeasurements(String parentPath)
    {
        String configPath = new File(parentPath, "config.xml").getAbsolutePath();
        AppConfig appConfig = loadAppConfig(configPath);
        String measurementsPath = new File (parentPath, appConfig.getWorkingFileName()).getAbsolutePath();
        return parseMeasurements(measurementsPath);
    }

    /* return 0 on success, other on error */
    public int exportCSV (List<Measurement> measurements, String filepath)
    {
        List<List<String>> elements = new ArrayList<List<String>>();

        List<String> header = new ArrayList<String>();
        header.add("gps");
        header.add("radiation");
        header.add("date");
        header.add("comment");

        elements.add(header);

        for (Measurement meas : measurements)
        {
            List<String> row = new ArrayList<String>();
            row.add(meas.getGPS());
            row.add(Integer.toString(meas.getRadiation()));
            row.add(meas.getDateTime());
            row.add(meas.getComment());
            elements.add(row);
        }

        CSVDoc csvDoc = new CSVDoc(elements);
        return csvDoc.saveToFile(filepath);
    }

    void createNewFile (String filepath) throws IOException {
        File file = new File(filepath);
        file.createNewFile();
    }

}
