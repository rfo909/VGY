package rf.vgy.protocol;

import java.io.*;


public class InputReaderThread implements Runnable {
    
    private InputStream in;
    private int bufSize;
    
    public InputReaderThread (InputStream in, int bufSize) {
    	this.in=in;
    	this.bufSize=bufSize;
    }
    
    
    private byte[] buf=new byte[64*1024];
    private int pos=0;

    public void run() {
        while (true) {
            try {
            	int count=in.read(buf,pos,buf.length-pos);
            	if (count>0) {
            		pos += count;
            	}
            } catch (Exception ex) {
                return;
            }
        }
    }
}