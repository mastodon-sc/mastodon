package org.mastodon.feature.update;

import java.util.Collection;
import java.util.LinkedList;

/**
 * A queue with a max fixed size.
 *
 * @author Jean-Yves Tinevez
 *
 * @param <T>
 *            the type of objects in the queue.
 */
public class SizedDeque< T > extends LinkedList< T >
{

	private static final long serialVersionUID = 1L;

	private final int maxSize;

	public SizedDeque( final int size )
	{
		super();
		this.maxSize = size;
	}

	@Override
	public void push( final T e )
	{
		trim( 1 );
		super.push( e );
	}

	@Override
	public boolean add( final T e )
	{
		trim( 1 );
		return super.add( e );
	}

	@Override
	public void add( final int index, final T e )
	{
		trim( 1 );
		super.add( index, e );
	}

	@Override
	public void addFirst( final T e )
	{
		trim( 1 );
		super.addFirst( e );
	}

	@Override
	public void addLast( final T e )
	{
		trim( 1 );
		super.addLast( e );
	}

	@Override
	public boolean addAll( final Collection< ? extends T > c )
	{
		boolean val = super.addAll( c );
		val = trim( 0 ) || val;
		return val;
	}

	@Override
	public boolean addAll( final int index, final Collection< ? extends T > c )
	{
		boolean val = super.addAll( index, c );
		val = trim( 0 ) || val;
		return val;
	}

	private boolean trim( final int sizeToAdd )
	{
		if ( sizeToAdd > maxSize )
		{
			clear();
			return true;
		}
		// If the stack is too big, remove elements until it's the right size.
		boolean val = false;
		while ( size() + sizeToAdd > maxSize )
		{
			removeLast();
			val = true;
		}
		return val;
	}

}
