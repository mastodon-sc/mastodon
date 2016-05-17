package net.trackmate.graph.collection.wrap;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import net.trackmate.graph.collection.RefList;

/**
 * Wraps a standard {@link List} in a {@link RefList}.
 *
 * @param <O>
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class RefListWrapper< O > extends AbstractRefCollectionWrapper< O, List< O > > implements RefList< O >
{
	public RefListWrapper( final List< O > list )
	{
		super( list );
	}

	@Override
	public boolean addAll( final int index, final Collection< ? extends O > c )
	{
		return collection.addAll( index, c );
	}

	@Override
	public O get( final int index )
	{
		return collection.get( index );
	}

	@Override
	public O set( final int index, final O element )
	{
		return collection.set( index, element );
	}

	@Override
	public void add( final int index, final O element )
	{
		collection.add( index, element );
	}

	@Override
	public O remove( final int index )
	{
		return collection.remove( index );
	}

	@Override
	public int indexOf( final Object o )
	{
		return collection.indexOf( o );
	}

	@Override
	public int lastIndexOf( final Object o )
	{
		return collection.lastIndexOf( o );
	}

	@Override
	public ListIterator< O > listIterator()
	{
		return collection.listIterator();
	}

	@Override
	public ListIterator< O > listIterator( final int index )
	{
		return collection.listIterator( index );
	}

	@Override
	public List< O > subList( final int fromIndex, final int toIndex )
	{
		return collection.subList( fromIndex, toIndex );
	}

	@Override
	public O get( final int index, final O obj )
	{
		return collection.get( index );
	}

	@Override
	public O remove( final int index, final O obj )
	{
		return collection.remove( index );
	}

	@Override
	public O set( final int index, final O obj, final O replacedObj )
	{
		return collection.set( index, obj );
	}

	@Override
	public void shuffle( final Random rand )
	{
		Collections.shuffle( collection, rand );
	}

	@Override
	public void sort( final Comparator< ? super O > comparator )
	{
		Collections.sort( collection, comparator );
	}

	@Override
	public void swap( final int i, final int j )
	{
		Collections.swap( collection, i, j );
	}
}
