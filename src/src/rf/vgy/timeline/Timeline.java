package rf.vgy.timeline;

import java.io.*;
import java.util.*;

public class Timeline {
	
	private final File dir;
	private final String name;
	
	private List<Block> blocks=new ArrayList<Block>();
	private Block writeBlock;
	
	public static String getDataFilePrefix(String name) {
		return name + "_data_";
	}
	
	public static String getIndexFile1Prefix(String name) {
		return name + "_ix1_";
	}
	
	public static String getIndexFile2Prefix(String name) {
		return name + "_ix2_";
	}
	
	public Timeline (File dir, String name) throws Exception {
		this.dir=dir;
		this.name=name;
	
		init();
		
	}
	
	private void init() throws Exception {
		String dataFilePrefix = Timeline.getDataFilePrefix(name);
		for (File f : dir.listFiles()) {
			String fName=f.getName();
			if (fName.startsWith(dataFilePrefix)) {
				String id = fName.substring(dataFilePrefix.length());
				long blockId=Long.parseLong(id);
				Block block=new Block(dir,blockId,name);
				blocks.add(block);
				if (writeBlock==null || block.getBlockId() > writeBlock.getBlockId()) {
					writeBlock=block;
				}
			}
		}
		if (writeBlock==null) {
			Block block=new Block(dir,0,name);
			blocks.add(block);
			writeBlock=block;
		}
		Collections.sort(blocks);
//		// verify sort
//		long x=-1;
//		for (Block b:blocks) {
//			if (b.getBlockId() < x) throw new Exception("INTERNAL ERROR: sort error");
//			x=b.getBlockId();
//		}
	}
	
	
	public synchronized void store (String key, byte[] data, int len) throws Exception {
		writeBlock.add(key, data, len);
		if (writeBlock.blockIsFull()) {
			long nextBlockId=writeBlock.getBlockId()+1;
			
			Block block=new Block(dir,nextBlockId,name);
			blocks.add(block);
			writeBlock=block;
		}
	}
	
	public void store (String key, String data) throws Exception {
		byte[] buf=data.getBytes("UTF-8");
		store(key,buf,buf.length);
	}

	public byte[] retrieve (String key) throws Exception {
		for (Block block:blocks) {
			byte[] data=block.get(key);
			if (data != null) return data;
		}
		return null;
	}
	
	public String retrieveString (String key) throws Exception {
		byte[] result=retrieve(key);
		if (result==null) return null;
		if (result.length==0) return "";
		return new String(result,"UTF-8");
	}
	

}
