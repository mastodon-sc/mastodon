package net.trackmate.graph.ref;

import java.util.ArrayList;

import net.trackmate.graph.GraphChangeListener;
import net.trackmate.graph.GraphListener;
import net.trackmate.graph.ListenableGraph;
import net.trackmate.pool.MappedElement;

public class ListenableGraphImp<
		VP extends AbstractListenableVertexPool< V, E, T >,
		EP extends AbstractListenableEdgePool< E, V, T >,
		V extends AbstractListenableVertex< V, E, T >,
		E extends AbstractListenableEdge< E, V, T >,
		T extends MappedElement >
	extends GraphWithFeaturesImp< VP, EP, V, E, T >
	implements ListenableGraph< V, E >
{
	protected final ArrayList< GraphListener< V, E > > listeners;

	protected final ArrayList< GraphChangeListener > changeListeners;

	protected boolean emitEvents;

	public ListenableGraphImp( final VP vertexPool, final EP edgePool )
	{
		super( vertexPool, edgePool );
		vertexPool.linkNotify( notifyPostInit );
		edgePool.linkNotify( notifyPostInit );
		listeners = new ArrayList< GraphListener<V,E> >();
		changeListeners = new ArrayList< GraphChangeListener >();
		emitEvents = true;
	}

	public ListenableGraphImp( final EP edgePool )
	{
		super( edgePool );
		vertexPool.linkNotify( notifyPostInit );
		edgePool.linkNotify( notifyPostInit );
		listeners = new ArrayList< GraphListener<V,E> >();
		changeListeners = new ArrayList< GraphChangeListener >();
		emitEvents = true;
	}

	private final NotifyPostInit< V, E > notifyPostInit = new NotifyPostInit< V, E >()
	{
		@Override
		public void notifyVertexAdded( final V vertex )
		{
			ListenableGraphImp.this.notifyVertexAdded( vertex );
		}

		@Override
		public void notifyEdgeAdded( final E edge )
		{
			ListenableGraphImp.this.notifyEdgeAdded( edge );
		}
	};

	@Override
	public void remove( final V vertex )
	{
		if ( emitEvents )
		{
			for ( final E edge : vertex.edges() )
				for ( final GraphListener< V, E > listener : listeners )
					listener.edgeRemoved( edge );
			for ( final GraphListener< V, E > listener : listeners )
				listener.vertexRemoved( vertex );
		}
		vertexPool.delete( vertex );
	}

	@Override
	public void remove( final E edge )
	{
		if ( emitEvents )
			for ( final GraphListener< V, E > listener : listeners )
				listener.edgeRemoved( edge );
		edgePool.delete( edge );
	}

	@Override
	public void removeAllLinkedEdges( final V vertex )
	{
		if ( emitEvents )
			for ( final E edge : vertex.edges() )
				for ( final GraphListener< V, E > listener : listeners )
					listener.edgeRemoved( edge );
		edgePool.deleteAllLinkedEdges( vertex );
	}

	@Override
	public synchronized boolean addGraphListener( final GraphListener< V, E > listener )
	{
		if ( ! listeners.contains( listener ) )
		{
			listeners.add( listener );
			return true;
		}
		return false;
	}

	@Override
	public synchronized boolean removeGraphListener( final GraphListener< V, E > listener )
	{
		return listeners.remove( listener );
	}

	@Override
	public synchronized boolean addGraphChangeListener( final GraphChangeListener listener )
	{
		if ( ! changeListeners.contains( listener ) )
		{
			changeListeners.add( listener );
			return true;
		}
		return false;
	}

	@Override
	public synchronized boolean removeGraphChangeListener( final GraphChangeListener listener )
	{
		return changeListeners.remove( listener );
	}

	@Override
	public void notifyVertexAdded( final V vertex )
	{
		if ( emitEvents )
			for ( final GraphListener< V, E > listener : listeners )
				listener.vertexAdded( vertex );
	}

	@Override
	public void notifyEdgeAdded( final E edge )
	{
		if ( emitEvents )
			for ( final GraphListener< V, E > listener : listeners )
				listener.edgeAdded( edge );
	}

	@Override
	public void pauseListeners()
	{
		emitEvents = false;
		vertexFeatures.pauseListeners();
		edgeFeatures.pauseListeners();
	}

	@Override
	public void resumeListeners()
	{
		emitEvents = true;
		vertexFeatures.resumeListeners();
		edgeFeatures.resumeListeners();
		for ( final GraphListener< V, E > listener : listeners )
			listener.graphRebuilt();
	}

	@Override
	public void notifyGraphChanged()
	{
		if ( emitEvents )
			for ( final GraphChangeListener listener : changeListeners )
				listener.graphChanged();
	}
}
