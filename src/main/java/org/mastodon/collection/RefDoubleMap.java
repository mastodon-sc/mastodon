package org.mastodon.collection;

import org.mastodon.pool.PoolObject;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.procedure.TObjectDoubleProcedure;
import gnu.trove.procedure.TObjectProcedure;

/**
 * Interface for maps that associate a key to a simple {@code double} value.
 * <p>
 * This interface and its implementations exist to take advantage of the compact
 * data storage offered on one side by the Trove library for standard object,
 * and of a derivative of a Trove class to deal specifically and advantageously
 * with {@link PoolObject}s. Here, we therefore decorate the Trove mother
 * interface with extra methods that accept an existing Ref object to control
 * garbage collection.
 *
 * @author Jean-Yves Tinevez&lt;jeanyves.tinevez@gmail.com&gt;
 *
 * @param <K>
 *            the type of the keys of the map.
 */
public interface RefDoubleMap< K > extends TObjectDoubleMap< K >
{

	/**
	 * Creates an object reference that can be used for processing with this
	 * map. Depending on concrete implementation, the object return can be
	 * {@code null}.
	 *
	 * @return a new object empty reference.
	 */
	public K createRef();

	/**
	 * Releases a previously created object reference. For standard object maps,
	 * this method does nothing.
	 *
	 * @param obj
	 *            the object reference to release.
	 */
	public void releaseRef( final K obj );

	/**
	 * Executes <tt>procedure</tt> for each key in the map.
	 *
	 * @param procedure
	 *            a {@code TIntProcedure} value.
	 * @param ref
	 *            an object reference that can be used for retrieval. Depending
	 *            on concrete implementation, this object can be cleared,
	 *            ignored or re-used.
	 * @return {@code false} if the loop over the keys terminated because
	 *         the procedure returned false for some key.
	 */
	public boolean forEachKey( TObjectProcedure< ? super K > procedure, K ref );

	/**
	 * Executes <tt>procedure</tt> for each key/value entry in the map.
	 *
	 * @param procedure
	 *            a {@code TObjectDoubleProcedure} value.
	 * @param ref
	 *            an object reference that can be used for retrieval. Depending
	 *            on concrete implementation, this object can be cleared,
	 *            ignored or re-used.
	 * @return {@code false} if the loop over the entries terminated because the
	 *         procedure returned false for some entry.
	 */
	public boolean forEachEntry( TObjectDoubleProcedure< ? super K > procedure, K ref );

	/**
	 * Retains only those entries in the map for which the procedure returns a
	 * true value.
	 *
	 * @param procedure
	 *            determines which entries to keep.
	 * @param ref
	 *            an object reference that can be used for retrieval. Depending
	 *            on concrete implementation, this object can be cleared,
	 *            ignored or re-used.
	 * @return {@code true} if the map was modified.
	 */
	public boolean retainEntries( TObjectDoubleProcedure< ? super K > procedure, K ref );

}
