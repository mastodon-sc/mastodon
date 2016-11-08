package org.mastodon.revised.trackscheme.display.style.dummygraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.mastodon.graph.GraphChangeListener;
import org.mastodon.graph.GraphListener;
import org.mastodon.graph.ListenableGraph;
import org.mastodon.graph.algorithm.traversal.DepthFirstSearch;
import org.mastodon.graph.algorithm.traversal.GraphSearch.SearchDirection;
import org.mastodon.graph.algorithm.traversal.SearchListener;
import org.mastodon.graph.object.AbstractObjectGraph;
import org.mastodon.graph.object.AbstractObjectIdGraph;
import org.mastodon.revised.ui.selection.Selection;

public class DummyGraph extends AbstractObjectIdGraph< DummyVertex, DummyEdge > implements ListenableGraph< DummyVertex, DummyEdge >
{
	public DummyGraph()
	{
		super( new Factory(), DummyVertex.class, DummyEdge.class, new HashSet<>(), new HashSet<>() );
	}

	private static class Factory implements AbstractObjectGraph.Factory< DummyVertex, DummyEdge >
	{
		@Override
		public DummyVertex createVertex()
		{
			return new DummyVertex();
		}

		@Override
		public DummyEdge createEdge( final DummyVertex source, final DummyVertex target )
		{
			return new DummyEdge( source, target );
		}
	}

	/*
	 * STATIC EXAMPLE
	 */

	public static enum Examples
	{
		CELEGANS( CElegansExample.graph, CElegansExample.selectedVertices, CElegansExample.selectedEdges );

		private final DummyGraph graph;

		private Selection< DummyVertex, DummyEdge > selection;

		private Examples( final DummyGraph graph, final Collection< DummyVertex > vertices, final Collection< DummyEdge > edges )
		{
			this.graph = graph;
			this.selection = new Selection<>( graph, graph.getIdBimap() );
			selection.setEdgesSelected( edges, true );
			selection.setVerticesSelected( vertices, true );
		}

		public DummyGraph getGraph()
		{
			return graph;
		}

		public Selection< DummyVertex, DummyEdge > getSelection()
		{
			return selection;
		}
	}

	private static final class CElegansExample
	{
		private static final DummyGraph graph;

		private static final Collection< DummyVertex > selectedVertices;

		private static final Collection< DummyEdge > selectedEdges;

		static
		{
			graph = new DummyGraph();

			final DummyVertex AB = graph.addVertex().init( "AB", 0 );

			final DummyVertex ABa = graph.addVertex().init( "AB.a", 1 );
			final DummyVertex ABp = graph.addVertex().init( "AB.p", 1 );
			graph.addEdge( AB, ABa );
			graph.addEdge( AB, ABp );

			final DummyVertex ABal = graph.addVertex().init( "AB.al", 2 );
			final DummyVertex ABar = graph.addVertex().init( "AB.ar", 2 );
			graph.addEdge( ABa, ABal );
			graph.addEdge( ABa, ABar );

			final DummyVertex ABpl = graph.addVertex().init( "AB.pl", 2 );
			final DummyVertex ABpr = graph.addVertex().init( "AB.pr", 2 );
			graph.addEdge( ABp, ABpl );
			graph.addEdge( ABp, ABpr );

			final DummyVertex ABala = graph.addVertex().init( "AB.ala", 3 );
			graph.addEdge( ABal, ABala );
			addFork( graph, ABala, 4 );

			final DummyVertex ABalp = graph.addVertex().init( "AB.alp", 3 );
			graph.addEdge( ABal, ABalp );
			addFork( graph, ABalp, 4 );

			final DummyVertex ABara = graph.addVertex().init( "AB.ara", 3 );
			graph.addEdge( ABar, ABara );
			addFork( graph, ABara, 4 );
			final DummyVertex ABarp = graph.addVertex().init( "AB.arp", 3 );
			graph.addEdge( ABar, ABarp );
			addFork( graph, ABarp, 4 );

			final DummyVertex ABpla = graph.addVertex().init( "AB.pla", 3 );
			graph.addEdge( ABpl, ABpla );
			addFork( graph, ABpla, 4 );

			final DummyVertex ABplp = graph.addVertex().init( "AB.plp", 3 );
			graph.addEdge( ABpl, ABplp );
			addFork( graph, ABplp, 4 );

			final DummyVertex ABpra = graph.addVertex().init( "AB.pra", 3 );
			graph.addEdge( ABpr, ABpra );
			addFork( graph, ABpra, 4 );

			final DummyVertex ABprp = graph.addVertex().init( "AB.prp", 3 );
			graph.addEdge( ABpr, ABprp );
			addFork( graph, ABprp, 4 );

			final DummyVertex P1 = graph.addVertex().init( "P1", 0 );

			final DummyVertex P2 = graph.addVertex().init( "P2", 1 );
			final DummyVertex EMS = graph.addVertex().init( "EMS", 1 );
			graph.addEdge( P1, P2 );
			graph.addEdge( P1, EMS );

			final DummyVertex P3 = graph.addVertex().init( "P3", 2 );
			graph.addEdge( P2, P3 );
			final DummyVertex P4 = graph.addVertex().init( "P4", 3 );
			graph.addEdge( P3, P4 );
			final DummyVertex Z2 = graph.addVertex().init( "Z2", 4 );
			graph.addEdge( P4, Z2 );
			addFork( graph, Z2, 2 );

			final DummyVertex E = graph.addVertex().init( "E", 3 );
			graph.addEdge( EMS, E );
			addFork( graph, E, 4 );

			final DummyVertex MS = graph.addVertex().init( "MS", 3 );
			graph.addEdge( EMS, MS );
			addFork( graph, MS, 4 );

			selectedVertices = new ArrayList<>();
			selectedEdges = new ArrayList<>();
			final DepthFirstSearch< DummyVertex, DummyEdge > dfs = new DepthFirstSearch<>( graph, SearchDirection.DIRECTED );
			dfs.setTraversalListener( new SearchListener< DummyVertex, DummyEdge, DepthFirstSearch< DummyVertex, DummyEdge > >()
			{
				@Override
				public void processVertexLate( final DummyVertex vertex, final DepthFirstSearch< DummyVertex, DummyEdge > search )
				{}

				@Override
				public void processVertexEarly( final DummyVertex vertex, final DepthFirstSearch< DummyVertex, DummyEdge > search )
				{
					selectedVertices.add( vertex );
				}

				@Override
				public void processEdge( final DummyEdge edge, final DummyVertex from, final DummyVertex to, final DepthFirstSearch< DummyVertex, DummyEdge > search )
				{
					selectedEdges.add( edge );
				}
			} );
			dfs.start( ABa );
		}

		private static void addFork( final DummyGraph graph, final DummyVertex mother, final int level )
		{
			if ( level <= 0 )
				return;

			final String label = mother.getLabel();
			final int timepoint = mother.getTimepoint();
			final DummyVertex daughterA = graph.addVertex().init( label + 'a', timepoint + 1 );
			graph.addEdge( mother, daughterA );
			addFork( graph, daughterA, level - 1 );

			final DummyVertex daughterP = graph.addVertex().init( label + 'p', timepoint + 1 );
			graph.addEdge( mother, daughterP );
			addFork( graph, daughterP, level - 1 );
		}
	}


	@Override
	public boolean addGraphListener( final GraphListener< DummyVertex, DummyEdge > listener )
	{
		return false;
	}

	@Override
	public boolean removeGraphListener( final GraphListener< DummyVertex, DummyEdge > listener )
	{
		return false;
	}

	@Override
	public boolean addGraphChangeListener( final GraphChangeListener listener )
	{
		return false;
	}

	@Override
	public boolean removeGraphChangeListener( final GraphChangeListener listener )
	{
		return false;
	}
}
