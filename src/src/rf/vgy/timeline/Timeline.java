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
	}
	
	/**
	 * Synchronized method, as it includes creating new blocks when needed
	 */
	public synchronized void add (String key, byte[] data, int len) throws Exception {
		writeBlock.add(key, data, len);
		if (writeBlock.blockIsFull()) {
			long nextBlockId=writeBlock.getBlockId()+1;
			//System.out.println("Creating new block " + nextBlockId);
			
			Block block=new Block(dir,nextBlockId,name);
			blocks.add(block);
			writeBlock=block;
		}
	}
	
	public void add (String key, String data) throws Exception {
		byte[] buf=data.getBytes("UTF-8");
		add(key,buf,buf.length);
	}

	/**
	 * NEEDS OPTIMIZATION. Blocks should be in sorted order, and
	 * there should be a pool of threads that would search N blocks in
	 * parallel at a time, starting with the highest block id's and moving
	 * down, looking for hit with largest block id (which means added latest)
	 */
	public byte[] get (String key) throws Exception {
		byte[] result=null;
		long blockId=-1;
		
		for (Block block:blocks) {
			byte[] data=block.get(key);
			if (data != null && block.getBlockId() > blockId) {
				blockId=block.getBlockId();
				result=data;
			}
		}
		return result;
	}
	
	public String getString (String key) throws Exception {
		byte[] result=get(key);
		if (result==null) return null;
		if (result.length==0) return "";
		return new String(result,"UTF-8");
	}
	
	

}
