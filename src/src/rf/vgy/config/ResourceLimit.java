package rf.vgy.config;

public class ResourceLimit {
	
	private long mBytesPerSecondRead;
	private long mBytesPerSecondWrite;
	
	public ResourceLimit(long mBytesPerSecondRead, long mBytesPerSecondWrite) {
		super();
		this.mBytesPerSecondRead = mBytesPerSecondRead;
		this.mBytesPerSecondWrite = mBytesPerSecondWrite;
	}

	public long getMBytesPerSecondRead() {
		return mBytesPerSecondRead;
	}

	public long getMBytesPerSecondWrite() {
		return mBytesPerSecondWrite;
	}


}
