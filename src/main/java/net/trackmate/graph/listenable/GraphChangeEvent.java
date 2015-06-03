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

	void edgeRemoved( final E edge, V source, V target )
	{
		edgeRemoved.add( edge );
		previousEdgeSource.put( edge, source );
		previousEdgeTarget.put( edge, target );
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

	public V getPreviousEdgeSource( final E edge, final V tmpRef )
	{
		return previousEdgeSource.get( edge, tmpRef );
	}

	public V getPreviousEdgeSource( final E edge )
	{
		return getPreviousEdgeSource( edge, source.vertexRef() );
	}

	public V getPreviousEdgeTarget( final E edge, final V tmpRef )
	{
		return previousEdgeTarget.get( edge, tmpRef );
	}

	public V getPreviousEdgeTarget( final E edge )
	{
		return getPreviousEdgeTarget( edge, source.vertexRef() );
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( super.toString() + "\n" );
		sb.append( " - Source = " + source.toString() + "\n" );
		if ( vertexAdded.isEmpty() )
		{
			sb.append( " - No vertices added.\n" );
		}
		else
		{
			sb.append( " - Vertices added: " + vertexAdded + "\n" );
		}
		if ( vertexRemoved.isEmpty() )
		{
			sb.append( " - No vertices removed.\n" );
		}
		else
		{
			sb.append( " - Vertices removed: " + vertexRemoved + "\n" );
		}
		if ( edgeAdded.isEmpty() )
		{
			sb.append( " - No edges added.\n" );
		}
		else
		{
			sb.append( " - Edges added: " + edgeAdded + "\n" );
		}
		if ( edgeRemoved.isEmpty() )
		{
			sb.append( " - No edges removed.\n" );
		}
		else
		{
			sb.append( " - Edges removed: [" );
			for ( final E e : edgeRemoved )
			{
				sb.append( "e(" );
				sb.append( previousEdgeSource.get( e ) + "->" + previousEdgeTarget.get( e ) );
				sb.append( "), " );
			}
			sb.deleteCharAt( sb.length() - 1 );
			sb.deleteCharAt( sb.length() - 1 );
			sb.append( "]" );
		}

		return sb.toString();
	}
}
