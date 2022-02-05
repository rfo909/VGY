package rf.vgy.protocol;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;


public class TCPClient  {
    
    private String addr;
    private int port;
    private InetAddress hostAddress;
 
    public TCPClient (String addr, int port) throws Exception {
        this.addr=addr;
        this.port=port;
        hostAddress=InetAddress.getByName(addr);
        
	}
    
    public byte[] doRPC(final byte[] request, final int requestLen) throws Exception {
    	Socket socket=null;
    	try {
	        socket = new Socket(hostAddress, port);
	        socket.setSoTimeout(20000);
	        InputStream in=socket.getInputStream();
	        OutputStream out=socket.getOutputStream();
	
	        String sLen=""+requestLen;
	    	byte[] buf1=sLen.getBytes("ISO-8859-1");
	    	out.write(buf1.length);
	    	out.write(buf1);
	    	out.write(request);
	    	
	    	socket.setSoTimeout(120000); // wait up to two minutes for a response to start coming
	    	int x=in.read();
	    	
	    	socket.setSoTimeout(20000);
	    	buf1=new byte[x];
	    	in.read(buf1);
	    	
	    	final int responseLen=Integer.parseInt(new String(buf1,"ISO-8859-1"));
	    	byte response[]=new byte[responseLen];
	    	int received=0;
	    	for(;;) {
	    		int count=in.read(response,received,responseLen-received);
	    		if (count >= 0) {
	    			received += count;
	    			if (received==responseLen) break;
	    		}
	    		if (count < 0) throw new Exception("read() failed");
	    	}
	    	return response;
    	
    	} finally {
    		if (socket != null) try {socket.close();} catch (Exception ex) {};
    	}
    }



    
}