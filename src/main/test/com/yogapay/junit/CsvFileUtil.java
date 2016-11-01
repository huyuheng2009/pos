package com.yogapay.junit;

import java.io.*;

public class CsvFileUtil {

	public static void main(String[] args) throws IOException {

		
		File csv = new File("E:\\0826.txt"); // CSV文件
		File sqlFile = new File("E:\\carbin.sql"); // sql文件
		BufferedReader br = new BufferedReader(new FileReader(csv));
		
		if(!sqlFile.exists())
			sqlFile.createNewFile();
        BufferedWriter bw = new BufferedWriter(new FileWriter(sqlFile)) ;
		
		String sql = "INSERT INTO `card_bin` "
				         + "(`bank_name`, `card_name`, `card_length`, `verify_length`,"
				         + " `verify_code`, `card_type`, `image_name`, `create_time`) VALUES ";
        
		// 读取直到最后一行 
		String line = ""; 
		 bw.write(sql+"\n");
		 String temp = "" ;
		while ((line = br.readLine()) != null) { 
		   // 把一行数据分割成多个字段
			String sqlString = "(";
			String [] record = temp.split(",") ;
			if (record.length<4) {
				temp+=line ;
				continue ;
			}else {
				record = temp.split(",") ;
				temp = line ;
			}
			
			for (int i = 0; i < record.length; i++) {
				if (i==2||i==3||i==4) {
					sqlString += record[i] + "," ;
				}else {
					sqlString += "'"+record[i]+"'," ;
				}
			}
			sqlString += "'psbc.jpg',now())," ;
            bw.write(sqlString+"\n");
		}
		br.close();
		bw.close();
	}

}
