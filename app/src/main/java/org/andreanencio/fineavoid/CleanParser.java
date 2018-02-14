package org.andreanencio.fineavoid;

import android.content.Context;
import java.io.*;
import java.util.*;

public final class CleanParser {

    private CleanParser() {
        throw new RuntimeException();
    }

    public static String parse(Context context, String streetName) {
        try {
            InputStream dbInput = context.getResources().openRawResource(R.raw.clean);
            String dbText = inputStream2String(dbInput);
            String[] lines = getLines(dbText);
            ArrayList<String> matches = new ArrayList<String>();
            String lastWord = streetName.substring(streetName.lastIndexOf(" ") + 1);

            for (String line : lines) {
                if (line.toLowerCase().contains(lastWord.trim().toLowerCase()))
                    matches.add(line);
            }

            if (matches.size() > 0)
                return matches.get(0);
            else
                return null;
        } catch (Exception ex) {
            return null;
        }
    }

    public static String[] getLines(String content) {
        String[] lines = content.split("\n");
        return lines;
    }

    public static String inputStream2String(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
