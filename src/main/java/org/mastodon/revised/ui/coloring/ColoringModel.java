package org.mastodon.revised.ui.coloring;

import java.util.Optional;

import org.mastodon.revised.model.tag.TagSetModel;
import org.mastodon.revised.model.tag.TagSetStructure;
import org.mastodon.util.Listeners;

/**
 * ColoringModel knows which coloring scheme is currently active.
 * Possible options are: none, by a tag set, by a feature (not implemented yet).
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

	public ColoringModel( final TagSetModel< ?, ? > tagSetModel )
	{
		this.tagSetModel = tagSetModel;
		this.listeners = new Listeners.SynchronizedList<>();
	}

	public Listeners< ColoringChangedListener > listeners()
	{
		return listeners;
	}

	public void colorByNone()
	{
		tagSet = null;
		listeners.list.forEach( ColoringChangedListener::coloringChanged );
	}

	public void colorByTagSet( final TagSetStructure.TagSet tagSet )
	{
		this.tagSet = tagSet;
		listeners.list.forEach( ColoringChangedListener::coloringChanged );
	}

	public TagSetStructure.TagSet getTagSet()
	{
		return tagSet;
	}

	public boolean noColoring()
	{
		return tagSet == null;
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
}
