package org.mastodon.feature.update;

import org.mastodon.collection.RefCollection;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefSet;

/**
 * Data class that stores the object that have been modified and their
 * neighbors,
 * <p>
 * The content of these modifications can be obtained via the {@link #get()} and
 * the {@link #getNeighbors()} methods.
 *
 * @param <O>
 *            the type of objects whose modifications are tracked.
 */
public class Update< O >
{

	private final RefSet< O > modified;

	private final RefSet< O > neighbors;

	Update( final RefCollection< O > pool )
	{
		this( RefCollections.createRefSet( pool ), RefCollections.createRefSet( pool ) );
	}

	/**
	 * For deserialization only.
	 */
	Update( final RefSet< O > modified, final RefSet< O > neighbors )
	{
		this.modified = modified;
		this.neighbors = neighbors;
	}

	void add( final O obj )
	{
		modified.add( obj );
		neighbors.remove( obj );
	}

	void addAsNeighbor( final O obj )
	{
		neighbors.add( obj );
	}

	void concatenate( final Update< O > other )
	{
		modified.addAll( other.modified );
		neighbors.addAll( other.neighbors );
		neighbors.removeAll( modified );
	}

	public RefSet< O > get()
	{
		return modified;
	}

	public RefSet< O > getNeighbors()
	{
		return neighbors;
	}

	public void remove( final O obj )
	{
		/*
		 * We still have to remove them in case we are operating on an
		 * object-based data structure, that does not automatically remove an
		 * object from all collections when it is deleted.
		 */
		modified.remove( obj );
		neighbors.remove( obj );
	}

	public void clear()
	{
		modified.clear();
		neighbors.clear();
	}

	@Override
	public String toString()
	{
		final StringBuilder str = new StringBuilder( super.toString() + ":" );
		str.append( "\n - modified:  " + modified );
		str.append( "\n - neighbors: " + neighbors );
		return str.toString();
	}

}
