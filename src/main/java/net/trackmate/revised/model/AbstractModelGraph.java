package net.trackmate.revised.model;

import net.trackmate.graph.AbstractEdge;
import net.trackmate.graph.AbstractEdgePool;
import net.trackmate.graph.AbstractVertex;
import net.trackmate.graph.AbstractVertexPool;
import net.trackmate.graph.GraphIdBimap;
import net.trackmate.graph.ListenableGraphImp;
import net.trackmate.graph.PoolObjectIdBimap;
import net.trackmate.graph.listenable.GraphListener;
import net.trackmate.graph.mempool.MappedElement;

public class AbstractModelGraph<
		VP extends AbstractVertexPool< V, E, T >,
		EP extends AbstractEdgePool< E, V, T >,
		V extends AbstractVertex< V, E, T >,
		E extends AbstractEdge< E, V, T >,
		T extends MappedElement >
	extends ListenableGraphImp< VP, EP, V, E, T >
{
	protected final GraphIdBimap< V, E > idmap;

	public AbstractModelGraph( final VP vertexPool, final EP edgePool )
	{
		super( vertexPool, edgePool );
		idmap = new GraphIdBimap< V, E >(
					new PoolObjectIdBimap< V >( vertexPool ),
					new PoolObjectIdBimap< E >( edgePool ) );
	}

	public AbstractModelGraph( final EP edgePool )
	{
		super( edgePool );
		idmap = new GraphIdBimap< V, E >(
				new PoolObjectIdBimap< V >( vertexPool ),
				new PoolObjectIdBimap< E >( edgePool ) );
	}

	protected void clear()
	{
		vertexPool.clear();
		edgePool.clear();
	}

	/**
	 * This is a little bit of a hack:
	 * We override {@link ListenableGraphImp#addVertex()} to not emit events.
	 * We want to emit {@link GraphListener#vertexAdded(net.trackmate.graph.Vertex)} only after the {@code Vertex.init(...)} has been called.
	 */
	@Override
	public V addVertex()
	{
		return vertexPool.create( vertexRef() );
	}

	/**
	 * This is a little bit of a hack:
	 * We override {@link ListenableGraphImp#addVertex(AbstractVertex)} to not emit events.
	 * We want to emit {@link GraphListener#vertexAdded(net.trackmate.graph.Vertex)} only after the {@code Vertex.init(...)} has been called.
	 */
	@Override
	public V addVertex( final V vertex )
	{
		return vertexPool.create( vertex );
	}

	/**
	 * Sends {@link GraphListener#vertexAdded(net.trackmate.graph.Vertex)} for
	 * the specified vertex. Must be called, after
	 * {@link #addVertex(AbstractVertex)} and {@code Vertex.init(...)} has been
	 * called.
	 *
	 * @param vertex
	 *            vertex for which to send
	 *            {@link GraphListener#vertexAdded(net.trackmate.graph.Vertex)}.
	 * @return the specified {@code vertex} argument.
	 */
	public V notifyVertexAdded( final V vertex )
	{
		if ( emitEvents )
			for ( final GraphListener< V, E > listener : listeners )
				listener.vertexAdded( vertex );
		return vertex;
	}

	@Override
	protected void pauseListeners()
	{
		super.pauseListeners();
	}

	@Override
	protected void resumeListeners()
	{
		super.resumeListeners();
	}

	@Override
	protected void notifyGraphChanged()
	{
		super.notifyGraphChanged();
	}
}
