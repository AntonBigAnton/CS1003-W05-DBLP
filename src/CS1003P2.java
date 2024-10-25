import java.util.HashMap;
import java.util.Arrays;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.io.File;
import java.io.IOException;

public class CS1003P2 {

    HashMap<String, String> arguments = new HashMap<String, String>();
    String url = "https://dblp.org/search/@/api?format=xml&c=0&h=40&q=test";

    public static void main(String[] args) {
        CS1003P2 engine = new CS1003P2();
        engine.run(args);
    }

    public void run(String[] args) {
        if (checkValidArguments(args)) {
            // Update the agruments and url variables
            getArguments(args);
            getURL();

            // Get the search venue, and run the corresponding method
            String search = arguments.get("--search");
            if (search.equals("venue")) {
                getVenue();
            }
            else if (search.equals("publication")) {
                getPublication();
            }
            else {
                getAuthor();
            }
        }
    }

    public boolean checkValidArguments(String[] args) {
        int cacheIndex = -1; // Store the --cache index
        // CHECK 1: Check for missing keywords
        if (!checkMissingArguments(args)) {
            return false;
        }
        for (int i=0; i<args.length; i++) {
            // CHECK 2: Check for search keywords validity
            if (args[i].equals("--search")) {
                if (i+1 == args.length) {
                    System.out.println("Missing value for --search");
                    System.out.println("Malformed command line arguments.");
                    return false;
                }
                else if (!checkValidSearch(args[i+1])) {
                    return false;
                }
                // CHECK 3: Check for cache validity
                else if (cacheIndex != -1) {
                    if (!checkValidCache(args[cacheIndex+1])) {
                        return false;
                    }
                }
            }
            // CHECK 4: Check for query keywords validity
            else if (args[i].equals("--query")) {
                if (i+1 == args.length) {
                    System.out.println("Missing value for --query");
                    System.out.println("Malformed command line arguments.");
                    return false;
                }
                else if (!checkValidQuery(args[i+1])) {
                    return false;
                }
            }
            // CHECK 5: Check for cache path existence
            else if (args[i].equals("--cache")) {
                cacheIndex = i;
                if (i+1 == args.length) {
                    System.out.println("Missing value for --cache");
                    System.out.println("Malformed command line arguments.");
                    return false;
                }
            }
        }
        // CHECK 6: Check for too many command line arguments
        if (args.length > 6) {
            System.out.println("Too many command line arguments");
            return false;
        }
        return true;
    }

    // Check for missing command line arguments
    public boolean checkMissingArguments(String[] args) {
        if (!Arrays.toString(args).contains("--query")) {
            System.out.println("Missing keyword --query");
            System.out.println("Malformed command line arguments.");
            return false;
        }
        else if (!Arrays.toString(args).contains("--search")) {
            System.out.println("Missing keyword --search");
            System.out.println("Malformed command line arguments.");
            return false;
        }
        else if (!Arrays.toString(args).contains("--cache")) {
            System.out.println("Missing keyword --cache");
            System.out.println("Malformed command line arguments.");
            return false;
        }
        else {
            return true;
        }
    }

    // Methods to check the values of the command line keywords
    // Check for a valid search, a.k.a if the search value is either "author", "publication" or "venue"
    public boolean checkValidSearch(String search) {
        if (!(search.equals("author") || search.equals("publication") || search.equals("venue"))) {
            System.out.println("Invalid value for --search: " + search);
            System.out.println("Malformed command line arguments.");
            return false;
        }
        else {
            return true;
        }
    }

    // Check for a valid cache, a.k.a if the cache file exists AND if it's a directory
    public boolean checkValidCache(String path) {
        if (Files.notExists(Paths.get(path))) {
            System.out.println("Cache directory doesn't exist: " + path);
            return false;
        }
        else if (!Files.isDirectory(Paths.get(path))) {
            System.out.println("Cache directory doesn't exist: " + path);
            return false;
        }
        else {
            return true;
        }
    }

    // Check for a valid query, a.k.a if the query is an actual query (and not another keyword like --cache, --search and --query)
    public boolean checkValidQuery(String query) {
        if (query.equals("--search") || query.equals("--cache") || query.equals("--query")) {
            System.out.println("Invalid value for --query: " + query);
            System.out.println("Malformed command line arguments.");
            return false;
        }
        else {
            return true;
        }
    }

    // Update the arguments HashMap with the command line keywords/values
    public void getArguments(String[] args) {
        for (int i=0; i<args.length; i+=2) {
            arguments.put(args[i], args[i+1]);
        }
    }

    // Update the url according to the user query
    public void getURL() {
        // Step 1: replace any space character with a "+" in the user query
        String[] q = arguments.get("--query").split(" ");
        String query = "";
        for (String s : q) {
            query += s + "+";
        }
        query = query.substring(0, query.length()-1); // Remove the last "+" at the end of the query

        // Step 2: replace the "test" string in the url with the actual user query
        url = url.replaceAll("test", query);

        // Step 3: replace the "@" character with the search venue 
        String search = arguments.get("--search");
        if (search.equals("publication")) {
            url = url.replace("@", "publ");
        }
        else {
            url = url.replace("@", search);
        }
    }

    // Encode the inputted url by using the URLEncoder.encode method
    public String getEncodedURL(String url) {
        String encodedURL = "";
        try {
            encodedURL = URLEncoder.encode(url, StandardCharsets.UTF_8.toString());
        }
        catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
        return encodedURL;
    }

    // Get the inputted xml file saved in the cache
    public File getSavedResponse(String xml) {
        String encodedURL = getEncodedURL(xml); // The files in the cache are encoded, so we have to encode the xml's url first
        String path = arguments.get("--cache") + "/" + encodedURL;
        File f = new File(path);

        if (f.isFile() && !f.isDirectory()) {
            return f;
        }
        else {
            return null;
        }
    }

    // Save the inputted xml in the cache, using DOM and StreamResult
    public void saveResponse(String xml) {
        String encodedURL = getEncodedURL(xml); // Encode the xml's url
        String path = arguments.get("--cache") + "/" + encodedURL;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(xml);

            TransformerFactory transformerFactory =  TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result =  new StreamResult(new File(path));
            transformer.transform(source, result);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Methods to check for a saved response, call the API accordingly if necessary, print out the information we need, and save the call in the cache
    // Get and print the necessary information for venue searches (name of the venues)
    public void getVenue() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            File savedResponse = getSavedResponse(url); // Get the saved resonse file (or null if there isn't any saved response)
            Document doc;
            if (savedResponse == null) {
                doc = db.parse(url);
                saveResponse(url); // Save the API call if there is no previously saved call
            }
            else {
                doc = db.parse(savedResponse);
            }
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("info"); // Get the "info" node
            for (int i = 0; i<nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) node;
                    System.out.println(e.getElementsByTagName("venue").item(0).getTextContent()); // Print out the different venues
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Get and print the necessary information for publication searches (title of the publications and number of the authors)
    public void getPublication() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            File savedResponse = getSavedResponse(url); // Get the saved resonse file (or null if there isn't any saved response)
            Document doc;
            if (savedResponse == null) {
                doc = db.parse(url);
                saveResponse(url); // Save the API call if there is no previously saved call
            }
            else {
                doc = db.parse(savedResponse);
            }
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("info"); // Get the "info" node
            for (int i = 0; i<nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) node;
                    System.out.print(e.getElementsByTagName("title").item(0).getTextContent()); // Print out the publication's title...
                    System.out.println(" (number of authors: " + e.getElementsByTagName("author").getLength() + ")"); // ... and the number of authors
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Get and print the necessary information for author searches (name of the author, total number of publications and total number of co-authors)
    public void getAuthor() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            File savedResponse1 = getSavedResponse(url); // Get the saved resonse file (or null if there isn't any saved response)
            Document doc1; // Two different documents: doc1 for the API call for the author, and doc2 for the API call for the publications and co-authors
            if (savedResponse1 == null) {
                doc1 = db.parse(url);
                saveResponse(url); // Save the API call if there is no previously saved call
            }
            else {
                doc1 = db.parse(savedResponse1);
            }
            doc1.getDocumentElement().normalize();

            NodeList nodeList = doc1.getElementsByTagName("info");
            if (nodeList.getLength() != 0) { // Check for the number of publications
                for (int i = 0; i<nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element e = (Element) node;
                        System.out.print(e.getElementsByTagName("author").item(0).getTextContent());

                        String authorUrl = e.getElementsByTagName("url").item(0).getTextContent() + ".xml";
                        File savedResponse2 = getSavedResponse(authorUrl); // Get the saved resonse file (or null if there isn't any saved response)
                        Document doc2;
                        if (savedResponse2 == null) {
                            doc2 = db.parse(authorUrl);
                            saveResponse(authorUrl); // Save the API call if there is no previously saved call
                        }
                        else {
                            doc2 = db.parse(savedResponse2);
                        }
                        doc2.getDocumentElement().normalize();
                        System.out.print(" - " + doc2.getElementsByTagName("r").getLength() + " publications");
                        System.out.println(" with " + doc2.getElementsByTagName("co").getLength() + " co-authors.");
                    }
                }
            }
            else {
                System.out.println(arguments.get("--query") + " - 0 publications with 0 co-authors.");
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
