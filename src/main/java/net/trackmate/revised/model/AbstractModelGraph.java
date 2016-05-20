package net.trackmate.revised.model;

import net.trackmate.graph.GraphFeatures;
import net.trackmate.graph.GraphIdBimap;
import net.trackmate.graph.ref.AbstractListenableEdge;
import net.trackmate.graph.ref.AbstractListenableEdgePool;
import net.trackmate.graph.ref.AbstractListenableVertex;
import net.trackmate.graph.ref.AbstractListenableVertexPool;
import net.trackmate.graph.ref.ListenableGraphImp;
import net.trackmate.pool.MappedElement;

public class AbstractModelGraph<
		VP extends AbstractListenableVertexPool< V, E, T >,
		EP extends AbstractListenableEdgePool< E, V, T >,
		V extends AbstractListenableVertex< V, E, T >,
		E extends AbstractListenableEdge< E, V, T >,
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

	@Override
	protected void clear()
	{
		vertexPool.clear();
		edgePool.clear();
		features.clear();
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
