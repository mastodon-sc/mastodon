package org.mastodon.revised.ui.coloring;

import java.util.Optional;

import org.mastodon.revised.model.tag.TagSetModel;
import org.mastodon.revised.model.tag.TagSetStructure;
import org.mastodon.revised.ui.coloring.feature.FeatureColorMode;
import org.mastodon.revised.ui.coloring.feature.FeatureColorModeManager;
import org.mastodon.util.Listeners;

/**
 * ColoringModel knows which coloring scheme is currently active.
 * Possible options are: none, by a tag set, by a feature.
 * <p>
 * Notifies listeners when coloring is changed.
 * <p>
 * Listens for disappearing tag sets or features.
 *
 * @author Tobias Pietzsch
 */
public class ColoringModel implements TagSetModel.TagSetModelListener
{
	public interface ColoringChangedListener
	{
		void coloringChanged();
	}

	private final TagSetModel< ?, ? > tagSetModel;

	private final Listeners.List< ColoringChangedListener > listeners;

	private TagSetStructure.TagSet tagSet;

	private FeatureColorMode featureColorMode;

	private final FeatureColorModeManager featureColorModeManager;

	public ColoringModel( final TagSetModel< ?, ? > tagSetModel, final FeatureColorModeManager featureColorModeManager )
	{
		this.tagSetModel = tagSetModel;
		this.featureColorModeManager = featureColorModeManager;
		this.listeners = new Listeners.SynchronizedList<>();
	}

	public Listeners< ColoringChangedListener > listeners()
	{
		return listeners;
	}

	public void colorByNone()
	{
		tagSet = null;
		featureColorMode = null;
		listeners.list.forEach( ColoringChangedListener::coloringChanged );
	}

	public void colorByTagSet( final TagSetStructure.TagSet tagSet )
	{
		this.tagSet = tagSet;
		this.featureColorMode = null;
		listeners.list.forEach( ColoringChangedListener::coloringChanged );
	}

	public TagSetStructure.TagSet getTagSet()
	{
		return tagSet;
	}

	public void colorByFeature( final FeatureColorMode featureColorMode )
	{
		this.featureColorMode = featureColorMode;
		this.tagSet = null;
		listeners.list.forEach( ColoringChangedListener::coloringChanged );
	}

	public FeatureColorMode getFeatureColorMode()
	{
		return featureColorMode;
	}

	public boolean noColoring()
	{
		return tagSet == null && featureColorMode == null;
	}

	@Override
	public void tagSetStructureChanged()
	{
		final TagSetStructure tss = tagSetModel.getTagSetStructure();
		if ( tagSet != null )
		{
			final int id = tagSet.id();
			final Optional< TagSetStructure.TagSet > ts = tss.getTagSets().stream().filter( t -> t.id() == id ).findFirst();
			if ( ts.isPresent() )
				colorByTagSet( ts.get() );
			else
				colorByNone();
		}
	}

	public TagSetStructure getTagSetStructure()
	{
		return tagSetModel.getTagSetStructure();
	}

	public FeatureColorModeManager getFeatureColorModeManager()
	{
		return featureColorModeManager;
	}
}
