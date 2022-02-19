package org.mastodon.views.grapher.datagraph;

import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.ByteMappedElementArray;
import org.mastodon.pool.Pool;
import org.mastodon.pool.SingleArrayMemPool;
import org.mastodon.pool.attributes.BooleanAttribute;
import org.mastodon.pool.attributes.ByteAttribute;
import org.mastodon.pool.attributes.DoubleAttribute;
import org.mastodon.pool.attributes.IndexAttribute;
import org.mastodon.pool.attributes.IntAttribute;

public class ScreenEdgePool extends Pool< ScreenEdge, ByteMappedElement >
{
	final IndexAttribute< ScreenEdge > origEdge = new IndexAttribute<>( ScreenEdge.layout.origEdge, this );
	final IndexAttribute< ScreenEdge > sourceScreenVertex = new IndexAttribute<>( ScreenEdge.layout.sourceScreenVertex, this );
	final IndexAttribute< ScreenEdge > targetScreenVertex = new IndexAttribute<>( ScreenEdge.layout.targetScreenVertex, this );
	final BooleanAttribute< ScreenEdge > selected = new BooleanAttribute<>( ScreenEdge.layout.selected, this );
	final ByteAttribute< ScreenEdge > transition = new ByteAttribute<>( ScreenEdge.layout.transition, this );
	final DoubleAttribute< ScreenEdge > ipRatio = new DoubleAttribute<>( ScreenEdge.layout.ipRatio, this );
	final IntAttribute< ScreenEdge > color = new IntAttribute<>( ScreenEdge.layout.color, this );

	public ScreenEdgePool( final int initialCapacity )
	{
		super( initialCapacity, ScreenEdge.layout, ScreenEdge.class, SingleArrayMemPool.factory( ByteMappedElementArray.factory ) );
	}

	@Override
	protected ScreenEdge createEmptyRef()
	{
		return new ScreenEdge( this );
	}

	@Override
	public ScreenEdge create( final ScreenEdge edge )
	{
		return super.create( edge );
	}

	@Override
	public void delete( final ScreenEdge edge )
	{
		super.delete( edge );
	}
}