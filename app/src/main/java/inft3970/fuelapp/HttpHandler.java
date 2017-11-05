package inft3970.fuelapp;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Class: HTTPHandler
 * Author: Shane
 * Purpose: This class handles the conversion of data from Stream to String, for use in HTTP URLs
 * Creation Date: 09-Sep-17
 * Modification Date: 05-Nov-17
 */

public class HttpHandler {
    private static final String TAG = HttpHandler.class.getSimpleName();

    /**
     * Method: getServiceCall
     * Purpose: Makes the request for the HTTP stream, and handles the response. It takes in a String with the
     * URL, a string containing the connection type, an Array of Strings for the headers, and a String for the body of the request.
     * Returns: A string containing the response.
     */
    public String getServiceCall(String reqUrl, String conType, String[][] headers, String body) {
        String response = null;
        try {
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod(conType);
            if(headers != null) {
                for(String[] header:headers) {
                    conn.setRequestProperty(header[0], header[1]);
                }
            }
            if(body != null) {
                OutputStream out = conn.getOutputStream();
                out.write(body.getBytes("UTF-8"));
            }
            InputStream in = new BufferedInputStream(conn.getInputStream());
            response = convertStreamToString(in);
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
        return response;
    }

    /**
     * Method: convertStreamToString
     * Purpose: Converts an input stream to a string. It takes in an Input Stream from which it reads.
     * Returns: The converted string, read from the input stream.
     */
    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    /**
     * Method: getAuthServiceCall
     * Purpose: Makes the authorised request for the HTTP stream, and handles the response. It takes in a String with the
     * URL, a string containing the connection type, an Array of Strings for the headers, and a String for the body of the request.
     * Returns: A string containing the response.
     */
    public String getAuthServiceCall(String reqUrl, String conType, String[] headers, String body) {
        String response = null;
        try {
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod(conType);
            if(headers != null) {
                conn.setRequestProperty(headers[0], headers[1]);
            }

            if(body != null) {
                OutputStream out = conn.getOutputStream();
                out.write(body.getBytes("UTF-8"));
            }
            InputStream in = new BufferedInputStream(conn.getInputStream());
            response = convertStreamToString(in);
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
        return response;
    }
}
