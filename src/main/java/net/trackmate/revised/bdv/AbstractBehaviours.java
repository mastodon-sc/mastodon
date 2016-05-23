package net.trackmate.revised.bdv;

import org.scijava.ui.behaviour.Behaviour;
import org.scijava.ui.behaviour.BehaviourMap;
import org.scijava.ui.behaviour.InputTriggerAdder;
import org.scijava.ui.behaviour.InputTriggerMap;
import org.scijava.ui.behaviour.io.InputTriggerConfig;

import bdv.viewer.TriggerBehaviourBindings;

public class AbstractBehaviours
{
	protected final InputTriggerMap inputTriggerMap;

	protected final BehaviourMap behaviourMap;

	protected final InputTriggerConfig config;

	protected final InputTriggerAdder inputTriggerAdder;

	/**
	 * Install an {@link InputTriggerMap} and a {@link BehaviourMap} with the
	 * given {@code name} in {@code triggerBehaviourBindings}.
	 *
	 * @param triggerBehaviourBindings
	 *            where to install the new {@link InputTriggerMap}/
	 *            {@link BehaviourMap}.
	 * @param name
	 *            name under which the new {@link InputTriggerMap}/
	 *            {@link BehaviourMap} is installed.
	 * @param config
	 *            overrides default key bindings.
	 * @param keyConfigContexts
	 *            for which context names in the config should key bindings be
	 *            retrieved.
	 */
	public AbstractBehaviours(
			final TriggerBehaviourBindings triggerBehaviourBindings,
			final String name,
			final InputTriggerConfig config,
			final String[] keyConfigContexts )
	{
		this.config = config;
		inputTriggerMap = new InputTriggerMap();
		behaviourMap = new BehaviourMap();
		triggerBehaviourBindings.addInputTriggerMap( name, inputTriggerMap );
		triggerBehaviourBindings.addBehaviourMap( name, behaviourMap );
		inputTriggerAdder = config.inputTriggerAdder( inputTriggerMap, keyConfigContexts );
	}

	public void behaviour( final Behaviour behaviour, final String name, final String... defaultTriggers )
	{
		inputTriggerAdder.put( name, defaultTriggers );
		behaviourMap.put( name, behaviour );
	}
}
