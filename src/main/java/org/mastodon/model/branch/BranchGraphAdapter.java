package org.mastodon.model.branch;

import java.util.Iterator;

import org.mastodon.adapter.IteratorAdapter;
import org.mastodon.adapter.RefBimap;
import org.mastodon.collection.RefCollection;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Edges;
import org.mastodon.graph.GraphChangeListener;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.GraphListener;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.branch.BranchGraph;

/**
 * Translates a branch graph of branch objects <code>BV</code> and
 * <code>BE</code> linked to a graph of <code>V</code> and <code>E</code> into a
 * branch graph of branch objects <code>BV</code> and <code>BE</code> linked to
 * a graph of <code>WV</code> and <code>WE</code>. Only the linked graph type
 * changes.
 * 
 * @author Jean-Yves Tinevez
 *
 * @param <BV>
 *            the type of branch vertices.
 * @param <BE>
 *            the type of branch edges.
 * @param <V>
 *            the type of the graph vertices to adapt.
 * @param <E>
 *            the type of the graph edges to adapt.
 * @param <WV>
 *            the type of the graph vertices exposed by this adapter.
 * @param <WE>
 *            the type of the graph edges exposed by this adapter.
 */
public class BranchGraphAdapter<
	BV extends Vertex< BE >,
	BE extends Edge< BV >,
	V extends Vertex< E >,
	E extends Edge< V >,
	WV extends Vertex< WE >,
	WE extends Edge< WV > >
		implements BranchGraph< BV, BE, WV, WE >
{

	private final BranchGraph< BV, BE, V, E > branchGraph;

	private final RefBimap< V, WV > vertexMap;

	private final RefBimap< E, WE > edgeMap;

	public BranchGraphAdapter(
			final BranchGraph< BV, BE, V, E > branchGraph,
			final RefBimap< V, WV > vertexMap,
			final RefBimap< E, WE > edgeMap )
	{
		this.branchGraph = branchGraph;
		this.vertexMap = vertexMap;
		this.edgeMap = edgeMap;
	}

	@Override
	public boolean addGraphListener( final GraphListener< BV, BE > listener )
	{
		return branchGraph.addGraphListener( listener );
	}

	@Override
	public boolean removeGraphListener( final GraphListener< BV, BE > listener )
	{
		return branchGraph.removeGraphListener( listener );
	}

	@Override
	public boolean addGraphChangeListener( final GraphChangeListener listener )
	{
		return branchGraph.addGraphChangeListener( listener );
	}

	@Override
	public boolean removeGraphChangeListener( final GraphChangeListener listener )
	{
		return branchGraph.removeGraphChangeListener( listener );
	}

	@Override
	public BE getEdge( final BV source, final BV target )
	{
		return branchGraph.getEdge( source, target );
	}

	@Override
	public BE getEdge( final BV source, final BV target, final BE ref )
	{
		return branchGraph.getEdge( source, target, ref );
	}

	@Override
	public Edges< BE > getEdges( final BV source, final BV target )
	{
		return branchGraph.getEdges( source, target );
	}

	@Override
	public Edges< BE > getEdges( final BV source, final BV target, final BV ref )
	{
		return branchGraph.getEdges( source, target, ref );
	}

	@Override
	public BV vertexRef()
	{
		return branchGraph.vertexRef();
	}

	@Override
	public BE edgeRef()
	{
		return branchGraph.edgeRef();
	}

	@Override
	public void releaseRef( final BV ref )
	{
		branchGraph.releaseRef( ref );
	}

	@Override
	public void releaseRef( final BE ref )
	{
		branchGraph.releaseRef( ref );
	}

	@Override
	public RefCollection< BV > vertices()
	{
		return branchGraph.vertices();
	}

	@Override
	public RefCollection< BE > edges()
	{
		return branchGraph.edges();
	}

	@Override
	public WE getLinkedEdge( final BE be, final WE ref )
	{
		final E eref = edgeMap.reusableLeftRef( ref );
		final E e = branchGraph.getLinkedEdge( be, eref );
		return edgeMap.getRight( e, ref );
	}

	@Override
	public WV getLinkedVertex( final BV bv, final WV ref )
	{
		final V vref = vertexMap.reusableLeftRef( ref );
		final V v = branchGraph.getLinkedVertex( bv, vref );
		return vertexMap.getRight( v, ref );
	}

	@Override
	public BE getBranchEdge( final WE edge, final BE ref )
	{
		return branchGraph.getBranchEdge( edgeMap.getLeft( edge ), ref );
	}

	@Override
	public BE getBranchEdge( final WV vertex, final BE ref )
	{
		return branchGraph.getBranchEdge( vertexMap.getLeft( vertex ), ref );
	}

	@Override
	public BV getBranchVertex( final WV vertex, final BV ref )
	{
		return branchGraph.getBranchVertex( vertexMap.getLeft( vertex ), ref );
	}

	@Override
	public GraphIdBimap< BV, BE > getGraphIdBimap()
	{
		return branchGraph.getGraphIdBimap();
	}

	@Override
	public Iterator< WV > vertexBranchIterator( final BE edge )
	{
		return new IteratorAdapter<>( branchGraph.vertexBranchIterator( edge ), vertexMap );
	}

	@Override
	public Iterator< WE > edgeBranchIterator( final BE edge )
	{
		return new IteratorAdapter<>( branchGraph.edgeBranchIterator( edge ), edgeMap );
	}
}
