package org.mastodon.feature.update;

import java.util.Collection;
import java.util.Collections;

import org.mastodon.feature.DefaultFeatureComputerService;
import org.mastodon.feature.update.GraphUpdate.UpdateLocality;
import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphListener;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.properties.PropertyChangeListener;

/**
 * Class used to follow updates in a graph, that is vertices / edges added,
 * removed or modified, and exposes these changes to be used elsewhere.
 * <p>
 * Each instance exposes 3 listeners {@link #graphListener()},
 * {@link #vertexPropertyChangeListener()} and
 * {@link #edgePropertyChangeListener()} that need to be registered as listeners
 * to the graph and the adequate vertex and edge properties for it to operate
 * properly.
 *
 * @author Jean-Yves Tinevez
 *
 * @param <V>
 *            the type of vertices in the graph.
 * @param <E>
 *            the type of edges in the graph.
 */
public class GraphUpdateStack< V extends Vertex< E >, E extends Edge< V > >
{

	/**
	 * Number of graph update commits we store before triggering a full
	 * recalculation.
	 */
	static final int BUFFER_SIZE = 10;

	private final MyVertexPropertyChangeListener vertexPropertyChangeListener;

	private final MyEdgePropertyChangeListener edgePropertyChangeListener;

	private final GraphUpdateStack< V, E >.MyGraphListener graphListener;

	private final SizedDeque< UpdateState< V, E > > stateStack;

	private GraphUpdate< V, E > currentUpdate;

	private final ReadOnlyGraph< V, E > graph;

	public GraphUpdateStack( final ReadOnlyGraph< V, E > graph )
	{
		this.graph = graph;
		this.stateStack = new SizedDeque<>( BUFFER_SIZE );

		this.vertexPropertyChangeListener = new MyVertexPropertyChangeListener();
		this.edgePropertyChangeListener = new MyEdgePropertyChangeListener();
		this.graphListener = new MyGraphListener();
		commit( Collections.emptyList() );
	}

	/**
	 * This method should only be called by the {@link DefaultFeatureComputerService}
	 * after the computation step.
	 * <p>
	 * It stacks the current changes and mark them for the specified feature
	 * keys, then starts building a new one.
	 *
	 * @param featureKeys
	 *            the keys of the features that were computed before this
	 *            commit.
	 */
	public void commit( final Collection< String > featureKeys )
	{
		currentUpdate = new GraphUpdate<>( graph );
		stateStack.push( new UpdateState<>( featureKeys, currentUpdate ) );
	}

	/**
	 * Returns the changes needed to update the feature with the specified key.
	 * A <code>null</code> value indicate that the feature should be re-computed
	 * for all the objects of the graph, without the possibility to use
	 * incremental updates. Otherwise, the objects to update can be retrieved
	 * with the {@link GraphUpdate#vertices(UpdateLocality)} and
	 * {@link GraphUpdate#edges(UpdateLocality)} methods.
	 *
	 * @param featureKey
	 *            the key of the feature to build a graph update for.
	 * @return a graph update object, or <code>null</code> if the full graph
	 *         needs to re-computed for this feature.
	 */
	public GraphUpdate< V, E > changesFor( final String featureKey )
	{
		final GraphUpdate< V, E > changes = new GraphUpdate<>( graph );
		for ( final UpdateState< V, E > updateState : stateStack )
		{
			changes.concatenate( updateState.getChanges() );
			if ( updateState.contains( featureKey ) )
				return changes;
		}
		return null;
	}

	public PropertyChangeListener< V > vertexPropertyChangeListener()
	{
		return vertexPropertyChangeListener;
	}

	public PropertyChangeListener< E > edgePropertyChangeListener()
	{
		return edgePropertyChangeListener;
	}

	public GraphListener< V, E > graphListener()
	{
		return graphListener;
	}

	private class MyGraphListener implements GraphListener< V, E >
	{

		private final V vref;

		public MyGraphListener()
		{
			this.vref = graph.vertexRef();
		}

		@Override
		public void graphRebuilt()
		{
			stateStack.clear();
		}

		@Override
		public void vertexAdded( final V vertex )
		{
			currentUpdate.add( vertex );
		}

		@Override
		public void vertexRemoved( final V vertex )
		{
			// Walk through the stack and remove trace of it.
			for ( final UpdateState< V, E > state : stateStack )
				state.getChanges().remove( vertex );
		}

		@Override
		public void edgeAdded( final E edge )
		{
			currentUpdate.add( edge );
			currentUpdate.addAsNeighbor( edge.getSource( vref ) );
			currentUpdate.addAsNeighbor( edge.getTarget( vref ) );
		}

		@Override
		public void edgeRemoved( final E edge )
		{
			// Walk through the stack and remove trace of it.
			for ( final UpdateState< V, E > state : stateStack )
				state.getChanges().remove( edge );

			currentUpdate.addAsNeighbor( edge.getSource( vref ) );
			currentUpdate.addAsNeighbor( edge.getTarget( vref ) );
		}
	}

	private final class MyVertexPropertyChangeListener implements PropertyChangeListener< V >
	{

		@Override
		public void propertyChanged( final V v )
		{
			currentUpdate.add( v );
			for ( final E e : v.edges() )
				currentUpdate.addAsNeighbor( e );
		}
	}

	private final class MyEdgePropertyChangeListener implements PropertyChangeListener< E >
	{
		private final V vref;

		public MyEdgePropertyChangeListener()
		{
			this.vref = graph.vertexRef();
		}

		@Override
		public void propertyChanged( final E e )
		{
			currentUpdate.add( e );
			currentUpdate.addAsNeighbor( e.getSource( vref ) );
			currentUpdate.addAsNeighbor( e.getTarget( vref ) );
		}
	}
}
