package net.trackmate.graph;

import gnu.trove.map.hash.TIntIntHashMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import net.trackmate.graph.collection.RefRefMap;
import net.trackmate.graph.mempool.MappedElement;

public class PoolObjectPoolObjectMap< K extends PoolObject< K, TK >, L extends PoolObject< L, TL >, TK extends MappedElement, TL extends MappedElement > implements RefRefMap< K, L >
{

	private final TIntIntHashMap indexmap;

	private final Pool< K, TK > keyPool;

	private final Pool< L, TL > valuePool;

	public PoolObjectPoolObjectMap( final Pool< K, TK > keyPool, final Pool< L, TL > valuePool )
	{
		this.indexmap = new TIntIntHashMap();
		this.keyPool = keyPool;
		this.valuePool = valuePool;
	}

	/*
	 * METHODS
	 */

	@Override
	public void clear()
	{
		indexmap.clear();
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public boolean containsKey( final Object key )
	{
		if ( key != null && key instanceof PoolObject )
			return indexmap.containsKey( ( ( K ) key ).getInternalPoolIndex() );
		else
			return false;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public boolean containsValue( final Object value )
	{
		if ( value != null && value instanceof PoolObject )
			return indexmap.containsValue( ( ( L ) value ).getInternalPoolIndex() );
		else
			return false;
	}

	@Override
	public Set< java.util.Map.Entry< K, L >> entrySet()
	{
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public L get( final Object key )
	{
		return get( key, valuePool.createRef() );
	}

	@Override
	public L get( final Object key, final L ref )
	{
		if ( key != null && key instanceof PoolObject )
		{
			@SuppressWarnings( "unchecked" )
			final int index = indexmap.get( ( ( K ) key ).getInternalPoolIndex() );
			ref.updateAccess( valuePool.getMemPool(), index );
			return ref;
		}
		else
		{
			return null;
		}
	}

	@Override
	public boolean isEmpty()
	{
		return indexmap.isEmpty();
	}

	@Override
	public Set< K > keySet()
	{
		return new PoolObjectSet< K, TK >( keyPool, indexmap.keySet() );
	}

	@Override
	public L put( final K key, final L value, final L ref )
	{
		final int index = indexmap.put( key.getInternalPoolIndex(), value.getInternalPoolIndex() );
		ref.updateAccess( valuePool.getMemPool(), index );
		return ref;
	}

	@Override
	public L put( final K key, final L value )
	{
		return put( key, value, valuePool.createRef() );
	}

	@Override
	public void putAll( final Map< ? extends K, ? extends L > m )
	{
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public L remove( final Object key, final L ref )
	{
		if ( key != null && key instanceof PoolObject )
		{
			@SuppressWarnings( "unchecked" )
			final int index = indexmap.remove( ( ( K ) key ).getInternalPoolIndex() );
			ref.updateAccess( valuePool.getMemPool(), index );
			return ref;
		}
		else
		{
			return null;
		}
	}

	@Override
	public L remove( final Object key )
	{
		return remove( key, valuePool.createRef() );
	}

	@Override
	public int size()
	{
		return indexmap.size();
	}

	@Override
	public Collection< L > values()
	{
		throw new UnsupportedOperationException( "Cannot return a collection view of the values." );
	}

	@Override
	public K createRef()
	{
		return keyPool.createRef();
	}

	@Override
	public void releaseRef( final K obj )
	{
		keyPool.releaseRef( obj );
	}

	@Override
	public L createValueRef()
	{
		return valuePool.createRef();
	}

	@Override
	public void releaseValueRef( final L obj )
	{
		valuePool.releaseRef( obj );
	}

}
