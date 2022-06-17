package org.eclipse.emf.henshin.variability.wrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.henshin.interpreter.matching.constraints.ReferenceConstraint;

import aima.core.logic.propositional.parsing.ast.Sentence;

public class ConstraintConfiguration {
	
	private List<ReferenceConstraint> constraints;
	private Map<String, Integer> config;
	
	public ConstraintConfiguration() {
		constraints = new ArrayList<ReferenceConstraint>();
		config = new HashMap<String, Integer>();
	}
	
	public void addConstraint(ReferenceConstraint constraint) {
		this.constraints.add(constraint);
	}
	
	public void setConfig(Map<String, Integer> config) {
		this.config.putAll(config);
	}
	
	public String toString() {
		String result = "Configuration '" + config.toString() + "' satisfy the following constraints: ";
		for (ReferenceConstraint c : constraints) {
			result += "\n\t Reference: " + c.getReference().getName();
			result += "\n\t Presence Condition " + c.getPresenceCondition().toString();
		}
		return result;
	}
	
	public Map<String, Integer> getConfig() {
		return this.config;
	}

	public List<ReferenceConstraint> getConstraints() {
		return this.constraints;
	}
	

	
}
