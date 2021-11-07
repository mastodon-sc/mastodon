package org.mastodon.views.grapher.datagraph;

import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.ByteMappedElementArray;
import org.mastodon.pool.Pool;
import org.mastodon.pool.SingleArrayMemPool;
import org.mastodon.pool.attributes.DoubleAttribute;

public class ScreenVertexRangePool extends Pool< ScreenVertexRange, ByteMappedElement >
{
	final DoubleAttribute< ScreenVertexRange > minX = new DoubleAttribute<>( ScreenVertexRange.layout.minX, this );
	final DoubleAttribute< ScreenVertexRange > maxX = new DoubleAttribute<>( ScreenVertexRange.layout.maxX, this );
	final DoubleAttribute< ScreenVertexRange > minY = new DoubleAttribute<>( ScreenVertexRange.layout.minY, this );
	final DoubleAttribute< ScreenVertexRange > maxY = new DoubleAttribute<>( ScreenVertexRange.layout.maxY, this );

	public ScreenVertexRangePool( final int initialCapacity )
	{
		super( initialCapacity, ScreenVertexRange.layout, ScreenVertexRange.class, SingleArrayMemPool.factory( ByteMappedElementArray.factory ) );
	}

	@Override
	protected ScreenVertexRange createEmptyRef()
	{
		return new ScreenVertexRange( this );
	}

	@Override
	public ScreenVertexRange create( final ScreenVertexRange vertex )
	{
		return super.create( vertex );
	}

	@Override
	public void delete( final ScreenVertexRange vertex )
	{
		super.delete( vertex );
	}
}