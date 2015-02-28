package net.trackmate.graph.collection;

import java.util.Collection;
import java.util.Iterator;

public interface RefCollection< O >
{
	/**
	 * Appends the specified element to the end of this list (optional
	 * operation).
	 * <p>
	 * Collections that support this operation may place limitations on what
	 * elements may be added to this collection. In particular, some collections
	 * will refuse to add <code>null</code> elements, and others will impose
	 * restrictions on the type of elements that may be added. Collection
	 * classes should clearly specify in their documentation any restrictions on
	 * what elements may be added.
	 *
	 * @param obj
	 *            element to be appended to this collection.
	 * @return <code>true</code> if the collection was modified by the add
	 *         operation.
	 * @throws UnsupportedOperationException
	 *             if the add operation is not supported by this collection.
	 * @throws ClassCastException
	 *             if the class of the specified element prevents it from being
	 *             added to this collection.
	 * @throws NullPointerException
	 *             if the specified element is null and this collection does not
	 *             permit null elements.
	 * @throws IllegalArgumentException
	 *             if some property of this element prevents it from being added
	 *             to this collection.
	 */
	public boolean add( final O obj );

	/**
	 * Appends all of the elements in the specified collection to this
	 * collection.
	 *
	 * @param objs
	 *            collection containing elements to be added to this collection.
	 * @return <code>true</code> if this list changed as a result of the call.
	 * @throws UnsupportedOperationException
	 *             if the addAll operation is not supported by this list
	 * @throws ClassCastException
	 *             if the class of an element of the specified collection
	 *             prevents it from being added to this list.
	 * @throws NullPointerException
	 *             if the specified collection contains one or more
	 *             <code>null</code> elements and this list does not permit null
	 *             elements, or if the specified collection is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if some property of an element of the specified collection
	 *             prevents it from being added to this collection.
	 */
	public boolean addAll( final Collection< ? extends O > objs );

	/**
	 * Returns <code>true</code> if this collection contains the specified
	 * element. More formally, returns true if and only if this collection
	 * contains at least one element e such that <code>
	 * ( o == null ? e == null : o.equals( e ) )
	 * </code>.
	 *
	 * @param obj
	 *            element whose presence in this list is to be tested.
	 *
	 * @return <code>true</code> if this list contains the specified element.
	 * @throws ClassCastException
	 *             if the type of the specified element is incompatible with
	 *             this list (optional).
	 * @throws NullPointerException
	 *             if the specified element is <code>null</code> and this list
	 *             does not permit null elements (optional).
	 */
	public boolean contains( final Object obj );

	/**
	 * Returns <code>true</code> if this collection contains all of the elements
	 * of the specified collection.
	 *
	 * @param objs
	 *            collection to be checked for containment in this collection.
	 * @return <code>true</code> if this list contains all of the elements of
	 *         the specified collection.
	 * @throws ClassCastException
	 *             if the types of one or more elements in the specified
	 *             collection are incompatible with this list (optional).
	 * @throws NullPointerException
	 *             if the specified collection contains one or more
	 *             <code>null</code> elements and this list does not permit
	 *             <code>null</code> elements (optional), or if the specified
	 *             collection is <code>null</code>.
	 */
	public boolean containsAll( final Collection< ? > objs );

	/**
	 * Tests whether this collection contains any values.
	 *
	 * @return <code>true</code> if the collection is empty.
	 */
	public boolean isEmpty();

	/**
	 * Returns an iterator over the elements in this collection.
	 *
	 * @return an iterator over the elements in this collection.
	 */
	public Iterator< O > iterator();

	/**
	 * Sets the size of the collection to 0, but does not change its capacity.
	 * This method can be used as an alternative to the {@link #clear()} method
	 * if you want to recycle a list without allocating new backing arrays.
	 */
	public void reset();

	/**
	 * Sets the size of the collection to 0, but does not change its capacity.
	 * This method can be used as an alternative to the {@link #clear()} method
	 * if you want to recycle a list without allocating new backing arrays.
	 * <p>
	 * This method differs from {@link #reset()} in that it does not clear the
	 * old values in the backing array. Thus, it is possible for getQuick to
	 * return stale data if this method is used and the caller is careless about
	 * bounds checking.
	 */
	public void resetQuick();

	/**
	 * Returns the number of elements in this collection. If this collection
	 * contains more than Integer.MAX_VALUE elements, returns Integer.MAX_VALUE.
	 *
	 * @return the number of elements in this list.
	 */
	public int size();

}
