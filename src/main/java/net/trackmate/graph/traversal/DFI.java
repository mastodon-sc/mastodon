package net.trackmate.graph.traversal;

import java.util.Iterator;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Edges;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.algorithm.AbstractGraphAlgorithm;
import net.trackmate.graph.collection.RefRefMap;
import net.trackmate.graph.collection.RefSet;
import net.trackmate.graph.collection.RefStack;
import net.trackmate.graph.util.Graphs;

public class DFI< V extends Vertex< E >, E extends Edge< V > > extends AbstractGraphAlgorithm< V, E > implements Iterator< V >
{

	private final RefSet< V > discovered;

	private final RefStack< V > vertexStack;

	private final RefStack< E > edgeStack;

	private final boolean directed;

	private final V next;

	private V vFetched;

	private V tmpRef;

	private E eFetched;

	private boolean hasNext;

	private boolean init;

	private final RefRefMap< V, V > treeParent;

	private final RefSet< V > processed;

	public DFI( final V root, final Graph< V, E > graph, final boolean directed )
	{
		super( graph );
		this.discovered = createVertexSet();
		this.processed = createVertexSet();
		this.vertexStack = createVertexStack();
		this.edgeStack = createEdgeStack();
		this.directed = directed;
		this.vFetched = vertexRef();
		this.eFetched = edgeRef();
		this.next = vertexRef();
		this.tmpRef = vertexRef();
		this.treeParent = createVertexVertexMap();
		reset( root );
	}

	public void reset( final V root )
	{
		vertexStack.clear();
		edgeStack.clear();
		discovered.clear();
		processed.clear();
		treeParent.clear();
		hasNext = true;
		vertexStack.push( root );
		init = true;
		fetchNext();
	}

	@Override
	public V next()
	{
		assign( vFetched, next );
		fetchNext();
		return next;
	}

	private void fetchNext()
	{
		boolean found = false;
		while ( !vertexStack.isEmpty() )
		{
			vFetched = vertexStack.pop( vFetched );
			if ( !init )
			{
				eFetched = edgeStack.pop( eFetched );
			}
			if ( !discovered.contains( vFetched ) )
			{
				found = true;
				break;
			}
		}
		if ( !found )
		{
			hasNext = false;
			return;
		}
		discovered.add( vFetched );

		if ( !init )
		{
			processEdge( eFetched, vFetched );
		}
		else
		{
			init = false;
		}
		processVertexEarly( vFetched );

		final Edges< E > edges;
		if ( directed )
		{
			edges = vFetched.outgoingEdges();
		}
		else
		{
			edges = vFetched.edges();
		}

		boolean willVisit = false;
		for ( final E edge : edges )
		{
			tmpRef = Graphs.getOppositeVertex( edge, vFetched, tmpRef );
			if ( !discovered.contains( tmpRef ) )
			{
				vertexStack.push( tmpRef );
				edgeStack.push( edge );
				treeParent.put( tmpRef, vFetched );
				willVisit = true;
			}
		}
		if ( !willVisit )
		{
			processVertexLate( vFetched );
			processed.add( vFetched );
			while ( true )
			{
				final V parent = treeParent.get( vFetched, tmpRef );
				if ( null == parent )
				{
					break;
				}

				if ( processed.contains( parent ) )
				{

				}

			}
		}
	}

	@Override
	public boolean hasNext()
	{
		return hasNext;
	}

	protected void processVertexLate( final V vertex )
	{
		System.out.println( " - finished processing vertex " + vertex );// DEBUG
	}

	protected void processVertexEarly( final V vertex )
	{
		System.out.println( " - discovered vertex " + vertex );// DEBUG

	}

	protected void processEdge( final E edge, final V v1 )
	{
		System.out.println( " - crossing edge " + edge + " from vertex " + Graphs.getOppositeVertex( edge, v1, vertexRef() ) + " to " + v1 );// DEBUG
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException( "remove() is not supported for graph iterators." );
	}
}
