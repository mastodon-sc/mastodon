package net.trackmate.graph.listenable;

import java.util.Set;
import java.util.WeakHashMap;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Vertex;

public class GraphChangeEventAggregator< V extends Vertex< E >, E extends Edge< V > >
{

	/**
	 * Counter for the depth of nested transactions. Each call to beginUpdate
	 * increments this counter and each call to endUpdate decrements it. When
	 * the counter reaches 0, the transaction is closed and the respective
	 * events are fired. Initial value is 0.
	 */
	private transient int updateLevel = 0;

	private transient boolean endingUpdate = false;

	private GraphChangeEvent< V, E > currentEdit;

	private final ListenableGraph< V, E > graph;

	private final boolean listening = true;

	private final WeakHashMap< GraphChangeEventListener< V, E >, Boolean > listeners;

	public GraphChangeEventAggregator( final ListenableGraph< V, E > graph )
	{
		this.graph = graph;
		this.currentEdit = new GraphChangeEvent< V, E >( graph );
		this.listeners = new WeakHashMap< GraphChangeEventListener< V, E >, Boolean >();
		graph.addGraphListener( new Aggregator() );
	}

	public boolean addGraphListener( final GraphChangeEventListener< V, E > listener )
	{
		return listeners.put( listener, Boolean.TRUE ) == null;
	}

	public boolean removeGraphListener( final GraphChangeEventListener< V, E > listener )
	{
		return listeners.remove( listener ) != null;
	}

	public Set< GraphChangeEventListener< V, E > > getGraphListeners()
	{
		return listeners.keySet();
	}

	private void fireEvent()
	{
		for ( final GraphChangeEventListener< V, E > listener : listeners.keySet() )
		{
			listener.graphChanged( currentEdit );
		}
	}

	private class Aggregator implements GraphListener< V, E >
	{
		@Override
		public void vertexAdded( final V vertex )
		{
			if ( !listening ) { return; }
			currentEdit.vertexAdded( vertex );
			if ( updateLevel == 0 )
			{
				fireEvent();
				currentEdit = new GraphChangeEvent< V, E >( graph );
			}
		}

		@Override
		public void vertexRemoved( final V vertex )
		{
			if ( !listening ) { return; }
			currentEdit.vertexRemoved( vertex );
			if ( updateLevel == 0 )
			{
				fireEvent();
				currentEdit = new GraphChangeEvent< V, E >( graph );
			}
		}

		@Override
		public void edgeAdded( final E edge )
		{
			if ( !listening ) { return; }
			currentEdit.edgeAdded( edge );
			if ( updateLevel == 0 )
			{
				fireEvent();
				currentEdit = new GraphChangeEvent< V, E >( graph );
			}
		}

		@Override
		public void edgeRemoved( final E edge )
		{
			if ( !listening ) { return; }
			currentEdit.edgeRemoved( edge, edge.getSource(), edge.getTarget() );
			if ( updateLevel == 0 )
			{
				fireEvent();
				currentEdit = new GraphChangeEvent< V, E >( graph );
			}
		}

//		@Override
//		public void updateBegun()
//		{
//			updateLevel++;
//		}
//
//		@Override
//		public void updateEnded()
//		{
//			updateLevel--;
//
//			if ( !endingUpdate )
//			{
//				endingUpdate = updateLevel == 0;
//				try
//				{
//					if ( endingUpdate && !currentEdit.isEmpty() )
//					{
//						fireEvent();
//						currentEdit = new GraphChangeEvent< V, E >( graph );
//					}
//				}
//				finally
//				{
//					endingUpdate = false;
//				}
//			}
//		}
//
//		@Override
//		public void updatePaused()
//		{
//			listening = false;
//		}
//
//		@Override
//		public void updateResumed()
//		{
//			listening = true;
//		}

	}
}
