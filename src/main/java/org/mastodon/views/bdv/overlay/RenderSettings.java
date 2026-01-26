/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.views.bdv.overlay;

import java.awt.Color;
import java.util.Objects;

import org.scijava.listeners.Listeners;

import bdv.ui.settings.style.Style;

/**
 * Abstract class for render settings for Mastodon overlays in BigDataViewer.
 * <p>
 * This abstract class provides common fields and methods for render settings
 * used in Mastodon overlays in BigDataViewer. It does not know of any vertex
 * shape, and defines only the fields that are common to all render settings
 * (edges drawing, colors, time range, etc.).
 * <p>
 * Concrete applications developed against a specific vertex shape should
 * subclass this class to add shape-specific rendering settings (like spot size,
 * shape, etc.).
 *
 * @author Tobias Pietzsch
 * @author Jean-Yves Tinevez
 *
 * @param <S>
 *            the concrete subclass of {@link RenderSettings}.
 */
public abstract class RenderSettings< S extends RenderSettings< S > > implements Style< S >
{

	public static final int DEFAULT_LIMIT_TIME_RANGE = 20;

	public static final boolean DEFAULT_USE_ANTI_ALIASING = true;

	public static final boolean DEFAULT_USE_GRADIENT = false;

	public static final boolean DEFAULT_DRAW_SPOTS = true;

	public static final boolean DEFAULT_DRAW_LINKS = true;

	public static final boolean DEFAULT_DRAW_LINKS_AHEAD_IN_TIME = false;

	public static final boolean DEFAULT_DRAW_ARROW_HEADS = false;

	public static final boolean DEFAULT_DRAW_SPOT_LABELS = false;

	public static final boolean DEFAULT_FILL_SPOTS = false;

	public static final double DEFAULT_SPOT_STROKE_WIDTH = 1.0;

	public static final double DEFAULT_LINK_STROKE_WIDTH = 1.0;

	public static final int DEFAULT_COLOR_SPOT_AND_PRESENT = Color.GREEN.getRGB();

	public static final int DEFAULT_COLOR_PAST = Color.RED.getRGB();

	public static final int DEFAULT_COLOR_FUTURE = Color.BLUE.getRGB();

	public interface UpdateListener
	{
		public void renderSettingsChanged();
	}

	private final Listeners.List< UpdateListener > updateListeners;

	protected RenderSettings()
	{
		updateListeners = new Listeners.SynchronizedList<>();
	}

	/**
	 * Returns a new render settings, copied from this instance.
	 *
	 * @param name
	 *            the name for the copied render settings.
	 * @return a new render settings instance.
	 */
	@Override
	public S copy( final String name )
	{
		final S rs = createInstance();
		@SuppressWarnings( "unchecked" )
		final S me = ( S ) this;
		rs.set( me );
		if ( name != null )
			rs.setName( name );
		return rs;
	}

	/**
	 * Creates a new instance of the concrete subclass.
	 *
	 * @return a new instance of the concrete subclass.
	 */
	protected abstract S createInstance();

	@Override
	public S copy()
	{
		return copy( null );
	}

	/**
	 * Sets this render settings to be identical to the given settings.
	 *
	 * @param settings
	 *            the settings to copy from.
	 */
	protected synchronized void set( final S settings )
	{
		final RenderSettings< S > rs = settings;
		name = rs.name;
		useAntialiasing = rs.useAntialiasing;
		useGradient = rs.useGradient;
		timeLimit = rs.timeLimit;
		drawLinks = rs.drawLinks;
		drawLinksAheadInTime = rs.drawLinksAheadInTime;
		drawArrowHeads = rs.drawArrowHeads;
		drawSpots = rs.drawSpots;
		drawSpotLabels = rs.drawSpotLabels;
		fillSpots = rs.fillSpots;
		spotStrokeWidth = rs.spotStrokeWidth;
		linkStrokeWidth = rs.linkStrokeWidth;
		colorSpot = rs.colorSpot;
		colorPast = rs.colorPast;
		colorFuture = rs.colorFuture;
	}

	protected void notifyListeners()
	{
		for ( final UpdateListener l : updateListeners.list )
			l.renderSettingsChanged();
	}

	public Listeners< UpdateListener > updateListeners()
	{
		return updateListeners;
	}

	/*
	 * DISPLAY SETTINGS FIELDS.
	 */

	/** The name of this render settings object. */
	private String name;

	/** Whether to use antialiasing (for drawing everything). */
	private boolean useAntialiasing = DEFAULT_USE_ANTI_ALIASING;

	/**
	 * If {@code true}, draw links using a gradient from source color to target
	 * color. If {@code false}, draw links using the target color.
	 */
	private boolean useGradient = DEFAULT_USE_GRADIENT;

	/**
	 * Maximum number of timepoints into the past for which outgoing edges
	 * should be drawn.
	 */
	private int timeLimit = DEFAULT_LIMIT_TIME_RANGE;

	/** Whether to draw links (at all). */
	private boolean drawLinks = DEFAULT_DRAW_LINKS;

	/**
	 * Whether to draw links ahead in time. They are otherwise drawn only
	 * backward in time.
	 */
	private boolean drawLinksAheadInTime = DEFAULT_DRAW_LINKS_AHEAD_IN_TIME;

	/** Whether to draw links with an arrow head, in time direction. */
	private boolean drawArrowHeads = DEFAULT_DRAW_ARROW_HEADS;

	/** Whether to draw spots (at all). */
	private boolean drawSpots = DEFAULT_DRAW_SPOTS;

	/** Whether to draw spot labels next to ellipses. */
	private boolean drawSpotLabels = DEFAULT_DRAW_SPOT_LABELS;

	/** Whether to fill spots. */
	private boolean fillSpots = DEFAULT_FILL_SPOTS;

	/** The stroke with of spots. */
	private double spotStrokeWidth = DEFAULT_SPOT_STROKE_WIDTH;

	/** The stroke with of links. */
	private double linkStrokeWidth = DEFAULT_LINK_STROKE_WIDTH;

	/** The color used to paint spots and links in the current time-point. */
	private int colorSpot = DEFAULT_COLOR_SPOT_AND_PRESENT;

	/** The color used to paint links in the past time-points. */
	private int colorPast = DEFAULT_COLOR_PAST;

	/** The color used to paint links in the future time-points. */
	private int colorFuture = DEFAULT_COLOR_FUTURE;

	/**
	 * Returns the name of this {@link RenderSettings}.
	 *
	 * @return the name.
	 */
	@Override
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the name of this {@link RenderSettings}.
	 *
	 * @param name
	 *            the name to set.
	 */
	@Override
	public synchronized void setName( final String name )
	{
		if ( !Objects.equals( this.name, name ) )
		{
			this.name = name;
			notifyListeners();
		}
	}

	/**
	 * Get the antialiasing setting.
	 *
	 * @return {@code true} if antialiasing is used.
	 */
	public boolean getUseAntialiasing()
	{
		return useAntialiasing;
	}

	/**
	 * Sets whether to use anti-aliasing for drawing.
	 *
	 * @param useAntialiasing
	 *            whether to use use anti-aliasing.
	 */
	public synchronized void setUseAntialiasing( final boolean useAntialiasing )
	{
		if ( this.useAntialiasing != useAntialiasing )
		{
			this.useAntialiasing = useAntialiasing;
			notifyListeners();
		}
	}

	/**
	 * Returns whether a gradient is used for drawing links.
	 *
	 * @return {@code true} if links are drawn using a gradient from source
	 *         color to target color, or {@code false}, if links are drawn using
	 *         the target color.
	 */
	public boolean getUseGradient()
	{
		return useGradient;
	}

	/**
	 * Sets whether to use a gradient for drawing links. If
	 * {@code useGradient=true}, draw links using a gradient from source color
	 * to target color. If {@code useGradient=false}, draw links using the
	 * target color.
	 *
	 * @param useGradient
	 *            whether to use a gradient for drawing links.
	 */
	public synchronized void setUseGradient( final boolean useGradient )
	{
		if ( this.useGradient != useGradient )
		{
			this.useGradient = useGradient;
			notifyListeners();
		}
	}

	/**
	 * Gets the maximum number of time-points into the past for which outgoing
	 * edges should be drawn.
	 *
	 * @return maximum number of time-points into the past to draw links.
	 */
	public int getTimeLimit()
	{
		return timeLimit;
	}

	/**
	 * Sets the maximum number of time-points into the past for which outgoing
	 * edges should be drawn.
	 *
	 * @param timeLimit
	 *            maximum number of time-points into the past to draw links.
	 */
	public synchronized void setTimeLimit( final int timeLimit )
	{
		if ( this.timeLimit != timeLimit )
		{
			this.timeLimit = timeLimit;
			notifyListeners();
		}
	}

	/**
	 * Gets whether to draw links (at all). For specific settings, see
	 * {@link #getTimeLimit()}, {@link #getUseGradient()}.
	 *
	 * @return {@code true} if links are drawn.
	 */
	public boolean getDrawLinks()
	{
		return drawLinks;
	}

	/**
	 * Gets whether to draw links ahead in time. They are otherwise drawn only
	 * backward in time.
	 *
	 * @return {@code true} if links are drawn ahead in time.
	 */
	public boolean getDrawLinksAheadInTime()
	{
		return drawLinksAheadInTime;
	}

	/**
	 * Gets whether to draw links with arrow heads.
	 *
	 * @return {@code true} if links are drawn with arrow heads.
	 */
	public boolean getDrawArrowHeads()
	{
		return drawArrowHeads;
	}

	/**
	 * Sets whether to draw links (at all). For specific settings, see
	 * {@link #setTimeLimit(int)}, {@link #setUseGradient(boolean)}.
	 *
	 * @param drawLinks
	 *            whether to draw links.
	 */
	public synchronized void setDrawLinks( final boolean drawLinks )
	{
		if ( this.drawLinks != drawLinks )
		{
			this.drawLinks = drawLinks;
			notifyListeners();
		}
	}

	/**
	 * Sets whether to draw links ahead in time. They are otherwise drawn only
	 * backward in time.
	 *
	 * @param drawLinksAheadInTime
	 *            whether to draw links ahead in time.
	 */
	public synchronized void setDrawLinksAheadInTime( final boolean drawLinksAheadInTime )
	{
		if ( this.drawLinksAheadInTime != drawLinksAheadInTime )
		{
			this.drawLinksAheadInTime = drawLinksAheadInTime;
			notifyListeners();
		}
	}

	/**
	 * Sets whether to draw links with arrow heads.
	 *
	 * @param drawArrowHeads
	 *            whether to draw links with arrow heads.
	 */
	public synchronized void setDrawArrowHeads( final boolean drawArrowHeads )
	{
		if ( this.drawArrowHeads != drawArrowHeads )
		{
			this.drawArrowHeads = drawArrowHeads;
			notifyListeners();
		}
	}

	/**
	 * Gets whether to draw spots (at all). For specific settings, see other
	 * spot drawing settings.
	 *
	 * @return {@code true} if spots are to be drawn.
	 * @see #getDrawSpotCenters()
	 * @see #getDrawSpotLabels()
	 */
	public boolean getDrawSpots()
	{
		return drawSpots;
	}

	/**
	 * Sets whether to draw spots (at all). For specific settings, see other
	 * spot drawing settings.
	 *
	 * @param drawSpots
	 *            whether to draw spots.
	 * @see #setDrawSpotCenters(boolean)
	 * @see #setDrawSpotLabels(boolean)
	 */
	public synchronized void setDrawSpots( final boolean drawSpots )
	{
		if ( this.drawSpots != drawSpots )
		{
			this.drawSpots = drawSpots;
			notifyListeners();
		}
	}

	/**
	 * Get whether spot labels are drawn next to ellipses.
	 *
	 * @return whether spot labels are drawn next to ellipses.
	 */
	public boolean getDrawSpotLabels()
	{
		return drawSpotLabels;
	}

	/**
	 * Set whether spot labels are drawn next to ellipses.
	 *
	 * @param drawSpotLabels
	 *            whether spot labels are drawn next to ellipses.
	 */
	public void setDrawSpotLabels( final boolean drawSpotLabels )
	{
		if ( this.drawSpotLabels != drawSpotLabels )
		{
			this.drawSpotLabels = drawSpotLabels;
			notifyListeners();
		}
	}

	/**
	 * Get whether spots are filled.
	 *
	 * @return whether spots are filled.
	 */
	public boolean getFillSpots()
	{
		return fillSpots;
	}

	/**
	 * Set whether spots are filled.
	 *
	 * @param fillSpots
	 *            whether spots are filled.
	 */
	public void setFillSpots( final boolean fillSpots )
	{
		if ( this.fillSpots != fillSpots )
		{
			this.fillSpots = fillSpots;
			notifyListeners();
		}
	}

	/**
	 * Get stroke width for spots.
	 *
	 * @return stroke width for spots.
	 */
	public double getSpotStrokeWidth()
	{
		return spotStrokeWidth;
	}

	/**
	 * Set stroke width for spots.
	 *
	 * @param spotStrokeWidth
	 *            stroke width for spots.
	 */
	public void setSpotStrokeWidth( final double spotStrokeWidth )
	{
		if ( this.spotStrokeWidth != spotStrokeWidth )
		{
			this.spotStrokeWidth = spotStrokeWidth;
			notifyListeners();
		}
	}

	/**
	 * Get stroke width for links.
	 *
	 * @return stroke width for links.
	 */
	public double getLinkStrokeWidth()
	{
		return linkStrokeWidth;
	}

	/**
	 * Set stroke width for links.
	 *
	 * @param linkStrokeWidth
	 *            stroke width for links.
	 */
	public void setLinkStrokeWidth( final double linkStrokeWidth )
	{
		if ( this.linkStrokeWidth != linkStrokeWidth )
		{
			this.linkStrokeWidth = linkStrokeWidth;
			notifyListeners();
		}
	}

	/**
	 * Returns the color used to paint spots and links in the current
	 * time-point.
	 *
	 * @return the color used to paint spots and links in the current
	 *         time-point.
	 */
	public int getColorSpot()
	{
		return colorSpot;
	}

	/**
	 * Sets the color used to paint spots and links in the current time-point.
	 *
	 * @param colorSpot
	 *            the color used to paint spots and links in the current
	 *            time-point.
	 */
	public synchronized void setColorSpot( final int colorSpot )
	{
		if ( this.colorSpot != colorSpot )
		{
			this.colorSpot = colorSpot;
			notifyListeners();
		}
	}

	/**
	 * Returns the color used to paint links in the past time-points.
	 *
	 * @return the color used to paint links in the past time-points.
	 */
	public int getColorPast()
	{
		return colorPast;
	}

	/**
	 * Sets the color used to paint links in the past time-points.
	 *
	 * @param colorPast
	 *            the color used to paint links in the past time-points.
	 */
	public synchronized void setColorPast( final int colorPast )
	{
		if ( this.colorPast != colorPast )
		{
			this.colorPast = colorPast;
			notifyListeners();
		}
	}

	/**
	 * Returns the color used to paint links in the future time-points.
	 *
	 * @return the color used to paint links in the future time-points.
	 */
	public int getColorFuture()
	{
		return colorFuture;
	}

	/**
	 * Sets the color used to paint links in the future time-points.
	 *
	 * @param colorFuture
	 *            the color used to paint links in the future time-points.
	 */
	public synchronized void setColorFuture( final int colorFuture )
	{
		if ( this.colorFuture != colorFuture )
		{
			this.colorFuture = colorFuture;
			notifyListeners();
		}
	}
}
