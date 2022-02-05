package rf.vgy.protocol;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class TCPServer {
    
    private int port;
    private ServerSocket serverSocket;
    
    private List<TCPServerConnection> connections=new ArrayList<TCPServerConnection>();
    
    private int rpcOk = 0;
    private int rpcFail = 0;
    
    
    /**
     * Called from TCPServerConnection
     */
    public synchronized void addConnection (TCPServerConnection x) {
    	connections.add(x);
    }
    
    public synchronized TCPServerConnection getPendingRequest() {
    	List<TCPServerConnection> toRemove=new ArrayList<TCPServerConnection>();

    	// register connections to be removed, and update stats
    	for (TCPServerConnection conn:connections) {
    		if (conn.hasStatus(TCPServerConnection.STATUS_READING_REQUEST_FAILED)) {
    			// not doing stats for these, just deleting
    			toRemove.add(conn);
    		} else if (conn.hasStatus(TCPServerConnection.STATUS_SENDING_RESPONSE_FAILED)) {
    			rpcFail++;
    			toRemove.add(conn);
    		} else if (conn.hasStatus(TCPServerConnection.STATUS_COMPLETED)) {
    			rpcOk++;
    			toRemove.add(conn);
    		} else if (conn.getCreateTime() < System.currentTimeMillis() - 3600000) {
    			// timed out
    			rpcFail++;
    			System.out.println("Got 1+ hour timed out connection - removing it: " + conn.getStatusString());
    			conn.closeSocket();
    			toRemove.add(conn);
    		}
    	}
    	
    	// remove obsolete connections
    	for (TCPServerConnection conn:toRemove) {
    		connections.remove(conn);
    	}
    	 	
    	
    	TCPServerConnection result=null;
    	
    	// locate connection that is ready
    	for (TCPServerConnection conn:connections) {
    		if (conn.hasStatus(TCPServerConnection.STATUS_READY)) {
    			conn.setStatus(TCPServerConnection.STATUS_PROCESSING);
    			result=conn;
    		}
    		System.out.println("created=" + conn.getCreateTime() + " status=" + conn.getStatusString());
    	}
    	
    	return result;
    	
    }
    
    public TCPServer (int port) throws Exception {
        this.port=port;
        serverSocket=new ServerSocket(port);
        TCPServerLoop serverLoop=new TCPServerLoop(serverSocket, this);
        (new Thread(serverLoop)).start();
    }
    
    
}