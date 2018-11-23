package org.mastodon.feature.ui;

import org.mastodon.revised.ui.coloring.feature.FeatureRangeCalculator;
import org.mastodon.util.Listeners;

/**
 * Provides and up-to-date set of feature projections, as well as
 * {@code FeatureRangeCalculator}s for vertices and edges.
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
	 */
	public Listeners< AvailableFeatureProjectionsListener > listeners();

	public AvailableFeatureProjections getAvailableFeatureProjections();

	public FeatureRangeCalculator getFeatureRangeCalculator();
}
