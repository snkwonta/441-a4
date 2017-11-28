//Sodienye Nkwonta
//ID: 30000197

import java.io.*;
import java.net.*;
import java.util.*;
import cpsc441.a4.shared.*;

/**
 * Router Class
 * 
 * This class implements the functionality of a router
 * when running the distance vector routing algorithm.
 * 
 * The operation of the router is as follows:
 * 1. send/receive HELLO message
 * 2. while (!QUIT)
 *      receive ROUTE messages
 *      update mincost/nexthop/etc
 * 3. Cleanup and return
 * 
 *      
 * @author 	Majid Ghaderi
 * @version	3.0
 *
 */
public class Router {
	
    int routerId;
	String serverName;
	int serverPort;
	int updateInterval;
	int[] linkcost;
	int[] nexthop;
	int[][] mincost;
	RtnTable table;
	Socket sock;
	ObjectInputStream dIn;
	ObjectOutputStream dOut;
	Timer timer;
	int numRouters;
	boolean localminCostVector = false;



	/**
     * Constructor to initialize the rouer instance 
     * 
     * @param routerId			Unique ID of the router starting at 0
     * @param serverName		Name of the host running the network server
     * @param serverPort		TCP port number of the network server
     * @param updateInterval	Time interval for sending routing updates to neighboring routers (in milli-seconds)
     */
	public Router(int routerId, String serverName, int serverPort, int updateInterval) {
		this.routerId = routerId;
		this.serverName = serverName;
		this.serverPort = serverPort;
		this.updateInterval = updateInterval;
		// to be completed
	}
	

    /**
     * starts the router 
     * 
     * @return The forwarding table of the router
     */
	public RtnTable start() {
		// to be completed
		try{
			// open TCP connection
			sock = new Socket(serverName, serverPort);
			dIn = new ObjectInputStream(sock.getInputStream());
			dOut = new ObjectOutputStream(sock.getOutputStream());
			
			//send, receive, and process HELLO
			DvrPacket dvr = new DvrPacket(this.routerId, DvrPacket.SERVER, DvrPacket.HELLO);
			dOut.writeObject(dvr);
			dOut.flush();
			
			//response
			DvrPacket serverResponse = (DvrPacket) dIn.readObject();
			
			numRouters = serverResponse.mincost.length;
			
			linkcost = serverResponse.mincost;
			mincost[routerId] = serverResponse.mincost;
//			mincost = new int[numRouters][numRouters];
//			mincost[routerId] = linkcost.clone();
			nexthop = new int[numRouters];
			
//			for(int i = 0 ; i < numRouters; i++) {
//				if(mincost[routerId][i] != 999) {
//					nexthop[i] = i;
//				}
//				else {
//					nexthop[i] = -1;
//				}
//				
//			}
			
			//start timer
			timer = new Timer(true);
			timer.schedule(new TimeoutHandler(this), updateInterval);
			DvrPacket packet;
			do{
				
				packet = (DvrPacket) dIn.readObject();
				processDvr(packet);
				
				//initialize neighbors/next
				
				
			} while(packet.type != DvrPacket.QUIT);
			
			//close socket and cancel timer
			timer.cancel();
			sock.close();
			
		}catch(IOException e){
			e.getMessage();
		}catch(ClassNotFoundException e){
			e.getMessage();
		}
		return new RtnTable(mincost[routerId], nexthop);
	}
	
	public void processDvr(DvrPacket dvr){
		if(dvr.sourceid == DvrPacket.SERVER){
			linkcost = dvr.mincost;
			mincost[dvr.sourceid] = dvr.mincost;
			
//			for(int i = 0 ; i < numRouters; i++) {
//				if(mincost[routerId][i] != 999) {
//					nexthop[i] = i;
//				}
//				else {
//					nexthop[i] = -1;
//				}
//				
//			}
		}else{
			
		}
		
		if(localminCostVector == true){
			
		} else {
			
		}
		
		//bellman ford algorithm
		
	}
	
	public void processTimeout() {
		// TODO Auto-generated method stub
		
	}

	
	
    /**
     * A simple test driver
     * 
     */
	public static void main(String[] args) {
		// default parameters
		int routerId = 0;
		String serverName = "localhost";
		int serverPort = 2227;
		int updateInterval = 1000; //milli-seconds
		
		if (args.length == 4) {
			routerId = Integer.parseInt(args[0]);
			serverName = args[1];
			serverPort = Integer.parseInt(args[2]);
			updateInterval = Integer.parseInt(args[3]);
		} else {
			System.out.println("incorrect usage, try again.");
			System.exit(0);
		}
			
		// print the parameters
		System.out.printf("starting Router #%d with parameters:\n", routerId);
		System.out.printf("Relay server host name: %s\n", serverName);
		System.out.printf("Relay server port number: %d\n", serverPort);
		System.out.printf("Routing update intwerval: %d (milli-seconds)\n", updateInterval);
		
		// start the router
		// the start() method blocks until the router receives a QUIT message
		Router router = new Router(routerId, serverName, serverPort, updateInterval);
		RtnTable rtn = router.start();
		System.out.println("Router terminated normally");
		
		// print the computed routing table
		System.out.println();
		System.out.println("Routing Table at Router #" + routerId);
		System.out.print(rtn.toString());
	}


}
