package pos;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.lang.StringUtils;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.GenericPackager;


public class ConnectTest {
	@org.junit.Test
	public void connectTest() throws ISOException{
		String hostIp = "127.0.0.1";
		int port = 4000;
//		String hostIp = "127.0.0.1";
//		int port = 19000;
		Socket socket = null;
		OutputStream os = null;
		int refNo = 1;
		try {
			socket = new Socket(hostIp, port);
//			

			ISOMsg pikMsg = new ISOMsg();
			pikMsg.setPackager(new GenericPackager(ConnectTest.class.getResourceAsStream("/packager/chinaums_pos.xml")));
			
			pikMsg.setMTI("0820");
			pikMsg.set(11, StringUtils.leftPad(refNo + "", 6, "0"));
			pikMsg.set(33, "888800000");
			//PIK
			pikMsg.set(53, "1000000000000000");

			pikMsg.set(53, "2000000000000000");
			
			os = socket.getOutputStream();
			
			os.flush();
			Thread t = new Thread();
//			try {
//				t.sleep(6 * 1000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			System.out.println("=============="+ISOUtil.hexString(pikMsg.pack()));
			os.write(pikMsg.pack());
			System.out.println("111111111111");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try{
				if(socket!=null) socket.close();
				if(os!=null) os.close();
			}catch(Exception e){}
		}
		
	} 
	
	@org.junit.Test
	public void send() throws Exception {
		Socket s = new Socket("192.168.1.30", 4000);
		OutputStream os = s.getOutputStream();
		os.write("0200202005C1A0C0900931000000051202100009140608201403200820140320376226901407210099D3207520000009280000003230313430333230383132313231313435313130303036313536113E5AB70DF07C8F000830313030303030324246313537323741".getBytes());
		os.flush();
		System.out.println("data send");
		System.in.read();
	}
	@org.junit.Test
	public void recive() throws Exception {
		ServerSocket ss = new ServerSocket(4001);
		while (true) {
			Socket s = ss.accept();
			System.out.println("incoming");
			try {
				InputStream is = s.getInputStream();
				byte[] buf = new byte[1024 *2];
				int len = is.read(buf);
				System.out.println("==="+len);
				System.out.println("------"+new String(buf)+"=====");
				System.out.println(ISOUtil.hexString(buf, 0, len));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
