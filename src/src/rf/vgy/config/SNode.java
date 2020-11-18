package rf.vgy.config;

import java.util.*;

public class SNode {
	
	private String id;
	private String host;
	private int port;
	
	private Map<String,String> locationTags=new HashMap<String,String>();
	
	private ResourceLimit external;
	private ResourceLimit internal;
	private ResourceLimit intraStorage;
	
	
	
	public SNode(String id, String host, int port, Map<String, String> locationTags, 
			ResourceLimit external, ResourceLimit internal, ResourceLimit intraStorage) {
		super();
		this.id = id;
		this.host = host;
		this.port = port;
		this.locationTags = locationTags;
		this.external = external;
		this.internal = internal;
		this.intraStorage = intraStorage;
	}



	public SNode (String id, String host, int port) {
		this.id=id;
		this.host=host;
		this.port=port;
		
		// Configuring DISK bandwidth distribution / Megabytes per second
		this.external=new ResourceLimit(3,3);
		this.internal=new ResourceLimit(4,4);
		this.intraStorage=new ResourceLimit(2,2);
	}
	
}
