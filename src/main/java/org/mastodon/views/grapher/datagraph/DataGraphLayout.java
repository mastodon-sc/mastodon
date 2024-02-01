/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.views.grapher.datagraph;

import java.util.Collection;

import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.collection.RefSet;
import org.mastodon.collection.ref.RefArrayList;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Edges;
import org.mastodon.graph.Vertex;
import org.mastodon.kdtree.ClipConvexPolytopeKDTree;
import org.mastodon.kdtree.IncrementalNearestNeighborSearchOnKDTree;
import org.mastodon.kdtree.KDTree;
import org.mastodon.kdtree.NearestNeighborSearchOnKDTree;
import org.mastodon.model.HasLabel;
import org.mastodon.model.SelectionModel;
import org.mastodon.pool.DoubleMappedElement;
import org.mastodon.spatial.HasTimepoint;
import org.mastodon.ui.coloring.GraphColorGenerator;
import org.scijava.listeners.Listeners;
import org.scijava.listeners.Listeners.List;

import net.imglib2.RealPoint;
import net.imglib2.algorithm.kdtree.ConvexPolytope;
import net.imglib2.algorithm.kdtree.HyperPlane;

public class DataGraphLayout< V extends Vertex< E > & HasTimepoint & HasLabel, E extends Edge< V > >
{

	private final DataGraph< V, E > dataGraph;

	private final Listeners.List< LayoutListener > listeners;

	private FeatureProjection< V > ypVertex;

	private FeatureProjection< V > xpVertex;

	private FeatureProjection< E > xpEdge;

	private FeatureProjection< E > ypEdge;

	private boolean incomingEdge;

	private RefSet< DataVertex > vertices;

	private double currentLayoutMinX;

	private double currentLayoutMaxX;

	private double currentLayoutMaxY;

	private double currentLayoutMinY;

	private final SelectionModel< DataVertex, DataEdge > selection;

	private KDTree< DataVertex, DoubleMappedElement > kdtree;

	// TODO. Warning: we keep a ref to screen vertices that were last generated
	// by the call to #cropAndScale(). Is this safe? Is this Ok?
	private KDTree< ScreenVertex, DoubleMappedElement > screenKDtree;

	private boolean paintEdges;

	public DataGraphLayout(
			final DataGraph< V, E > dataGraph,
			final SelectionModel< DataVertex, DataEdge > selection )
	{
		this.dataGraph = dataGraph;
		this.selection = selection;
		this.listeners = new Listeners.SynchronizedList<>();
		this.vertices = RefCollections.createRefSet( dataGraph.vertices() );
	}

	public void setXFeatureVertex( final FeatureProjection< V > xproj )
	{
		this.xpVertex = xproj;
		this.xpEdge = null;
	}

	public void setYFeatureVertex( final FeatureProjection< V > yproj )
	{
		this.ypVertex = yproj;
		this.ypEdge = null;
	}

	public void setXFeatureEdge( final FeatureProjection< E > xproj, final boolean incoming )
	{
		this.xpEdge = xproj;
		this.incomingEdge = incoming;
		this.xpVertex = null;
	}

	public void setYFeatureEdge( final FeatureProjection< E > yproj, final boolean incoming )
	{
		this.ypEdge = yproj;
		this.incomingEdge = incoming;
		this.ypVertex = null;
	}

	/**
	 * Specify what vertices to layout.
	 * 
	 * @param vertices
	 *            the vertices to layout.
	 */
	public void setVertices( final RefSet< DataVertex > vertices )
	{
		this.vertices = vertices;
	}

	/**
	 * Sets whether the screen edges will be generated.
	 * 
	 * @param paintEdges
	 *            if <code>true</code> the screen edges will be generated.
	 */
	public void setPaintEdges( final boolean paintEdges )
	{
		this.paintEdges = paintEdges;
	}

	/*
	 * Layout
	 */

	/**
	 * Resets X and Y position based on the current feature specifications for
	 * the current vertices in the data graph.
	 */
	public void layout()
	{
		if ( vertices.isEmpty() )
		{
			currentLayoutMinX = -1.;
			currentLayoutMaxX = 1.;
			currentLayoutMinY = -1.;
			currentLayoutMaxY = 1.;
		}
		else
		{
			currentLayoutMinX = Double.POSITIVE_INFINITY;
			currentLayoutMaxX = Double.NEGATIVE_INFINITY;
			currentLayoutMinY = Double.POSITIVE_INFINITY;
			currentLayoutMaxY = Double.NEGATIVE_INFINITY;
			if ( ( xpVertex != null || xpEdge != null ) && ( ypVertex != null || ypEdge != null ) )
			{
				final V ref = dataGraph.idmap.vertexIdBimap().createRef();
				for ( final DataVertex dv : vertices )
				{
					final int id = dv.getModelVertexId();
					final V v = dataGraph.idmap.getVertex( id, ref );

					final double x = getXFeatureValue( v );
					if ( x > currentLayoutMaxX )
						currentLayoutMaxX = x;
					if ( x < currentLayoutMinX )
						currentLayoutMinX = x;

					final double y = getYFeatureValue( v );
					if ( y > currentLayoutMaxY )
						currentLayoutMaxY = y;
					if ( y < currentLayoutMinY )
						currentLayoutMinY = y;

					dv.setLayoutX( x );
					dv.setLayoutY( y );
				}
				dataGraph.idmap.vertexIdBimap().releaseRef( ref );
			}

			// Regen kdtree
			final Collection< DataVertex > collection;
			if ( vertices instanceof Collection )
			{
				collection = vertices;
			}
			else
			{
				collection = new RefArrayList<>( dataGraph.getVertexPool() );
				vertices.forEach( collection::add );
			}
			kdtree = KDTree.kdtree( collection, dataGraph.getVertexPool() );
		}

		notifyListeners();
	}

	private double getXFeatureValue( final V v )
	{
		return getFeatureValue( v, xpVertex, xpEdge );
	}

	private double getYFeatureValue( final V v )
	{
		return getFeatureValue( v, ypVertex, ypEdge );
	}

	private double getFeatureValue( final V v, final FeatureProjection< V > xpv, final FeatureProjection< E > xpe )
	{
		if ( xpv != null )
			return xpv.value( v );

		if ( xpe != null )
		{
			final Edges< E > edges = ( incomingEdge )
					? v.incomingEdges()
					: v.outgoingEdges();
			if ( edges.size() != 1 )
				return Double.NaN;
			return xpe.value( edges.iterator().next() );
		}
		return Double.NaN;
	}

	private void notifyListeners()
	{
		for ( final LayoutListener l : listeners.list )
			l.layoutChanged( this );
	}

	/**
	 * Returns the closest active vertex from the specified coordinate.
	 * 
	 * @param centerPos
	 *            the coordinate, in screen coordinates.
	 * @param ref
	 *            a ref to a vertex object.
	 * @return the closest vertex, or <code>null</code> if it cannot be found.
	 */
	public DataVertex getClosestActiveVertex( final RealPoint centerPos, final DataVertex ref )
	{
		final NearestNeighborSearchOnKDTree< ScreenVertex, DoubleMappedElement > search =
				new NearestNeighborSearchOnKDTree<>( screenKDtree );
		final ScreenVertex sv = search.get();
		return ( sv == null ) ? null : dataGraph.getVertexPool().getObject( sv.getDataVertexId(), ref );
	}

	/**
	 * Returns the set of data vertices that are painted according to this
	 * layout instance, within the specified <b>screen coordinates</b>.
	 * 
	 * @param x1
	 *            x min in screen coordinates.
	 * @param y1
	 *            y min in screen coordinates.
	 * @param x2
	 *            x max in screen coordinates.
	 * @param y2
	 *            y max in screen coordinates.
	 * @return a new {@link RefSet}.
	 */
	public RefSet< DataVertex > getDataVerticesWithin( final double x1, final double y1, final double x2,
			final double y2 )
	{
		final RefSet< DataVertex > set = RefCollections.createRefSet( dataGraph.vertices() );
		if ( screenKDtree == null )
			return set;

		final double lx1 = Math.min( x1, x2 );
		final double lx2 = Math.max( x1, x2 );
		final double ly1 = Math.min( y1, y2 );
		final double ly2 = Math.max( y1, y2 );

		// Make hyperplanes for transform view.
		final HyperPlane hpMinX = new HyperPlane( new double[] { 1., 0. }, lx1 );
		final HyperPlane hpMaxX = new HyperPlane( new double[] { -1., 0. }, -lx2 );
		final HyperPlane hpMinY = new HyperPlane( new double[] { 0., 1. }, ly1 );
		final HyperPlane hpMaxY = new HyperPlane( new double[] { 0., -1. }, -ly2 );

		// Convex polytope from hyperplanes.
		final ConvexPolytope polytope = new ConvexPolytope( hpMinX, hpMinY, hpMaxX, hpMaxY );
		final ClipConvexPolytopeKDTree< ScreenVertex, DoubleMappedElement > clip =
				new ClipConvexPolytopeKDTree<>( screenKDtree );
		clip.clip( polytope );

		// To data vertices.
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
		final ClipConvexPolytopeKDTree< DataVertex, DoubleMappedElement > clip =
				new ClipConvexPolytopeKDTree<>( kdtree );
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
			final double x1 = v1.getLayoutX();
			if ( Double.isNaN( x1 ) )
				continue;

			final double y1 = v1.getLayoutY();
			if ( Double.isNaN( y1 ) )
				continue;

			if ( v1.getScreenVertexIndex() < 0 )
			{
				final int v1si = screenVertices.size();
				v1.setScreenVertexIndex( v1si );
				final int id1 = v1.getInternalPoolIndex();
				final String label1 = v1.getLabel();
				final boolean selected1 = selection.isSelected( v1 );

				final double sx1 = transform.layoutToScreenX( x1 ) + decorationsOffsetX;
				final double sy1 = transform.layoutToScreenY( y1 );
				screenVertexPool.create( sv ).init( id1, label1, sx1, sy1, selected1, colorGenerator.color( v1 ) );
				screenVertices.add( sv );
			}

			if ( paintEdges )
			{
				for ( final DataEdge e : v1.edges() )
				{
					e.getSource( v2 );
					if ( v2.equals( v1 ) )
						e.getTarget( v2 );

					// Do not paint edges that connect vertices not in the set.
					if ( !vertices.contains( v2 ) )
						continue;

					if ( v2.getScreenVertexIndex() < 0 )
					{
						final double x2 = v2.getLayoutX();
						if ( Double.isNaN( x2 ) )
							continue;

						final double y2 = v2.getLayoutY();
						if ( Double.isNaN( y2 ) )
							continue;

						// not visited. We have to create it.
						final int v2si = screenVertices.size();
						v2.setScreenVertexIndex( v2si );
						final int id2 = v2.getInternalPoolIndex();
						final String label2 = v2.getLabel();
						final boolean selected2 = selection.isSelected( v2 );

						final double sx2 = transform.layoutToScreenX( x2 ) + decorationsOffsetX;
						final double sy2 = transform.layoutToScreenY( y2 );
						screenVertexPool.create( sv ).init( id2, label2, sx2, sy2, selected2,
								colorGenerator.color( v2 ) );
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
						screenEdgePool.create( se ).init( eid, sourceScreenVertexIndex, targetScreenVertexIndex,
								eselected, colorGenerator.color( e, v2, v1 ) );
						screenEdges.add( se );
					}
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
		final IncrementalNearestNeighborSearchOnKDTree< ScreenVertex, DoubleMappedElement > search =
				new IncrementalNearestNeighborSearchOnKDTree<>( svkdtree );
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

	/**
	 * Interface for listeners that are notified when the layout of the data
	 * graph has changed.
	 */
	public interface LayoutListener
	{

		/**
		 * Notified after the layout has been done.
		 * 
		 * @param layout
		 *            the layout.
		 */
		public void layoutChanged( final DataGraphLayout< ?, ? > layout );
	}
}
