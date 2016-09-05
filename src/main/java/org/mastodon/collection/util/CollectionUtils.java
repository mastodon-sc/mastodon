package org.mastodon.collection.util;

import static org.mastodon.collection.RefCollections.wrap;
import static org.mastodon.collection.RefCollections.wrapAsStack;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import org.mastodon.RefPool;
import org.mastodon.collection.IntRefMap;
import org.mastodon.collection.RefCollection;
import org.mastodon.collection.RefDeque;
import org.mastodon.collection.RefDoubleMap;
import org.mastodon.collection.RefIntMap;
import org.mastodon.collection.RefList;
import org.mastodon.collection.RefObjectMap;
import org.mastodon.collection.RefRefMap;
import org.mastodon.collection.RefSet;
import org.mastodon.collection.RefStack;
import org.mastodon.collection.ref.IntRefHashMap;
import org.mastodon.collection.ref.RefArrayDeque;
import org.mastodon.collection.ref.RefArrayList;
import org.mastodon.collection.ref.RefArrayStack;
import org.mastodon.collection.ref.RefDoubleHashMap;
import org.mastodon.collection.ref.RefIntHashMap;
import org.mastodon.collection.ref.RefObjectHashMap;
import org.mastodon.collection.ref.RefPoolBackedRefCollection;
import org.mastodon.collection.ref.RefRefHashMap;
import org.mastodon.collection.ref.RefSetImp;
import org.mastodon.collection.wrap.IntRefMapWrapper;
import org.mastodon.collection.wrap.RefCollectionWrapper;
import org.mastodon.collection.wrap.RefDoubleMapWrapper;
import org.mastodon.collection.wrap.RefIntMapWrapper;
import org.mastodon.pool.Pool;

/**
 * Static utility methods to create collections for objects of a specified
 * {@link RefCollection}.
 * <p>
 * This specified {@link RefCollection} is for example the collection of
 * vertices of a graph. Depending on the implementation, this vertex collection
 * could be a {@link Pool} or a wrapped standard {@link java.util.Collection} (see {@link RefCollectionWrapper} etc).
 * <p>
 * If specified {@link RefCollection} implements interfaces for providing specific
 * {@link RefCollection} implementations (e.g., {@link ListCreator}) these
 * specific implementations are used. Otherwise, standard {@code java.util}
 * {@link Collections} are created and wrapped as {@link RefCollection}.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class CollectionUtils
{
	public static < O > RefSet< O > createRefSet( final RefCollection< O > collection )
	{
		final RefPool< O > pool = tryGetRefPool( collection );
		if ( pool != null )
			return new RefSetImp<>( pool );
		else
			return wrap( new HashSet< O >() );
	}

	public static < O > RefSet< O > createRefSet( final RefCollection< O > collection, final int initialCapacity )
	{
		final RefPool< O > pool = tryGetRefPool( collection );
		if ( pool != null )
			return new RefSetImp<>( pool, initialCapacity );
		else
			return wrap( new HashSet< O >( initialCapacity ) );
	}

	public static < O > RefList< O > createRefList( final RefCollection< O > collection )
	{
		final RefPool< O > pool = tryGetRefPool( collection );
		if ( pool != null )
			return new RefArrayList<>( pool );
		else
			return wrap( new ArrayList< O >() );
	}

	public static < O > RefList< O > createRefList( final RefCollection< O > collection, final int initialCapacity )
	{
		final RefPool< O > pool = tryGetRefPool( collection );
		if ( pool != null )
			return new RefArrayList<>( pool, initialCapacity );
		else
			return wrap( new ArrayList< O >( initialCapacity ) );
	}

	public static < O > RefDeque< O > createRefDeque( final RefCollection< O > collection )
	{
		final RefPool< O > pool = tryGetRefPool( collection );
		if ( pool != null )
			return new RefArrayDeque<>( pool );
		else
			return wrap( new ArrayDeque< O >() );
	}

	public static < O > RefDeque< O > createRefDeque( final RefCollection< O > collection, final int initialCapacity )
	{
		final RefPool< O > pool = tryGetRefPool( collection );
		if ( pool != null )
			return new RefArrayDeque<>( pool, initialCapacity );
		else
			return wrap( new ArrayDeque< O >( initialCapacity ) );
	}

	public static < O > RefStack< O > createRefStack( final RefCollection< O > collection )
	{
		final RefPool< O > pool = tryGetRefPool( collection );
		if ( pool != null )
			return new RefArrayStack<>( pool );
		else
			return wrapAsStack( new ArrayDeque< O >() );
	}

	public static < O > RefStack< O > createRefStack( final RefCollection< O > collection, final int initialCapacity )
	{
		final RefPool< O > pool = tryGetRefPool( collection );
		if ( pool != null )
			return new RefArrayStack<>( pool, initialCapacity );
		else
			return wrapAsStack( new ArrayDeque< O >( initialCapacity ) );
	}

	public static < O, T > RefObjectMap< O, T > createRefObjectMap( final RefCollection< O > collection )
	{
		final RefPool< O > pool = tryGetRefPool( collection );
		if ( pool != null )
			return new RefObjectHashMap<>( pool );
		else
			return wrap( new HashMap< O, T >() );
	}

	public static < O, T > RefObjectMap< O, T > createRefObjectMap( final RefCollection< O > collection, final int initialCapacity )
	{
		final RefPool< O > pool = tryGetRefPool( collection );
		if ( pool != null )
			return new RefObjectHashMap<>( pool, initialCapacity );
		else
			return wrap( new HashMap< O, T >( initialCapacity ) );
	}

	public static < O > RefRefMap< O, O > createRefRefMap( final RefCollection< O > collection )
	{
		final RefPool< O > pool = tryGetRefPool( collection );
		if ( pool != null )
			return new RefRefHashMap<>( pool, pool );
		else
			return wrap( new HashMap< O, O >() );
	}

	public static < O > RefRefMap< O, O > createRefRefMap( final RefCollection< O > collection, final int initialCapacity )
	{
		final RefPool< O > pool = tryGetRefPool( collection );
		if ( pool != null )
			return new RefRefHashMap<>( pool, pool, initialCapacity );
		else
			return wrap( new HashMap< O, O >( initialCapacity ) );
	}

	public static < O > RefIntMap< O > createRefIntMap( final RefCollection< O > collection, final int noEntryValue )
	{
		final RefPool< O > pool = tryGetRefPool( collection );
		if ( pool != null )
			return new RefIntHashMap<>( pool, noEntryValue );
		else
			return new RefIntMapWrapper<>( noEntryValue );
	}

	public static < O > RefIntMap< O > createRefIntMap( final RefCollection< O > collection, final int noEntryValue, final int initialCapacity )
	{
		final RefPool< O > pool = tryGetRefPool( collection );
		if ( pool != null )
			return new RefIntHashMap<>( pool, noEntryValue, initialCapacity );
		else
			return new RefIntMapWrapper<>( noEntryValue, initialCapacity );
	}

	public static < O > RefDoubleMap< O > createRefDoubleMap( final RefCollection< O > collection, final double noEntryValue )
	{
		final RefPool< O > pool = tryGetRefPool( collection );
		if ( pool != null )
			return new RefDoubleHashMap<>( pool, noEntryValue );
		else
			return new RefDoubleMapWrapper<>( noEntryValue );
	}

	public static < O > RefDoubleMap< O > createRefDoubleMap( final RefCollection< O > collection, final double noEntryValue, final int initialCapacity )
	{
		final RefPool< O > pool = tryGetRefPool( collection );
		if ( pool != null )
			return new RefDoubleHashMap<>( pool, noEntryValue, initialCapacity );
		else
			return new RefDoubleMapWrapper<>( noEntryValue, initialCapacity );
	}

	public static < O > IntRefMap< O > createIntRefMap( final RefCollection< O > collection, final int noEntryKey )
	{
		final RefPool< O > pool = tryGetRefPool( collection );
		if ( pool != null )
			return new IntRefHashMap<>( pool, noEntryKey );
		else
			return new IntRefMapWrapper<>( noEntryKey );
	}

	public static < O > IntRefMap< O > createIntRefMap( final RefCollection< O > collection, final int noEntryKey, final int initialCapacity )
	{
		final RefPool< O > pool = tryGetRefPool( collection );
		if ( pool != null )
			return new IntRefHashMap<>( pool, noEntryKey, initialCapacity );
		else
			return new IntRefMapWrapper<>( noEntryKey, initialCapacity );
	}

	private static < O > RefPool< O > tryGetRefPool( final RefCollection< O > collection )
	{
		return ( collection instanceof RefPoolBackedRefCollection )
				? ( (org.mastodon.collection.ref.RefPoolBackedRefCollection< O > ) collection ).getRefPool()
				: null;
	}
}
