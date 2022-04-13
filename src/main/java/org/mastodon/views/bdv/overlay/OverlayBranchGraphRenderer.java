package org.mastodon.views.bdv.overlay;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

import org.mastodon.kdtree.ClipConvexPolytope;
import org.mastodon.model.FocusModel;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.SelectionModel;
import org.mastodon.spatial.SpatialIndex;
import org.mastodon.ui.coloring.GraphColorGenerator;
import org.mastodon.util.GeometryUtil;
import org.mastodon.views.bdv.overlay.ScreenVertexMath.Ellipse;
import org.mastodon.views.bdv.overlay.Visibilities.Visibility;

import net.imglib2.realtransform.AffineTransform3D;

public class OverlayBranchGraphRenderer< BV extends OverlayVertex< BV, BE >, BE extends OverlayEdge< BE, BV >, V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > >
		extends OverlayGraphRenderer< BV, BE >
{

	private final OverlayBranchGraph< BV, BE, V, E > branchGraph;

	private final OverlayGraph< V, E > wrappedGraph;

	public OverlayBranchGraphRenderer(
			final OverlayBranchGraph< BV, BE, V, E > branchGraph,
			final OverlayGraph< V, E > graph,
			final HighlightModel< BV, BE > highlight,
			final FocusModel< BV, BE > focus,
			final SelectionModel< BV, BE > selection,
			final GraphColorGenerator< BV, BE > coloring )
	{
		super( branchGraph, highlight, focus, selection, coloring );
		this.branchGraph = branchGraph;
		this.wrappedGraph = graph;
	}

	@Override
	public void drawOverlays( final Graphics g )
	{
		final Graphics2D graphics = ( Graphics2D ) g;
		final BasicStroke defaultVertexStroke = new BasicStroke( ( float ) settings.getSpotStrokeWidth() );
		final BasicStroke highlightedVertexStroke = new BasicStroke( 4f );
		final BasicStroke focusedVertexStroke = new BasicStroke( 2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[] { 8f, 3f }, 0 );
		final BasicStroke defaultEdgeStroke = new BasicStroke( ( float ) settings.getLinkStrokeWidth() );
		final BasicStroke highlightedEdgeStroke = new BasicStroke( 3f );

		final AffineTransform3D transform = getRenderTransformCopy();
		final int currentTimepoint = renderTimepoint;

		final double maxDepth = getMaxDepth( transform );

		final Object antialiasing = settings.getUseAntialiasing()
				? RenderingHints.VALUE_ANTIALIAS_ON
				: RenderingHints.VALUE_ANTIALIAS_OFF;
		graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, antialiasing );

		final BV ref1 = graph.vertexRef();
		final BV ref2 = graph.vertexRef();
		final BE ref3 = graph.edgeRef();
		final BV source = graph.vertexRef();
		final BV target = graph.vertexRef();

		final double sliceDistanceFade = settings.getEllipsoidFadeDepth();
		final double timepointDistanceFade = 0.5;

		final ScreenVertexMath screenVertexMath = new ScreenVertexMath();
		final boolean drawPointsAlways = drawPointsAlways();
		final boolean drawPointsMaybe = drawPointsMaybe();
		final boolean useGradient = settings.getUseGradient();
		final boolean drawArrowHeads = settings.getDrawArrowHeads();
		final int colorSpot = settings.getColorSpot();
		final int colorPast = settings.getColorPast();
		final int colorFuture = settings.getColorFuture();

		index.readLock().lock();
		try
		{
			if ( settings.getDrawLinks() )
			{
				final BE highlighted = highlight.getHighlightedEdge( ref3 );

				graphics.setStroke( defaultEdgeStroke );
				forEachVisibleEdge( transform, currentTimepoint, ( edge, td0, td1, sd0, sd1, x0, y0, x1, y1 ) -> {
					final boolean isHighlighted = edge.equals( highlighted );

					edge.getSource( source );
					edge.getTarget( target );
					final int edgeColor = coloring.color( edge, source, target );
					final Color c1 = getColor(
							sd1,
							td1,
							sliceDistanceFade,
							timepointDistanceFade,
							selection.isSelected( edge ),
							isHighlighted,
							colorSpot,
							colorPast,
							colorFuture,
							edgeColor );
					if ( useGradient )
					{
						final Color c0 = getColor(
								sd0,
								td0,
								sliceDistanceFade,
								timepointDistanceFade,
								selection.isSelected( edge ),
								isHighlighted,
								colorSpot,
								colorPast,
								colorFuture,
								edgeColor );
						graphics.setPaint( new GradientPaint( x0, y0, c0, x1, y1, c1 ) );
					}
					else
					{
						graphics.setPaint( c1 );
					}
					if ( isHighlighted )
						graphics.setStroke( highlightedEdgeStroke );
					graphics.drawLine( x0, y0, x1, y1 );

					// Draw arrows for edge direction.
					if ( drawArrowHeads )
					{
						final double dx = x1 - x0;
						final double dy = y1 - y0;
						final double alpha = Math.atan2( dy, dx );
						final double l = 5;
						final double theta = Math.PI / 6.;
						final int x1a = ( int ) Math.round( x1 - l * Math.cos( alpha - theta ) );
						final int x1b = ( int ) Math.round( x1 - l * Math.cos( alpha + theta ) );
						final int y1a = ( int ) Math.round( y1 - l * Math.sin( alpha - theta ) );
						final int y1b = ( int ) Math.round( y1 - l * Math.sin( alpha + theta ) );
						graphics.drawLine( x1, y1, x1a, y1a );
						graphics.drawLine( x1, y1, x1b, y1b );
					}

					if ( isHighlighted )
						graphics.setStroke( defaultEdgeStroke );
				});
			}

			if ( settings.getDrawSpots() )
			{
				final double ellipsoidFadeDepth = settings.getEllipsoidFadeDepth();
				final boolean drawSpotLabels = settings.getDrawSpotLabels();
				final boolean drawEllipsoidSliceIntersection = settings.getDrawEllipsoidSliceIntersection();
				final boolean drawEllipsoidSliceProjection = settings.getDrawEllipsoidSliceProjection();
				final double pointFadeDepth = settings.getPointFadeDepth();
				final boolean fillSpots = settings.getFillSpots();
				final Visibility< BV, BE > visibility = visibilities.getVisibility();
				
				final BV highlighted = highlight.getHighlightedVertex( ref1 );
				final BV focused = focus.getFocusedVertex( ref2 );

				graphics.setStroke( defaultVertexStroke );
				final AffineTransform torig = graphics.getTransform();

				final SpatialIndex< BV > si = index.getSpatialIndex( currentTimepoint );
				final ClipConvexPolytope< BV > ccp = si.getClipConvexPolytope();
				ccp.clip( getVisiblePolytopeGlobal( transform, currentTimepoint ) );
				for ( final BV vertex : ccp.getInsideValues() )
				{
					if ( !visibility.isVisible( vertex ) )
						continue;

					final int color = coloring.color( vertex );
					final boolean isHighlighted = vertex.equals( highlighted );
					final boolean isFocused = vertex.equals( focused );

					screenVertexMath.init( vertex, transform );

					final double x = screenVertexMath.getViewPos()[ 0 ];
					final double y = screenVertexMath.getViewPos()[ 1 ];
					final double z = screenVertexMath.getViewPos()[ 2 ];
					final double sd = sliceDistance( z, maxDepth );

					if ( drawEllipsoidSliceIntersection )
					{
						if ( screenVertexMath.intersectsViewPlane() )
						{
							final Ellipse ellipse = screenVertexMath.getIntersectEllipse();

							graphics.setColor( getColor(
									0,
									0,
									ellipsoidFadeDepth,
									timepointDistanceFade,
									selection.isSelected( vertex ),
									isHighlighted,
									colorSpot,
									colorPast,
									colorFuture,
									color ) );
							if ( isHighlighted )
								graphics.setStroke( highlightedVertexStroke );
							else if ( isFocused )
								graphics.setStroke( focusedVertexStroke );
							drawEllipse( graphics, ellipse, torig, fillSpots );
							if ( isHighlighted || isFocused )
								graphics.setStroke( defaultVertexStroke );

							if ( !drawEllipsoidSliceProjection && drawSpotLabels )
								drawEllipseLabel( graphics, ellipse, vertex.getLabel() );
						}
					}

					if ( sd > -1 && sd < 1 )
					{
						if ( drawEllipsoidSliceProjection )
						{
							final Ellipse ellipse = screenVertexMath.getProjectEllipse();

							graphics.setColor( getColor(
									sd,
									0,
									ellipsoidFadeDepth,
									timepointDistanceFade,
									selection.isSelected( vertex ),
									isHighlighted,
									colorSpot,
									colorPast,
									colorFuture,
									color ) );
							if ( isHighlighted )
								graphics.setStroke( highlightedVertexStroke );
							else if ( isFocused )
								graphics.setStroke( focusedVertexStroke );
							drawEllipse( graphics, ellipse, torig, fillSpots );
							if ( isHighlighted || isFocused )
								graphics.setStroke( defaultVertexStroke );

							if ( drawSpotLabels )
								drawEllipseLabel( graphics, ellipse, vertex.getLabel() );

							graphics.setTransform( torig );
						}

						if ( drawPointsAlways || ( drawPointsMaybe && !screenVertexMath.intersectsViewPlane() ) )
						{
							graphics.setColor( getColor(
									sd,
									0,
									pointFadeDepth,
									timepointDistanceFade,
									selection.isSelected( vertex ),
									isHighlighted,
									colorSpot,
									colorPast,
									colorFuture,
									color ) );
							double radius = pointRadius;
							if ( isHighlighted || isFocused )
								radius *= 2;
							final int ox = ( int ) ( x - radius );
							final int oy = ( int ) ( y - radius );
							final int ow = ( int ) ( 2 * radius );
							if ( isFocused )
								graphics.fillRect( ox, oy, ow, ow );
							else
								graphics.fillOval( ox, oy, ow, ow );
						}
					}
				}
			}
		}
		finally
		{
			index.readLock().unlock();
		}
		branchGraph.releaseRef( ref1 );
		branchGraph.releaseRef( ref2 );
	}

	@Override
	public BE getEdgeAt( final int x, final int y, final double tolerance, final BE ref )
	{
		if ( !settings.getDrawLinks() )
			return null;

		final AffineTransform3D transform = getRenderTransformCopy();
		final int currentTimepoint = renderTimepoint;

		class Op implements EdgeOperation< BE >
		{
			final double squTolerance = tolerance * tolerance;

			double bestSquDist = Double.POSITIVE_INFINITY;

			boolean found = false;

			@Override
			public void apply( final BE edge, final double td0, final double td1, final double sd0, final double sd1, final int x0, final int y0, final int x1, final int y1 )
			{
				final double squDist = GeometryUtil.squSegmentDist( x, y, x0, y0, x1, y1 );
				if ( squDist <= squTolerance && squDist < bestSquDist )
				{
					found = true;
					bestSquDist = squDist;
					ref.refTo( edge );
				}
			}
		};
		final Op op = new Op();

		index.readLock().lock();
		try
		{
			forEachVisibleEdge( transform, currentTimepoint, op );
		}
		finally
		{
			index.readLock().unlock();
		}
		return op.found ? ref : null;
	}

}
