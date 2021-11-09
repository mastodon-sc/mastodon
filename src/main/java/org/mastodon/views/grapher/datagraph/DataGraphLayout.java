package org.mastodon.views.grapher.datagraph;

import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.collection.RefSet;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.kdtree.ClipConvexPolytopeKDTree;
import org.mastodon.kdtree.IncrementalNearestNeighborSearchOnKDTree;
import org.mastodon.kdtree.KDTree;
import org.mastodon.kdtree.NearestNeighborSearchOnKDTree;
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

	private KDTree< ScreenVertex, DoubleMappedElement > screenKDtree;

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

	/**
	 * 
	 * @param centerPos
	 *            in screen coordinates.
	 * @param ref
	 * @return
	 */
	public DataVertex getClosestActiveVertex( final RealPoint centerPos, final DataVertex ref )
	{
		final NearestNeighborSearchOnKDTree< ScreenVertex, DoubleMappedElement > search = new NearestNeighborSearchOnKDTree<>( screenKDtree );
		final ScreenVertex sv = search.get();
		return dataGraph.getVertexPool().getObject( sv.getDataVertexId(), ref );
	}

	/**
	 * 
	 * @param x1
	 *            x min in screen coordinates.
	 * @param y1
	 *            y min in screen coordinates.
	 * @param x2
	 *            x max in screen coordinates.
	 * @param y2
	 *            y max in screen coordinates.
	 * @return
	 */
	public RefSet< DataVertex > getDataVerticesWithin( final double x1, final double y1, final double x2, final double y2 )
	{
		// Make hyperplanes for transform view.
		final HyperPlane hpMinX = new HyperPlane( new double[] { 1., 0. }, x1 );
		final HyperPlane hpMaxX = new HyperPlane( new double[] { -1., 0. }, -x2 );
		final HyperPlane hpMinY = new HyperPlane( new double[] { 0., 1. }, y1 );
		final HyperPlane hpMaxY = new HyperPlane( new double[] { 0., -1. }, -y2 );

		// Convex polytope from hyperplanes.
		final ConvexPolytope polytope = new ConvexPolytope( hpMinX, hpMinY, hpMaxX, hpMaxY );
		final ClipConvexPolytopeKDTree< ScreenVertex, DoubleMappedElement > clip = new ClipConvexPolytopeKDTree<>( screenKDtree );
		clip.clip( polytope );

		// To data vertices.
		final RefSet< DataVertex > set = RefCollections.createRefSet( dataGraph.vertices() );
		final DataVertex ref = dataGraph.vertexRef();
		for ( final ScreenVertex sv : clip.getInsideValues() )
			set.add( dataGraph.getVertexPool().getObject( sv.getDataVertexId(), ref ) );
		dataGraph.releaseRef( ref );
		return set;
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

		// Get only visible data vertices.
		final Iterable< DataVertex > inside = clip.getInsideValues();

		// Reset them and their neighbors.
		for ( final DataVertex v1 : inside )
		{
			v1.setScreenVertexIndex( -1 );
			for ( final DataEdge e : v1.edges() )
			{
				e.getSource( v2 );
				if ( v2.equals( v1 ) )
					e.getTarget( v2 );
				v2.setScreenVertexIndex( -1 );
				e.setScreenEdgeIndex( -1 );
			}
		}

		// Create screen vertices and edges.
		for ( final DataVertex v1 : inside )
		{
			if ( v1.getScreenVertexIndex() < 0 )
			{
				final int v1si = screenVertices.size();
				v1.setScreenVertexIndex( v1si );
				final int id1 = v1.getInternalPoolIndex();
				final String label1 = v1.getLabel();
				final boolean selected1 = selection.isSelected( v1 );

				final double x1 = v1.getLayoutX();
				final double y1 = v1.getLayoutY();
				final double sx1 = transform.layoutToScreenX( x1 ) + decorationsOffsetX;
				final double sy1 = transform.layoutToScreenY( y1 ) + decorationsOffsetY;
				screenVertexPool.create( sv ).init( id1, label1, sx1, sy1, selected1, colorGenerator.color( v1 ) );
				screenVertices.add( sv );
			}

			// neighbors.
			for ( final DataEdge e : v1.edges() )
			{
				e.getSource( v2 );
				if ( v2.equals( v1 ) )
					e.getTarget( v2 );

				if ( v2.getScreenVertexIndex() < 0 )
				{
					// not visited. We have to create it.
					final int v2si = screenVertices.size();
					v2.setScreenVertexIndex( v2si );
					final int id2 = v2.getInternalPoolIndex();
					final String label2 = v2.getLabel();
					final boolean selected2 = selection.isSelected( v2 );

					final double x2 = v2.getLayoutX();
					final double y2 = v2.getLayoutY();
					final double sx2 = transform.layoutToScreenX( x2 ) + decorationsOffsetX;
					final double sy2 = transform.layoutToScreenY( y2 ) + decorationsOffsetY;
					screenVertexPool.create( sv ).init( id2, label2, sx2, sy2, selected2, colorGenerator.color( v2 ) );
					screenVertices.add( sv );
				}

				if ( e.getScreenEdgeIndex() < 0 )
				{
					final int esi = screenEdges.size();
					e.setScreenEdgeIndex( esi );
					final int sourceScreenVertexIndex = v1.getScreenVertexIndex();
					final int targetScreenVertexIndex = v2.getScreenVertexIndex();
					final int eid = e.getInternalPoolIndex();
					final boolean eselected = selection.isSelected( e );
					screenEdgePool.create( se ).init( eid, sourceScreenVertexIndex, targetScreenVertexIndex, eselected, colorGenerator.color( e, v2, v1 ) );
					screenEdges.add( se );
				}
			}
		}
		dataGraph.releaseRef( v2 );
		screenEdgePool.releaseRef( se );
		screenVertexPool.releaseRef( sv );

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

		/*
		 * Screen kdtree
		 */
		screenKDtree = KDTree.kdtree( screenVertices, screenVertexPool );
	}

	public interface LayoutListener
	{

		/**
		 * Notifies after the layout has been done.
		 */
		public void layoutChanged( final DataGraphLayout< ?, ? > layout );
	}
}
