package net.trackmate.revised.model;

import net.trackmate.graph.GraphFeatures;
import net.trackmate.graph.GraphIdBimap;
import net.trackmate.graph.listenable.GraphListener;
import net.trackmate.graph.ref.AbstractEdge;
import net.trackmate.graph.ref.AbstractEdgePool;
import net.trackmate.graph.ref.AbstractVertex;
import net.trackmate.graph.ref.AbstractVertexWithFeatures;
import net.trackmate.graph.ref.AbstractVertexWithFeaturesPool;
import net.trackmate.graph.ref.ListenableGraphImp;
import net.trackmate.pool.MappedElement;

public class AbstractModelGraph<
		VP extends AbstractVertexWithFeaturesPool< V, E, T >,
		EP extends AbstractEdgePool< E, V, T >,
		V extends AbstractVertexWithFeatures< V, E, T >,
		E extends AbstractEdge< E, V, T >,
		T extends MappedElement >
	extends ListenableGraphImp< VP, EP, V, E, T >
	implements ModelGraph_HACK_FIX_ME< V, E >
{
	protected final GraphIdBimap< V, E > idmap;

	protected final GraphFeatures< V, E > features;

	public AbstractModelGraph( final VP vertexPool, final EP edgePool )
	{
		super( vertexPool, edgePool );
		idmap = new GraphIdBimap< V, E >( vertexPool, edgePool );
		features = new GraphFeatures<>( this );
		vertexPool.linkFeatures( features );
	}

	public AbstractModelGraph( final EP edgePool )
	{
		super( edgePool );
		idmap = new GraphIdBimap< V, E >( vertexPool, edgePool );
		features = new GraphFeatures<>( this );
		vertexPool.linkFeatures( features );
	}

	protected void clear()
	{
		vertexPool.clear();
		edgePool.clear();
		features.clear();
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
	@Override
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
