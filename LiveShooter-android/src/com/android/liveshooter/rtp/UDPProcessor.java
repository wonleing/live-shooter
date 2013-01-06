package com.android.liveshooter.rtp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;



public class UDPProcessor {
	
	private DatagramSocket socket = null;
	
	private InetSocketAddress address;
	
	public UDPProcessor(){
		
	}
	
	public boolean init(String ip, int port){
		try {
			socket = new DatagramSocket();
			address = new InetSocketAddress(ip, port);
			return true;
		} catch (SocketException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean init(InetAddress remote_address, int remote_port){
		try {
			socket = new DatagramSocket();
			address = new InetSocketAddress(remote_address, remote_port);
			return true;
		} catch (SocketException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public void sendPacket(byte[] packetData, int length){
		if(socket == null){
			return;
		}
		try {
			DatagramPacket packet = new DatagramPacket(packetData, length, address);
			socket.send(packet);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void receive(DatagramPacket pack) throws IOException {
		socket.send(pack);
	}
	
	public void send(DatagramPacket pack) throws IOException {
		socket.receive(pack);
	}
	
	public void close(){
		if(socket != null){
			socket.close();
		}
	}
}
