package helpers;

import core.HtmlObjectAttribute;
import sun.net.www.protocol.file.FileURLConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static helpers.Regex.*;

public class HelperMethods {
    /**
     * @param tag tag string
     * @return integer representing the state of the current tag
     * 1 => open-closed tag
     * 2 => closed tag
     * 0 => open tag
     */
    public static int getTagState(String tag) {
        if (tag.matches(".*\\s*/>$")) return 1;
        else if (tag.startsWith("</") || tag.equals("-->")) return 2;
        else return 0;
    }

    /**
     * Public method to download html web page from the internet
     *
     * @param urlString The website required for downloading
     * @return The html script of the website
     */
    public static String downloadWebPage(String urlString) {
        ExecutorService executors = Executors.newSingleThreadExecutor();
        try {

            Future<String> stringFuture = executors.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    HttpURLConnection httpURLConnection = null;
                    FileURLConnection fileURLConnection = null;
                    BufferedReader reader = null;
                    StringBuilder builder = new StringBuilder();

                    try {
                        URL url = new URL(urlString);
                        try {
                            httpURLConnection = (HttpURLConnection) url.openConnection();
                            httpURLConnection.setRequestProperty("user-agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:54.0) Gecko/20100101 Firefox/54.0");
                            reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                        } catch (ClassCastException e) {
                            fileURLConnection = (FileURLConnection) url.openConnection();
                            reader = new BufferedReader(new InputStreamReader(fileURLConnection.getInputStream()));
                        }
                        String line;

                        while ((line = reader.readLine()) != null) {
                            builder.append(line).append("\n");
                        }

                        if (httpURLConnection != null)
                            httpURLConnection.disconnect();

                        if (fileURLConnection != null)
                            fileURLConnection.close();

                        return builder.toString();

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (httpURLConnection != null) httpURLConnection.disconnect();
                    }

                    return "";
                }
            });
            executors.shutdown();
            String string = stringFuture.get();
            stringFuture.cancel(true);
            return string;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * Public method to get the html tag name out of html tag string
     *
     * @param htmlTag the html tag
     * @return the tag name
     */
    public static String getHtmlTagName(String htmlTag) {
        if (htmlTag.matches(REGEX_TAG_ALL)) {
            if (htmlTag.equals("<!--") || htmlTag.equals("-->")) return "comment";
            Matcher matcher = Pattern.compile("\\w(\\w|\\d)*").matcher(htmlTag);
            matcher.find();
            return matcher.group();
        }

        return "";
    }

    /**
     * Public method to return the html attributes out of html tag script
     *
     * @param htmlTag The html tag
     * @return an ArrayList containing all the html tag attributes
     */
    public static ArrayList<HtmlObjectAttribute> getHtmlTagAttributes(String htmlTag) {
        ArrayList<HtmlObjectAttribute> attributes = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\s[^\\s=<>/]+(\\s?=\\s?((\"[\\S\\s]*?\")|('[\\S\\s]*?')))?").matcher(htmlTag);
        while (matcher.find()) {
            String[] attr = matcher.group().split("=", 2);
            try {

                if (attr.length == 2 && attr[1].length() > 0 && !attr[1].equals("\"\""))
                    attributes.add(
                            new HtmlObjectAttribute(
                                    attr[0].replace(" ", "")
                                    , attr[1].substring(1, attr[1].length() - 1)
                            )
                    );

                else {

                    attributes.add(
                            new HtmlObjectAttribute(
                                    attr[0]
                                    , ""
                            )
                    );
                }
            } catch (StringIndexOutOfBoundsException e) {
                System.out.println(attr[1]);
                e.printStackTrace();
            }
        }

        return attributes;

    }
}
