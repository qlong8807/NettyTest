/**
 * 
 */
package com.cyber.netty;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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
			PrintWriter os = new PrintWriter(socket.getOutputStream());
			// 由Socket对象得到输入流，并构造相应的BufferedReader对象
			BufferedReader is = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			String readline = "hello,i am java";
			System.out.println("Client:" + readline);// 在系统标准输出上打印读入的字符串
			os.println(readline);// 将从系统标准输入读入的字符串输出到Server
			os.flush();
			
			System.out.println("Server:" + is.readLine());// 从Server读入一字符串，并打印到标准输出上
			os.close(); // 关闭Socket输出流
			is.close(); // 关闭Socket输入流
			socket.close(); // 关闭Socket
		} catch (Exception e) {
			System.out.println("Error" + e); // 出错，则打印出错信息
		}
	}
}
