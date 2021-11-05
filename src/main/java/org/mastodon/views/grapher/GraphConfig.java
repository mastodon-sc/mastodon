package org.mastodon.views.grapher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mastodon.feature.Dimension;

public class GraphConfig
{

	private final SpecPair xFeature;

	private final List< SpecPair > yFeatures;

	private final boolean trackOfSelection;

	private final boolean connect;

	GraphConfig(
			final SpecPair xFeature,
			final List< SpecPair > yFeatures,
			final boolean trackOfSelection,
			final boolean connect )
	{
		this.xFeature = xFeature;
		this.yFeatures = Collections.unmodifiableList( yFeatures );
		this.trackOfSelection = trackOfSelection;
		this.connect = connect;
	}

	public SpecPair getXFeature()
	{
		return xFeature;
	}

	public List< SpecPair > getYFeatures()
	{
		return yFeatures;
	}

	public boolean graphTrackOfSelection()
	{
		return trackOfSelection;
	}

	public boolean drawConnected()
	{
		return connect;
	}

	/**
	 * Splits the current graph config in several of them, where each graph
	 * config only contains Y features that have the same dimension.
	 * 
	 * @return a new unodifiable map.
	 */
	public Map< Dimension, GraphConfig > splitByDimension()
	{
		final Map< Dimension, List< SpecPair > > spPerDimension = new HashMap<>();
		for ( final SpecPair specPair : yFeatures )
		{
			final Dimension dimension = specPair.projectionSpec.projectionDimension;
			final List< SpecPair > list = spPerDimension.computeIfAbsent( dimension, d -> new ArrayList<>() );
			list.add( specPair );
		}
		
		final Map< Dimension, GraphConfig > gcs = new HashMap<>( spPerDimension.size() );
		for ( final Dimension dimension : spPerDimension.keySet() )
		{
			final List< SpecPair > list = spPerDimension.get( dimension );
			final GraphConfig gc = new GraphConfig( xFeature, list, trackOfSelection, connect );
			gcs.put( dimension, gc );
		}
		return Collections.unmodifiableMap( gcs );
	}

	@Override
	public String toString()
	{
		return super.toString() +
				"\n - xFeature: " + xFeature +
				"\n - yFeatures: " + yFeatures +
				"\n - track of selection: " + trackOfSelection +
				"\n - do connect: " + connect;
	}
}
