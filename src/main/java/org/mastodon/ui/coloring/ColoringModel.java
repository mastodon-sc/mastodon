package org.mastodon.ui.coloring;

import org.mastodon.feature.FeatureModel;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.model.tag.TagSetStructure.TagSet;
import org.mastodon.ui.coloring.feature.FeatureColorMode;
import org.mastodon.ui.coloring.feature.FeatureColorModeManager;
import org.scijava.listeners.Listeners;

/**
 * ColoringModel knows which coloring scheme is currently active. Possible
 * options are: none, by a tag set, by a feature.
 * <p>
 * This particular implementation also offers coloring of vertices and edges
 * based on the features defined for the branch graph it is associated with. The
 * branch graph instance needs to be specified.
 * <p>
 * Notifies listeners when coloring is changed.
 * <p>
 * Listens for disappearing tag sets or features.
 *
 * @author Tobias Pietzsch
 */
public interface ColoringModel
{

	public interface ColoringChangedListener
	{
		void coloringChanged();
	}

	void colorByNone();

	void colorByTagSet( TagSet tagSet );

	TagSet getTagSet();

	void colorByFeature( FeatureColorMode featureColorMode );

	FeatureColorMode getFeatureColorMode();

	boolean noColoring();

	TagSetStructure getTagSetStructure();

	FeatureColorModeManager getFeatureColorModeManager();

	/**
	 * Returns {@code true} if the specified color mode is valid against the
	 * {@link FeatureModel}. That is: the feature projections that the color
	 * mode rely on are declared in the feature model, and of the right class.
	 *
	 * @param mode
	 *            the color mode
	 * @return {@code true} if the color mode is valid.
	 */
	boolean isValid( FeatureColorMode mode );

	GraphColorGenerator< ?, ? > getFeatureGraphColorGenerator();

	Listeners< ColoringChangedListener > listeners();

}