package pos;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.dbutils.QueryRunner;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.tlv.TLVList;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.yogapay.core.server.PackagerNavigator;
import com.yogapay.core.server.jpos.HEXChannel;
import com.yogapay.core.server.jpos.Utils;

public class UpdatePublicKey {
	
//	static String batchNo = "100000";
//	static String merchantNo = "131245111203458";
//	static String terminalNo = "21009503";
	
/*	static String batchNo = "000001";
	static String merchantNo = "162020000000008";
	static String terminalNo = "16220009";*/

    static String batchNo = "000001";
	static String merchantNo = "846584000100225";
	static String terminalNo = "02000308";

	static String ip = "58.60.171.38";
	static int port = 7780;
	static int count = 0;
	
	ISOMsg request = new ISOMsg();
	static QueryRunner  dao = null;
//	static HEXChannel channel = new HEXChannel("120.132.177.194", 4000,
//			PackagerNavigator.COMMON_PACKAGER);
	static HEXChannel channel = new HEXChannel(ip, port,
			PackagerNavigator.ZFT_PACKAGER);
	
	static{
		DruidDataSource dataSource = null;
		
		try {
			Properties props = new Properties();
			InputStream is = UpdatePublicKey.class
					.getResourceAsStream("/config/jdbcTest.properties");
			props.load(is);
			dataSource = (DruidDataSource) DruidDataSourceFactory
					.createDataSource(props);
			dao = new QueryRunner(dataSource);
			
//			channel.setHeader(ISOUtil.hex2byte("6000060000602200000000"));
            channel.setHeader(ISOUtil.hex2byte("6000040000"+"000000000000"));
			channel.setTimeout(50000);
			channel.connect();
			channel.setLogger(null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws ISOException, IOException, SQLException {
		downloadPublicKey();
		endPublicKeyDownload();
//		downloadParams();
		
	}
	
	public static void downloadPublicKey() throws ISOException, IOException, SQLException{
		ISOMsg pkUpResponse = statusSendPublicKey(count);
		System.out.println(Utils.dump(pkUpResponse));
		System.out.println(ISOUtil.hexdump(pkUpResponse.pack()));
		String f62 = pkUpResponse.getString(62);
		String[] arrs = f62.split("9F06");
		count += (arrs.length-1);
		System.out.println("count======"+count);
		if(arrs != null && arrs.length >0){
			if(!("31".equals(arrs[0]) || "32".equals(arrs[0]) || "33".equals(arrs[0]))){
				return;
			}
			downloadPublicKeyInfo(arrs, f62);
			if("32".equals(arrs[0])){
				downloadPublicKey();
			}
//			String sql = "delete from rid_struct";
//			dao.update(sql);
			
		}	
	}
	
	public static void endPublicKeyDownload() throws ISOException, IOException{
		ISOMsg request = new ISOMsg();
		request.setPackager(PackagerNavigator.COMMON_PACKAGER);
		request.setMTI("0800");
		request.set(41, terminalNo);
		request.set(42, merchantNo);
		request.set(60, "00"+batchNo+"371");
		request.unset(62);
		System.out.println(ISOUtil.hexdump(request.pack()));
		System.out.println(Utils.dump(request));
		
		//发送
		channel.send(request);
		ISOMsg pkDownloadResponse = channel.receive();
		
		System.out.println(Utils.dump(pkDownloadResponse));
		System.out.println(ISOUtil.hexdump(pkDownloadResponse.pack()));
	}
	
	public static ISOMsg statusSendPublicKey(int count) throws ISOException, IOException{
		//pos状态上送公钥下载
				ISOMsg request = new ISOMsg();
				request.setPackager(PackagerNavigator.COMMON_PACKAGER);
				request.setMTI("0820");
				request.set(41, terminalNo);
				request.set(42, merchantNo);
				request.set(60, "00"+batchNo+"372");
				request.set(62, ("1"+ISOUtil.padleft(count+"", 2, '0')).getBytes());
				
				System.out.println(ISOUtil.hexdump(request.pack()));
				System.out.println(Utils.dump(request));
				
				//发送
				channel.send(request);
				ISOMsg pkUpResponse = channel.receive();
				return pkUpResponse;
	}
	
	public static void downloadPublicKeyInfo(String[] arrs, String f62) throws ISOException, IOException, SQLException{
		for(int i=1; i<arrs.length; i++){
			TLVList tlvList = new TLVList();
			tlvList.unpack(ISOUtil.hex2byte(("9F06"+arrs[i])));
			String rid = tlvList.getString(0x9F06);
			String ind = tlvList.getString(0x9F22);	
			String exp = tlvList.getString(0xDF05);
			
			TLVList t = new TLVList();
			t.append(0x9F06, rid);
			t.append(0x9F22, ind);
			
			//POS参数传递
			ISOMsg req = new ISOMsg();
			req.setPackager(PackagerNavigator.COMMON_PACKAGER);
			req.setMTI("0800");
			req.set(41, terminalNo);
			req.set(42, merchantNo);
			req.set(60, "00"+batchNo+"370");
			req.set(62, t.pack());
			
			System.out.println("************rid="+rid+"&ind="+ind);
			
			System.out.println(ISOUtil.hexdump(req.pack()));
			System.out.println(Utils.dump(req));
			
			channel.send(req);
			ISOMsg pSResponse = channel.receive();
			System.out.println(Utils.dump(pSResponse));
			
			f62 = pSResponse.getString(62);
			if(pSResponse != null && pSResponse.getString(62) != null){
				if(pSResponse.getString(62).startsWith("31")){
					TLVList tlvlist = new TLVList();
					tlvlist.unpack(ISOUtil.hex2byte(f62), 1);
					String tlv_9f06 = tlvlist.getString(0x9F06);
					String tlv_9f22 = tlvlist.getString(0x9F22);
					String tlv_df05 = tlvlist.getString(0xdf05);
					String tlv_df06 = tlvlist.getString(0xdf06);
					String tlv_df07 = tlvlist.getString(0xdf07);
					String tlv_df02 = tlvlist.getString(0xdf02);
					String tlv_df04 = tlvlist.getString(0xdf04);
					String tlv_df03 = tlvlist.getString(0xdf03);
					
					String sql = "insert into rid_struct (`rid`,`ind`,`exp`,`hash_alg`,`rid_alg`,`mod`,`idx`,`ck`) values (?,?,?,?,?,?,?,?)";
					dao.update(sql, new Object[]{tlv_9f06, tlv_9f22, new String(ISOUtil.hex2byte(tlv_df05)), tlv_df06, tlv_df07, tlv_df02, tlv_df04, tlv_df03});
//					String update_terminal_sql = "update pos_terminal pt set pt.is_updated = 1";
//					dao.update(update_terminal_sql);
				}
			}
		}
	}
}
