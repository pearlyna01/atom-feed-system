/* a client that sends PUT request to aggregation server */
//import libraries for socket
import java.io.*;
import java.net.*;
import java.util.Scanner; // Import the Scanner class to read text files
import java.util.ArrayList;

//import libraries for XML parser
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

// class to parse a feed item
class AFeed {
    String id="";
    String subtitle="";
    String title="";
    String link="";
    String updated="";
    String author="";
    String summary="";

    // set id
    public void setId(String n){
        this.id = n;
    }
    // set subtitle
    public void setSub(String n){
        this.subtitle = n;
    }
    // set title
    public void setTitle(String n){
        this.title = n;
    }
    // set link
    public void setLink(String n){
        this.link = n;
    }
    // set updated 
    public void setUpdate(String n){
        this.updated = n;
    }
    // set author
    public void setAuth(String n){
        this.author = n;
    }
    // set summary
    public void setSum(String n){
        this.summary = n;
    } 
}

public class ContentServer {
    static Lamport localClock = new Lamport();
    static int noOfFeeds = 0;

    // check if feed/entry has title/link/id
    public static Boolean checkFeed(AFeed feed) {
        boolean check = true;
        if (feed.id=="") { check = false; } 
        if (feed.title=="") { check = false; } 
        if (feed.link=="") { check = false; } 
        return check;
    }
    
    // returns list of elements. Eg. "title":(element containing the title)
    public static ArrayList<Element> elements(AFeed feed,Document aDoc) {
        ArrayList<Element> arr = new ArrayList<Element>();
        
        try {
            // creating title tag
            if (feed.title != "") {
                Element title = aDoc.createElement("title");
                title.appendChild(aDoc.createTextNode(feed.title));
                arr.add(title);
            }

            // creating subtitle tag
            if (feed.subtitle != "") {
                Element subtitle = aDoc.createElement("subtitle");
                subtitle.appendChild(aDoc.createTextNode(feed.subtitle));
                arr.add(subtitle);
            }

            // creating link tag
            if (feed.link != "") {
                Element link = aDoc.createElement("link");
                link.appendChild(aDoc.createTextNode(feed.link));
                arr.add(link);
            }

            // creating updated tag
            if (feed.updated != "") {
                Element updated = aDoc.createElement("updated");
                updated.appendChild(aDoc.createTextNode(feed.updated));
                arr.add(updated);
            }

            // creating author tag
            if (feed.author != "") {
                Element author = aDoc.createElement("author");
                Element name = aDoc.createElement("name");
                author.appendChild(name);
                name.appendChild(aDoc.createTextNode(feed.author));
                arr.add(author);
            }

            // creating id tag
            if (feed.id != "") {
                Element id = aDoc.createElement("id");
                id.appendChild(aDoc.createTextNode(feed.id));
                arr.add(id);
            }
            
            // creaating summary tag
            if (feed.summary != "") {
                Element summary = aDoc.createElement("summary");
                summary.appendChild(aDoc.createTextNode(feed.summary));
                arr.add(summary);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arr;
    }

    // returns the xml file format(as String) of the input. Parses the input string
    public static String getXMLfeed(AFeed feed,Boolean isEntry) {
        String result="";
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuild = docFactory.newDocumentBuilder();
            Document aDoc = docBuild.newDocument();

            // root element <feed>
            Element rootEl = aDoc.createElement("feed");
            aDoc.appendChild(rootEl);

            // setting attributes of feed
            Attr attr = aDoc.createAttribute("xmlns");
            attr.setValue("http://www.w3.org/2005/Atom");
            rootEl.setAttributeNode(attr);
            attr = aDoc.createAttribute("xml:lang");
            attr.setValue("en-US");
            rootEl.setAttributeNode(attr);

            ArrayList<Element> list = elements(feed, aDoc);
            
            //  if it is an entry, add the entry tag and append it to root element
            if (isEntry == true) {
                Element entry = aDoc.createElement("entry");
                for (Element element : list) {
                    entry.appendChild(element);
                }
                rootEl.appendChild(entry);
            // else, append it to root element
            } else {
                for (Element element : list) {
                    rootEl.appendChild(element);
                }
            }

            // transform the XML content into a string 
            TransformerFactory transf = TransformerFactory.newInstance();
            Transformer transformer = transf.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(aDoc);
            StreamResult resultt = new StreamResult(new StringWriter());
            transformer.transform(source, resultt);
            result = resultt.getWriter().toString(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // create PUT message
    public static String PUT_msg(String msg,String clientID) {
        localClock.increT();
        int time = localClock.getTime();
        String t = Integer.toString(time);
        int len = msg.length();
        String result = "PUT /atom.xml HTTP/1.1\r\n"+
                        "User-agent: ATOMClient/1/0\r\n" +
                        "Content-Type: text/xml\r\n" +
                        "Content-Length: " + len + "\r\n" + 
                        "clock:" + t + "\r\n" +
                        "clientID:" + clientID + "\r\n\r\n" +
                        msg;
        return result;
    }

    // parses URL to get server name and port
    public static String[] parseURL(String input) {
        String[] result = {"null","null"};

        //add 'http://' if input don't have it
        if (!input.contains("http://")) {
            input = "http://" + input;
        }

        // extract servername and port number
        try {
            URL aURL = new URL(input);
            result[0] = aURL.getHost();
            result[1] = String.valueOf(aURL.getPort());
        } catch (Exception e) {
            System.err.println("Unable to parse URL");
            e.printStackTrace();
        }
        return result;
    }

    // return text file as a ArrayList of String. Each String is an entry.
    public static ArrayList<String> readFile(String file) {
        ArrayList<String> result = new ArrayList<String>();
        String Line ="";
        try {
            File textInput = new File(file);
            Scanner scanFile = new Scanner(textInput);
            
            while (scanFile.hasNextLine()) {
                String data = scanFile.nextLine();
                if (data.startsWith("entry")) {
                    result.add(Line);
                    Line = "";
                } else {
                    Line += data + "\n";
                }
            }
            result.add(Line); 
            scanFile.close();
        } catch (FileNotFoundException e) {
            System.out.println("Read file error");
            e.printStackTrace();
        }
        return result;  
    }
    
    // parse each entry as a feed object
    public static AFeed parseAFeed(String input) {
        AFeed feed = new AFeed();
        Scanner scan = new Scanner(input);

        // set the values of the feed object based on what the line starts with
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            if (line.startsWith("title:")) {
                String substr = line.substring(6);
                feed.setTitle(substr);
            } else if (line.startsWith("subtitle:")) {
                String substr = line.substring(9);
                feed.setSub(substr);
            } else if (line.startsWith("link:")) {
                String substr = line.substring(5);
                feed.setLink(substr);
            } else if (line.startsWith("updated:")) {
                String substr = line.substring(8);
                feed.setUpdate(substr);
            } else if (line.startsWith("author:")) {
                String substr = line.substring(7);
                feed.setAuth(substr);
            } else if (line.startsWith("id:")) {
                String substr = line.substring(3);
                feed.setId(substr);
            } else if (line.contains("summary:")) {
                String substr = line.substring(8);
                feed.setSum(substr);
            } 
        }
        scan.close();
        return feed;
    }

    // sends each feed at a time
    public static void sendPUTmsg(String server, int port,String feed,boolean isEntry,String clientID) {
        Socket clientSocket = null;
        // try to reconnect 3 times if server is down
        for (int j = 0; j < 3; j++) {
            try {
                clientSocket = new Socket(server,port); 
            } catch (IOException e) {
                // wait for 5 milliseconds before reconnect
                try {
                    Thread.sleep(10);
                } catch (InterruptedException IE) {
                    System.out.println("GET client interrupted");
                }
            }
            if (clientSocket != null) {
                break;
            }
        }
        
        if (clientSocket != null) {
            try {        
                //output and input streams
                DataInputStream inStream = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream outStream = new DataOutputStream(clientSocket.getOutputStream());
                
                // write PUT message to server
                String str = PUT_msg(feed,clientID);
                outStream.writeUTF(str);  

                // receive response from server 
                String received = inStream.readUTF();
                System.out.println(received);

                clientSocket.close();
                System.out.println("content server socket closed.");
                inStream.close();
                outStream.close();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Unable to send to Aggregation server");
            }  
        }
    }

    // send a notification to the Aggregation server that it is alive
    public static void sendIsAlive(String server,int port,String clientID) {
        try {
            Socket clientSocket = new Socket(server,port);
        
            //output and input streams
            DataInputStream inStream = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream outStream = new DataOutputStream(clientSocket.getOutputStream());
            
            // write PUT message to server
            String str = "isAlive:" + "clientID:"+ clientID;
            outStream.writeUTF(str);  

            // receive response from server 
            String received = inStream.readUTF();
            if (received.contains("clock:")){
                int i = str.indexOf("clock:");
                String n = str.substring(i+6);
                int newI = Integer.parseInt(n);
                localClock.maxC(newI);
            }
            System.out.println(received);

            clientSocket.close();
            System.out.println("content server socket closed.");
            inStream.close();
            outStream.close();
        } catch (Exception e) {
            System.out.println("Unable to send message that content server is alive");
        }
    }

    // load the bad XML file for testing and return it as a string
    public static String getBadXML() {
        String result ="";
        try {
            File textInput = new File("badXML.txt");
            Scanner scanFile = new Scanner(textInput);
            
            while (scanFile.hasNextLine()) {
                String data = scanFile.nextLine();
                result += data + "\n";
            }
            scanFile.close();
        } catch (FileNotFoundException e) {
            System.out.println("Read file error");
            e.printStackTrace();
        }
        return result;  
    }

    public static void main(String args[]) throws IOException {
        // parse the input file into an array of AFeed objects
        String file = args[1];
        ArrayList<String> input = readFile(file);
        noOfFeeds = input.size();
        AFeed[] feedList = new AFeed[noOfFeeds];
        int i = 0;
        for (String line : input) {
            AFeed aFeed = parseAFeed(line);
            feedList[i] = aFeed;
            i++; 
        }

        // get server, port number and file location from arguments
        String[] parsedUrl = parseURL(args[0]);
        String serverName = parsedUrl[0];
        int port = Integer.parseInt(parsedUrl[1]);
        String CSid = args[2];
        
        // test malform XML by injecting string 
        if (args.length == 5) {
            if (args[4].contains("testM")) {
                String str = getBadXML();
                sendPUTmsg(serverName, port, str, true, CSid);
                return;
            }
        }

        // send PUT message every 100 millisecs
        for (int j = 0; j < noOfFeeds; j++) {
            boolean isEntry = true;
            String str = getXMLfeed(feedList[j], isEntry);
            sendPUTmsg(serverName, port, str, isEntry, CSid);
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }

        // send heartbeats to the Aggregation Server every 6 secs
        if (args.length == 4) {
            int noOfBeats = Integer.parseInt(args[3]);
            for (int q = 0; q < noOfBeats; q++) {
                sendIsAlive(serverName, port, CSid);
                try {
                    Thread.sleep(6000);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }
}

