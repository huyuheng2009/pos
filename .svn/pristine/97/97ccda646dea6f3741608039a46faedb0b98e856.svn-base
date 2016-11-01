package com.yogapay.core.server.jpos;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;

public class Utils {
	public static String dump(ISOMsg msg) {
        ISOMsg msgClone = (ISOMsg)msg.clone();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
            wipe(msgClone,35);
            wipe(msgClone,36);
            wipe(msgClone,52);
            wipe(msgClone,55);
            msgClone.dump(new PrintStream(baos), "");
			baos.flush();
		} catch (IOException e) {
			// ignore
		} finally {
			try {
				baos.close();
			} catch (IOException e) {
				// ignore
			}
		}
		return new String(baos.toByteArray());
	}

    /**
     * @Todo 擦除指定域（field）信息
     * @param logStr
     * @param field 指定域
     * @return
     */
    public static String wipe(String logStr,int field){
        String[] logs = logStr.split("id=\""+field+"\"");
        if(logs.length>1) {
            String[] logs_1 = logs[1].split("\"/>",2);
            logStr = logs[0]+"id=\""+field+"\" value=\"WIPED\"/>".concat(logs_1[1]);
        }
        return logStr;
    }

    /**
     * @Todo 擦除指定域（field）信息
     * @param msg
     * @param field 指定域
     */
    public static void wipe(ISOMsg msg,int field){
       if(msg.hasField(field)) {
           try {
               msg.set(field,"WIPED");
           } catch (ISOException e) {
               e.printStackTrace();
           }
       }
    }
}
