package org.mastodon.collection;

import java.util.NoSuchElementException;

/**
 * A stack thas is a {@link RefCollection}. Provides standard {@code push()}, {@code pop()}, and {@code peek()} methods
 * as well as variants that take object references that can be used for
 * retrieval. Depending on concrete implementation, these object references can
 * be cleared, ignored or re-used.
 *
 * @param <O>
 *            the type of elements maintained by this stack.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public interface RefStack< O > extends RefCollection< O >
{
    /**
     * Pushes an element onto the top of this stack.
     *
     * @param obj the element to push.
     */
	public void push( O obj );

    /**
     * Retrieves, but does not remove, the object at the top of this stack, or
     * returns {@code null} if this stack is empty.
     *
	 * @return the object at the top of this stack or
     *         {@code null} if this stack is empty.
     */
    public O peek();

    /**
     * Retrieves, but does not remove, the object at the top of this stack, or
     * returns {@code null} if this stack is empty.
     *
	 * <p>
	 * This method is a (potentially) allocation-free version of
	 * {@link #peek()}.
	 *
	 * @param obj
	 *            an object reference that can be used for retrieval. Depending
	 *            on concrete implementation, this object can be cleared,
	 *            ignored or re-used.
	 * @return the object at the top of this stack or
     *         {@code null} if this stack is empty. The object
	 *         actually returned might be the one specified as parameter,
	 *         depending on concrete implementation.
     */
    public O peek( final O obj );

	/**
	 * Removes and returns the object at the top of this stack.
	 *
	 * @return the object at the top of this stack.
	 * @throws NoSuchElementException
	 *             if this stack is empty.
	 */
	public O pop();

	/**
	 * Removes and returns the object at the top of this stack.
	 *
	 * <p>
	 * This method is a (potentially) allocation-free version of
	 * {@link #pop()}.
	 *
	 * @param obj
	 *            an object reference that can be used for retrieval. Depending
	 *            on concrete implementation, this object can be cleared,
	 *            ignored or re-used.
	 * @return the object at the top of this stack. The object
	 *         actually returned might be the one specified as parameter,
	 *         depending on concrete implementation.
	 * @throws NoSuchElementException
	 *             if this stack is empty.
	 */
	public O pop( final O obj );

	/**
	 * Returns the 1-based position where an object is on this stack. If the
	 * object {@code obj} occurs as an item in this stack, this method
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
	public int search( final Object obj );
}
