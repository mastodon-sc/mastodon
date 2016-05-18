package net.trackmate;

//TODO: javadoc
public interface RefPool< O >
{
	/**
	 * Generates an object reference.
	 *
	 * @return a new, uninitialized, reference object.
	 */
	public O createRef();

	/**
	 * Releases a previously created reference object.
	 *
	 * @param obj
	 *            the reference object to release.
	 */
	public void releaseRef( final O obj );

	/**
	 * Make {@code obj} refer to the object at {@code index} in the pool.
	 *
	 * @param id
	 *            internal pool index.
	 * @param obj
	 *            reference that will refer to object at {@code index} when the
	 *            method returns.
	 */
	public O getObject( final int id, final O obj );

	/**
	 * TODO javadoc
	 *
	 * TODO rename?
	 *
	 * @return
	 */
	public Class< O > getRefClass();
}
