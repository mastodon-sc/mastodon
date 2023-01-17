/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.views.trackscheme.display.style.dummygraph;

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
import org.mastodon.model.DefaultSelectionModel;
import org.mastodon.model.SelectionModel;

public class DummyGraph extends AbstractObjectIdGraph< DummyVertex, DummyEdge >
		implements ListenableGraph< DummyVertex, DummyEdge >
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
		CELEGANS( CElegansExample.graph, CElegansExample.selectedVertices, CElegansExample.selectedEdges ),
		DIVIDING_CELL( DividingCellExample.graph, DividingCellExample.selectedVertices,
				DividingCellExample.selectedEdges );

		private final DummyGraph graph;

		private SelectionModel< DummyVertex, DummyEdge > selectionModel;

		private Examples( final DummyGraph graph, final Collection< DummyVertex > vertices,
				final Collection< DummyEdge > edges )
		{
			this.graph = graph;
			this.selectionModel = new DefaultSelectionModel<>( graph, graph.getIdBimap() );
			selectionModel.setEdgesSelected( edges, true );
			selectionModel.setVerticesSelected( vertices, true );
		}

		public DummyGraph getGraph()
		{
			return graph;
		}

		public SelectionModel< DummyVertex, DummyEdge > getSelectionModel()
		{
			return selectionModel;
		}
	}

	private static final class DividingCellExample
	{
		private static final DummyGraph graph;

		private static final Collection< DummyVertex > selectedVertices;

		private static final Collection< DummyEdge > selectedEdges;

		static
		{
			graph = new DummyGraph();
			selectedVertices = new ArrayList<>();
			selectedEdges = new ArrayList<>();

			DummyVertex previous = null;
			for ( int tp = 0; tp < 11; tp++ )
			{
				if ( tp == 6 )
				{
					for ( int j = 0; j < 10; j++ )
					{
						final DummyVertex v = graph.addVertex().init( "Z" + j + "C", tp );
						graph.addEdge( previous, v );
						previous = v;
					}
				}
				else
				{
					final DummyVertex v = graph.addVertex().init( "Z" + tp, tp );
					if ( previous != null )
						graph.addEdge( previous, v );

					previous = v;
				}
			}

			DummyVertex previous1 = previous;
			DummyVertex previous2 = previous;
			for ( int tp = 11; tp < 17; tp++ )
			{
				final DummyVertex v1 = graph.addVertex().init( "AB" + tp, tp );
				graph.addEdge( previous1, v1 );
				previous1 = v1;

				if ( tp == 13 )
				{
					DummyVertex previous2bis = previous2;
					for ( int j = 0; j < 10; j++ )
					{
						final DummyVertex v2 = graph.addVertex().init( "P" + j + "C", tp );
						final DummyEdge e2 = graph.addEdge( previous2bis, v2 );
						selectedVertices.add( v2 );
						selectedEdges.add( e2 );
						previous2bis = v2;
					}
					previous2 = previous2bis;
				}
				else
				{
					final DummyVertex v2 = graph.addVertex().init( "P" + tp, tp );
					final DummyEdge e2 = graph.addEdge( previous2, v2 );
					selectedVertices.add( v2 );
					selectedEdges.add( e2 );
					previous2 = v2;
				}
			}
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
			final DepthFirstSearch< DummyVertex, DummyEdge > dfs =
					new DepthFirstSearch<>( graph, SearchDirection.DIRECTED );
			dfs.setTraversalListener(
					new SearchListener< DummyVertex, DummyEdge, DepthFirstSearch< DummyVertex, DummyEdge > >()
					{
						@Override
						public void processVertexLate( final DummyVertex vertex,
								final DepthFirstSearch< DummyVertex, DummyEdge > search )
						{}

						@Override
						public void processVertexEarly( final DummyVertex vertex,
								final DepthFirstSearch< DummyVertex, DummyEdge > search )
						{
							selectedVertices.add( vertex );
						}

						@Override
						public void processEdge( final DummyEdge edge, final DummyVertex from, final DummyVertex to,
								final DepthFirstSearch< DummyVertex, DummyEdge > search )
						{
							selectedEdges.add( edge );
						}

						@Override
						public void crossComponent( final DummyVertex from, final DummyVertex to,
								final DepthFirstSearch< DummyVertex, DummyEdge > search )
						{}
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
