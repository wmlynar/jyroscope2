package com.github.jy2.smlib.messages;

import java.io.Serializable;
import java.util.ArrayList;

import com.jyroscope.annotations.Message;

//@Message("smlib_msgs/SmStructure")
public class SmStructure implements Serializable {
	
	private String start;
	private ArrayList<StateTransition> transitions;
	
	public String getStart() {
		return start;
	}
	public void setStart(String initialState) {
		this.start = initialState;
	}
	public ArrayList<StateTransition> getTransitions() {
		return transitions;
	}
	public void setTransitions(ArrayList<StateTransition> stateTransitions) {
		this.transitions = stateTransitions;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		result = prime * result + ((transitions == null) ? 0 : transitions.hashCode());
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
		SmStructure other = (SmStructure) obj;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		if (transitions == null) {
			if (other.transitions != null)
				return false;
		} else if (!transitions.equals(other.transitions))
			return false;
		return true;
	}	
}
