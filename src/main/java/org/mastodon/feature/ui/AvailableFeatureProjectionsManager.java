package org.mastodon.feature.ui;

import org.mastodon.util.Listeners;

public interface AvailableFeatureProjectionsManager
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
}
