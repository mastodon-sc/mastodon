package org.mastodon.revised.ui.coloring.feature;

import java.util.ArrayList;
import java.util.Collection;

import org.mastodon.app.ui.settings.style.Style;
import org.mastodon.mamut.feature.SpotNLinksFeature;
import org.mastodon.revised.ui.coloring.ColorMap;
import org.mastodon.util.Listeners;

/**
 * Data class that stores a configuration for coloring graph objects based on
 * feature values.
 *
 * @author Jean-Yves Tinevez
 */
public class FeatureColorMode implements Style< FeatureColorMode >
{

	/**
	 * Supported modes for edge coloring.
	 */
	public enum EdgeColorMode
	{
		/**
		 * Each edge has a color that depends on a numerical feature defined for
		 * this edge.
		 */
		EDGE( "Edge" ),
		/**
		 * Edges have a color determined by a numerical feature of their source
		 * vertex.
		 */
		SOURCE_VERTEX( "Source vertex" ),
		/**
		 * Edges have a color determined by a numerical feature of their target
		 * vertex.
		 */
		TARGET_VERTEX( "Target vertex" ),
		/**
		 * Edges are painted with a default color.
		 */
		NONE( "Default" );

		private final String label;

		private EdgeColorMode( final String label )
		{
			this.label = label;
		}

		@Override
		public String toString()
		{
			return label;
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
		VERTEX( "Vertex" ),
		/**
		 * Vertices have a color determined by a numerical feature of their
		 * incoming edge, iff they have exactly one incoming edge.
		 */
		INCOMING_EDGE( "Incoming edge" ),
		/**
		 * Vertices have a color determined by a numerical feature of their
		 * outgoing edge, iff they have exactly one outgoing edge.
		 */
		OUTGOING_EDGE( "Outgoing edge" ),
		/**
		 * Vertices are painted with a default color.
		 */
		NONE( "Default" );

		private final String label;

		private VertexColorMode( final String label )
		{
			this.label = label;
		}

		@Override
		public String toString()
		{
			return label;
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
	private String[] vertexFeatureProjection;

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
	private String[] edgeFeatureProjection;

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

	public synchronized void setVertexFeatureProjection( final String vertexFeatureKey, final String vertexProjectionKey )
	{
		if ( !this.vertexFeatureProjection[ 0 ].equals( vertexFeatureKey ) || !this.vertexFeatureProjection[ 1 ].equals( vertexProjectionKey ) )
		{
			this.vertexFeatureProjection = new String[] { vertexFeatureKey, vertexProjectionKey };
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

	public synchronized void setEdgeFeatureProjection( final String edgeFeatureKey, final String edgeProjectionKey )
	{
		if ( !this.edgeFeatureProjection[ 0 ].equals( edgeFeatureKey ) || !this.edgeFeatureProjection[ 1 ].equals( edgeProjectionKey ) )
		{
			this.edgeFeatureProjection = new String[] { edgeFeatureKey, edgeProjectionKey };
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

	public String[] getVertexFeatureProjection()
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

	public String[] getEdgeFeatureProjection()
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
		final StringBuilder str = new StringBuilder( super.toString() );
		str.append( "\n  name: " + name );
		str.append( "\n  vertex color mode: " + vertexColorMode );
		str.append( "\n  vertex feature projection: " + vertexFeatureProjection[ 0 ] + " -> " + vertexFeatureProjection[ 1 ] );
		str.append( "\n  vertex color map: " + vertexColorMap );
		str.append( String.format( "\n  vertex feature range: [ %.1f - %.1f ]", vertexRangeMin, vertexRangeMax ) );
		str.append( "\n  edge color mode: " + edgeColorMode );
		str.append( "\n  edge feature projection: " + edgeFeatureProjection[ 0 ] + " -> " + edgeFeatureProjection[ 1 ] );
		str.append( "\n  edge color map: " + edgeColorMap );
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
		N_LINKS.vertexFeatureProjection = new String[] { SpotNLinksFeature.KEY, SpotNLinksFeature.KEY };
		N_LINKS.vertexRangeMin = 0;
		N_LINKS.vertexRangeMax = 3;
		N_LINKS.edgeColorMode = EdgeColorMode.SOURCE_VERTEX;
		N_LINKS.edgeColorMap = N_LINKS.vertexColorMap;
		N_LINKS.edgeFeatureProjection = N_LINKS.vertexFeatureProjection;
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
}
