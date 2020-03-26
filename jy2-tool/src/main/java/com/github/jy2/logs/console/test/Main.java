package com.github.jy2.logs.console.test;

import com.github.jy2.logs.console.model.Model;
import com.github.jy2.logs.console.utils.DisplayUtils;

import go.jyroscope.ros.rosgraph_msgs.Log;

public class Main {

	public static void main(String[] args) {

		Model model = new Model();
		Log log;

		log = new Log();
		log.level = Log.INFO;
		log.name = "node1";
		log.file = "file1";
		log.line = 1;
		log.msg = "message1";
		log.function = "function1";
		model.add(log);

		log = new Log();
		log.level = Log.INFO;
		log.name = "node1";
		log.file = "file1";
		log.line = 1;
		log.msg = "message2";
		log.function = "function1";
		model.add(log);

		log = new Log();
		log.level = Log.INFO;
		log.name = "node1";
		log.file = "file2";
		log.line = 2;
		log.msg = "message2";
		log.function = "function2";
		model.add(log);

		log = new Log();
		log.level = Log.WARN;
		log.name = "node1";
		log.file = "file3";
		log.line = 3;
		log.msg = "message3";
		log.function = "function3";
		model.add(log);

		log = new Log();
		log.level = Log.INFO;
		log.name = "node2";
		log.file = "file1";
		log.line = 1;
		log.msg = "message1 message1 message1 message1\n message1 message1 message1 message1 message1 message1 message1 message1\nmessage1 message1 message1 message1 message1 message1";
		log.function = "function1";
		model.add(log);

		log = new Log();
		log.level = Log.INFO;
		log.name = null;
		log.file = "file4";
		log.line = 1;
		log.msg = "message4";
		log.function = "function1";
		model.add(log);

		DisplayUtils.display(model, false, 10);

	}

}
