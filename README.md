Run tests:
- run ./test.sh

How AggregationServer works:
- The arguments it accepts are port and the duration of how long that the server runs before closing its server port.
    - eg java AggregationServer 4567 duration:20 
- For every socket it accepts, it goes into a thread. 
- A time is set to check any expired feeds for every 3 secs.
- The server has 2 hashmaps: 1 for clients and its timestamps and 1 for the clients and its feed.

How Content Server works:
- The arguments it accepts are the server:port, file input, the content server id and the number of heartbeats to send to ATOM server. 
    - java ContentServer localhost:4567 input.txt 1
- It sends each entry every 100ms. 

How GETClient works:
- The arguments are server:port.
    - eg java GETClient localhost:4567
- resends if it does not get a status 200 OK.
- It strips the XML tags and prints out the contents.

How testing works:
- The bash script compares the expected output and output. Any errors would be shown if there is a difference in output.