package com.github.jy2.smlib.messages;

import java.io.Serializable;
import java.util.ArrayList;

import com.jyroscope.annotations.Message;

//@Message("smlib_msgs/StateTransition")
public class StateTransition implements Serializable {
	
	private String from;
	private ArrayList<String> to;
	
	public String getFrom() {
		return from;
	}
	public void setFrom(String fromState) {
		this.from = fromState;
	}
	public ArrayList<String> getTo() {
		return to;
	}
	public void setTo(ArrayList<String> toStates) {
		this.to = toStates;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StateTransition other = (StateTransition) obj;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (to == null) {
			if (other.to != null)
				return false;
		} else if (!to.equals(other.to))
			return false;
		return true;
	}
	
	
}
