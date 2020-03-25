package com.github.jy2.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Consumer;

import com.github.jy2.JyroscopeCore;
import com.github.jy2.ParameterClient;
import com.github.jy2.ParameterListener;
import com.github.jy2.mapper.RosTypeConverters;

public class MainParameters {

	public static void main(String[] args) throws InterruptedException {
//		RosTypeConverters.scanAnnotationsAndInitialize();
		JyroscopeCore jy2 = new JyroscopeCore();
		jy2.addRemoteMaster("http://localhost:11311", "localhost", "/jy2" + new Random().nextInt());

		ParameterClient ps = jy2.getParameterClient();

		ps.addParameterListener("/", new ParameterListener() {
			
			@Override
			public void onParameterUpdated(String name, Object value) {
				System.out.println(name + " " + value);
			}
		});

		ArrayList<String> list = new ArrayList<>();
		list.add("a");
		list.add("b");
		ps.setParameter("/aaaList", list);
		HashMap<Integer, Double> map = new HashMap<>();
		map.put(1, 1.0);
		map.put(2, 2.0);
		ps.setParameter("/aaaMap", map);

		Object result = ps.getParameter("/aaaList");
		ArrayList<Object> l = (ArrayList<Object>) result;
		System.out.println(l);

		result = ps.getParameter("/aaaMap");
		HashMap<Object, Object> m = (HashMap<Object, Object>) result;
		System.out.println(m);

		System.out.println(ps.hasParameter("/adasdas"));
		System.out.println(ps.hasParameter("/aaaList"));
		ps.deleteParameter("/aaaList");
		System.out.println(ps.hasParameter("/aaaList"));

		ArrayList<String> params = ps.getParameterNames();
		System.out.println(params);

		ps.setParameter("/bbb", map);
	}

}
