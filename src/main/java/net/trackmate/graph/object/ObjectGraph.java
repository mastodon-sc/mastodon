package net.trackmate.graph.object;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import net.trackmate.graph.zzgraphinterfaces.Graph;

public class ObjectGraph< K > implements Graph< ObjectVertex< K >, ObjectEdge< K > >
{

	private final Collection< ObjectVertex< K > > vertices = new HashSet< ObjectVertex< K > >();

	private final Collection< ObjectEdge< K >> edges = new HashSet< ObjectEdge< K > >();

	@Override
	public ObjectVertex< K > addVertex()
	{
		final ObjectVertex< K > v = new ObjectVertex< K >();
		vertices.add( v );
		return v;
	}

	@Override
	public ObjectVertex< K > addVertex( final ObjectVertex< K > vertex )
	{
		return addVertex();
	}

	@Override
	public ObjectEdge< K > addEdge( final ObjectVertex< K > source, final ObjectVertex< K > target )
	{
		if ( !vertices.contains( source ) ) { throw new IllegalArgumentException( "Source vertex " + source + " does not belong to this graph." ); }
		if ( !vertices.contains( target ) ) { throw new IllegalArgumentException( "Target vertex " + target + " does not belong to this graph." ); }
		final ObjectEdge< K > edge = new ObjectEdge< K >( source, target );
		source.outgoingEdges().add( edge );
		source.edges().add( edge );
		target.incomingEdges().add( edge );
		target.edges().add( edge );
		edges.add( edge );
		return edge;
	}

	@Override
	public ObjectEdge< K > addEdge( final ObjectVertex< K > source, final ObjectVertex< K > target, final ObjectEdge< K > edge )
	{
		return addEdge( source, target );
	}

	@Override
	public ObjectEdge< K > insertEdge( final ObjectVertex< K > source, final int sourceOutIndex, final ObjectVertex< K > target, final int targetInIndex )
	{
		if ( !vertices.contains( source ) ) { throw new IllegalArgumentException( "Source vertex " + source + " does not belong to this graph." ); }
		if ( !vertices.contains( target ) ) { throw new IllegalArgumentException( "Target vertex " + target + " does not belong to this graph." ); }
		final ObjectEdge< K > edge = new ObjectEdge< K >( source, target );
		source.outgoingEdges().add( Math.min( Math.max( 0, sourceOutIndex ), source.outgoingEdges().size() ), edge );
		source.edges().add( edge );
		target.incomingEdges().add( Math.min( Math.max( 0, targetInIndex ), target.incomingEdges().size() ), edge );
		target.edges().add( edge );
		edges.add( edge );
		return edge;
	}

	@Override
	public ObjectEdge< K > insertEdge( final ObjectVertex< K > source, final int sourceOutIndex, final ObjectVertex< K > target, final int targetInIndex, final ObjectEdge< K > edge )
	{
		return insertEdge( source, sourceOutIndex, target, targetInIndex );
	}

	@Override
	public ObjectEdge< K > getEdge( final ObjectVertex< K > source, final ObjectVertex< K > target )
	{
		for ( final ObjectEdge< K > e : source.outgoingEdges() )
		{
			if ( target.incomingEdges().contains( e ) ) { return e; }
		}
		return null;
	}

	@Override
	public ObjectEdge< K > getEdge( final ObjectVertex< K > source, final ObjectVertex< K > target, final ObjectEdge< K > edge )
	{
		return getEdge( source, target );
	}

	@Override
	public void remove( final ObjectVertex< K > vertex )
	{
		if ( vertices.remove( vertex ) )
		{
			for ( final ObjectEdge< K > e : vertex.incomingEdges() )
			{
				e.getSource().edges().remove( e );
				e.getSource().outgoingEdges().remove( e );
			}
			for ( final ObjectEdge< K > e : vertex.outgoingEdges() )
			{
				e.getTarget().edges().remove( e );
				e.getTarget().incomingEdges().remove( e );
			}
		}
	}

	@Override
	public void remove( final ObjectEdge< K > edge )
	{
		if ( edges.remove( edge ) )
		{
			edge.getSource().outgoingEdges().remove( edge );
			edge.getSource().edges().remove( edge );
			edge.getTarget().incomingEdges().remove( edge );
			edge.getTarget().edges().remove( edge );
		}
	}

	@Override
	public void removeAllLinkedEdges( final ObjectVertex< K > vertex )
	{
		final ArrayList< ObjectEdge< K > > edges = new ArrayList< ObjectEdge< K > >( vertex.edges() );
		for ( final ObjectEdge< K > edge : edges )
		{
			remove( edge );
		}
	}

	@Override
	public Iterator< ObjectVertex< K > > vertexIterator()
	{
		return vertices.iterator();
	}

	@Override
	public Iterator< ObjectEdge< K > > edgeIterator()
	{
		return edges.iterator();
	}

	@Override
	public Collection< ObjectVertex< K > > vertices()
	{
		return Collections.unmodifiableCollection( vertices );
	}

	@Override
	public Collection< ObjectEdge< K > > edges()
	{
		return Collections.unmodifiableCollection( edges );
	}

	@Override
	public ObjectVertex< K > vertexRef()
	{
		return null;
	}

	@Override
	public ObjectEdge< K > edgeRef()
	{
		return null;
	}

	@Override
	public void releaseRef( final ObjectVertex< K > ref )
	{}

	@Override
	public void releaseRef( final ObjectEdge< K > ref )
	{}

	@Override
	public void releaseRef( final ObjectVertex< K >... refs )
	{}

	@Override
	public void releaseRef( final ObjectEdge< K >... refs )
	{}

	public Collection< ObjectVertex< K >> getVertices()
	{
		return vertices;
	}

	public Collection< ObjectEdge< K >> getEdges()
	{
		return edges;
	}
}
