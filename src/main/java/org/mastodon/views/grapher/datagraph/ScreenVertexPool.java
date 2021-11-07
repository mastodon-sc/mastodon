package org.mastodon.views.grapher.datagraph;

import org.mastodon.RefPool;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.ByteMappedElementArray;
import org.mastodon.pool.Pool;
import org.mastodon.pool.SingleArrayMemPool;
import org.mastodon.pool.attributes.BooleanAttribute;
import org.mastodon.pool.attributes.ByteAttribute;
import org.mastodon.pool.attributes.DoubleAttribute;
import org.mastodon.pool.attributes.IndexAttribute;
import org.mastodon.pool.attributes.IntAttribute;
import org.mastodon.properties.ObjPropertyMap;

public class ScreenVertexPool extends Pool< ScreenVertex, ByteMappedElement >
{
	final RefPool< DataVertex > dataVertexPool;

	final IndexAttribute< ScreenVertex > origVertex = new IndexAttribute<>( ScreenVertex.layout.origVertex, this );
	final DoubleAttribute< ScreenVertex > xOffset = new DoubleAttribute<>( ScreenVertex.layout.xOffset, this );
	final DoubleAttribute< ScreenVertex > yOffset = new DoubleAttribute<>( ScreenVertex.layout.yOffset, this );
	final DoubleAttribute< ScreenVertex > vertexDist = new DoubleAttribute<>( ScreenVertex.layout.vertexDist, this );
	final BooleanAttribute< ScreenVertex > selected = new BooleanAttribute<>( ScreenVertex.layout.selected, this );
	final BooleanAttribute< ScreenVertex > ghost = new BooleanAttribute<>( ScreenVertex.layout.ghost, this );
	final ByteAttribute< ScreenVertex > transition = new ByteAttribute<>( ScreenVertex.layout.transition, this );
	final IndexAttribute< ScreenVertex > ipScreenVertex = new IndexAttribute<>( ScreenVertex.layout.ipScreenVertex, this );
	final DoubleAttribute< ScreenVertex > ipRatio = new DoubleAttribute<>( ScreenVertex.layout.ipRatio, this );
	final ObjPropertyMap< ScreenVertex, String > label = new ObjPropertyMap<>( this );
	final IntAttribute< ScreenVertex > color = new IntAttribute<>( ScreenVertex.layout.color, this );

	public ScreenVertexPool( final int initialCapacity, final RefPool< DataVertex > dataVertexPool )
	{
		super( initialCapacity, ScreenVertex.layout, ScreenVertex.class, SingleArrayMemPool.factory( ByteMappedElementArray.factory ) );
		this.dataVertexPool = dataVertexPool;
	}

	@Override
	protected ScreenVertex createEmptyRef()
	{
		return new ScreenVertex( this );
	}

	@Override
	public ScreenVertex create( final ScreenVertex vertex )
	{
		return super.create( vertex );
	}

	@Override
	public void delete( final ScreenVertex vertex )
	{
		super.delete( vertex );
	}
}