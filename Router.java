//Sodienye Nkwonta
//ID: 30000197

import java.io.*;
import java.net.*;
import java.util.*;
import cpsc441.a4.shared.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
	int [] linkcost;
	int [] nexthop;
	int [][] mincost;
	boolean [] isNeighbor;
	RtnTable table;
	Socket sock;
	ObjectInputStream dIn;
	ObjectOutputStream dOut;
//	Timer timer1;
	ScheduledExecutorService timer;
	Future<?> start;
	int numOfRouters;
	



	/**
     * Constructor to initialize the router instance 
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
//		// to be completed
		try{
//			// open TCP connection
			sock = new Socket(serverName, serverPort);
			dIn = new ObjectInputStream(sock.getInputStream());
			dOut = new ObjectOutputStream(sock.getOutputStream());
//			
//			//send, receive, and process HELLO
			DvrPacket dvr = new DvrPacket(this.routerId, DvrPacket.SERVER, DvrPacket.HELLO);
			dOut.writeObject(dvr);
			dOut.flush();
//			
			//response
			DvrPacket serverResponse = (DvrPacket) dIn.readObject();
//			
			numOfRouters = serverResponse.mincost.length;
//			
			linkcost = new int[numOfRouters];
			linkcost = serverResponse.mincost;
			
			mincost = new int[numOfRouters][numOfRouters];
			mincost[routerId] = linkcost.clone();
			
			nexthop = new int[numOfRouters];
			
//			int i = 0;
			
			for(int i = 0; i<numOfRouters; i++){
				
				if(i==routerId){
					
					nexthop[i] = i;
					
				}else if(linkcost[i] != DvrPacket.INFINITY){
					
					nexthop[i] = i;
					
				}else{
					
					nexthop[i] = -1;
					
				}
				
			}
	
//			
//			for(int i = 0 ; i < numOfRouters; i++) {
//				if(mincost[routerId][i] != 999) {
//					nexthop[i] = i;
//				}
//				else {
//					nexthop[i] = -1;
//				}
//				
//			}
//			
			//start timer
			timer  = Executors.newScheduledThreadPool(1);
			start = timer.scheduleAtFixedRate(new TimeoutHandler(this), updateInterval, updateInterval, TimeUnit.MILLISECONDS);

			DvrPacket packet = new DvrPacket();
			do{
				
				//something wrong
//				packet = (DvrPacket) dIn.readObject();
//				processDvr(packet);
				
				try{
					packet = (DvrPacket) dIn.readObject();
					processDvr(packet);					
				}catch(Exception e){
					System.out.println(e.getMessage());
				}
				
//				//initialize neighbors/next
				
				
			} while(packet.type != DvrPacket.QUIT);
//			
//			//close socket and cancel timer

			dIn.close();
//			dOut.close();
			timer.shutdown();
			sock.close();
			
		}catch(IOException e){
			e.printStackTrace();
		}catch(ClassNotFoundException e){
			e.printStackTrace();
		}
		return new RtnTable(mincost[routerId], nexthop);
	}
	
	public void processDvr(DvrPacket dvr){
		boolean localminCostVector = false;
		if(dvr.sourceid == DvrPacket.SERVER){
				if(dvr.type == DvrPacket.HELLO){
					numOfRouters = dvr.mincost.length;
					linkcost = new int[numOfRouters];
					linkcost = dvr.mincost;
				
					mincost = new int[numOfRouters][numOfRouters];
					mincost[routerId] = dvr.mincost;
					
					isNeighbor = new boolean[numOfRouters];
					
				
//				nexthop = new int[numOfRouters];
			} else if(dvr.type == DvrPacket.ROUTE){
				linkcost = new int[numOfRouters];
				linkcost = dvr.mincost;
				
				mincost = new int[numOfRouters][numOfRouters];
				mincost[routerId] = dvr.mincost;
				
//				nexthop = new int[numOfRouters];
			} else if(dvr.type == DvrPacket.QUIT){
				//quit program
			}

		}else{
			mincost[dvr.sourceid] = dvr.mincost;
			
			if(!localminCostVector){
				//send local mincost vector to neighbors
				
				for(int i = 0; i < numOfRouters; i++){
					
					//come back to this
					if(i == routerId || linkcost[i] == DvrPacket.INFINITY)
						continue;
					
						DvrPacket pkttoNeighbors  = new DvrPacket(this.routerId, i, DvrPacket.ROUTE, mincost[routerId]);
						
						try{
							//something wrong
							dOut.writeObject(pkttoNeighbors);
							dOut.flush();
						}catch(IOException e){
							e.printStackTrace();
						}
						
					
				}
				//restart timer
				
				start.cancel(true);
				start = timer.scheduleAtFixedRate(new TimeoutHandler(this), updateInterval, updateInterval, TimeUnit.MILLISECONDS);
			} else {
				//do nothing
			}
		}
		
		
		//bellman ford algorithm
		
	}
	
	public void processTimeout() {
		// TODO Auto-generated method stub
		for(int i = 0; i < numOfRouters; i++){
			
			//come back to this
			if(i == routerId || linkcost[i] != DvrPacket.INFINITY){
				
			}else {

				try{
					DvrPacket pkttoNeighbors  = new DvrPacket(this.routerId, i, DvrPacket.ROUTE, mincost[routerId]);
					dOut.writeObject(pkttoNeighbors);
					dOut.flush();
				}catch(IOException e){
					e.getMessage();
				
				}
			}
				
			
		}
		
		
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
		System.out.printf("Routing update interval: %d (milli-seconds)\n", updateInterval);
		
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
