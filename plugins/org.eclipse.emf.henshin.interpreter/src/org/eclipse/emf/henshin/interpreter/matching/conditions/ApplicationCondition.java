/**
 * <copyright>
 * Copyright (c) 2010-2014 Henshin developers. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 which 
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * </copyright>
 */
package org.eclipse.emf.henshin.interpreter.matching.conditions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.henshin.interpreter.ApplicationMonitor;
import org.eclipse.emf.henshin.interpreter.EGraph;
import org.eclipse.emf.henshin.interpreter.matching.constraints.DomainSlot;
import org.eclipse.emf.henshin.interpreter.matching.constraints.ReferenceConstraint;
import org.eclipse.emf.henshin.interpreter.matching.constraints.Variable;
import org.eclipse.emf.henshin.interpreter.monitoring.PerformanceMonitor;
import org.eclipse.emf.henshin.interpreter.monitoring.VariableCheck;
import org.eclipse.emf.henshin.variability.wrapper.ConstraintConfiguration;
import org.eclipse.emf.henshin.variability.wrapper.FeatureConfiguration;

/**
 * Application condition.
 * 
 * @author Enrico Biermann, Christian Krause
 */
public class ApplicationCondition implements IFormula {

	// Target graph:
	public final EGraph graph;

	// Domain map:
	public final Map<Variable, DomainSlot> domainMap;

	// Domain variants:
	public Map<Variable, List<DomainSlot>> domainVariantMap;

	// Formula:
	public IFormula formula;

	// Variables:
	public List<Variable> variables;

	// Performance Monitor
	private PerformanceMonitor monitor = null;

	/**
	 * Default constructor.
	 * 
	 * @param graph     Target graph.
	 * @param domainMap Domain map.
	 * @param monitor   Monitor to collect performance data
	 */
	public ApplicationCondition(EGraph graph, Map<Variable, DomainSlot> domainMap, ApplicationMonitor monitor) {
		this.domainMap = domainMap;
		this.graph = graph;
		// Set Monitor
		if (monitor instanceof PerformanceMonitor) {
			this.monitor = (PerformanceMonitor) monitor;
		}
	}

	public ApplicationCondition(EGraph graph, Map<Variable, DomainSlot> domainMap, ApplicationMonitor monitor,
			List<Map<String, Integer>> configMap) {
		this.domainMap = domainMap;
		this.graph = graph;
		// Set Monitor
		if (monitor instanceof PerformanceMonitor) {
			this.monitor = (PerformanceMonitor) monitor;
		}

		this.domainVariantMap = new HashMap<Variable, List<DomainSlot>>();
		for (Map.Entry<Variable, DomainSlot> entry : domainMap.entrySet()) {
			// Initialize the domain variant map
			this.domainVariantMap.put(entry.getKey(), new ArrayList<DomainSlot>());
		}
	}
	
	/**
	 * Find a graph.
	 * @return <code>true</code> if a graph was found.
	 */
	public boolean findGraph() {
		for (Variable var : variables) {
			if (!var.typeConstraint.instantiationPossible(domainMap.get(var), graph)) {
				return false;
			}
			//monitor Variable information
			if(monitor!=null){
				this.monitor.addVariableInfoRecord(var.variableId,var.name,var.typeConstraint.type.getName(),variables.indexOf(var),graph.getDomainSize(var.typeConstraint.type,var.typeConstraint.strictTyping));
			}
		}
		return findMatch(0);
	}

	/**
	 * Find a graph.
	 * 
	 * @return <code>true</code> if a graph was found.
	 */
	public boolean findGraph(List<Map<String, Integer>> seenConfigurations, Map<String, Integer> config) {
		for (Variable var : variables) {
			if (!var.typeConstraint.instantiationPossible(domainMap.get(var), graph)) {
				return false;
			}
			// monitor Variable information
			if (monitor != null) {
				this.monitor.addVariableInfoRecord(var.variableId, var.name, var.typeConstraint.type.getName(),
						variables.indexOf(var),
						graph.getDomainSize(var.typeConstraint.type, var.typeConstraint.strictTyping));
			}
		}
		return findVBMatch(0, seenConfigurations, config);
	}

	int numberOfVariants = 0;

	/**
	 * Finds a match for the variable at the given index in the LHS-variables
	 * vector.
	 */
	protected boolean findVBMatch(int index, List<Map<String, Integer>> seenConfigurations,
			Map<String, Integer> config) {

		// Matched all variables?
		if (index == variables.size()) {

			// Final variable re-checks:
			for (Variable variable : variables) {
				if (variable.requiresFinalCheck) {
					DomainSlot slot = domainMap.get(variable);
					if (!slot.recheck(variable, domainMap)) {
						return false;
					}
				}
			}

			// Evaluate formula:
			return formula.eval();

		}

		// Otherwise try to match the last variable:
		Variable variable = variables.get(index);
		DomainSlot slot = domainMap.get(variable);

		// create new CheckVariableRecord
		VariableCheck varCheckRecord = null;
		if (monitor != null) {
			varCheckRecord = new VariableCheck(variable.variableId, true);
			slot.setVarCheckRecord(varCheckRecord);
		}
				
		boolean valid = false;
		while (!valid) {
			
			valid = slot.instantiate(variable, domainMap, graph, seenConfigurations, config);

			if (valid) {
				// monitor checked variable
				if (monitor != null) {
					this.monitor.addVariableCheckRecord(varCheckRecord);
				}
				valid = findVBMatch(index + 1, seenConfigurations, config); // recursion
				// create new CheckVariableRecord after recursion
				if (monitor != null) {
					varCheckRecord = new VariableCheck(variable.variableId, false);
					slot.setVarCheckRecord(varCheckRecord);
					if (!valid) {
						this.monitor.addBacktrackRecord(variable.variableId);
					}
				}
			}
			if (!valid) {

				slot.unlock(variable);
				if (!slot.instantiationPossible()) {

					slot.clear(variable);
					// monitor checked variable
					if (monitor != null) {
						this.monitor.addVariableCheckRecord(varCheckRecord);
					}
					return false;
				}
			}
		}

		// Found a match.
		return true;

	}

//	/**
//	 * Finds a match for the variable at the given index in the LHS-variables
//	 * vector.
//	 */
	protected boolean findMatch(int index) {

		// Matched all variables?
		if (index == variables.size()) {

			// Final variable re-checks:
			for (Variable variable : variables) {
				if (variable.requiresFinalCheck) {
					DomainSlot slot = domainMap.get(variable);
					if (!slot.recheck(variable, domainMap)) {
						return false;
					}
				}
			}

			// Evaluate formula:
			return formula.eval();

		}

		// Otherwise try to match the last variable:
		Variable variable = variables.get(index);
		DomainSlot slot = domainMap.get(variable);

		// create new CheckVariableRecord
		VariableCheck varCheckRecord = null;
		if (monitor != null) {
			varCheckRecord = new VariableCheck(variable.variableId, true);
			slot.setVarCheckRecord(varCheckRecord);
		}

		boolean valid = false;
		while (!valid) {
			valid = slot.instantiate(variable, domainMap, graph);
			if (valid) {
				// monitor checked variable
				if (monitor != null) {
					this.monitor.addVariableCheckRecord(varCheckRecord);
					;
				}
				valid = findMatch(index + 1); // recursion
				// create new CheckVariableRecord after recursion
				if (monitor != null) {
					varCheckRecord = new VariableCheck(variable.variableId, false);
					slot.setVarCheckRecord(varCheckRecord);
					if (!valid) {
						this.monitor.addBacktrackRecord(variable.variableId);
					}
				}
			}
			if (!valid) {
				slot.unlock(variable);
				if (!slot.instantiationPossible()) {
					slot.clear(variable);
					// monitor checked variable
					if (monitor != null) {
						this.monitor.addVariableCheckRecord(varCheckRecord);
					}
					return false;
				}
			}
		}

		// Found a match.
		return true;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.emf.henshin.interpreter.matching.conditions.IFormula#eval()
	 */
	@Override
	public boolean eval() {

		// Find a graph:
		boolean result = findGraph();

		// Reset the variables:
		for (Variable var : variables) {
			domainMap.get(var).reset(var);
		}

		// Done.
		return result;

	}

}