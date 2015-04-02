package net.trackmate.graph.listenable;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.collection.CollectionUtils;
import net.trackmate.graph.collection.RefRefMap;
import net.trackmate.graph.collection.RefSet;

public class GraphChangeEvent< V extends Vertex< E >, E extends Edge< V > >
{
	private final Graph< V, E > source;

	private final RefSet< V > vertexAdded;

	private final RefSet< E > edgeAdded;

	private final RefSet< E > edgeRemoved;

	private final RefSet< V > vertexRemoved;

	private final RefRefMap< E, V > previousEdgeSource;

	private final RefRefMap< E, V > previousEdgeTarget;

	GraphChangeEvent( final Graph< V, E > source )
	{
		this.source = source;
		this.vertexAdded = CollectionUtils.createVertexSet( source );
		this.vertexRemoved = CollectionUtils.createVertexSet( source );
		this.edgeAdded = CollectionUtils.createEdgeSet( source );
		this.edgeRemoved = CollectionUtils.createEdgeSet( source );
		this.previousEdgeSource = CollectionUtils.createEdgeVertexMap( source );
		this.previousEdgeTarget = CollectionUtils.createEdgeVertexMap( source );
	}

	void vertexAdded( final V vertex )
	{
		vertexAdded.add( vertex );
	}

	void vertexRemoved( final V vertex )
	{
		vertexRemoved.add( vertex );
	}

	void edgeAdded( final E edge )
	{
		edgeAdded.add( edge );
	}

	void edgeRemoved( final E edge )
	{
		edgeRemoved.add( edge );
		previousEdgeSource.put( edge, edge.getSource() );
		previousEdgeTarget.put( edge, edge.getTarget() );
	}

	public boolean isEmpty()
	{
		return vertexAdded.isEmpty()
				&& edgeAdded.isEmpty()
				&& edgeRemoved.isEmpty()
				&& vertexRemoved.isEmpty();
	}

	public Graph< V, E > getSource()
	{
		return source;
	}

	public RefSet< E > getEdgeAdded()
	{
		return edgeAdded;
	}

	public RefSet< E > getEdgeRemoved()
	{
		return edgeRemoved;
	}

	public RefSet< V > getVertexAdded()
	{
		return vertexAdded;
	}

	public RefSet< V > getVertexRemoved()
	{
		return vertexRemoved;
	}

	public V getPreviousEdgeSource( final E edge )
	{
		return previousEdgeSource.get( edge );
	}

	public V getPreviousEdgeTarget( final E edge )
	{
		return previousEdgeTarget.get( edge );
	}

}
