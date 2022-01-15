/* a client that sends GET request to aggregation server */
//import libraries for socket
import java.net.*;
import java.util.concurrent.TimeUnit;
import java.io.*;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;


public class GETClient {
    static Lamport timer = new Lamport();
    
    // return the XML part from the string
    public static String getXMLpart(String input) {
        Scanner scan = new Scanner(input);
        String output = "";
        boolean check = false;
        while (scan.hasNextLine()) {
            String aLine = scan.nextLine();

            // getting XML part
            if (aLine.startsWith("<?xml")) {
                check = true;
            } else if (aLine.startsWith("</feed>")) {
                output += "</feed>";
                check = false;
            }
            if ((check == true) && (aLine.length()!=0)){
                output += aLine + "\n";
            } 
        }
        scan.close();
        return output;
    }

    // Remove XML tags from message received from Aggregate Server
    public static void stripXML(String msg) {
        if (msg =="") {
            System.out.print("empty message");
        } else {
            try {
                // parse the XML 
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuild = docFactory.newDocumentBuilder();
                Document doc = docBuild.parse(new InputSource(new StringReader(msg)));
                doc.getDocumentElement().normalize();
                Element root = doc.getDocumentElement();
                NodeList nList = root.getChildNodes();
                for (int a = 0; a < nList.getLength(); a++) {
                    Node nNode = nList.item(a);
                    if ((nNode.getNodeType() == Node.ELEMENT_NODE) && (nNode.getNodeName() != "#text")) {
                        if (nNode.getNodeName() == "entry") {
                            System.out.println("entry");
                            NodeList entryList = nNode.getChildNodes();

                            // print out content of each tag
                            for (int b = 0; b < entryList.getLength(); b++) {
                                Node entryN = entryList.item(b);
                                if ((entryN.getNodeType() == Node.ELEMENT_NODE) && (entryN.getNodeName() != "#text")) {
                                    if (entryN.getNodeName() == "author") {
                                        System.out.print(entryN.getNodeName() + " : ");
                                        Node nameN = entryN.getChildNodes().item(1);
                                        System.out.println(nameN.getTextContent());
                                    } else {
                                        Element el = (Element) entryN;
                                        System.out.print(entryN.getNodeName() + " : ");
                                        System.out.println(el.getTextContent());
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
    }
    
    // parses the URL from a string. returns server name and port.
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
    
    // return true if the message string received is HTTP OK
    public static Boolean checkStat(String msg){
        boolean result = false;
        String[] Msg = msg.split("\n");
        if (Msg[0].contains("HTTP/1.1 200 OK")) {
            result = true;
        }
        return result;
    }

    // Sends request and returns received message from server after sending GET request. Takes in input and output streams
    public static String receivedMsg(DataInputStream in,DataOutputStream out) {
        String received = "failed";
        int t = timer.getTime();
        String str = Integer.toString(t);
        try {
            String getReq = "GET /atom.xml\n" + str + "\n";
            out.writeUTF(getReq);  
            received = in.readUTF();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return received;
    }

    public static void main(String[] args) throws IOException {
        String[] parsedUrl = parseURL(args[0]);
        String serverName = parsedUrl[0];
        int port = Integer.parseInt(parsedUrl[1]);
        Socket clientSocket = null;
        try {
            // try to reconnect 3 times if server is down
            for (int j = 0; j < 3; j++) {
                try {
                   clientSocket = new Socket(serverName,port); 
                   break;
                } catch (IOException e) {
                    // wait for 3 seconds before reconnect
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException IE) {
                        System.out.println("GET client interrupted");
                    }
                }
            }
        
            //output and input streams
            DataInputStream inStream = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream outStream = new DataOutputStream(clientSocket.getOutputStream());
            
            boolean checkStatus = false;

            // retry up to 4 times if no HTTP OK
            for (int i = 0; i < 4; i++) {
                String received = receivedMsg(inStream, outStream);

                //checks if msg received has HTTP ok. Else, break loop
                checkStatus = checkStat(received);

                String parsed = getXMLpart(received);
                stripXML(parsed);
                if (checkStatus==true) { break; }
            }
            
            clientSocket.close();
            inStream.close();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
}
