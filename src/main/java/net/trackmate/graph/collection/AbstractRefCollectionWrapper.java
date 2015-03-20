package net.trackmate.graph.collection;

import java.util.Collection;
import java.util.Iterator;

public abstract class AbstractRefCollectionWrapper< O, C extends Collection<  O  > > implements RefCollection< O >
{
	protected final C collection;

	AbstractRefCollectionWrapper( final C collection )
	{
		this.collection = collection;
	}

	@Override
	public int size()
	{
		return collection.size();
	}

	@Override
	public boolean isEmpty()
	{
		return collection.isEmpty();
	}

	@Override
	public boolean contains( final Object o )
	{
		return collection.contains( o );
	}

	@Override
	public Iterator< O > iterator()
	{
		return collection.iterator();
	}

	@Override
	public Object[] toArray()
	{
		return collection.toArray();
	}

	@Override
	public < T > T[] toArray( final T[] a )
	{
		return collection.toArray( a );
	}

	@Override
	public boolean add( final O e )
	{
		return collection.add( e );
	}

	@Override
	public boolean remove( final Object o )
	{
		return collection.remove( o );
	}

	@Override
	public boolean containsAll( final Collection< ? > c )
	{
		return collection.containsAll( c );
	}

	@Override
	public boolean addAll( final Collection< ? extends O > c )
	{
		return collection.addAll( c );
	}

	@Override
	public boolean removeAll( final Collection< ? > c )
	{
		return collection.removeAll( c );
	}

	@Override
	public boolean retainAll( final Collection< ? > c )
	{
		return collection.retainAll( c );
	}

	@Override
	public void clear()
	{
		collection.clear();
	}

	@Override
	public O createRef()
	{
		return null;
	}

	@Override
	public void releaseRef( final O obj )
	{}

}
