package org.mastodon.mamut.model;

import org.mastodon.graph.GraphIdBimap;
import org.mastodon.io.properties.StringPropertyMapSerializer;
import org.mastodon.model.AbstractModelGraph;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.properties.PropertyChangeListener;

public class ModelGraph extends AbstractModelGraph< ModelGraph, SpotPool, LinkPool, Spot, Link, ByteMappedElement >
{
	public ModelGraph()
	{
		this( 1000 );
	}

	public ModelGraph( final int initialCapacity )
	{
		super( new LinkPool( initialCapacity, new SpotPool( initialCapacity ) ) );

		vertexPropertySerializers.put( "label", new StringPropertyMapSerializer<>( vertexPool.label ) );
	}

	SpotPool getVertexPool()
	{
		return vertexPool;
	}

	LinkPool getEdgePool()
	{
		return edgePool;
	}

	GraphIdBimap< Spot, Link > idmap()
	{
		return idmap;
	}

	/**
	 * Register a {@link PropertyChangeListener} that will be notified when
	 * a {@code Spot}s label is changed.
	 *
	 * @param listener
	 *            the listener to register.
	 * @return {@code true} if the listener was successfully registered.
	 *         {@code false} if it was already registered.
	 */
	public boolean addVertexLabelListener( final PropertyChangeListener< Spot > listener )
	{
		return vertexPool.label.propertyChangeListeners().add( listener );
	}

	/**
	 * Removes the specified {@link PropertyChangeListener} from the set of
	 * VertexLabel listeners.
	 *
	 * @param listener
	 *            the listener to remove.
	 * @return {@code true} if the listener was present in the listeners of this
	 *         model and was successfully removed.
	 */
	public boolean removeVertexLabelListener( final PropertyChangeListener< Spot > listener )
	{
		return vertexPool.label.propertyChangeListeners().remove( listener );
	}
}
