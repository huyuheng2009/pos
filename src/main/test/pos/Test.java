package pos;

import com.yogapay.mobile.utils.FinanceUtil;
import junit.framework.Assert;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;
import org.jpos.tlv.TLVList;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Test {

	public static void main(String[] argss) throws ISOException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        System.out.println(format.format(new Date()));
	}

}
