package com.github.jy2.smlib.messages;

import java.io.Serializable;

import com.jyroscope.annotations.Message;

//@Message("smlib_msgs/SmState")
public class SmState implements Serializable {

	private String state;
	private String input;
	private String output;
	private String context;
	private String config;
	private String attributes;
	private double time;
	private double life;
	private double duration;

	public String getState() {
		return state;
	}

	public void setState(String currentState) {
		this.state = currentState;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String configuration) {
		this.config = configuration;
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}

	public double getLife() {
		return life;
	}

	public void setLife(double life) {
		this.life = life;
	}

	public double getDuration() {
		return duration;
	}

	public void setDuration(double timeInState) {
		this.duration = timeInState;
	}

	public String getAttributes() {
		return attributes;
	}

	public void setAttributes(String attributes) {
		this.attributes = attributes;
	}
}
