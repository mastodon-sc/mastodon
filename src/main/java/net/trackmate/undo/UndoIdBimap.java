package net.trackmate.undo;

import gnu.trove.impl.Constants;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import net.trackmate.RefPool;

/**
 * TODO
 *
 * @param <O>
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class UndoIdBimap< O > implements RefPool< O >
{
	/**
	 * value used to declare that the requested value is not in the map.
	 */
	public static final int NO_ENTRY_VALUE = -1;

	private final TIntIntMap undoIdToObjectId;

	private final TIntIntMap objectIdToUndoId;

	private final RefPool< O > idmap;

	private int idgen;

	/**
	 * TODO
	 *
	 * @param idmap
	 */
	public UndoIdBimap( final RefPool< O > idmap )
	{
		this.idmap = idmap;
		undoIdToObjectId = new TIntIntHashMap( Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, NO_ENTRY_VALUE, NO_ENTRY_VALUE );
		objectIdToUndoId = new TIntIntHashMap( Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, NO_ENTRY_VALUE, NO_ENTRY_VALUE );
		idgen = 0;
	}

	/**
	 * TODO
	 *
	 * Creates new id if {@code o} is not in map yet.
	 *
	 * @param o
	 * @return
	 */
	@Override
	public synchronized int getId( final O o )
	{
		final int objectId = idmap.getId( o );
		int undoId = objectIdToUndoId.get( objectId );
		if ( undoId == NO_ENTRY_VALUE )
		{
			undoId = idgen++;
			objectIdToUndoId.put( objectId, undoId );
			undoIdToObjectId.put( undoId, objectId );
		}
		return undoId;
	}

	/**
	 * TODO
	 *
	 * @param undoId
	 * @param o
	 */
	public synchronized void put( final int undoId, final O o )
	{
		final int objectId = idmap.getId( o );
		objectIdToUndoId.put( objectId, undoId );
		undoIdToObjectId.put( undoId, objectId );
	}

	/**
	 * TODO
	 *
	 * @param undoId
	 * @param ref
	 * @return
	 */
	@Override
	public O getObject( final int undoId, final O ref )
	{
		final int objectId = undoIdToObjectId.get( undoId );
		if ( objectId == NO_ENTRY_VALUE )
			return null;
		else
			return idmap.getObject( objectId, ref );
	}

	@Override
	public O createRef()
	{
		return idmap.createRef();
	}

	@Override
	public void releaseRef( final O ref )
	{
		idmap.releaseRef( ref );
	}

	@Override
	public Class< O > getRefClass()
	{
		return idmap.getRefClass();
	}
}
