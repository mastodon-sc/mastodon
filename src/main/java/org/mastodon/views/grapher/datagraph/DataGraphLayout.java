package org.mastodon.views.grapher.datagraph;

import java.util.Collection;
import java.util.Iterator;

import org.mastodon.collection.RefList;
import org.mastodon.collection.RefSet;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.kdtree.ClipConvexPolytopeKDTree;
import org.mastodon.kdtree.IncrementalNearestNeighborSearchOnKDTree;
import org.mastodon.kdtree.KDTree;
import org.mastodon.model.SelectionModel;
import org.mastodon.pool.DoubleMappedElement;
import org.mastodon.ui.coloring.GraphColorGenerator;
import org.mastodon.views.grapher.SpecPair;
import org.scijava.listeners.Listeners;
import org.scijava.listeners.Listeners.List;

import net.imglib2.RealPoint;
import net.imglib2.algorithm.kdtree.ConvexPolytope;
import net.imglib2.algorithm.kdtree.HyperPlane;

public class DataGraphLayout< V extends Vertex< E >, E extends Edge< V > >
{

	private final DataGraph< V, E > dataGraph;

	private final Listeners.List< LayoutListener > listeners;

	private final FeatureModel featureModel;

	private FeatureProjection< V > yp;

	private FeatureProjection< V > xp;

	private double currentLayoutMinX;

	private double currentLayoutMaxX;

	private double currentLayoutMaxY;

	private double currentLayoutMinY;

	private final SelectionModel< DataVertex, DataEdge > selection;

	private KDTree< DataVertex, DoubleMappedElement > kdtree;

	public DataGraphLayout(
			final DataGraph< V, E > dataGraph,
			final FeatureModel featureModel,
			final SelectionModel< DataVertex, DataEdge > selection )
	{
		this.dataGraph = dataGraph;
		this.featureModel = featureModel;
		this.selection = selection;
		this.listeners = new Listeners.SynchronizedList<>();
	}

	public void setXFeature( final SpecPair x )
	{
		this.xp = x.getProjection( featureModel );
	}

	public void setYFeature( final SpecPair y )
	{
		this.yp = y.getProjection( featureModel );
	}
	/*
	 * Layout
	 */

	/**
	 * Resets X and Y position based on the specified feature specifications.
	 */
	public void layout()
	{
		currentLayoutMinX = Double.POSITIVE_INFINITY;
		currentLayoutMaxX = Double.NEGATIVE_INFINITY;
		currentLayoutMinY = Double.POSITIVE_INFINITY;
		currentLayoutMaxY = Double.NEGATIVE_INFINITY;
		if ( xp != null && yp != null )
		{
			final DataVertex ref = dataGraph.vertexRef();
			for ( final V v : this.dataGraph.modelGraph.vertices() )
			{
				final double x = xp.value( v );
				if ( x > currentLayoutMaxX )
					currentLayoutMaxX = x;
				if ( x < currentLayoutMinX )
					currentLayoutMinX = x;

				final double y = yp.value( v );
				if ( y > currentLayoutMaxY )
					currentLayoutMaxY = y;
				if ( y < currentLayoutMinY )
					currentLayoutMinY = y;

				final int id = this.dataGraph.idmap.getVertexId( v );
				final DataVertex dv = this.dataGraph.idToDataVertex.get( id, ref );
				dv.setLayoutX( x );
				dv.setLayoutY( y );
			}
			dataGraph.releaseRef( ref );

			// Regen kdtree
			kdtree = KDTree.kdtree( dataGraph.vertices(), dataGraph.getVertexPool() );
		}

		notifyListeners();
	}

	private void notifyListeners()
	{
		for ( final LayoutListener l : listeners.list )
			l.layoutChanged( this );
	}

	public DataVertex getClosestActiveVertex( final RealPoint centerPos, final double ratioXtoY, final DataVertex ref )
	{
		// TODO Auto-generated method stub
		return null;
	}

	public DataVertex getClosestActiveVertexWithin( final double lx1, final double ly1, final double lx2, final double ly2, final double ratioXtoY, final DataVertex vertexRef )
	{
		// TODO Auto-generated method stub
		return null;
	}

	public RefSet< DataVertex > getActiveVerticesWithin( final double lx1, final double ly1, final double lx2, final double ly2 )
	{
		// TODO Auto-generated method stub
		return null;
	}

	public List< LayoutListener > layoutListeners()
	{
		return listeners;
	}

	public double getCurrentLayoutMinX()
	{
		return currentLayoutMinX;
	}

	public double getCurrentLayoutMaxX()
	{
		return currentLayoutMaxX;
	}

	public double getCurrentLayoutMinY()
	{
		return currentLayoutMinY;
	}

	public double getCurrentLayoutMaxY()
	{
		return currentLayoutMaxY;
	}

	public void cropAndScale(
			final ScreenTransform transform,
			final ScreenEntities screenEntities,
			final int decorationsOffsetX,
			final int decorationsOffsetY,
			final GraphColorGenerator< DataVertex, DataEdge > colorGenerator )
	{
		if ( kdtree == null )
			return;

		final double minX = transform.getMinX();
		final double maxX = transform.getMaxX();
		final double minY = transform.getMinY();
		final double maxY = transform.getMaxY();
		screenEntities.screenTransform().set( transform );

		final RefList< ScreenVertex > screenVertices = screenEntities.getVertices();
		final RefList< ScreenEdge > screenEdges = screenEntities.getEdges();
		final ScreenVertexPool screenVertexPool = screenEntities.getVertexPool();
		final ScreenEdgePool screenEdgePool = screenEntities.getEdgePool();

		System.out.println( "\n\nReceiving " + screenVertices ); // DEBUG

		final DataVertex vref1 = dataGraph.vertexRef();
		final DataVertex v2 = dataGraph.vertexRef();
		final ScreenVertex sv = screenVertexPool.createRef();
		final ScreenEdge se = screenEdgePool.createRef();

		// Make hyperplanes for transform view.

		final HyperPlane hpMinX = new HyperPlane( new double[] { 1., 0. }, minX );
		final HyperPlane hpMaxX = new HyperPlane( new double[] { -1., 0. }, -maxX );
		final HyperPlane hpMinY = new HyperPlane( new double[] { 0., 1. }, minY );
		final HyperPlane hpMaxY = new HyperPlane( new double[] { 0., -1. }, -maxY );

		// Convex polytope from hyperplanes.
		final ConvexPolytope polytope = new ConvexPolytope( hpMinX, hpMinY, hpMaxX, hpMaxY );
		final ClipConvexPolytopeKDTree< DataVertex, DoubleMappedElement > clip = new ClipConvexPolytopeKDTree<>( kdtree );
		clip.clip( polytope );

		// Iterate only over visible data vertices.
		final Iterable< DataVertex > vertices = clip.getInsideValues();
		
		for ( final DataVertex v1 : vertices )
		{
			final int v1si = screenVertices.size();

			v1.setScreenVertexIndex( v1si );
			final int id = v1.getInternalPoolIndex();
			final String label = v1.getLabel();
			final boolean selected = selection.isSelected( v1 );

			final double x1 = v1.getLayoutX();
			final double y1 = v1.getLayoutY();
			final double sx1 = transform.layoutToScreenX( x1 ) + decorationsOffsetX;
			final double sy1 = transform.layoutToScreenY( y1 ) + decorationsOffsetY;
			screenVertexPool.create( sv ).init( id, label, sx1, sy1, selected, colorGenerator.color( v1 ) );
			screenVertices.add( sv );

			// Set edges to unitialized.
			v1.edges().forEach( e -> e.setScreenEdgeIndex( -1 ) );
		}

		// Create edges.
		for ( final DataVertex v1 : vertices )
		{
			for ( final DataEdge edge : v1.edges() )
			{
				if ( edge.getScreenEdgeIndex() >= 0 )
					continue; // Already added.

				edge.getSource( v2 );
				final int sourceScreenVertexIndex;
				final int targetScreenVertexIndex;
				if ( v2.equals( v1 ) )
				{
					edge.getTarget( v2 );
					// v1 is the source, v2 the target.
					sourceScreenVertexIndex = v1.getScreenVertexIndex();
					targetScreenVertexIndex = v2.getScreenVertexIndex();
				}
				else
				{
					sourceScreenVertexIndex = v2.getScreenVertexIndex();
					targetScreenVertexIndex = v1.getScreenVertexIndex();
				}
				if ( sourceScreenVertexIndex < 0 || targetScreenVertexIndex < 0 )
					continue;

				final int eid = edge.getInternalPoolIndex();
				final boolean eselected = selection.isSelected( edge );
				screenEdgePool.create( se ).init( eid, sourceScreenVertexIndex, targetScreenVertexIndex, eselected, colorGenerator.color( edge, v2, v1 ) );
				screenEdges.add( se );
				final int sei = se.getInternalPoolIndex();
				edge.setScreenEdgeIndex( sei );
			}
		}

		screenEdgePool.releaseRef( se );
		screenVertexPool.releaseRef( sv );
		dataGraph.releaseRef( vref1 );
		System.out.println( "Screen vertices: " + screenEntities.getVertices() ); // DEBUG
		System.out.println( "Screen edges: " + screenEntities.getEdges() ); // DEBUG

		/*
		 * Let's set min dist from closest vertex.
		 */
		final KDTree< ScreenVertex, DoubleMappedElement > svkdtree = KDTree.kdtree( screenVertices, screenVertexPool );
		final IncrementalNearestNeighborSearchOnKDTree< ScreenVertex, DoubleMappedElement > search = new IncrementalNearestNeighborSearchOnKDTree<>( svkdtree );
		for ( final ScreenVertex svd : screenVertices )
		{
			double distance = Double.POSITIVE_INFINITY;
			search.search( svd );
			search.next(); // svd itself.
			if ( search.hasNext() )
			{
				search.next(); // closest next.
				distance = search.getDistance();
			}
			svd.setVertexDist( distance );
		}
	}

	private static final String printList( final Collection< ? > list )
	{
		final StringBuilder str = new StringBuilder();
		final int n = list.size();
		final Iterator< ? > it = list.iterator();
		for ( int i = 0; i < n; i++ )
			str.append( String.format( "\n %5d: %s", i, it.next() ) );
		return str.toString();
	}

	public interface LayoutListener
	{

		/**
		 * Notifies after the layout has been done.
		 */
		public void layoutChanged( final DataGraphLayout< ?, ? > layout );
	}

}
