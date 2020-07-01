package org.mastodon.feature.ui;

import org.mastodon.ui.coloring.feature.FeatureRangeCalculator;
import org.scijava.listeners.Listeners;

/**
 * Provides and up-to-date set of feature projections, as well as
 * {@code FeatureRangeCalculator}s for vertices and edges.
 * <p>
 * Used for FeatureColorModes.
 *
 * @author Tobias Pietzsch
 */
public interface FeatureProjectionsManager
{
	public interface AvailableFeatureProjectionsListener
	{
		/**
		 * Notifies a listener that the set of available feature projections has
		 * changed.
		 */
		public void availableFeatureProjectionsChanged();
	}

	/**
	 * Exposes the list of listeners that are notified when the set of available
	 * feature projections has changed.
	 * 
	 * @return the listeners.
	 */
	public Listeners< AvailableFeatureProjectionsListener > listeners();

	public AvailableFeatureProjections getAvailableFeatureProjections();

	public FeatureRangeCalculator getFeatureRangeCalculator();
}
