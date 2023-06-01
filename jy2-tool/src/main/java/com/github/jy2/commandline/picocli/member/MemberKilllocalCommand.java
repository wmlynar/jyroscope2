package com.github.jy2.commandline.picocli.member;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;

import com.github.jy2.Publisher;
import com.github.jy2.commandline.picocli.Main;
import com.github.jy2.di.JyroscopeDi;
import com.github.jy2.api.LogSeldom;

import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command(name = "killlocal", description = "Kill all members running on this machine except self")
public class MemberKilllocalCommand implements Runnable {

	private static final LogSeldom LOG = JyroscopeDi.getLog();

	@ParentCommand
	MemberCommand parent;

	public void run() {

		HashSet<InetAddress> localInterfaces = getLocalNetworkInterfaces();

		ArrayList<String> list = Main.introspector.getMemberList();
		for (String member : list) {
			if (member.equals(Main.di.getMemberName())) {
				continue;
			}
//			try {
			if (localInterfaces.contains(Main.introspector.getMemberAddress(member))) {
				System.out.println("Killing member: " + member);
				Publisher<String> pub = Main.di.createPublisher("/operations" + member.replace('-', '_'), String.class);
				pub.publish("kill");
			}
//			} catch (UnknownHostException e) {
//				LOG.error("Exception caught", e);
//			}
		}
	}

	public HashSet<InetAddress> getLocalNetworkInterfaces() {
		HashSet<InetAddress> list = new HashSet<>();
		try {
			Enumeration<NetworkInterface> ifEnum = NetworkInterface.getNetworkInterfaces();
			while (ifEnum.hasMoreElements()) {
				NetworkInterface netIf = ifEnum.nextElement();
				if (!netIf.isUp()) {
					continue;
				}
				Enumeration<InetAddress> adrEnum = netIf.getInetAddresses();
				while (adrEnum.hasMoreElements()) {
					list.add(adrEnum.nextElement());
				}
			}
		} catch (SocketException e) {
			LOG.error("Exception caught", e);
		}
		return list;
	}

}
