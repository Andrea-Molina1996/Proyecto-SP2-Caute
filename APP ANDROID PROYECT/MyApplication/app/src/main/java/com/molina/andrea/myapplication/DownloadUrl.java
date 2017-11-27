package com.molina.andrea.myapplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by amoli on 10/20/2017.
 */

public class DownloadUrl {
    public String readUrl(String myUrl) throws IOException{
        String data = "";
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(myUrl); // created the url
            urlConnection = (HttpURLConnection) url.openConnection(); // opened the connection
            urlConnection.connect(); // connected

            inputStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer sb = new StringBuffer();

            // read each line one by one with a for loop
            String line = "";
            while((line = br.readLine()) != null){
                sb.append(line); // appending it to the stringbuffer
            }

            data = sb.toString();
            br.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            inputStream.close();
            urlConnection.disconnect();
        }
        return data;    // this will be in json format
    }
}
