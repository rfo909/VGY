package rf.vgy.timeline;

import java.io.*;

public class Test {
	
	class Runner implements Runnable {
		private String prefix;
		private Timeline timeline;
		private String dataPath;
		private byte[] buf;
		public static final int MAX = 1024*1024*16;
		
		public Runner (String prefix, Timeline timeline, String dataPath) {
			this.prefix=prefix;
			this.timeline=timeline;
			this.dataPath=dataPath;
			this.buf=new byte[MAX];
		}
		public void run() {
			File dir=new File(dataPath);
			traverseDir(dir);
		}
		private void traverseDir(File dir) {
			for (File f:dir.listFiles()) {
				if (f.isFile()) {
					if (f.length() < MAX) try {
						FileInputStream fis=new FileInputStream(f);
						int len=fis.read(buf);
						if (len > 0) {
							timeline.add(f.getCanonicalPath(), buf, len);
							System.out.println("Runner " + prefix + ": " + f.getCanonicalPath());
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				} else if (f.isDirectory()) {
					traverseDir(f);
				}
			}
		}
	}
	
	public void test() throws Exception {
		File dir=new File("/home/roar/xxx/vgy");
		String name="main";
		
		Timeline t=new Timeline(dir,name);
		Runner a=new Runner("a", t, "/home/roar/CFT");
		Runner b=new Runner("b", t, "/home/roar/Pictures");
		Runner c=new Runner("c", t, "/home/roar/Downloads");
		Runner d=new Runner("d", t, "/home/roar/Roar");
		(new Thread(a)).start();
		(new Thread(b)).start();
		(new Thread(c)).start();
		(new Thread(d)).start();
	}

	public static void main (String[] args) throws Exception {
		(new Test()).test();
	}
	
}
