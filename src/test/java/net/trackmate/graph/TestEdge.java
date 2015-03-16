package net.trackmate.graph;

import net.trackmate.graph.mempool.ByteMappedElement;

public class TestEdge extends AbstractEdge< TestEdge, TestVertex, ByteMappedElement >
{
	protected static final int SIZE_IN_BYTES = AbstractEdge.SIZE_IN_BYTES;

	protected TestEdge( final AbstractEdgePool< TestEdge, TestVertex, ByteMappedElement > pool )
	{
		super( pool );
	}

	@Override
	public String toString()
	{
		final TestVertex v = this.vertexPool.createRef();
		final StringBuilder sb = new StringBuilder();
		sb.append( "e(" );
		getSource( v );
		sb.append( v.getId() );
		sb.append( " -> " );
		getTarget( v );
		sb.append( v.getId() );
		sb.append( ")" );
		this.vertexPool.releaseRef( v );
		return sb.toString();
	}
}