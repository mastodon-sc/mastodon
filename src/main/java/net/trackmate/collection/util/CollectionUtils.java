package net.trackmate.collection.util;

import java.util.Collections;

import net.trackmate.collection.IntRefMap;
import net.trackmate.collection.RefCollection;
import net.trackmate.collection.RefDeque;
import net.trackmate.collection.RefDoubleMap;
import net.trackmate.collection.RefIntMap;
import net.trackmate.collection.RefList;
import net.trackmate.collection.RefObjectMap;
import net.trackmate.collection.RefRefMap;
import net.trackmate.collection.RefSet;
import net.trackmate.collection.RefStack;
import net.trackmate.graph.ReadOnlyGraph;

/**
 * Static utility methods to create {@link RefCollection}s of vertices and edges
 * of a {@link ReadOnlyGraph}.
 * <p>
 * If the graph implements interfaces for providing specific
 * {@link RefCollection} implementations (e.g., {@link ListCreator}) these
 * specific implementations are used. Otherwise, standard {@code java.util}
 * {@link Collections} are created and wrapped as {@link RefCollection}.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class CollectionUtils
{
	public static interface SetCreator< O >
	{
		public RefSet< O > createRefSet();

		public RefSet< O > createRefSet( final int initialCapacity );
	}

	public static interface ListCreator< O >
	{
		public RefList< O > createRefList();

		public RefList< O > createRefList( final int initialCapacity );
	}

	public static interface DequeCreator< O >
	{
		public RefDeque< O > createRefDeque();

		public RefDeque< O > createRefDeque( final int initialCapacity );
	}

	public static interface StackCreator< O >
	{
		public RefStack< O > createRefStack();

		public RefStack< O > createRefStack( final int initialCapacity );
	}

	public static interface MapCreator< O >
	{
		public < T > RefObjectMap< O, T > createRefObjectMap();

		public < T > RefObjectMap< O, T > createRefObjectMap( final int initialCapacity );

		public RefRefMap< O, O > createRefRefMap();

		public RefRefMap< O, O > createRefRefMap( final int initialCapacity );

		public RefIntMap< O > createRefIntMap( final int noEntryValue );

		public RefIntMap< O > createRefIntMap( final int noEntryValue, final int initialCapacity );

		public IntRefMap< O > createIntRefMap( final int noEntryKey );

		public IntRefMap< O > createIntRefMap( final int noEntryKey, final int initialCapacity );

		public RefDoubleMap< O > createRefDoubleMap( final double noEntryValue );

		public RefDoubleMap< O > createRefDoubleMap( final double noEntryValue, final int initialCapacity );
	}

	public static interface CollectionCreator< O > extends
			SetCreator< O >,
			ListCreator< O >,
			DequeCreator< O >,
			StackCreator< O >,
			MapCreator< O >
	{}
}
