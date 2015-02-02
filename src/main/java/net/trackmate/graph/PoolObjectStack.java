package net.trackmate.graph;

import gnu.trove.list.array.TIntArrayList;
import net.trackmate.graph.mempool.MappedElement;

/**
 * A stack implementation for {@link PoolObject}s entirely based on a
 * {@link PoolObjectList}.
 * 
 * @author Jean-Yves Tinevez
 * 
 * @param <O>
 *            recursive type of the {@link PoolObject}s stored in this stack.
 * @param <T>
 *            the MappedElement type.
 */
public class PoolObjectStack< O extends PoolObject< O, T >, T extends MappedElement > extends PoolObjectList< O, T >
{

	/*
	 * CONSTRUCTOR
	 */

	/**
	 * Instantiates an empty stack for the specified pool with default capacity.
	 * 
	 * @param pool
	 *            the pool to draw objects from in order to build this stack.
	 */
	public PoolObjectStack( final Pool< O, T > pool )
	{
		super( pool );
	}

	/**
	 * Instantiates an empty stack for the specified pool.
	 * 
	 * @param pool
	 *            the pool to draw objects from in order to build this stack.
	 * @param initialCapacity
	 *            the initial capacity.
	 */
	public PoolObjectStack( final Pool< O, T > pool, final int initialCapacity )
	{
		super( pool, initialCapacity );
	}

	protected PoolObjectStack( final PoolObjectList< O, T > list, final TIntArrayList indexSubList )
	{
		super( list, indexSubList );
	}

	/*
	 * METHODS
	 */

	/**
	 * Tests if this stack is empty.
	 * 
	 * @return <code>true</code> if and only if this stack contains no items;
	 *         <code>false</code> otherwise.
	 */
	public boolean empty()
	{
		return size() < 1;
	}

	/**
	 * Looks at the object at the top of this stack without removing it from the
	 * stack.
	 * 
	 * @param obj
	 *            the pool object ref to update with the content of the top
	 *            item.
	 * @return the given object, updated to be the item at the top of this
	 *         stack.
	 * @throws ArrayIndexOutOfBoundsException
	 *             if this stack is empty.
	 */
	public O peek( final O obj )
	{
		return get( size() - 1, obj );
	}

	/**
	 * Removes the object at the top of this stack and returns that object as
	 * the value of this function.
	 * 
	 * @param obj
	 *            the pool object ref to update with the content of the top
	 *            item.
	 * @return the given object, updated to be the item at the top of this
	 *         stack.
	 * @throws ArrayIndexOutOfBoundsException
	 *             if this stack is empty.
	 */
	public O pop( final O obj )
	{
		return remove( size() - 1, obj );
	}

	/**
	 * Pushes an item onto the top of this stack.
	 * 
	 * @param obj
	 *            the item to be pushed onto this stack.
	 * @return the item argument.
	 */
	public O push( final O obj )
	{
		add( obj );
		return obj;
	}

	/**
	 * Returns the 1-based position where an object is on this stack. If the
	 * object <code>obj</code> occurs as an item in this stack, this method
	 * returns the distance from the top of the stack of the occurrence nearest
	 * the top of the stack; the topmost item on the stack is considered to be
	 * at distance 1.
	 * 
	 * @param obj
	 *            the desired object.
	 * @return the 1-based position from the top of the stack where the object
	 *         is located; the return value -1 indicates that the object is not
	 *         on the stack.
	 */
	public int search( final Object obj )
	{
		if ( !( obj instanceof PoolObject ) ) { return -1; }
		@SuppressWarnings( "rawtypes" )
		final int value = ( ( PoolObject ) obj ).getInternalPoolIndex();
		final int index = getIndexCollection().lastIndexOf( value );
		if ( index < 0 )
		{
			return -1;
		}
		else
		{
			return size() - index;
		}
	}

}
