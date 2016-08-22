package net.trackmate.revised.model.mamut;

import java.util.ArrayList;

import net.trackmate.graph.GraphIdBimap;
import net.trackmate.graph.features.Features;
import net.trackmate.graph.ref.AbstractListenableEdgePool;
import net.trackmate.pool.ByteMappedElement;
import net.trackmate.pool.ByteMappedElementArray;
import net.trackmate.pool.MemPool;
import net.trackmate.pool.PoolObject;
import net.trackmate.pool.SingleArrayMemPool;
import net.trackmate.revised.model.AbstractModelGraph;
import net.trackmate.revised.model.AbstractSpotPool;
import net.trackmate.undo.attributes.Attribute;
import net.trackmate.undo.attributes.Attributes;

public class ModelGraph extends AbstractModelGraph< ModelGraph, SpotPool, LinkPool, Spot, Link, ByteMappedElement >
{
	private final ArrayList< SpotRadiusListener > spotRadiusListeners;

	public final Attribute< Spot > VERTEX_COVARIANCE;

	public ModelGraph()
	{
		this( 1000 );
	}

	public ModelGraph( final int initialCapacity )
	{
		super( new LinkPool( initialCapacity, new SpotPool( initialCapacity ) ) );
		spotRadiusListeners = new ArrayList<>();

		VERTEX_COVARIANCE = vertexAttributes.createAttribute( Spot.createCovarianceAttributeSerializer(), "vertex covariance" );
	}

	Features< Spot > vertexFeatures()
	{
		return vertexFeatures;
	}

	Features< Link > edgeFeatures()
	{
		return edgeFeatures;
	}

	Attributes< Spot > vertexAttributes()
	{
		return vertexAttributes;
	}

	Attributes< Link > edgeAttributes()
	{
		return edgeAttributes;
	}

	GraphIdBimap< Spot, Link > idmap()
	{
		return idmap;
	}

	void notifyBeforeVertexCovarianceChange( final Spot spot )
	{
		vertexAttributes.notifyBeforeAttributeChange( VERTEX_COVARIANCE, spot );
	}

	void notifyRadiusChanged( final Spot spot )
	{
		for ( final SpotRadiusListener l : spotRadiusListeners )
			l.radiusChanged( spot );
	}

	/**
	 * Register a {@link SpotRadiusListener} that will be notified when
	 * a Spot's radius is changed.
	 *
	 * @param listener
	 *            the listener to register.
	 * @return {@code true} if the listener was successfully registered.
	 *         {@code false} if it was already registered.
	 */
	public boolean addSpotRadiusListener( final SpotRadiusListener listener )
	{
		if ( ! spotRadiusListeners.contains( listener ) )
		{
			spotRadiusListeners.add( listener );
			return true;
		}
		return false;
	}

	/**
	 * Removes the specified {@link SpotRadiusListener} from the set of
	 * listeners.
	 *
	 * @param listener
	 *            the listener to remove.
	 * @return {@code true} if the listener was present in the listeners of this
	 *         model and was successfully removed.
	 */
	public boolean removeSpotRadiusListener( final SpotRadiusListener listener )
	{
		return spotRadiusListeners.remove( listener );
	}
}

class SpotPool extends AbstractSpotPool< Spot, Link, ByteMappedElement, ModelGraph >
{
	SpotPool( final int initialCapacity )
	{
		this( initialCapacity, new SpotFactory() );
	}

	private SpotPool( final int initialCapacity, final SpotFactory f )
	{
		super( 3, initialCapacity, f );
		f.vertexPool = this;
	}

	private static class SpotFactory implements PoolObject.Factory< Spot, ByteMappedElement >
	{
		private SpotPool vertexPool;

		@Override
		public int getSizeInBytes()
		{
			return Spot.SIZE_IN_BYTES;
		}

		@Override
		public Spot createEmptyRef()
		{
			return new Spot( vertexPool );
		}

		@Override
		public MemPool.Factory< ByteMappedElement > getMemPoolFactory()
		{
			return SingleArrayMemPool.factory( ByteMappedElementArray.factory );
		}

		@Override
		public Class< Spot > getRefClass()
		{
			return Spot.class;
		}
	};
}

class LinkPool extends AbstractListenableEdgePool< Link, Spot, ByteMappedElement >
{
	LinkPool( final int initialCapacity, final SpotPool vertexPool )
	{
		this( initialCapacity, new LinkFactory(), vertexPool );
	}

	private LinkPool( final int initialCapacity, final LinkPool.LinkFactory f, final SpotPool vertexPool )
	{
		super( initialCapacity, f, vertexPool );
		f.edgePool = this;
	}

	private static class LinkFactory implements PoolObject.Factory< Link, ByteMappedElement >
	{
		private LinkPool edgePool;

		@Override
		public int getSizeInBytes()
		{
			return Link.SIZE_IN_BYTES;
		}

		@Override
		public Link createEmptyRef()
		{
			return new Link( edgePool );
		}

		@Override
		public MemPool.Factory< ByteMappedElement > getMemPoolFactory()
		{
			return SingleArrayMemPool.factory( ByteMappedElementArray.factory );
		}

		@Override
		public Class< Link > getRefClass()
		{
			return Link.class;
		}
	};
}
