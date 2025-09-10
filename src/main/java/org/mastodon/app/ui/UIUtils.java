package org.mastodon.app.ui;

import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.mastodon.RefPool;
import org.mastodon.adapter.RefBimap;
import org.mastodon.app.ViewGraph;
import org.mastodon.collection.RefCollection;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Edges;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;

public class UIUtils
{

	/**
	 * Possibly appends the project name to a given window title, making sure we
	 * do not append to an already appended project name.
	 *
	 * @param title
	 *            the initial window name.
	 * @param projectName
	 *            the project name.
	 * @return an adjusted window name.
	 */
	private static final String adjustTitle( final String title, final String projectName )
	{
		if ( projectName == null || projectName.isEmpty() )
			return title;

		final String separator = " - ";
		final int index = title.indexOf( separator );
		final String prefix = ( index < 0 ) ? title : title.substring( 0, index );
		return prefix + separator + projectName;
	}

	public static void adjustTitle( final JDialog dialog, final String projectName )
	{
		dialog.setTitle( adjustTitle( dialog.getTitle(), projectName ) );
	}

	public static void adjustTitle( final JFrame frame, final String projectName )
	{
		frame.setTitle( adjustTitle( frame.getTitle(), projectName ) );
	}

	public static void adjustTitle( final Window w, final String projectName )
	{
		if ( w instanceof JDialog )
			adjustTitle( ( JDialog ) w, projectName );
		else if ( w instanceof JFrame )
			adjustTitle( ( JFrame ) w, projectName );
	}

	/**
	 * A {@link ViewGraph} that simply exposes the graph it wraps.
	 *
	 * @author Jean-Yves Tinevez
	 *
	 * @param <V>
	 *            the type of vertices in the view and wrapped graphs.
	 * @param <E>
	 *            the type of edges in the view and wrapped graphs.
	 */
	private static class IdentityViewGraph< V extends Vertex< E >, E extends Edge< V > > implements ViewGraph< V, E, V, E >
	{

		private final ReadOnlyGraph< V, E > wrappedGraph;

		private final IdentityRefBimap< V > vertexIdBimap;

		private final IdentityRefBimap< E > edgeIdBimap;

		private IdentityViewGraph( final ReadOnlyGraph< V, E > graph, final GraphIdBimap< V, E > idBimap )
		{
			this.wrappedGraph = graph;
			this.vertexIdBimap = new IdentityRefBimap<>( idBimap.vertexIdBimap() );
			this.edgeIdBimap = new IdentityRefBimap<>( idBimap.edgeIdBimap() );
		}

		@Override
		public E getEdge( final V source, final V target )
		{
			return wrappedGraph.getEdge( source, target );
		}

		@Override
		public E getEdge( final V source, final V target, final E ref )
		{
			return wrappedGraph.getEdge( source, target, ref );
		}

		@Override
		public Edges< E > getEdges( final V source, final V target )
		{
			return wrappedGraph.getEdges( source, target );
		}

		@Override
		public Edges< E > getEdges( final V source, final V target, final V ref )
		{
			return wrappedGraph.getEdges( source, target, ref );
		}

		@Override
		public V vertexRef()
		{
			return wrappedGraph.vertexRef();
		}

		@Override
		public E edgeRef()
		{
			return wrappedGraph.edgeRef();
		}

		@Override
		public void releaseRef( final V ref )
		{
			wrappedGraph.releaseRef( ref );
		}

		@Override
		public void releaseRef( final E ref )
		{
			wrappedGraph.releaseRef( ref );
		}

		@Override
		public RefCollection< V > vertices()
		{
			return wrappedGraph.vertices();
		}

		@Override
		public RefCollection< E > edges()
		{
			return wrappedGraph.edges();
		}

		@Override
		public RefBimap< V, V > getVertexMap()
		{
			return vertexIdBimap;
		}

		@Override
		public RefBimap< E, E > getEdgeMap()
		{
			return edgeIdBimap;
		}

		private final class IdentityRefBimap< O > implements RefBimap< O, O >
		{

			private final RefPool< O > pool;

			public IdentityRefBimap( final RefPool< O > pool )
			{
				this.pool = pool;
			}

			@Override
			public O getLeft( final O right )
			{
				return right;
			}

			@Override
			public O getRight( final O left, final O ref )
			{
				return left;
			}

			@Override
			public O reusableLeftRef( final O ref )
			{
				return ref;
			}

			@Override
			public O reusableRightRef()
			{
				return pool.createRef();
			}

			@Override
			public void releaseRef( final O ref )
			{
				pool.releaseRef( ref );
			}
		}
	}

	/**
	 * Wraps the specified graph in a {@link ViewGraph} that plainly exposes it.
	 *
	 * @param graph
	 *            the graph to wrap.
	 * @param idBimap
	 *            a {@link GraphIdBimap} for the wrapped graph.
	 * @return an identity wrapper graph.
	 * @param <V>
	 *            the type of vertices in the graph.
	 * @param <E>
	 *            the type of edges in the graph.
	 */
	public static final < V extends Vertex< E >, E extends Edge< V > > ViewGraph< V, E, V, E >
			wrap( final ReadOnlyGraph< V, E > graph, final GraphIdBimap< V, E > idBimap )
	{
		return new IdentityViewGraph<>( graph, idBimap );
	}

}
