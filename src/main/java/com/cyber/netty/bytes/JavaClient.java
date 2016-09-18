/**
 * 
 */
package com.cyber.netty.bytes;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

import com.cyber.encry.JsonUtil;

/**
 * @author zyl
 * @date 2016年9月1日
 * 
 */
public class JavaClient {
	public static void main(String args[]) {
		try {
			Socket socket = new Socket("127.0.0.1", 8080);
			// 由Socket对象得到输出流，并构造PrintWriter对象
			// PrintWriter os = new PrintWriter(socket.getOutputStream());
			DataOutputStream os = new DataOutputStream(socket.getOutputStream());
			// 由Socket对象得到输入流，并构造相应的BufferedReader对象
			BufferedReader is = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			String readline = "{\"messageType\":4,\"content\":\"admin-123456\"}";
			// String readline =
			// "{\"messageType\":1,\"content\":\"asdfasdf\",\"validateSerialize\":\"d30d91717eb8f05a7389a5b83f8c2bf1\"}";
			// readline = Base64Util.encode(readline);
			MessageProtocol mp = new MessageProtocol();
			mp.setMessageType(2);
			mp.setContent("hello bytes");
			readline = JsonUtil.objectToJson(mp);
			System.out.println("Client:" + readline);// 在系统标准输出上打印读入的字符串
			byte[] bytes = readline.getBytes();
			int length = bytes.length;
			byte[] b = new byte[length + 8];
			System.out.println(length);
			// os.write(length);
			System.out.println(Convert.bytesToHexString(Convert.intTobytes(length, 8)));
			System.out.println("===");
			byte[] byteArray = Convert.longTobytes(length, 8);
			System.arraycopy(byteArray, 0, b, 0, 8);
			System.arraycopy(bytes, 0, b, 8, length);
			// System.out.println("------------------");
			// System.out.println(bytes);
			System.out.println(Convert.bytesToHexString(b));

			os.write(b);
			os.flush();
			String serverResponse = is.readLine();
			System.out.println("Server:" + serverResponse);// 从Server读入一字符串，并打印到标准输出上
			os.close(); // 关闭Socket输出流
			is.close(); // 关闭Socket输入流
			socket.close(); // 关闭Socket
		} catch (Exception e) {
			System.out.println("Error" + e); // 出错，则打印出错信息
		}
	}

}
