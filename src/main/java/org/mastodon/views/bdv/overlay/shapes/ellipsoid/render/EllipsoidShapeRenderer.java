package org.mastodon.views.bdv.overlay.shapes.ellipsoid.render;

import static org.mastodon.views.bdv.overlay.OverlayGraphRenderer.getMaxDepth;
import static org.mastodon.views.bdv.overlay.OverlayGraphRenderer.sliceDistance;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.kdtree.ClipConvexPolytope;
import org.mastodon.kdtree.IncrementalNearestNeighborSearch;
import org.mastodon.model.FocusModel;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.SelectionModel;
import org.mastodon.spatial.SpatialIndex;
import org.mastodon.ui.coloring.GraphColorGenerator;
import org.mastodon.views.bdv.overlay.OverlayGraph;
import org.mastodon.views.bdv.overlay.Visibilities;
import org.mastodon.views.bdv.overlay.Visibilities.Visibility;
import org.mastodon.views.bdv.overlay.shapes.VertexShapeRenderer;
import org.mastodon.views.bdv.overlay.shapes.ellipsoid.EllipsoidOverlayEdge;
import org.mastodon.views.bdv.overlay.shapes.ellipsoid.EllipsoidOverlayVertex;
import org.mastodon.views.bdv.overlay.shapes.ellipsoid.render.ScreenVertexMath.Ellipse;
import org.mastodon.views.bdv.overlay.shapes.ellipsoid.style.EllipsoidRenderSettings;

import net.imglib2.RealPoint;
import net.imglib2.algorithm.kdtree.ConvexPolytope;
import net.imglib2.neighborsearch.NearestNeighborSearch;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.util.LinAlgHelpers;

public class EllipsoidShapeRenderer< V extends EllipsoidOverlayVertex< V, E >, E extends EllipsoidOverlayEdge< E, V > >
		implements VertexShapeRenderer< V, E, EllipsoidRenderSettings >
{

	private final AffineTransform3D transform;

	private EllipsoidRenderSettings settings;

	public static final double pointRadius = 2.5;

	private final HighlightModel< V, E > highlight;

	private final FocusModel< V > focus;

	private final SelectionModel< V, E > selection;

	private final GraphColorGenerator< V, E > coloring;

	private final Visibilities< V, E > visibilities;

	private final ScreenVertexMath screenVertexMath;

	private final OverlayGraph< V, E > graph;

	public EllipsoidShapeRenderer(
			final OverlayGraph< V, E > graph,
			final HighlightModel< V, E > highlight,
			final FocusModel< V > focus,
			final SelectionModel< V, E > selection,
			final GraphColorGenerator< V, E > coloring,
			final Visibilities< V, E > visibilities )
	{
		this.graph = graph;
		this.transform = new AffineTransform3D();
		this.highlight = highlight;
		this.focus = focus;
		this.selection = selection;
		this.coloring = coloring;
		this.visibilities = visibilities;
		this.screenVertexMath = new ScreenVertexMath();
	}

	@Override
	public void drawVertices( final Graphics2D graphics, final Iterable< V > vertices )
	{
		// Special vertices
		final V ref1 = graph.vertexRef();
		final V ref2 = graph.vertexRef();
		final V highlighted = highlight.getHighlightedVertex( ref1 );
		final V focused = focus.getFocusedVertex( ref2 );

		// Default strokes
		final BasicStroke defaultVertexStroke = new BasicStroke( ( float ) settings.getSpotStrokeWidth() );
		final BasicStroke highlightedVertexStroke = new BasicStroke( 4f );
		final BasicStroke focusedVertexStroke = new BasicStroke( 2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[] { 8f, 3f }, 0 );

		// Colors
		final int colorSpot = settings.getColorSpot();
		final int colorPast = settings.getColorPast();
		final int colorFuture = settings.getColorFuture();

		// What to draw
		final boolean drawPointsAlways = drawPointsAlways();
		final boolean drawPointsMaybe = drawPointsMaybe();
		final double timepointDistanceFade = 0.5;
		final double maxDepth = getMaxDepth( transform, settings );
		final double ellipsoidFadeDepth = settings.getEllipsoidFadeDepth();
		final boolean drawSpotLabels = settings.getDrawSpotLabels();
		final boolean drawEllipsoidSliceIntersection = settings.getDrawEllipsoidSliceIntersection();
		final boolean drawEllipsoidSliceProjection = settings.getDrawEllipsoidSliceProjection();
		final double pointFadeDepth = settings.getPointFadeDepth();
		final boolean fillSpots = settings.getFillSpots();
		final Visibility< V, E > visibility = visibilities.getVisibility();

		// Setup graphics
		graphics.setStroke( defaultVertexStroke );
		final AffineTransform tOrig = graphics.getTransform();

		for ( final V vertex : vertices )
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
					drawEllipse( graphics, ellipse, tOrig, fillSpots );
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
					drawEllipse( graphics, ellipse, tOrig, fillSpots );
					if ( isHighlighted || isFocused )
						graphics.setStroke( defaultVertexStroke );

					if ( drawSpotLabels )
						drawEllipseLabel( graphics, ellipse, vertex.getLabel() );

					graphics.setTransform( tOrig );
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

		graph.releaseRef( ref1 );
		graph.releaseRef( ref2 );
	}

	@Override
	public V getVertexAt(
			final int x,
			final int y,
			final double tolerance,
			final SpatialIndex< V > si,
			final ConvexPolytope polytope,
			final double maxRadiusSquared,
			final V ref )
	{
		final double[] lPos = new double[] { x, y, 0 };
		final double[] gPos = new double[ 3 ];
		transform.applyInverse( gPos, lPos );
		final double[] xy = new double[] { gPos[ 0 ], gPos[ 1 ] };

		final Visibility< V, E > visibility = visibilities.getVisibility();
		boolean found = false;
		final double maxDepth = getMaxDepth( transform, settings );

		if ( settings.getDrawEllipsoidSliceProjection() )
		{
			final double[] vPos = new double[ 3 ];
			double minDist = Double.MAX_VALUE;

			final ClipConvexPolytope< V > ccp = si.getClipConvexPolytope();
			ccp.clip( polytope );
			for ( final V vertex : ccp.getInsideValues() )
			{
				if ( !visibility.isVisible( vertex ) )
					continue;

				screenVertexMath.init( vertex, transform );
				final double z = screenVertexMath.getViewPos()[ 2 ];
				final double sd = sliceDistance( z, maxDepth );
				if ( sd > -1 && sd < 1 && screenVertexMath.projectionContainsView( xy ) )
				{
					found = true;
					vertex.localize( vPos );
					final double d = LinAlgHelpers.squareDistance( vPos, gPos );
					if ( d < minDist )
					{
						minDist = d;
						ref.refTo( vertex );
					}
				}
			}
		}

		if ( !found && settings.getDrawEllipsoidSliceIntersection() )
		{
			final IncrementalNearestNeighborSearch< V > inns = si.getIncrementalNearestNeighborSearch();
			inns.search( RealPoint.wrap( gPos ) );
			while ( inns.hasNext() )
			{
				final V vertex = inns.next();
				if ( !visibility.isVisible( vertex ) )
					continue;

				if ( inns.getSquareDistance() > maxRadiusSquared )
					break;
				screenVertexMath.init( vertex, transform );
				if ( screenVertexMath.containsGlobal( gPos ) )
				{
					found = true;
					ref.refTo( vertex );
					break;
				}
			}
		}

		if ( !found && settings.getDrawSpotCenters() )
		{
			final NearestNeighborSearch< V > nns = si.getNearestNeighborSearch();
			nns.search( RealPoint.wrap( gPos ) );
			final V vertex = nns.getSampler().get();
			if ( vertex != null && visibility.isVisible( vertex ) )
			{
				screenVertexMath.init( vertex, transform );
				final double z = screenVertexMath.getViewPos()[ 2 ];
				final double sd = sliceDistance( z, maxDepth );
				if ( sd > -1 && sd < 1 )
				{
					final double[] p = screenVertexMath.getViewPos();
					final double dx = p[ 0 ] - x;
					final double dy = p[ 1 ] - y;
					final double dr = pointRadius + tolerance;
					if ( dx * dx + dy * dy <= dr * dr )
					{
						found = true;
						ref.refTo( vertex );
					}
				}
			}
		}
		return found ? ref : null;
	}

	@Override
	public void setTransform( final AffineTransform3D t )
	{
		this.transform.set( t );
	}

	@Override
	public void setShapeSettings( final EllipsoidRenderSettings settings )
	{
		this.settings = settings;
	}

	/**
	 * Points (ellipsoid centers) are always drawn if
	 * <ul>
	 * <li>point drawing is enabled ({@code drawPoints}),</li>
	 * </ul>
	 * and either
	 * <ul>
	 * <li>either points shall be drawn for visible ellipses too
	 * ({@code drawPointsForEllipses}),</li>
	 * <li>or no ellipses are visible anyways
	 * ({@code !drawEllipsoidSliceIntersection} and
	 * {@code !drawEllipsoidSliceProjection}).</li>
	 * </ul>
	 *
	 * @return whether to draw points always
	 */
	protected boolean drawPointsAlways()
	{
		return settings.getDrawSpotCenters()
				&& ( ( !settings.getDrawEllipsoidSliceIntersection() && !settings.getDrawEllipsoidSliceProjection() )
						|| settings.getDrawSpotCentersForEllipses() );
	}

	/**
	 * Points (ellipsoid centers) are possibly drawn if
	 * <ul>
	 * <li>point drawing is enabled ({@code drawPoints}), and</li>
	 * <li>{@code !drawEllipsoidSliceProjection} (otherwise would draw an
	 * ellipse instead), and</li>
	 * <li>({@code drawEllipsoidSliceIntersection} (drawing ellipses but
	 * possibly they are not intersection the view plane).</li>
	 * </ul>
	 * (or of course if {@code drawPointsAlways()==true}).
	 *
	 * @return whether to draw points depending on ellipse intersection with
	 *         view plane.
	 */
	protected boolean drawPointsMaybe()
	{
		return settings.getDrawSpotCenters()
				&& !settings.getDrawEllipsoidSliceProjection() && settings.getDrawEllipsoidSliceIntersection();
	}

	// TODO: move to RenderSettings
	private static final Font font = new Font( "SansSerif", Font.PLAIN, 9 );

	private static void drawEllipseLabel( final Graphics2D graphics, final Ellipse ellipse, final String label )
	{
		final double[] tr = ellipse.getCenter();
		final FontRenderContext frc = graphics.getFontRenderContext();
		final TextLayout layout = new TextLayout( label, font, frc );
		final Rectangle2D bounds = layout.getBounds();
		final float tx = ( float ) ( tr[ 0 ] - bounds.getCenterX() );
		final float ty = ( float ) ( tr[ 1 ] - bounds.getCenterY() );
		layout.draw( graphics, tx, ty );
	}

	/**
	 * Routine to draw an ellipse.
	 *
	 * @param graphics
	 *            the graphics context.
	 * @param ellipse
	 *            the ellipse to draw.
	 * @param torig
	 *            the original transform of the graphics context. If null, the
	 *            current transform of the graphics context is used.
	 * @param fillSpots
	 *            if true, the ellipse is filled, otherwise only the outline is
	 *            drawn.
	 */
	public static void drawEllipse( final Graphics2D graphics, final Ellipse ellipse, AffineTransform torig, final boolean fillSpots )
	{
		if ( torig == null )
			torig = graphics.getTransform();

		final double[] tr = ellipse.getCenter();
		final double theta = ellipse.getTheta();
		final double w = ellipse.getHalfWidth();
		final double h = ellipse.getHalfHeight();
		final Ellipse2D ellipse2D = new Ellipse2D.Double( -w, -h, 2. * w, 2. * h );

		graphics.translate( tr[ 0 ], tr[ 1 ] );
		graphics.rotate( theta );
		if ( fillSpots )
		{
			graphics.fill( ellipse2D );
			final Color color = graphics.getColor();
			graphics.setColor( Color.BLACK );
			graphics.draw( ellipse2D );
			graphics.setColor( color );
		}
		else
		{
			graphics.draw( ellipse2D );
		}

		graphics.setTransform( torig );
	}

	/**
	 * Generates a color suitable to paint an abject that might be away from the
	 * focus plane, or away in time.
	 *
	 * @param sd
	 *            sliceDistande, between -1 and 1. see
	 *            {@link #sliceDistance(double, double)}.
	 * @param td
	 *            timeDistande, between -1 and 1. see
	 *            {@link #timeDistance(double, double, double)}.
	 * @param sdFade
	 *            between 0 and 1, from which |sd| value color starts to fade
	 *            (alpha value decreases).
	 * @param tdFade
	 *            between 0 and 1, from which |td| value color starts to fade
	 *            (alpha value decreases).
	 * @param isSelected
	 *            whether to use selected or un-selected color scheme.
	 * @param color
	 *            the color assigned to the object when using a coloring scheme.
	 * @param isHighlighted
	 *            whether the object is highlighted.
	 * @param colorSpot
	 *            the default color to paint the spot.
	 * @param colorPast
	 *            the color used to paint objects that are in the past.
	 * @param colorFuture
	 *            the color used to paint objects that are in the future.
	 * @return vertex/edge color suitable for display in a BDV.
	 */
	protected static Color getColor(
			final double sd,
			final double td,
			final double sdFade,
			final double tdFade,
			final boolean isSelected,
			final boolean isHighlighted,
			final int colorSpot,
			final int colorPast,
			final int colorFuture,
			final int color )
	{
		/*
		 * |sf| = { 0 for |sd| <= sdFade, linear from 0 to 1 for |sd| = sdFade
		 * to |sd| = 1 }
		 *
		 * sgn(sf) = sgn(sd)
		 */
		final double sf;
		if ( sd > 0 )
			sf = Math.max( 0, ( sd - sdFade ) / ( 1 - sdFade ) );
		else
			sf = -Math.max( 0, ( -sd - sdFade ) / ( 1 - sdFade ) );

		final double tf;
		final int colorTo;
		if ( td > 0 )
		{
			tf = Math.max( 0, ( td - tdFade ) / ( 1 - tdFade ) );
			colorTo = isSelected
					? complementaryColor( colorFuture )
					: colorFuture;
		}
		else
		{
			tf = -Math.max( 0, ( -td - tdFade ) / ( 1 - tdFade ) );
			colorTo = isSelected
					? complementaryColor( colorPast )
					: colorPast;
		}
		final int colorFrom = isSelected
				? complementaryColor( colorSpot )
				: colorSpot;

		if ( color == 0 )
		{
			// No coloring. Color are set by the RenderSettings.
			final int r0 = ( colorFrom >> 16 ) & 0xff;
			final int g0 = ( colorFrom >> 8 ) & 0xff;
			final int b0 = ( colorFrom ) & 0xff;
			final int r1 = ( colorTo >> 16 ) & 0xff;
			final int g1 = ( colorTo >> 8 ) & 0xff;
			final int b1 = ( colorTo ) & 0xff;
			final double r = ( Math.abs( td ) * ( r1 - r0 ) + r0 ) / 255.;
			final double g = ( Math.abs( td ) * ( g1 - g0 ) + g0 ) / 255.;
			final double b = ( Math.abs( td ) * ( b1 - b0 ) + b0 ) / 255.;
			final double a = Math.max(
					isHighlighted
							? 0.8
							: ( isSelected ? 0.6 : 0.4 ),
					( 1 + tf ) * ( 1 - Math.abs( sf ) ) );
			return new Color( truncRGBA( r, g, b, a ), true );
		}
		else
		{
			/*
			 * Use some default coloring when selected. The same than when we
			 * have no coloring. There is a chance that this color is confused
			 * with a similar color in the ColorMap then.
			 */
			final int a0 = isSelected ? 255 : ( ( color >> 24 ) & 0xff );
			final int r0 = isSelected ? 255 : ( ( color >> 16 ) & 0xff );
			final int g0 = isSelected ? 0 : ( ( color >> 8 ) & 0xff );
			final int b0 = isSelected ? 25 : ( ( color ) & 0xff );
			final double r = ( 1 + Math.abs( td ) ) * r0 / 255;
			final double g = ( 1 + Math.abs( td ) ) * g0 / 255;
			final double b = ( 1 + Math.abs( td ) ) * b0 / 255;
			final double a = Math.max(
					isHighlighted
							? 0.8
							: ( isSelected ? 0.6 : 0.4 ),
					a0 / 255f * ( 1 + tf ) * ( 1 - Math.abs( sf ) ) );
			return new Color( truncRGBA( r, g, b, a ), true );
		}
	}

	private static final int complementaryColor( final int color )
	{
		return 0xff000000 | ~color;
	}

	private static int trunc255( final int i )
	{
		return Math.min( 255, Math.max( 0, i ) );
	}

	private static int truncRGBA( final int r, final int g, final int b, final int a )
	{
		return ARGBType.rgba(
				trunc255( r ),
				trunc255( g ),
				trunc255( b ),
				trunc255( a ) );
	}

	private static int truncRGBA( final double r, final double g, final double b, final double a )
	{
		return truncRGBA(
				( int ) ( 255 * r ),
				( int ) ( 255 * g ),
				( int ) ( 255 * b ),
				( int ) ( 255 * a ) );
	}

	@Override
	public RefList< V > filterVisibleVertices(
			final Iterable< V > in,
			final AffineTransform3D transform,
			final int timepoint,
			final int width,
			final int height )
	{
		final Visibility< V, E > visibility = visibilities.getVisibility();
		final RefList< V > contextList = RefCollections.createRefList( graph.vertices() );

		final double maxDepth = getMaxDepth( transform, settings );
		final boolean drawPointsAlways = drawPointsAlways();
		final boolean drawPointsMaybe = drawPointsMaybe();
		final boolean drawEllipsoidSliceIntersection = settings.getDrawEllipsoidSliceIntersection();
		final boolean drawEllipsoidSliceProjection = settings.getDrawEllipsoidSliceProjection();

		for ( final V vertex : in )
		{
			if ( !visibility.isVisible( vertex ) )
				continue;

			screenVertexMath.init( vertex, transform );

			if ( drawEllipsoidSliceIntersection )
			{
				if ( screenVertexMath.intersectsViewPlane() && screenVertexMath.intersectionIntersectsViewInterval( 0, width, 0, height ) )
				{
					contextList.add( vertex );
					continue;
				}
			}

			final double z = screenVertexMath.getViewPos()[ 2 ];
			final double sd = sliceDistance( z, maxDepth );
			if ( -1 < sd && sd < 1 )
			{
				if ( drawEllipsoidSliceProjection && screenVertexMath.projectionIntersectsViewInterval( 0, width, 0, height ) )
				{
					contextList.add( vertex );
					continue;
				}

				if ( drawPointsAlways || ( drawPointsMaybe && !screenVertexMath.intersectsViewPlane() ) )
				{
					final double x = screenVertexMath.getViewPos()[ 0 ];
					final double y = screenVertexMath.getViewPos()[ 1 ];
					if ( 0 <= x && x <= width && 0 <= y && y <= height )
						contextList.add( vertex );
				}
			}
		}

		return contextList;
	}
}
