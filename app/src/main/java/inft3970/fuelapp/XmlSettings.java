package inft3970.fuelapp;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


/**
 * Created by shane on 7/10/2017.
 */

public class XmlSettings {
    public  static final String TAG = XmlSettings.class.getSimpleName();

    Context context = App.getContext();

    public void writeXml(String[] settingData) {
        FileOutputStream fos;

        try {
            fos = context.openFileOutput("Settings.xml", Context.MODE_PRIVATE);

            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(fos, "UTF-8");
            serializer.startDocument(null, true);
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

            serializer.startTag(null, "Settings");
            serializer.startTag(null, "FuelType");
            serializer.text(settingData[0]);
            serializer.endTag(null, "FuelType");
            serializer.startTag(null, "Brands");
            serializer.text(settingData[1]);
            serializer.endTag(null, "Brands");
            serializer.endTag(null, "Settings");
            serializer.endDocument();
            serializer.flush();

            fos.close();

        } catch(FileNotFoundException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public ArrayList<String> readXml() {
        FileInputStream fis = null;
        InputStreamReader isr = null;
        String data = null;
        ArrayList<String> settingsArray = new ArrayList<>();

        try {
            fis = context.openFileInput("Settings.xml");
            isr = new InputStreamReader(fis);
            char[] inputBuffer = new char[fis.available()];
            isr.read(inputBuffer);
            data = new String(inputBuffer);
            isr.close();
            fis.close();

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(data));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_DOCUMENT) {
                    System.out.println("Start document");
                } else if (eventType == XmlPullParser.START_TAG) {
                    System.out.println("Start tag " + xpp.getName());
                } else if (eventType == XmlPullParser.END_TAG) {
                    System.out.println("End tag " + xpp.getName());
                } else if (eventType == XmlPullParser.TEXT) {
                    if(!xpp.getText().contains("\n")) {
                    settingsArray.add(xpp.getText());
                }
                }
                eventType = xpp.next();
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage());
        } catch(IOException e) {
            Log.e(TAG, e.getMessage());
        } catch(XmlPullParserException e) {
            Log.e(TAG, e.getMessage());
        }
        return settingsArray;
    }

}
