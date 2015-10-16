package net.trackmate.graph;


/**
 * Bidirectional mapping between integer IDs and objects.
 *
 * <p>
 * Implementations:
 * <ul>
 * <li>A mapping between {@link PoolObject}s and their internal pool
 * indices. Implemented in {@link PoolObjectIdBimap}.</li>
 * <li>a mapping between Java objects and IDs that are assigned upon first
 * access. Not implemented yet.</li>
 * </ul>
 *
 * <p>
 * TODO: in which package should this be?
 *
 * @param <O>
 *            the object type.
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public interface IdBimap< O >
{
	public int getId( O o );

	public O getObject( int id, O ref );
}