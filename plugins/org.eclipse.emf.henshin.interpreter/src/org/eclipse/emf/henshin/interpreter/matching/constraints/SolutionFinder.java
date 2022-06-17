/**
 * <copyright>
 * Copyright (c) 2010-2014 Henshin developers. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 which 
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * </copyright>
 */
package org.eclipse.emf.henshin.interpreter.matching.constraints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.henshin.interpreter.ApplicationMonitor;
import org.eclipse.emf.henshin.interpreter.EGraph;
import org.eclipse.emf.henshin.interpreter.matching.conditions.ApplicationCondition;
import org.eclipse.emf.henshin.interpreter.matching.conditions.ConditionHandler;
import org.eclipse.emf.henshin.variability.wrapper.FeatureConfiguration;

/**
 * Solution finder. This is the internal realization of the match finder.
 * 
 * @author Enrico Biermann, Christian Krause
 */
public class SolutionFinder extends ApplicationCondition {

	// Attribute condition handler:
	protected final ConditionHandler conditionHandler;

	// Started flag:
	protected boolean started;

	// Next solution:
	protected Solution nextSolution;

	/**
	 * Default constructor.
	 * 
	 * @param graph             Target graph.
	 * @param variableDomainMap Variable domain map.
	 * @param conditionHandler  Attribute condition handler.
	 * @param monitor           Monitor to collect performance data
	 */
	public SolutionFinder(EGraph graph, Map<Variable, DomainSlot> variableDomainMap, ConditionHandler conditionHandler,
			ApplicationMonitor monitor) {
		super(graph, variableDomainMap, monitor); // added monitor
		this.conditionHandler = conditionHandler;
		this.started = false;
	}

	/**
	 * Find a new solution.
	 * 
	 * @return <code>true</code> if a new solution was found.
	 */
	public boolean findSolution(List<Map<String, Integer>> seenConfigurations) {

		boolean matchIsPossible = false;
		if (!started) {
			started = true;
			matchIsPossible = true;
		} else {
			int varCount = variables.size();
			for (int i = varCount - 1; i >= 0; i--) {
				Variable var = variables.get(i);

				if (domainMap.get(var).unlock(var)) {
					matchIsPossible = true;
					break;
				} else {
					domainMap.get(var).clear(var);

				}
			}
		}

		// Get the feature configuration for current's solution
		Map<String, Integer> config = FeatureConfiguration.INSTANCE.getCurrentConfig().getConfig();
		
		if (matchIsPossible) {
			boolean success = findGraph(seenConfigurations, config);
			if (success) {
				nextSolution = new Solution(variables, domainMap, conditionHandler, FeatureConfiguration.INSTANCE.getCurrentConfig().getConfig());
			}
			return success;
		}

		// No solution found.
		return false;

	}

	/**
	 * Returns the next solution. On consecutive calls other matches will be
	 * returned.
	 * 
	 * @return A solution or <code>null</code> if no match exists.
	 */
	public Solution getNextSolution() {
		if (findSolution(new ArrayList<Map<String, Integer>>())) {
			return nextSolution;
		}
		return null;
	}

	public Solution getNextSolution(List<Map<String, Integer>> seenConfigurations) {
		if (findSolution(seenConfigurations)) {
			return nextSolution;
		}
		return null;
	}

}
