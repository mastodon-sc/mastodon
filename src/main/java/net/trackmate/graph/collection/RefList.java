package net.trackmate.graph.collection;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * List-like interface for collections that operates on possibly reusable
 * references.
 */
public interface RefList< O > extends RefCollection< O >
{

	/**
	 * Inserts the specified element at the specified position in this list
	 * (optional operation). Shifts the element currently at that position (if
	 * any) and any subsequent elements to the right (adds one to their
	 * indices).
	 *
	 * @param index
	 *            index at which the specified element is to be inserted
	 *            element.
	 * @param obj
	 *            element to be inserted.
	 * @throws UnsupportedOperationException
	 *             if the add operation is not supported by this list.
	 * @throws ClassCastException
	 *             if the class of the specified element prevents it from being
	 *             added to this list.
	 * @throws NullPointerException
	 *             if the specified element is null and this list does not
	 *             permit null elements.
	 * @throws IllegalArgumentException
	 *             if some property of the specified element prevents it from
	 *             being added to this list.
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range (index < 0 || index > size()).
	 */
	public void add( final int index, final O obj );

	/**
	 * Inserts all of the elements in the specified collection into this list at
	 * the specified position (optional operation). Shifts the element currently
	 * at that position (if any) and any subsequent elements to the right
	 * (increases their indices). The new elements will appear in this list in
	 * the order that they are returned by the specified collection's iterator.
	 * The behavior of this operation is undefined if the specified collection
	 * is modified while the operation is in progress. (Note that this will
	 * occur if the specified collection is this list, and it's nonempty.)
	 *
	 * @param index
	 *            index at which to insert the first element from the specified
	 *            collection.
	 * @param objs
	 *            collection containing elements to be added to this list.
	 * @return <code>true</code> if this list changed as a result of the call.
	 * @throws UnsupportedOperationException
	 *             if the addAll operation is not supported by this list.
	 * @throws ClassCastException
	 *             if the class of an element of the specified collection
	 *             prevents it from being added to this list.
	 * @throws NullPointerException
	 *             if the specified collection contains one or more null
	 *             elements and this list does not permit null elements, or if
	 *             the specified collection is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if some property of an element of the specified collection
	 *             prevents it from being added to this list.
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range (index < 0 || index > size()).
	 */
	public boolean addAll( final int index, final Collection< ? extends O > objs );

	/**
	 * Returns the element at the specified position in this list.
	 *
	 * @param index
	 *            index of the element to return.
	 * @param obj
	 *            an object reference that can be used for retrieval. Depending
	 *            on concrete implementation, this object can be cleared,
	 *            ignored or re-used.
	 * @return the element at the specified index. The object actually returned
	 *         might be the one specified as parameter, depending on concrete
	 *         implementation.
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range (index < 0 || index >= size()).
	 */
	public O get( final int index, final O obj );

	/**
	 * Returns a list iterator over the elements in this list (in proper
	 * sequence).
	 *
	 * @return a list iterator over the elements in this list (in proper
	 *         sequence).
	 */
	public ListIterator< O > listIterator();

	/**
	 * Returns a list iterator of the elements in this list (in proper
	 * sequence), starting at the specified position in this list. The specified
	 * index indicates the first element that would be returned by an initial
	 * call to next. An initial call to previous would return the element with
	 * the specified index minus one.
	 *
	 * @param index
	 *            index of first element to be returned from the list iterator
	 *            (by a call to the next method).
	 * @return a list iterator of the elements in this list (in proper
	 *         sequence), starting at the specified position in this list.
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range (index < 0 || index > size()).
	 */
	public ListIterator< O > listIterator( final int index );

	/**
	 * Removes the element at the specified position in this list (optional
	 * operation). Shifts any subsequent elements to the left (subtracts one
	 * from their indices). Returns the element that was removed from the list.
	 *
	 * @param index
	 *            the index of the element to be removed.
	 * @param obj
	 *            an object reference that can be used for retrieval. Depending
	 *            on concrete implementation, this object can be cleared,
	 *            ignored or re-used.
	 * @return the element previously at the specified position.
	 * @throws UnsupportedOperationException
	 *             if the remove operation is not supported by this list.
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range (index < 0 || index >= size()).
	 */
	public O remove( final int index, final O obj );

	/**
	 * Replaces the element at the specified position in this list with the
	 * specified element (optional operation).
	 *
	 * @param index
	 *            index of the element to replace.
	 * @param obj
	 *            an object reference that can be used for retrieval. Depending
	 *            on concrete implementation, this object can be cleared,
	 *            ignored or re-used.
	 * @param replacedObj
	 *            element to be stored at the specified position.
	 * @return the element previously at the specified position.
	 * @throws UnsupportedOperationException
	 *             if the set operation is not supported by this list.
	 * @throws ClassCastException
	 *             if the class of the specified element prevents it from being
	 *             added to this list.
	 * @throws NullPointerException
	 *             if the specified element is null and this list does not
	 *             permit null elements.
	 * @throws IllegalArgumentException
	 *             if some property of the specified element prevents it from
	 *             being added to this list.
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range (index < 0 || index >= size()).
	 */
	public O set( final int index, final O obj, final O replacedObj );

	/**
	 * Returns a view of the portion of this list between the specified
	 * fromIndex, inclusive, and toIndex, exclusive. (If <code>fromIndex</code>
	 * and <code>toIndex</code> are equal, the returned list is empty.) The
	 * returned list is backed by this list, so non-structural changes in the
	 * returned list are reflected in this list, and vice-versa. The returned
	 * list supports all of the optional list operations supported by this list.
	 * <p>
	 * This method eliminates the need for explicit range operations (of the
	 * sort that commonly exist for arrays). Any operation that expects a list
	 * can be used as a range operation by passing a subList view instead of a
	 * whole list. For example, the following idiom removes a range of elements
	 * from a list:
	 *
	 * <pre>
	 * list.subList( from, to ).clear();
	 * </pre>
	 *
	 * Similar idioms may be constructed for <code>indexOf</code> and
	 * <code>lastIndexOf</code>, and all of the algorithms in the Collections
	 * class can be applied to a subList.
	 * <p>
	 * The semantics of the list returned by this method become undefined if the
	 * backing list (i.e., this list) is <i>structurally modified</i> in any way
	 * other than via the returned list. (Structural modifications are those
	 * that change the size of this list, or otherwise perturb it in such a
	 * fashion that iterations in progress may yield incorrect results.)
	 *
	 * @param fromIndex
	 *            low endpoint (inclusive) of the subList.
	 * @param toIndex
	 *            high endpoint (exclusive) of the subList.
	 * @return a view of the specified range within this list.
	 * @throws IndexOutOfBoundsException
	 *             for an illegal endpoint index value (fromIndex < 0 || toIndex
	 *             > size || fromIndex > toIndex).
	 */
	public List< O > subList( final int fromIndex, final int toIndex );

}
