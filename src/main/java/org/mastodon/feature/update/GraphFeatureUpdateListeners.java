package org.mastodon.feature.update;

import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphListener;
import org.mastodon.graph.Vertex;
import org.mastodon.properties.PropertyChangeListener;

/**
 * Listeners for incremental changes.
 */
public class GraphFeatureUpdateListeners
{

	/**
	 * Returns a new {@link GraphListener} that will feed changes to the
	 * specified update stacks.
	 *
	 * @param <V>
	 *            the type of vertices in the graph to listen to.
	 * @param <E>
	 *            the type of edges in the graph to listen to.
	 * @param vertexUpdates
	 *            the update stack that stores changes to vertices.
	 * @param edgeUpdates
	 *            the update stack that stores changes to edges.
	 * @param vref
	 *            a reference to a vertex object.
	 * @return a new {@link GraphListener}.
	 */
	public static < V extends Vertex< E >, E extends Edge< V > > GraphListener< V, E > graphListener( final UpdateStack< V > vertexUpdates, final UpdateStack< E > edgeUpdates, final V vref )
	{
		return new MyGraphListener< V, E >( vertexUpdates, edgeUpdates, vref );
	}

	/**
	 * Returns a new {@link PropertyChangeListener} that will feed changes to a
	 * vertex property to the specified update stacks.
	 *
	 * @param <V>
	 *            the type of vertices in the graph to listen to.
	 * @param <E>
	 *            the type of edges in the graph to listen to.
	 * @param vertexUpdates
	 *            the update stack that stores changes to vertices.
	 * @param edgeUpdates
	 *            the update stack that stores changes to edges.
	 * @return a new {@link PropertyChangeListener}.
	 */
	public static < V extends Vertex< E >, E extends Edge< V > > PropertyChangeListener< V > vertexPropertyListener( final UpdateStack< V > vertexUpdates, final UpdateStack< E > edgeUpdates )
	{
		return new MyVertexPropertyChangeListener<>( vertexUpdates, edgeUpdates );
	}

	/**
	 * Returns a new {@link PropertyChangeListener} that will feed changes to a
	 * edge property to the specified update stacks.
	 *
	 * @param <V>
	 *            the type of vertices in the graph to listen to.
	 * @param <E>
	 *            the type of edges in the graph to listen to.
	 * @param vertexUpdates
	 *            the update stack that stores changes to vertices.
	 * @param edgeUpdates
	 *            the update stack that stores changes to edges.
	 * @param vref
	 *            a reference to a vertex object.
	 * @return a new {@link PropertyChangeListener}.
	 */
	public static < V extends Vertex< E >, E extends Edge< V > > PropertyChangeListener< E > edgePropertyListener( final UpdateStack< V > vertexUpdates, final UpdateStack< E > edgeUpdates, final V vref )
	{
		return new MyEdgePropertyChangeListener<>( vertexUpdates, edgeUpdates, vref );
	}

	private static class MyGraphListener< V extends Vertex< E >, E extends Edge< V > > implements GraphListener< V, E >
	{

		private final V vref;

		private final UpdateStack< V > vertexUpdates;

		private final UpdateStack< E > edgeUpdates;

		public MyGraphListener( final UpdateStack< V > vertexUpdates, final UpdateStack< E > edgeUpdates, final V vref )
		{
			this.vertexUpdates = vertexUpdates;
			this.edgeUpdates = edgeUpdates;
			this.vref = vref;
		}

		@Override
		public void graphRebuilt()
		{
			vertexUpdates.clear();
			edgeUpdates.clear();
		}

		@Override
		public void vertexAdded( final V vertex )
		{
			vertexUpdates.addModified( vertex );
		}

		@Override
		public void vertexRemoved( final V vertex )
		{
			vertexUpdates.remove( vertex );
		}

		@Override
		public void edgeAdded( final E edge )
		{
			edgeUpdates.addModified( edge );
			vertexUpdates.addNeighbor( edge.getSource( vref ) );
			vertexUpdates.addNeighbor( edge.getTarget( vref ) );
		}

		@Override
		public void edgeRemoved( final E edge )
		{
			edgeUpdates.remove( edge );
			vertexUpdates.addNeighbor( edge.getSource( vref ) );
			vertexUpdates.addNeighbor( edge.getTarget( vref ) );
		}
	}

	private static final class MyVertexPropertyChangeListener< V extends Vertex< E >, E extends Edge< V > > implements PropertyChangeListener< V >
	{

		private final UpdateStack< V > vertexUpdates;

		private final UpdateStack< E > edgeUpdates;

		public MyVertexPropertyChangeListener( final UpdateStack< V > vertexUpdates, final UpdateStack< E > edgeUpdates )
		{
			this.vertexUpdates = vertexUpdates;
			this.edgeUpdates = edgeUpdates;
		}

		@Override
		public void propertyChanged( final V v )
		{
			vertexUpdates.addModified( v );
			for ( final E e : v.edges() )
				edgeUpdates.addNeighbor( e );
		}
	}

	private static final class MyEdgePropertyChangeListener< V extends Vertex< E >, E extends Edge< V > > implements PropertyChangeListener< E >
	{
		private final V vref;

		private final UpdateStack< V > vertexUpdates;

		private final UpdateStack< E > edgeUpdates;

		public MyEdgePropertyChangeListener( final UpdateStack< V > vertexUpdates, final UpdateStack< E > edgeUpdates, final V vref )
		{
			this.vertexUpdates = vertexUpdates;
			this.edgeUpdates = edgeUpdates;
			this.vref = vref;
		}

		@Override
		public void propertyChanged( final E e )
		{
			edgeUpdates.addModified( e );
			vertexUpdates.addNeighbor( e.getSource( vref ) );
			vertexUpdates.addNeighbor( e.getTarget( vref ) );
		}
	}
}
