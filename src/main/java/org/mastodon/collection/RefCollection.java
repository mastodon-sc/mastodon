package org.mastodon.collection;

import java.util.Collection;

/**
 * Interface for collections that can manage reference objects.
 * 
 * @param <O>
 *            the type of the object to manage in this collection.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public interface RefCollection< O > extends Collection< O >
{
	/**
	 * Generates an object reference that can be used for retrieval. Depending
	 * on concrete implementation, this object returned can be
	 * {@code null.}
	 * 
	 * @return a new, uninitialized, reference object.
	 */
	public O createRef();

	/**
	 * Releases a previously created reference object. Depending on concrete
	 * implementation, this method might not do anything.
	 * 
	 * @param obj
	 *            the reference object to release.
	 */
	public void releaseRef( final O obj );
}
