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
import java.io.Writer;
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
    private final String configPath = "config.xml";

    class Measurement {
        private String gpsData;
        private int radiation;
        private String dateTime;

        private String comment;

        public Measurement(String _gpsData, int _radiation, String _dateTime)
        {
            this.gpsData = _gpsData;
            this.radiation = _radiation;
            this.dateTime = _dateTime;
            this.comment = "";
        }

        public Measurement(String _gpsData, int _radiation, String _dateTime, String _comment)
        {
            this.gpsData = _gpsData;
            this.radiation = _radiation;
            this.dateTime = _dateTime;
            this.comment = _comment;
        }

        public String getGPS()
        {
            return this.gpsData;
        }

        public int getRadiation()
        {
            return this.radiation;
        }

        public String getDateTime()
        {
            return this.dateTime;
        }

        public String getComment() { return this.comment; }

    }

    class AppConfig {
        private String workingFilePath;
        private boolean addComments;
        public AppConfig(String _workingFilePath, String _addComments) {
            this.workingFilePath = _workingFilePath;
            if (_addComments.contains("true"))
            {
                this.addComments = true;
            } else {
                this.addComments = false;
            }
        }

        public void setWorkingFilePath(String _workingFilePath)
        {
            this.workingFilePath = _workingFilePath;
        }

        public void setAddComments(boolean _addComments)
        {
            this.addComments = _addComments;
        }

        public String getWorkingFilePath ()
        {
            return workingFilePath;
        }
        public boolean getAddComments()
        {
            return addComments;
        }
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

    protected AppConfig createAppConfig(String _workingFilePath, String _addComments)
    {
        return new AppConfig(_workingFilePath, _addComments);
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

        return createAppConfig(workingFile.getAttribute("path"), addComments.getAttribute("value"));
    }

    public List<Measurement> loadMeasurements()
    {
        AppConfig appConfig = loadAppConfig(configPath);
        String measurementsPath = appConfig.getWorkingFilePath();
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

        elements.add(header);

        for (Measurement meas : measurements)
        {
            List<String> row = new ArrayList<String>();
            row.add(meas.getGPS());
            row.add(Integer.toString(meas.getRadiation()));
            row.add(meas.getDateTime());
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
