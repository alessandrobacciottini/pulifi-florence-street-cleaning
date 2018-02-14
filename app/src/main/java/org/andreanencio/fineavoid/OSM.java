package org.andreanencio.fineavoid;

import java.net.*;
import java.io.*;
import java.util.*;

public class OSM {

    public static String parseJSON(String json) {
        int resultStart = json.indexOf("<result");
        String result = json.substring(resultStart + 2);

        int angleStart = result.indexOf('>');
        int angleEnd = result.indexOf("</result>");
        String data = result.substring(angleStart, angleEnd);

        String[] split = data.split(",");
        return split[1];
    }

    public static ArrayList<String> queryLocalDB(String streetName, String dbPath) {

        ArrayList<String> matches = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(dbPath)));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.toLowerCase().contains(streetName.toLowerCase()))
                    matches.add(line);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return matches;
    }

    public static String getJSON(String url, int timeout) {

        HttpURLConnection c = null;
        try {
            URL u = new URL(url);
            c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.setRequestProperty("Content-length", "0");
            c.setUseCaches(false);
            c.setAllowUserInteraction(false);
            c.setConnectTimeout(timeout);
            c.setReadTimeout(timeout);
            c.connect();
            int status = c.getResponseCode();

            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null)
                        sb.append(line + "\n");
                    br.close();
                    return sb.toString();
            }
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (c != null) {
                try {
                    c.disconnect();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return null;
    }
}
