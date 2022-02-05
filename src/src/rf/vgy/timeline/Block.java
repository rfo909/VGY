package rf.vgy.timeline;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

public class Block {
	
	private final long blockId;
	private final String name;
		
	private final File dataFile, indexFile1, indexFile2;
	
	private final RandomAccessFile raf;
	
	
	private HashMap<String,Long> index=new HashMap<String,Long>();
	

	//public static final int BLOCK_FULL_BYTES = 2^30;
	//public static final int BLOCK_FULL_INDEX_SIZE = 2^20;
	
	public static final int BLOCK_FULL_BYTES = 1024*1024*1024;
	public static final int BLOCK_FULL_INDEX_SIZE = 10000;
	
	
	public Block(File dir, long blockId, String name) throws Exception {
		this.blockId = blockId;
		this.name = name;
		
		this.dataFile=new File(dir + File.separator + Timeline.getDataFilePrefix(name) + blockId);
		this.indexFile1=new File(dir + File.separator + Timeline.getIndexFile1Prefix(name) + blockId);
		this.indexFile2=new File(dir + File.separator + Timeline.getIndexFile2Prefix(name) + blockId);

		this.raf=new RandomAccessFile(dataFile,"rws"); // synchronous
		
		if (dataFile.exists()) readIndex();
	}
	
	public synchronized boolean blockIsFull() throws Exception {
		return raf.length() > BLOCK_FULL_BYTES || index.size() > BLOCK_FULL_INDEX_SIZE;
	}
	
	private synchronized void readIndex() throws Exception {
		if (!indexFile1.exists() || !indexFile2.exists()) return;
		
		File source;
		if (indexFile1.length() != indexFile2.length()) {
			// partial sync 
			if (indexFile1.length() > indexFile2.length()) {
				source=indexFile2;  // use the smallest, which is guaranteed to be consistent
			} else {
				source=indexFile1;
			}
		} else {
			source=indexFile1;
		}
		BufferedReader br=null;
		try {
			br=new BufferedReader(new FileReader(source));
			for (;;) {
				String line=br.readLine();
				if (line==null) break;
				int pos=line.indexOf(":");
				String key=line.substring(0,pos);
				
				line=line.substring(pos+1);
				pos=line.indexOf(":");
				long timestamp=Long.parseLong(line.substring(0,pos));
				long offset=Long.parseLong(line.substring(pos+1));
				index.put(key, offset);
			}
		} finally {
			if (br != null) try {br.close();} catch (Exception ex) {};
		}
	}

	/**
	 * TODO: harden code
	 */
	public synchronized void add (String key, byte[] data, int dataLength) throws Exception {
		if (key.indexOf('\n') >= 0 || key.indexOf('\r') >= 0 || key.indexOf(':') >= 0) throw new Exception("Invalid key, newline and colon not allowed: " + key);
		
		// Append data to datafile
		long dataPos=raf.length();
		
		raf.seek(dataPos);
		raf.write(getLengthBytes(dataLength));
		raf.write(data, 0, dataLength);
		
		String indexLine=key + ":" + System.currentTimeMillis() + ":" + dataPos;
		
		// now update the two index files
		PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(indexFile1, true)));
		pw.println(indexLine);
		pw.close();
		
		pw = new PrintWriter(new BufferedWriter(new FileWriter(indexFile2, true)));
		pw.println(indexLine);
		pw.close();
		
		// and update the index object
		index.put(key, dataPos);
	}

	private byte[] getLengthBytes (int i) {
		ByteBuffer b=ByteBuffer.allocate(4);
		b.putInt(i);
		byte[] result = b.array();
//		  byte[] result = new byte[4];
//
//		  result[0] = (byte) (i >> 24);
//		  result[1] = (byte) (i >> 16);
//		  result[2] = (byte) (i >> 8);
//		  result[3] = (byte) (i /*>> 0*/);
		  
		  int j=getLengthFromBytes(result);
		  if (j != i) throw new RuntimeException("UGH-----------------------------------------------------");
	
		  return result;
	}
	
	private int getLengthFromBytes (byte[] buf) {
		ByteBuffer b=ByteBuffer.allocate(4);
		int i = b.wrap(buf).getInt();
		if (i<0) throw new RuntimeException("Invalid length: " + i);
		return i;
//		
//		((byte) buf[0])<<24
//		return ((byte) (buf[0]<<24)) | ((byte)(buf[1]<<16)) | ((byte)(buf[2]<<8)) | ((byte)buf[3]);
	}

	/**
	 * Return data for key, or null if not found
	 */
	public synchronized byte[] get (String key) throws Exception {
		Long pos=index.get(key);
		//System.out.println("get.pos=" + pos);
		if (pos==null) return null;
		
		// read data
		raf.seek(pos);
		byte[] len=new byte[4];
		if (raf.read(len) != len.length) throw new Exception("read failed: length field");
		int length=getLengthFromBytes (len);
		
		System.out.println("length=" + length);
		
		
		if (length == 0) return new byte[0];
		
		byte[] data=new byte[length];
		if (raf.read(data) != data.length) throw new Exception("read failed: data");
		return data;
	}

	public long getBlockId() {
		return blockId;
	}
	
	public String getName() {
		return name;
	}
	

}
