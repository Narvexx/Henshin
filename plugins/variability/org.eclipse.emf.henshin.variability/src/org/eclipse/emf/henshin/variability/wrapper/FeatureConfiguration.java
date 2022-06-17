package org.eclipse.emf.henshin.variability.wrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.henshin.interpreter.matching.constraints.ReferenceConstraint;
import org.eclipse.emf.henshin.model.Edge;
import org.eclipse.emf.henshin.variability.matcher.FeatureExpression;
import org.eclipse.emf.henshin.variability.util.SatChecker;

import aima.core.logic.propositional.parsing.ast.Sentence;

public class FeatureConfiguration {

	public final static FeatureConfiguration INSTANCE = new FeatureConfiguration();

	private List<String> features;

	/** Keep track of already solved presence conditions. */
	private Map<Sentence, Map<String, Integer>> presenceConditionConfigCache;

	private ConstraintConfiguration currentConfig;

	private List<ConstraintConfiguration> constraintConfigurations;
	
	public String currentState;
	public String currentRule;

	private FeatureConfiguration() {
		this.features = new ArrayList<String>();
		this.presenceConditionConfigCache = new HashMap<Sentence, Map<String, Integer>>();
		this.currentConfig = new ConstraintConfiguration();
		this.constraintConfigurations = new ArrayList<ConstraintConfiguration>();
	}

	public Map<String, Integer> getContradictingFeatureConfig(Map<String, Integer> featureConfigMap,
			Map<String, Integer> config) {
		Map<String, Integer> result = new HashMap<String, Integer>();
		if (config == null || config.isEmpty()) {
			return result;
		}
		for (Map.Entry<String, Integer> entry : featureConfigMap.entrySet()) {
			String key = entry.getKey();
			Integer value = entry.getValue();
			if (config.get(key) != value) {
				result.put(key, config.get(key));
			}
		}
		return result;
	}

	// Initialize all features in a given variability-based rule.
	public void initializeConfigurations(VariabilityRule rule) {
		for (String feature : rule.getFeatures()) {
			if (!this.features.contains(feature)) {
				this.features.add(feature);
			}
		}
	}

	public List<ConstraintConfiguration> getConfigurations() {
		return this.constraintConfigurations;
	}

	public ConstraintConfiguration getCurrentConfig() {
		return this.currentConfig;
	}

	public void reset() {
		this.currentConfig = new ConstraintConfiguration();
		this.constraintConfigurations = new ArrayList<ConstraintConfiguration>();
	}

	/**
	 * 
	 * @return Returns the next configuration or null if there is not any.
	 */
	public ConstraintConfiguration getNextConfiguration() {
		int index = this.constraintConfigurations.indexOf(this.currentConfig);
		int lastElementIndex = this.constraintConfigurations.size() - 1;
		if (index < lastElementIndex) {
			this.currentConfig = this.constraintConfigurations.get(index + 1);
			return currentConfig;
		}
		return null;
	}

	/**
	 * 
	 * @param constraints
	 */
	public void getConstraintConfigs(List<ReferenceConstraint> constraints, Map<String, Integer> configAssumption,
			List<ReferenceConstraint> alreadyVisited) {

		for (ReferenceConstraint constraint : constraints) {
			if (!alreadyVisited.contains(constraint)) {
				alreadyVisited.add(constraint);
				Sentence presenceCondition = constraint.getPresenceCondition();
				ConstraintConfiguration config = new ConstraintConfiguration();
				if (presenceCondition != null) {
					Map<String, Integer> featureConfigMap = FeatureConfiguration.INSTANCE
							.getSatisfiableConfig(presenceCondition);
					if (!configAssumption.equals(featureConfigMap)) {
						Map<String, Integer> contradictingFeatureConfig = FeatureConfiguration.INSTANCE
								.getContradictingFeatureConfig(featureConfigMap, configAssumption);
						if (contradictingFeatureConfig.isEmpty()) {
							Map<String, Integer> levelConfigAssumption = new HashMap<String, Integer>();
							levelConfigAssumption.putAll(featureConfigMap);

							config.setConfig(featureConfigMap);
							config.addConstraint(constraint);

							this.constraintConfigurations.add(config);

							// Also, add valid expanded configurations
							getConstraintConfigs(constraints, levelConfigAssumption,
									new ArrayList<ReferenceConstraint>());
						}
					}
				}
			}
		}
	}

	// Get a single feature configuration that satisfies a given presence condition.
	public Map<String, Integer> getSatisfiableConfig(Sentence presenceCondition) {

		// Return cached configuration if possible
		if (presenceConditionConfigCache.get(presenceCondition) != null) {
			return presenceConditionConfigCache.get(presenceCondition);
		}

		Map<String, Integer> result = new HashMap<String, Integer>();
		SatChecker checker = new SatChecker();
		if (checker.isSatisfiable(presenceCondition)) {
			List<String> solution = checker.getSolution();
			if (solution.size() > 0) {
				for (String feature : this.features) {
					if (solution.indexOf(feature) >= 0) {
						result.put(feature, 1);
					} else {
						result.put(feature, 0);
					}
				}
			} else {
				for (String feature : this.features) {
					result.put(feature, 0);
				}
			}
		}
		return result;
	}

	public void setConfig(Sentence presenceCondition) {
		 this.currentConfig.setConfig(getSatisfiableConfig(presenceCondition));
	}
}
