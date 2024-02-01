/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.ui.coloring;

import java.util.Optional;
import java.util.stream.Stream;

import org.mastodon.feature.FeatureModel;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.ui.coloring.feature.FeatureColorMode;
import org.mastodon.ui.coloring.feature.FeatureColorModeManager;
import org.mastodon.ui.coloring.feature.Projections;
import org.mastodon.ui.coloring.feature.ProjectionsFromFeatureModel;
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
public abstract class ColoringModel
{

	public enum ColoringStyle
	{
		NONE, BY_TRACK, BY_FEATURE, BY_TAGSET;
	}

	public interface ColoringChangedListener
	{
		void coloringChanged();
	}

	private final TagSetModel< ?, ? > tagSetModel;

	private TagSetStructure.TagSet tagSet;

	protected FeatureColorMode featureColorMode;

	private final FeatureColorModeManager featureColorModeManager;

	protected final Projections projections;

	private final Listeners.List< ColoringChangedListener > listeners;

	private ColoringStyle style;

	public ColoringModel(
			final TagSetModel< ?, ? > tagSetModel,
			final FeatureColorModeManager featureColorModeManager,
			final FeatureModel featureModel )
	{
		this.tagSetModel = tagSetModel;
		this.featureColorModeManager = featureColorModeManager;
		this.projections = new ProjectionsFromFeatureModel( featureModel );
		this.listeners = new Listeners.SynchronizedList<>();
		this.style = ColoringStyle.NONE;
	}

	public Listeners< ColoringChangedListener > listeners()
	{
		return listeners;
	}

	public void colorByNone()
	{
		style = ColoringStyle.NONE;
		tagSet = null;
		featureColorMode = null;
		listeners.list.forEach( ColoringChangedListener::coloringChanged );
	}

	public void colorByTrack()
	{
		style = ColoringStyle.BY_TRACK;
		tagSet = null;
		featureColorMode = null;
		listeners.list.forEach( ColoringChangedListener::coloringChanged );
	}

	public void colorByTagSet( final TagSetStructure.TagSet tagSet )
	{
		style = ColoringStyle.BY_TAGSET;
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
		style = ColoringStyle.BY_FEATURE;
		this.featureColorMode = featureColorMode;
		this.tagSet = null;
		listeners.list.forEach( ColoringChangedListener::coloringChanged );
	}

	public FeatureColorMode getFeatureColorMode()
	{
		return featureColorMode;
	}

	public ColoringStyle getColoringStyle()
	{
		return style;
	}

	public void tagSetStructureChanged()
	{
		if ( tagSet != null )
		{
			final int id = tagSet.id();
			final TagSetStructure tss = tagSetModel.getTagSetStructure();
			final Optional< TagSetStructure.TagSet > ts =
					tss.getTagSets().stream().filter( t -> t.id() == id ).findFirst();
			if ( ts.isPresent() )
				colorByTagSet( ts.get() );
			else
				colorByNone();
		}
	}

	public void featureColorModesChanged()
	{
		if ( featureColorMode != null )
		{
			final String name = featureColorMode.getName();
			final Optional< FeatureColorMode > mode = Stream.concat(
					featureColorModeManager.getBuiltinStyles().stream(),
					featureColorModeManager.getUserStyles().stream() )
					.filter( m -> m.getName().equals( name ) && isValid( m ) )
					.findFirst();
			if ( mode.isPresent() )
				colorByFeature( mode.get() );
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

	/**
	 * Returns {@code true} if the specified color mode is valid against the
	 * {@link FeatureModel}. That is: the feature projections that the color
	 * mode rely on are declared in the feature model, and of the right class.
	 *
	 * @param mode
	 *            the color mode
	 * @return {@code true} if the color mode is valid.
	 */
	public abstract boolean isValid( FeatureColorMode mode );

	/**
	 * Returns a {@link GraphColorGenerator} that can color graph objects based
	 * on the feature color mode defined in this model.
	 * <p>
	 * The {@link #isValid(FeatureColorMode)} method must return
	 * <code>true</code> only for the modes that are defined for the graph
	 * objects to color with this model.
	 * 
	 * @return a {@link GraphColorGenerator}.
	 */
	public abstract GraphColorGenerator< ?, ? > getFeatureGraphColorGenerator();
}
