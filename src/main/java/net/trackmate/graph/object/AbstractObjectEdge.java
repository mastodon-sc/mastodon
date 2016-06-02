package net.trackmate.graph.object;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Vertex;

public abstract class AbstractObjectEdge< E extends AbstractObjectEdge< E, V >, V extends Vertex< ? > > implements Edge< V >
{
	private final V source;

	private final V target;

	protected AbstractObjectEdge( final V source, final V target )
	{
		this.source = source;
		this.target = target;
	}

	@Override
	public V getSource()
	{
		return source;
	}

	@Override
	public V getSource( final V vertex )
	{
		return source;
	}

	@Override
	public int getSourceOutIndex()
	{
		int outIndex = 0;
		for ( final Object e : source.outgoingEdges() )
		{
			if ( e.equals( this ) )
				break;
			++outIndex;
		}
		return outIndex;
	}

	@Override
	public V getTarget()
	{
		return target;
	}

	@Override
	public V getTarget( final V vertex )
	{
		return target;
	}

	@Override
	public int getTargetInIndex()
	{
		int inIndex = 0;
		for ( final Object e : target.incomingEdges() )
		{
			if ( e.equals( this ) )
				break;
			++inIndex;
		}
		return inIndex;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "e(" );
		sb.append( source.toString() );
		sb.append( " -> " );
		sb.append( target.toString() );
		sb.append( ")" );
		return sb.toString();
	}
}
