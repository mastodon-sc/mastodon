package org.mastodon.views.trackscheme;

import org.junit.Test;
import org.mastodon.adapter.SelectionModelAdapter;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.ModelGraphTrackSchemeProperties;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.DefaultRootsModel;
import org.mastodon.model.DefaultSelectionModel;
import org.mastodon.model.RootsModel;
import org.mastodon.model.SelectionModel;
import org.mastodon.ui.coloring.GraphColorGeneratorAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link LineageTreeLayoutImp}
 */
public class LineageTreeLayoutImpTest
{
	/**
	 * Tests if the {@link LineageTreeLayout} properly layouts a graph that is
	 * shaped like this:
	 * <pre>
	 *   O
	 *  / \
	 * O   O
	 *  \ /
	 *   O
	 * </pre>
	 */
	@Test
	public void testDiamondGraph()
	{
		// setup
		ModelGraph graph = initDiamondModelGraph();
		TrackSchemeGraph<Spot, Link> tsGraph = new TrackSchemeGraph<>( graph, graph.getGraphIdBimap(), new ModelGraphTrackSchemeProperties( graph ) );
		ScreenEntities screenEntities = new ScreenEntities( tsGraph );
		ScreenTransform transform = initScreenTransform();
		LineageTreeLayout layout = initLineageTreeLayout( graph, tsGraph );
		// run
		layout.layout();
		layout.cropAndScale( transform, screenEntities, 0, 0, new GraphColorGeneratorAdapter<>( tsGraph.getVertexMap(), tsGraph.getEdgeMap() ) );
		// test
		assertEquals( Arrays.asList( "a", "b", "c", "d" ), getVertexLabels( screenEntities ) );
		assertEquals( Arrays.asList( "a->b", "a->c", "b->d", "c->d"), getEdges( screenEntities ) );
	}


	private ModelGraph initDiamondModelGraph()
	{
		ModelGraph graph = new ModelGraph();
		Spot a = addSpot( graph, 0, "a", 0, 0, 0 );
		Spot b = addSpot( graph, 1, "b", -1, 0, 0 );
		Spot c = addSpot( graph, 1, "c", 1, 0, 0 );
		Spot d = addSpot( graph, 2, "d", 0, 0, 0 );
		graph.addEdge( a, b );
		graph.addEdge( a, c );
		graph.addEdge( c, d );
		graph.addEdge( b, d );
		return graph;
	}

	private LineageTreeLayout initLineageTreeLayout( ModelGraph graph, TrackSchemeGraph<Spot, Link> tsGraph )
	{
		SelectionModel<Spot, Link> selectionModel = new DefaultSelectionModel<>( graph, graph.getGraphIdBimap() );
		SelectionModel<TrackSchemeVertex, TrackSchemeEdge> tsSelectionModel = new SelectionModelAdapter<>( selectionModel, tsGraph.getVertexMap(), tsGraph.getEdgeMap() );
		RootsModel<TrackSchemeVertex> tsRootsModel = new DefaultRootsModel<>( graph, tsGraph );
		LineageTreeLayout layout = new LineageTreeLayoutImp( tsRootsModel, tsGraph, tsSelectionModel );
		return layout;
	}

	private ScreenTransform initScreenTransform()
	{
		ScreenTransform transform = new ScreenTransform();
		transform.set( -1, 5, -1, 5, 601, 601 );
		return transform;
	}

	private Spot addSpot( ModelGraph graph, int timepointId, String label, double... pos )
	{
		Spot spot = graph.addVertex().init( timepointId, pos, 1 );
		spot.setLabel( label );
		return spot;
	}

	private List<String> getVertexLabels( ScreenEntities screenEntities )
	{
		List<String> labels = new ArrayList<>();
		for ( ScreenVertex v : screenEntities.getVertices() )
			labels.add( v.getLabel() );
		labels.sort( String::compareTo );
		return labels;
	}

	private List<String> getEdges( ScreenEntities screenEntities )
	{
		List<String> labels = new ArrayList<>();
		for ( ScreenVertex v : screenEntities.getVertices() )
			labels.add( v.getLabel() );
		List<String> edges = new ArrayList<>();
		for ( ScreenEdge e : screenEntities.getEdges() )
			edges.add( labels.get( e.getSourceScreenVertexIndex() ) + "->" +
					labels.get( e.getTargetScreenVertexIndex() ) );
		edges.sort( String::compareTo );
		return edges;
	}

}
