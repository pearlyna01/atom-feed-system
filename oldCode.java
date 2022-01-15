public class oldCode {
    
}
// A place to store old code for reference

// receving client without thread on Server side
/*
try  {
    clientSocket = serverSocket.accept();  
    
    // Receiving message
    InputStreamReader in = new InputStreamReader(clientSocket.getInputStream());
    BufferedReader reader = new BufferedReader(in);
    //String inputLine = reader.readLine();
    //System.out.println(inputLine);
    String inputLine;
    while ((inputLine = reader.readLine()) != null) {
        System.out.println(inputLine);
    }
    in.close();
    reader.close();
    System.out.println("the end");
    
    // Sending message
    PrintWriter clientReq = new PrintWriter(clientSocket.getOutputStream());
    clientReq.println("responding to client\n");  
    clientReq.flush();
} catch (IOException e) {
    System.out.println("Error trying to listen to port");
    System.out.println(e.getMessage());
}
serverSocket.close();
*/

//old code for GETClient
// try {
//     Socket clientSocket = new Socket(serverName,port);
    
//     //send to server
//     PrintWriter printW = new PrintWriter(clientSocket.getOutputStream());
//     printW.println("this is from client");
//     printW.flush();

//     //receive server response
//     InputStreamReader in = new InputStreamReader(clientSocket.getInputStream());
//     BufferedReader read = new BufferedReader(in);

//     String str = read.readLine();
//     System.out.println("From server: "+str);
//     clientSocket.close();
// } catch (Exception e) {
//     e.printStackTrace();
// }
