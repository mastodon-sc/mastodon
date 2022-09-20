/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.ui.coloring.feature;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import org.mastodon.app.ui.settings.style.Style;
import org.mastodon.ui.coloring.ColorMap;
import org.scijava.listeners.Listeners;
import org.yaml.snakeyaml.Yaml;

/**
 * Data class that stores a configuration for coloring graph objects based on
 * feature values.
 *
 * @author Jean-Yves Tinevez
 */
public class FeatureColorMode implements Style< FeatureColorMode >
{

	/**
	 * Constant for the key of a feature that builds the default color mode.
	 * We hardcode it here is that we do not have to depend on concrete implementations.
	 */
	private static final String DEFAULT_KEY = "Spot N links";

	/**
	 * Supported modes for edge coloring.
	 */
	public enum EdgeColorMode
	{
		/**
		 * Each edge has a color that depends on a numerical feature defined for
		 * this edge.
		 */
		EDGE( "Edge", TargetType.EDGE ),
		/**
		 * Edges have a color determined by a numerical feature of their source
		 * vertex.
		 */
		SOURCE_VERTEX( "Source vertex", TargetType.VERTEX ),
		/**
		 * Edges have a color determined by a numerical feature of their target
		 * vertex.
		 */
		TARGET_VERTEX( "Target vertex", TargetType.VERTEX ),
		/**
		 * Edges are painted with a default color.
		 */
		NONE( "Default", null );

		private final String label;

		private final TargetType targetType;

		private EdgeColorMode( final String label, final TargetType targetType )
		{
			this.label = label;
			this.targetType = targetType;
		}

		@Override
		public String toString()
		{
			return label;
		}

		public TargetType targetType()
		{
			return targetType;
		}
	}

	/**
	 * Supported modes for vertex coloring.
	 */
	public enum VertexColorMode
	{
		/**
		 * Each vertex has a color determined by a numerical feature defined for
		 * this vertex.
		 */
		VERTEX( "Vertex", TargetType.VERTEX ),
		/**
		 * Vertices have a color determined by a numerical feature of their
		 * incoming edge, iff they have exactly one incoming edge.
		 */
		INCOMING_EDGE( "Incoming edge", TargetType.EDGE ),
		/**
		 * Vertices have a color determined by a numerical feature of their
		 * outgoing edge, iff they have exactly one outgoing edge.
		 */
		OUTGOING_EDGE( "Outgoing edge", TargetType.EDGE ),
		/**
		 * Vertices are painted with a default color.
		 */
		NONE( "Default", null );

		private final String label;

		private final TargetType targetType;

		private VertexColorMode( final String label, final TargetType targetType )
		{
			this.label = label;
			this.targetType = targetType;
		}

		@Override
		public String toString()
		{
			return label;
		}

		public TargetType targetType()
		{
			return targetType;
		}
	}

	public interface UpdateListener
	{
		public void featureColorModeChanged();
	}

	private final Listeners.List< UpdateListener > updateListeners;

	private FeatureColorMode()
	{
		updateListeners = new Listeners.SynchronizedList<>();
	}

	private void notifyListeners()
	{
		for ( final UpdateListener l : updateListeners.list )
			l.featureColorModeChanged();
	}

	public Listeners< UpdateListener > updateListeners()
	{
		return updateListeners;
	}

	@Override
	public FeatureColorMode copy()
	{
		return copy( null );
	}

	@Override
	public FeatureColorMode copy( final String name )
	{
		final FeatureColorMode fcm = new FeatureColorMode();
		fcm.set( this );
		if ( name != null )
			fcm.setName( name );
		return fcm;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public synchronized void setName( final String name )
	{
		if ( this.name != name )
		{
			this.name = name;
			notifyListeners();
		}
	}

	public synchronized void set( final FeatureColorMode mode )
	{
		name = mode.name;
		vertexColorMode = mode.vertexColorMode;
		vertexFeatureProjection = mode.vertexFeatureProjection;
		vertexColorMap = mode.vertexColorMap;
		vertexRangeMin = mode.vertexRangeMin;
		vertexRangeMax = mode.vertexRangeMax;
		edgeColorMode = mode.edgeColorMode;
		edgeFeatureProjection = mode.edgeFeatureProjection;
		edgeColorMap = mode.edgeColorMap;
		edgeRangeMin = mode.edgeRangeMin;
		edgeRangeMax = mode.edgeRangeMax;
		notifyListeners();
	}

	/*
	 * COLOR MODE FIELDS.
	 */

	/**
	 * The name of this feature color mode object.
	 */
	private String name;

	/**
	 * The vertex color mode.
	 */
	private VertexColorMode vertexColorMode;

	/**
	 * The key pair for vertex feature projection (feature key, projection of
	 * feature key).
	 */
	private FeatureProjectionId vertexFeatureProjection;

	/**
	 * The key of the color map for vertex feature coloring.
	 */
	private String vertexColorMap;

	/**
	 * The min range value for vertex feature values.
	 */
	private double vertexRangeMin;

	/**
	 * The max range value for vertex feature values.
	 */
	private double vertexRangeMax;

	/**
	 * The edge color mode.
	 */
	private EdgeColorMode edgeColorMode;

	/**
	 * The key pair for edge feature projection (feature key, projection of
	 * feature key).
	 */
	private FeatureProjectionId edgeFeatureProjection;

	/**
	 * The key of the color map for edge feature coloring.
	 */
	private String edgeColorMap;

	/**
	 * The min range value for edge feature values.
	 */
	private double edgeRangeMin;

	/**
	 * The max range value for edge feature values.
	 */
	private double edgeRangeMax;

	public synchronized void setVertexColorMode( final VertexColorMode vertexColorMode )
	{
		if ( this.vertexColorMode != vertexColorMode )
		{
			this.vertexColorMode = vertexColorMode;
			notifyListeners();
		}
	}

	public synchronized void setVertexFeatureProjection( final FeatureProjectionId vertexFeatureProjection )
	{
		if ( !Objects.equals( this.vertexFeatureProjection, vertexFeatureProjection ) )
		{
			this.vertexFeatureProjection = vertexFeatureProjection;
			notifyListeners();
		}
	}

	public synchronized void setVertexColorMap( final String vertexColorMap )
	{
		if ( this.vertexColorMap != vertexColorMap )
		{
			this.vertexColorMap = vertexColorMap;
			notifyListeners();
		}
	}

	public synchronized void setVertexRange( final double vertexRangeMin, final double vertexRangeMax )
	{
		if ( this.vertexRangeMin != vertexRangeMin || this.vertexRangeMax != vertexRangeMax )
		{
			this.vertexRangeMin = Math.min( vertexRangeMin, vertexRangeMax );
			this.vertexRangeMax = Math.max( vertexRangeMin, vertexRangeMax );
			notifyListeners();
		}
	}

	public synchronized void setEdgeColorMode( final EdgeColorMode edgeColorMode )
	{
		if ( this.edgeColorMode != edgeColorMode )
		{
			this.edgeColorMode = edgeColorMode;
			notifyListeners();
		}
	}

	public synchronized void setEdgeFeatureProjection( final FeatureProjectionId edgeFeatureProjection )
	{
		if ( !Objects.equals( this.edgeFeatureProjection, edgeFeatureProjection ) )
		{
			this.edgeFeatureProjection = edgeFeatureProjection;
			notifyListeners();
		}
	}


	public synchronized void setEdgeColorMap( final String edgeColorMap )
	{
		if ( this.edgeColorMap != edgeColorMap )
		{
			this.edgeColorMap = edgeColorMap;
			notifyListeners();
		}
	}

	public synchronized void setEdgeRange( final double edgeRangeMin, final double edgeRangeMax )
	{
		if ( this.edgeRangeMin != edgeRangeMin || this.edgeRangeMax != edgeRangeMax )
		{
			this.edgeRangeMin = Math.min( edgeRangeMin, edgeRangeMax );
			this.edgeRangeMax = Math.max( edgeRangeMin, edgeRangeMax );
			notifyListeners();
		}
	}

	public VertexColorMode getVertexColorMode()
	{
		return vertexColorMode;
	}

	public FeatureProjectionId getVertexFeatureProjection()
	{
		return vertexFeatureProjection;
	}

	public String getVertexColorMap()
	{
		return vertexColorMap;
	}

	public double getVertexRangeMin()
	{
		return vertexRangeMin;
	}

	public double getVertexRangeMax()
	{
		return vertexRangeMax;
	}

	public EdgeColorMode getEdgeColorMode()
	{
		return edgeColorMode;
	}

	public FeatureProjectionId getEdgeFeatureProjection()
	{
		return edgeFeatureProjection;
	}

	public String getEdgeColorMap()
	{
		return edgeColorMap;
	}

	public double getEdgeRangeMin()
	{
		return edgeRangeMin;
	}

	public double getEdgeRangeMax()
	{
		return edgeRangeMax;
	}

	@Override
	public String toString()
	{
		final StringBuilder str = new StringBuilder();
		str.append( "\n  name: " ).append( name );
		str.append( "\n  vertex color mode: " ).append( vertexColorMode );
		str.append( "\n  vertex feature projection: " ).append( vertexFeatureProjection );
		str.append( "\n  vertex color map: " ).append( vertexColorMap );
		str.append( String.format( "\n  vertex feature range: [ %.1f - %.1f ]", vertexRangeMin, vertexRangeMax ) );
		str.append( "\n  edge color mode: " ).append( edgeColorMode );
		str.append( "\n  edge feature projection: " ).append( edgeFeatureProjection );
		str.append( "\n  edge color map: " ).append( edgeColorMap );
		str.append( String.format( "\n  edge feature range: [ %.1f - %.1f ]", edgeRangeMin, edgeRangeMax ) );
		return str.toString();
	}

	/*
	 * DEFAULT FEATURE COLOR MODE LIBRARY.
	 */

	private static final FeatureColorMode N_LINKS;
	static
	{
		N_LINKS = new FeatureColorMode();
		N_LINKS.name = "Number of links";
		N_LINKS.vertexColorMode = VertexColorMode.VERTEX;
		N_LINKS.vertexColorMap = ColorMap.PARULA.getName();
		N_LINKS.vertexFeatureProjection = new FeatureProjectionId( DEFAULT_KEY, DEFAULT_KEY, N_LINKS.vertexColorMode.targetType() );
		N_LINKS.vertexRangeMin = 0;
		N_LINKS.vertexRangeMax = 3;
		N_LINKS.edgeColorMode = EdgeColorMode.SOURCE_VERTEX;
		N_LINKS.edgeColorMap = N_LINKS.vertexColorMap;
		N_LINKS.edgeFeatureProjection = new FeatureProjectionId( DEFAULT_KEY, DEFAULT_KEY, N_LINKS.edgeColorMode.targetType() );
		N_LINKS.edgeRangeMin = N_LINKS.vertexRangeMin;
		N_LINKS.edgeRangeMax = N_LINKS.vertexRangeMax;
	}

	public static final Collection< FeatureColorMode > defaults;
	static
	{
		defaults = new ArrayList<>( 1 );
		defaults.add( N_LINKS );
	}

	public static FeatureColorMode defaultMode()
	{
		return N_LINKS;
	}

	public static void main( final String[] args )
	{
		Yaml yaml = FeatureColorModeIO.createYaml();
		final ArrayList< Object > objects = new ArrayList<>();
		objects.add( N_LINKS );
		final StringWriter writer = new StringWriter();
		yaml.dumpAll( objects.iterator(), writer );

		System.out.println( "writer = " + writer );

		yaml = FeatureColorModeIO.createYaml();
		final Iterable< Object > objs = yaml.loadAll( new StringReader( writer.toString() ) );
		System.out.println( "objs = " + objs.iterator().next() );
	}
}
