package org.mastodon.tomancak;

import gnu.trove.impl.Constants;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.mastodon.collection.IntRefMap;
import org.mastodon.collection.ref.IntRefArrayMap;
import org.mastodon.graph.ref.AbstractEdgePool;
import org.mastodon.graph.ref.AbstractEdgePool.AbstractEdgeLayout;
import org.mastodon.graph.ref.AbstractVertexPool;
import org.mastodon.graph.ref.AbstractVertexPool.AbstractVertexLayout;
import org.mastodon.graph.ref.GraphImp;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.ByteMappedElementArray;
import org.mastodon.pool.SingleArrayMemPool;
import org.mastodon.pool.attributes.DoubleAttribute;
import org.mastodon.pool.attributes.IndexAttribute;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.Spot;

/**
 * Graph that represents potential matches between Spots in two (or more) models.
 * Every vertex corresponds to a Spot, every vertex corresponds to a potential match.
 * There are no edges between vertices corresponding to the same model.
 * (I.e., there is a vertex coloring such that all vertices corresponding to the same model have the same color.
 * In the case of two models, the graph is bipartite.)
 * Edges are directed: {@code A --> B} means "the ellipsoid of {@code A} contains the center of {@code B}".
 */
public class MatchingGraph extends GraphImp<
		MatchingGraph.MatchingVertexPool,
		MatchingGraph.MatchingEdgePool,
		MatchingVertex, MatchingEdge, ByteMappedElement >
{
	public static MatchingGraph newWithAllSpots( final Dataset... datasets )
	{
		final List< ModelGraph > graphs = Arrays.stream( datasets ).map( ds -> ds.model().getGraph() ).collect( Collectors.toList() );
		final int capacity = graphs.stream().mapToInt( g -> g.vertices().size() ).sum();
		final MatchingGraph matching = new MatchingGraph( graphs, capacity );
		final MatchingVertex ref = matching.vertexRef();
		for ( ModelGraph graph : graphs )
			for ( Spot spot : graph.vertices() )
				matching.getVertex( spot, ref );
		return matching;
	}

	private final List< IntRefMap< MatchingVertex > > graphToSpotToMatchingVertex;

	public MatchingGraph( final ModelGraph... modelGraphs )
	{
		this( Arrays.asList( modelGraphs ), 10000 );
	}

	public MatchingGraph(
			final List< ModelGraph > modelGraphs,
			final int initialCapacity )
	{
		super( new MatchingEdgePool(
				initialCapacity,
				new MatchingVertexPool(
						initialCapacity,
						modelGraphs ) ) );
		final int numModelGraphs = modelGraphs.size();
		graphToSpotToMatchingVertex = new ArrayList<>( numModelGraphs );
		for ( int i = 0; i < numModelGraphs; i++ )
			graphToSpotToMatchingVertex.add( new IntRefArrayMap<>( vertexPool ) );
	}

	public MatchingVertex getVertex( final Spot spot )
	{
		return getVertex( spot, vertexRef() );
	}

	public MatchingVertex getVertex( final Spot spot, final MatchingVertex ref )
	{
		final int graphId = vertexPool.modelGraphIndex( spot );
		final int spotId = spot.getInternalPoolIndex();

		final IntRefMap< MatchingVertex > map = graphToSpotToMatchingVertex.get( graphId );
		MatchingVertex v = map.get( spotId, ref );
		if ( v == null )
		{
			v = super.addVertex( ref ).init( graphId, spotId );
			map.put( spotId, v );
		}
		return v;
	}

	/**
	 * Find conflicts, i.e., connected components with cardinality other than 1 or 2.
	 *
	 * @param warnOnly
	 * 		if {@code false}, throws an {@code IllegalArgumentException} if conflicts are found.
	 * 		if {@code true}, only prints warnings.
	 */
	public void checkForConflicts( final boolean warnOnly )
	{
		boolean ambiguous = false;
		for ( final MatchingVertex v : vertices() )
		{
			if ( v.edges().size() == 0 )
				// not matched
				continue;

			if ( v.outgoingEdges().size() == 1 &&
					v.incomingEdges().size() == 1 &&
					v.outgoingEdges().get( 0 ).getTarget().equals( v.incomingEdges().get( 0 ).getSource() ) )
				// perfectly matched
				continue;

			// otherwise matching is ambiguous
			System.out.println( "ambiguous: " + v.getSpot() );
			System.out.println( "           in(" + v.incomingEdges().size() + ") out(" + v.outgoingEdges().size() + ")" );
			ambiguous = true;
		}

		if ( ambiguous && !warnOnly )
			throw new IllegalArgumentException( "MatchingGraph is ambiguous" );
	}

	@Override
	public MatchingVertex addVertex()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public MatchingVertex addVertex( final MatchingVertex ref )
	{
		throw new UnsupportedOperationException();
	}

	/*
	 * vertex and edge pools
	 */

	static class MatchingVertexLayout extends AbstractVertexLayout
	{
		final IndexField graphId = indexField();
		final IndexField spotId = indexField();
	}

	static MatchingVertexLayout vertexLayout = new MatchingVertexLayout();

	static class MatchingVertexPool extends AbstractVertexPool< MatchingVertex, MatchingEdge, ByteMappedElement >
	{
		final List< ModelGraph > modelGraphs;
		final TObjectIntMap< ModelGraph > modelGraphToIndex;

		final IndexAttribute< MatchingVertex > graphId = new IndexAttribute<>( vertexLayout.graphId, this );
		final IndexAttribute< MatchingVertex > spotId = new IndexAttribute<>( vertexLayout.spotId, this );

		private MatchingVertexPool( final int initialCapacity, final List< ModelGraph > modelGraphs )
		{
			super( initialCapacity, vertexLayout, MatchingVertex.class, SingleArrayMemPool.factory( ByteMappedElementArray.factory ) );
			this.modelGraphs = modelGraphs;
			modelGraphToIndex = new TObjectIntHashMap<>( Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1 );
			for ( int i = 0; i < modelGraphs.size(); i++ )
				modelGraphToIndex.put( modelGraphs.get( i ), i );
		}

		@Override
		protected MatchingVertex createEmptyRef()
		{
			return new MatchingVertex( this );
		}

		int modelGraphIndex( final Spot spot )
		{
			final int i = modelGraphToIndex.get( spot.getModelGraph() );
			if ( i < 0 )
				throw new IllegalArgumentException();
			return i;
		}
	}

	static class MatchingEdgeLayout extends AbstractEdgeLayout
	{
		final DoubleField distSqu = doubleField();
		final DoubleField mahalDistSqu = doubleField();
	}

	static MatchingEdgeLayout edgeLayout = new MatchingEdgeLayout();

	static class MatchingEdgePool extends AbstractEdgePool< MatchingEdge, MatchingVertex, ByteMappedElement >
	{
		final DoubleAttribute< MatchingEdge > distSqu = new DoubleAttribute<>( edgeLayout.distSqu, this );
		final DoubleAttribute< MatchingEdge > mahalDistSqu = new DoubleAttribute<>( edgeLayout.mahalDistSqu, this );

		private MatchingEdgePool( final int initialCapacity, final MatchingVertexPool vertexPool )
		{
			super( initialCapacity, edgeLayout, MatchingEdge.class, SingleArrayMemPool.factory( ByteMappedElementArray.factory ), vertexPool );
			vertexPool.linkEdgePool( this );
		}

		@Override
		protected MatchingEdge createEmptyRef()
		{
			return new MatchingEdge( this );
		}
	}
}
