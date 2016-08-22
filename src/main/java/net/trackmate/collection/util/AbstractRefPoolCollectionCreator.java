package net.trackmate.collection.util;

import java.util.Collection;

import net.trackmate.RefPool;
import net.trackmate.collection.RefCollection;
import net.trackmate.collection.ref.IntRefHashMap;
import net.trackmate.collection.ref.RefArrayDeque;
import net.trackmate.collection.ref.RefArrayList;
import net.trackmate.collection.ref.RefArrayStack;
import net.trackmate.collection.ref.RefDoubleHashMap;
import net.trackmate.collection.ref.RefIntHashMap;
import net.trackmate.collection.ref.RefObjectHashMap;
import net.trackmate.collection.ref.RefRefHashMap;
import net.trackmate.collection.ref.RefSetImp;
import net.trackmate.collection.util.CollectionUtils.CollectionCreator;

/**
 * Base class for wrappers of {@link RefPool} that offer access to collections.
 * <p>
 * This class wraps a {@link RefPool} and offers methods to generate various
 * collections based on the wrapped pool. It offers a bridge between the
 * {@link RefPool} framework and the Java {@link Collection} framework.
 * <p>
 * This class implements the {@link RefCollection} interface itself, and
 * therefore allows for questing the underlying pool using the
 * {@link Collection} methods. Only the {@code isEmpty(),} {@code size(),}
 * {@code iterator(),} {@code createRef()}, and {@code releaseRef()} methods are
 * guaranteed to be implemented.
 * <p>
 * Other {@link Collection} methods that are unsuited for
 * pools throw an {@link UnsupportedOperationException}:
 * <ul>
 * <li>{@link #contains(Object)}
 * <li>{@link #containsAll(Collection)}
 * <li>{@link #toArray()}
 * <li>{@link #toArray(Object[])}
 * <li>{@link #add(Object)}
 * <li>{@link #addAll(Collection)}
 * <li>{@link #remove(Object)}
 * <li>{@link #removeAll(Collection)}
 * <li>{@link #retainAll(Collection)}
 * <li>{@link #clear()}
 * </ul>
 * If these methods are needed, it is probably best to create an adequate
 * collection from the pool using the <i>create*</i> methods.
 *
 * @param <O>
 *            the type of the pool object used in the wrapped {@link RefPool}.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public abstract class AbstractRefPoolCollectionCreator< O, P extends RefPool< O > > implements CollectionCreator< O >
{
	protected final P pool;

	public AbstractRefPoolCollectionCreator( final P pool )
	{
		this.pool = pool;
	}

	@Override
	public O createRef()
	{
		return pool.createRef();
	}

	@Override
	public void releaseRef( final O obj )
	{
		pool.releaseRef( obj );
	}

	@Override
	public boolean isEmpty()
	{
		return size() == 0;
	}

	/*
	 * The remaining RefCollection methods throw UnsupportedOperationException.
	 * Some of them could be implemented, but it is probably not a good idea to
	 * use the Pool as a Collection in this way.
	 */

	/**
	 * This method is inapplicable to {@link RefPool}s and throw an
	 * {@link UnsupportedOperationException}.
	 */
	@Override
	public boolean contains( final Object o )
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * This method is inapplicable to {@link RefPool}s and throw an
	 * {@link UnsupportedOperationException}.
	 */
	@Override
	public Object[] toArray()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * This method is inapplicable to {@link RefPool}s and throw an
	 * {@link UnsupportedOperationException}.
	 */
	@Override
	public < T > T[] toArray( final T[] a )
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * This method is inapplicable to {@link RefPool}s and throw an
	 * {@link UnsupportedOperationException}.
	 */
	@Override
	public boolean add( final O e )
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * This method is inapplicable to {@link RefPool}s and throw an
	 * {@link UnsupportedOperationException}.
	 */
	@Override
	public boolean remove( final Object o )
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * This method is inapplicable to {@link RefPool}s and throw an
	 * {@link UnsupportedOperationException}.
	 */
	@Override
	public boolean containsAll( final Collection< ? > c )
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * This method is inapplicable to {@link RefPool}s and throw an
	 * {@link UnsupportedOperationException}.
	 */
	@Override
	public boolean addAll( final Collection< ? extends O > c )
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * This method is inapplicable to {@link RefPool}s and throw an
	 * {@link UnsupportedOperationException}.
	 */
	@Override
	public boolean removeAll( final Collection< ? > c )
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * This method is inapplicable to {@link RefPool}s and throw an
	 * {@link UnsupportedOperationException}.
	 */
	@Override
	public boolean retainAll( final Collection< ? > c )
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * This method is inapplicable to {@link RefPool}s and throw an
	 * {@link UnsupportedOperationException}.
	 */
	@Override
	public void clear()
	{
		throw new UnsupportedOperationException();
	}

	/*
	 * SetCreator
	 */

	@Override
	public RefSetImp< O > createRefSet()
	{
		return new RefSetImp< O >( pool );
	}

	@Override
	public RefSetImp< O > createRefSet( final int initialCapacity )
	{
		return new RefSetImp< O >( pool, initialCapacity );
	}

	/*
	 * ListCreator
	 */

	@Override
	public RefArrayList< O > createRefList()
	{
		return new RefArrayList< O >( pool );
	}

	@Override
	public RefArrayList< O > createRefList( final int initialCapacity )
	{
		return new RefArrayList< O >( pool, initialCapacity );
	}

	/*
	 * DequeCreator
	 */

	@Override
	public RefArrayDeque< O > createRefDeque()
	{
		return new RefArrayDeque< O >( pool );
	}

	@Override
	public RefArrayDeque< O > createRefDeque( final int initialCapacity )
	{
		return new RefArrayDeque< O >( pool, initialCapacity );
	}

	/*
	 * StackCreator
	 */

	@Override
	public RefArrayStack< O > createRefStack()
	{
		return new RefArrayStack< O >( pool );
	}

	@Override
	public RefArrayStack< O > createRefStack( final int initialCapacity )
	{
		return new RefArrayStack< O >( pool, initialCapacity );
	}

	/*
	 * MapCreator
	 */

	@Override
	public < T > RefObjectHashMap< O, T > createRefObjectMap()
	{
		return new RefObjectHashMap<>( pool );
	}

	@Override
	public < T > RefObjectHashMap< O, T > createRefObjectMap( final int initialCapacity )
	{
		return new RefObjectHashMap<>( pool, initialCapacity );
	}

	@Override
	public RefRefHashMap< O, O > createRefRefMap()
	{
		return new RefRefHashMap<>( pool, pool );
	}

	@Override
	public RefRefHashMap< O, O > createRefRefMap( final int initialCapacity )
	{
		return new RefRefHashMap<>( pool, pool, initialCapacity );
	}

	@Override
	public RefIntHashMap< O > createRefIntMap(final int noEntryValue )
	{
		return new RefIntHashMap< O >( pool, noEntryValue );
	}

	@Override
	public RefIntHashMap< O > createRefIntMap( final int noEntryValue, final int initialCapacity )
	{
		return new RefIntHashMap< O >( pool, noEntryValue, initialCapacity );
	}

	@Override
	public IntRefHashMap< O > createIntRefMap( final int noEntryKey )
	{
		return new IntRefHashMap< O >( pool, noEntryKey );
	}

	@Override
	public IntRefHashMap< O > createIntRefMap( final int noEntryKey, final int initialCapacity )
	{
		return new IntRefHashMap< O >( pool, noEntryKey, initialCapacity );
	}

	@Override
	public RefDoubleHashMap< O > createRefDoubleMap( final double noEntryValue )
	{
		return new RefDoubleHashMap< >( pool, noEntryValue );
	}

	@Override
	public RefDoubleHashMap< O > createRefDoubleMap( final double noEntryValue, final int initialCapacity )
	{
		return new RefDoubleHashMap< >( pool, noEntryValue, initialCapacity );
	}
}
