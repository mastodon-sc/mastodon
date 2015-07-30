package net.trackmate.trackscheme;

import static net.trackmate.trackscheme.ScreenVertex.Transition.APPEAR;
import static net.trackmate.trackscheme.ScreenVertex.Transition.DISAPPEAR;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

import net.trackmate.trackscheme.ScreenVertex.Transition;
import net.trackmate.trackscheme.laf.TrackSchemeStyle;

public class TrackSchemeGhostLAF extends DefaultTrackSchemeLAF
{

	private final Stroke ghostStroke = new BasicStroke( 1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] { 3.0f }, 0.0f );


	/*
	 * CONSTRUCTORS
	 */

	public TrackSchemeGhostLAF( final VertexOrder order, final LineageTreeLayout layout, final TrackSchemeStyle style )
	{
		super( order, layout, style );
	}

	/*
	 * METHODS
	 */


	@Override
	public void beforeDrawVertex( final Graphics2D g2 )
	{}

	@Override
	public void beforeDrawVertexRange( final Graphics2D g2 )
	{
		g2.setColor( style.vertexRangeColor );
	}

	@Override
	protected void drawVertexFull( final Graphics2D g2, final ScreenVertex vertex )
	{
		final Transition transition = vertex.getTransition();
		final boolean disappear = ( transition == DISAPPEAR );
		final double ratio = vertex.getInterpolationCompletionRatio();
		final boolean selected = vertex.isSelected();
		final boolean ghost = vertex.isGhost();

		double spotdiameter = Math.min( vertex.getVertexDist() - 10.0, maxDisplayVertexSize );
		if ( disappear )
			spotdiameter *= ( 1 + ratio );
		final double spotradius = spotdiameter / 2;

		final Color fillColor = getColor( selected, transition, ratio, style.vertexFillColor, style.selectedVertexFillColor );
		final Color drawColor = getColor( selected, transition, ratio, style.vertexDrawColor, style.selectedVertexDrawColor );

		final double x = vertex.getX();
		final double y = vertex.getY();
		final int ox = ( int ) x - ( int ) spotradius;
		final int oy = ( int ) y - ( int ) spotradius;
		final int sd = 2 * ( int ) spotradius;

		if ( ghost )
		{
			g2.setStroke( ghostStroke );
			final Color ghostFillColor = new Color(
					style.backgroundColor.getRed(),
					style.backgroundColor.getGreen(),
					style.backgroundColor.getBlue(),
					fillColor.getAlpha() );
			g2.setColor( ghostFillColor );
			g2.fillOval( ox, oy, sd, sd );
			g2.setColor( drawColor );
			g2.drawOval( ox, oy, sd, sd );
		}
		else
		{
			g2.setStroke( style.vertexStroke );
			g2.setColor( fillColor );
			g2.fillOval( ox, oy, sd, sd );
			g2.setColor( drawColor );
			g2.drawOval( ox, oy, sd, sd );
		}

		final int maxLabelLength = ( int ) ( spotdiameter / avgLabelLetterWidth );
		if ( maxLabelLength > 2 && !disappear )
		{
			String label = vertex.getLabel();
			if ( label.length() > maxLabelLength )
				label = label.substring( 0, maxLabelLength - 2 ) + "...";

			final FontRenderContext frc = g2.getFontRenderContext();
			final TextLayout layout = new TextLayout( label, style.font, frc );
			final Rectangle2D bounds = layout.getBounds();
			final float tx = ( float ) ( x - bounds.getCenterX() );
			final float ty = ( float ) ( y - bounds.getCenterY() );
			layout.draw( g2, tx, ty );
		}
	}

	@Override
	public void beforeDrawEdge( final Graphics2D g2 )
	{}

	@Override
	public void drawEdge( final Graphics2D g2, final ScreenEdge edge, final ScreenVertex vs, final ScreenVertex vt )
	{
		Transition transition = vs.getTransition();
		double ratio = vs.getInterpolationCompletionRatio();
		if ( vt.getTransition() == APPEAR )
		{
			transition = APPEAR;
			ratio = vt.getInterpolationCompletionRatio();
		}
		final boolean selected = edge.isSelected();
		final boolean ghost = vs.isGhost() || vt.isGhost();
		final Color drawColor = getColor( selected, transition, ratio, style.edgeColor, style.selectedEdgeColor );
		g2.setColor( drawColor );
		g2.setStroke( ghost ? ghostStroke : style.edgeStroke );
		g2.drawLine( ( int ) vs.getX(), ( int ) vs.getY(), ( int ) vt.getX(), ( int ) vt.getY() );
	}
}
