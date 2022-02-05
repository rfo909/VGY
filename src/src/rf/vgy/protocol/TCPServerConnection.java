package rf.vgy.protocol;

import java.net.Socket;
import java.io.*;

public class TCPServerConnection {

	public static final int STATUS_READING_REQUEST = 0;
    public static final int STATUS_READING_REQUEST_FAILED = 1;
    public static final int STATUS_READY = 2;
    public static final int STATUS_PROCESSING = 3;  
    	// returned from TCPServer.getPendingRequest(). to block repeat returns (if there are more than one thread processing requests)
    public static final int STATUS_SENDING_RESPONSE = 4;
    public static final int STATUS_SENDING_RESPONSE_FAILED = 5;
    public static final int STATUS_COMPLETED = 6;
    
    private static final String[] STATUS_NAMES = {
    		"STATUS_READING_REQUEST",
    		"STATUS_READING_REQUEST_FAILED",
    		"STATUS_READY",
    		"STATUS_PROCESSING",
    		"STATUS_SENDING_RESPONSE",
    		"STATUS_SENDING_RESPONSE_FAILED",
    		"STATUS_COMPLETED",
    };
    
	private final long createTime;
	
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    
    private int status=STATUS_READING_REQUEST;
    private byte[] requestData;
    
    
    public TCPServerConnection (Socket socket) throws Exception {
    	this.createTime=System.currentTimeMillis();
        this.socket=socket;

        in=socket.getInputStream();
        out=socket.getOutputStream();
        
        new Thread(new DataReader()).start();
    }
    
    public long getCreateTime() {
    	return createTime;
    }
    
    public synchronized String getStatusString() {
    	return STATUS_NAMES[status];
    }
    
    public synchronized void setStatus(int st) {
    	this.status=st;
    }
    
    public synchronized boolean hasStatus (int st) {
    	return (this.status==st);
    }
    
    public synchronized void setRequestData (byte[] data) {
    	this.requestData = data;
    }
    
    public synchronized byte[] getRequestData () {
    	return this.requestData;
    }
    
    public void closeSocket() {
    	if (socket != null) try {
    		socket.close();
    	} catch (Exception ex) {
    		// ignore
    	}
    	socket=null;
    }
    
    public void sendResponse (byte[] data, int len) {
    	new Thread(new DataSender(data,len)).start();
    }
    
    class DataReader implements Runnable {
        public void run () {
        	setStatus(STATUS_READING_REQUEST);
        	try {
		    	socket.setSoTimeout(20000);
		    	int x=in.read();
		    	
		    	socket.setSoTimeout(20000);
		    	byte[] buf1=new byte[x];
		    	in.read(buf1);
		    	
		    	final int len=Integer.parseInt(new String(buf1,"ISO-8859-1"));
		    	byte data[]=new byte[len];
		    	int received=0;
		    	for(;;) {
		    		int count=in.read(data,received,len-received);
		    		if (count >= 0) {
		    			received += count;
		    			if (received==len) break;
		    		}
		    	}
		    	setRequestData(data);
		    	setStatus(STATUS_READY);
        	} catch (Exception ex) {
        		setStatus(STATUS_READING_REQUEST_FAILED);
        		closeSocket();
        	}
        }

    }
    
    class DataSender implements Runnable {
    	private byte[] data;
    	private int len;
    	public DataSender (byte[] data, int len) {
    		this.data=data;
    		this.len=len;
    	}
        public void run () {
        	setStatus(STATUS_SENDING_RESPONSE);
        	try {
    	        String sLen=""+len;
    	    	byte[] buf1=sLen.getBytes("ISO-8859-1");
    	    	out.write(buf1.length);
    	    	out.write(buf1);
    	    	out.write(data);
    	    	setStatus(STATUS_COMPLETED);
    	    } catch (Exception ex) {
    	    	setStatus(STATUS_SENDING_RESPONSE_FAILED);
        		closeSocket();
        	}
        }

    }
    

}