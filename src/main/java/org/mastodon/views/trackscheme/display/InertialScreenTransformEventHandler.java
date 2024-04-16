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
package org.mastodon.views.trackscheme.display;

import java.util.Timer;
import java.util.TimerTask;

import org.mastodon.ui.keymap.KeyConfigScopes;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.views.trackscheme.LineageTreeLayout;
import org.mastodon.views.trackscheme.LineageTreeLayout.LayoutListener;
import org.mastodon.views.trackscheme.ScreenTransform;
import org.mastodon.views.trackscheme.display.OffsetHeaders.OffsetHeadersListener;
import org.mastodon.views.trackscheme.display.animate.AbstractTransformAnimator;
import org.mastodon.views.trackscheme.display.animate.InertialScreenTransformAnimator;
import org.mastodon.views.trackscheme.display.animate.InterpolateScreenTransformAnimator;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.ScrollBehaviour;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.util.AbstractNamedBehaviour;
import org.scijava.ui.behaviour.util.Behaviours;

import bdv.TransformEventHandler;

public class InertialScreenTransformEventHandler
		implements
		TransformEventHandler,
		LayoutListener,
		OffsetHeadersListener
{
	public static final String DRAG_TRANSLATE = "drag translate";

	public static final String SCROLL_TRANSLATE = "scroll translate";

	public static final String ZOOM_X = "zoom horizontal";

	public static final String ZOOM_Y = "zoom vertical";

	public static final String ZOOM_XY = "zoom";

	private static final String[] DRAG_TRANSLATE_KEYS = new String[] { "button2", "button3" };

	private static final String[] SCROLL_TRANSLATE_KEYS = new String[] { "scroll" };

	private static final String[] ZOOM_X_KEYS = new String[] { "shift scroll" };

	private static final String[] ZOOM_Y_KEYS = new String[] { "ctrl scroll", "alt scroll" };

	private static final String[] ZOOM_XY_KEYS = new String[] { "meta scroll", "ctrl shift scroll" };

	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigScopes.MASTODON, KeyConfigContexts.TRACKSCHEME );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( DRAG_TRANSLATE, DRAG_TRANSLATE_KEYS, "Pan the view by mouse-dragging." );
			descriptions.add( SCROLL_TRANSLATE, SCROLL_TRANSLATE_KEYS, "Pan the view by scrolling." );
			descriptions.add( ZOOM_X, ZOOM_X_KEYS, "Zoom horizontally by scrolling." );
			descriptions.add( ZOOM_Y, ZOOM_Y_KEYS, "Zoom vertically by scrolling." );
			descriptions.add( ZOOM_XY, ZOOM_XY_KEYS, "Zoom by scrolling." );
		}
	}

	private final TranslateDragBehaviour translateDragBehaviour;

	private final TranslateScrollBehaviour translateScrollBehaviour;

	private final ZoomScrollBehaviour zoomScrollBehaviourX;

	private final ZoomScrollBehaviour zoomScrollBehaviourY;

	private final ZoomScrollBehaviour zoomScrollBehaviourXY;

	/*
	 * Configuration options
	 */

	private final double zoomScrollSensitivity = 0.05;

	private final boolean enableInertialZoom = false;

	private final boolean enableIntertialTranslate = true;

	/**
	 * The delay in ms between inertial movements updates.
	 */
	private static final long INERTIAL_ANIMATION_PERIOD = 20;

	/**
	 * Sets the maximal zoom level in X.
	 */
	private static final double MIN_SIBLINGS_ON_CANVAS = 4;

	/**
	 * Sets the maximal zoom level in Y.
	 */
	private static final double MIN_TIMEPOINTS_ON_CANVAS = 4;

	private static final double EPSILON = 0.0000000001;

	// ...JY settings...
	//	private static final double borderRatioX = 0.5;
	//	private static final double borderRatioY = 0.5;
	//	private static final double maxSizeFactorX = 8;
	//	private static final double maxSizeFactorY = 8;
	//	private static final double boundXLayoutBorder = 0;
	//	private static final double boundYLayoutBorder = 0;

	// ...TP settings...
	private static final double borderRatioX = 0;

	private static final double borderRatioY = 0;

	private static final double borderAbsX = 10;

	private static final double borderAbsY = 10;

	private static final double maxSizeFactorX = 1;

	private static final double maxSizeFactorY = 1;

	static final double boundXLayoutBorder = 0.5;

	static final double boundYLayoutBorder = 0.5;

	// ...still something else...
	//	private static final double borderRatioX = 0.1;
	//	private static final double borderRatioY = 0.1;
	//	private static final double maxSizeFactorX = 1 / 0.8;
	//	private static final double maxSizeFactorY = 1 / 0.8;
	//	private static final double boundXLayoutBorder = 0;
	//	private static final double boundYLayoutBorder = 0;

	/**
	 * A zoom command to a window smaller than this value (in layout
	 * coordinates) when fully zoomed in, will trigger a full zoom out.
	 */
	private static final double ZOOM_LIMIT = 0.1;

	/**
	 * Current boundaries to enforce for the transform.
	 *
	 * See {@link #layoutChanged(LineageTreeLayout)} and
	 * {@link #setLayoutRangeY(double, double)} for computation of the X and Y
	 * range, respectively.
	 *
	 * See
	 * {@link ConstrainScreenTransform#constrainTransform(ScreenTransform, double, double, double, double, double, double, double, double, double, double, double, double)}
	 * for details on enforcing transform boundaries.
	 */
	private double boundXMin, boundXMax, boundYMin, boundYMax;

	/**
	 * Current maximum size to enforce for the transform.
	 */
	private double maxSizeX, maxSizeY;

	/**
	 * current canvas width.
	 */
	private int canvasWidth;

	/**
	 * current canvas height.
	 */
	private int canvasHeight;

	/**
	 * current width of vertical header.
	 */
	private int headerWidth;

	/**
	 * current height of horizontal header.
	 */
	private int headerHeight;

	/**
	 * Whether the transform should stay fully zoomed out in X when the
	 * {@link #layoutChanged(LineageTreeLayout) layout changes}.
	 */
	private boolean stayFullyZoomedOut;

	/**
	 * Timer that runs {@link #currentTimerTask}.
	 */
	private final Timer timer;

	/**
	 * The task running the current animation.
	 */
	private TimerTask currentTimerTask;

	private AbstractTransformAnimator< ScreenTransform > animator;

	private final ScreenTransformState transformState;

	public InertialScreenTransformEventHandler( final ScreenTransformState transformState )
	{
		this.transformState = transformState;

		timer = new Timer( "TrackScheme transform animation", true );
		currentTimerTask = null;

		translateDragBehaviour = new TranslateDragBehaviour();
		translateScrollBehaviour = new TranslateScrollBehaviour();
		zoomScrollBehaviourX = new ZoomScrollBehaviour( ZOOM_X, ScrollAxis.X );
		zoomScrollBehaviourY = new ZoomScrollBehaviour( ZOOM_Y, ScrollAxis.Y );
		zoomScrollBehaviourXY = new ZoomScrollBehaviour( ZOOM_XY, ScrollAxis.XY );
	}

	@Override
	public void install( final Behaviours behaviours )
	{
		behaviours.namedBehaviour( translateDragBehaviour, DRAG_TRANSLATE_KEYS );
		behaviours.namedBehaviour( translateScrollBehaviour, SCROLL_TRANSLATE_KEYS );
		behaviours.namedBehaviour( zoomScrollBehaviourX, ZOOM_X_KEYS );
		behaviours.namedBehaviour( zoomScrollBehaviourY, ZOOM_Y_KEYS );
		behaviours.namedBehaviour( zoomScrollBehaviourXY, ZOOM_XY_KEYS );
	}

	@Override
	public synchronized void setCanvasSize( final int width, final int height, final boolean updateTransform )
	{
		canvasWidth = width;
		canvasHeight = height;
		updateTransformScreenSize();
	}

	@Override
	public synchronized void updateHeaderSize( final int width, final int height )
	{
		headerWidth = width;
		headerHeight = height;
		updateTransformScreenSize();
	}

	private void updateTransformScreenSize()
	{
		final ScreenTransform transform = transformState.get();
		transform.setScreenSize(
				Math.max( canvasWidth - headerWidth, 1 ),
				Math.max( canvasHeight - headerHeight, 1 ) );
		updateMaxSizeX( transform.getScreenWidth() );
		updateMaxSizeY( transform.getScreenHeight() );
		transformState.set( transform );
	}

	@Override
	public synchronized void layoutChanged( final LineageTreeLayout layout )
	{
		final ScreenTransform transform = transformState.get();

		boundXMin = layout.getCurrentLayoutMinX() - boundXLayoutBorder;
		boundXMax = layout.getCurrentLayoutMaxX() + boundXLayoutBorder;

		if ( boundXMax - boundXMin < MIN_SIBLINGS_ON_CANVAS )
		{
			final double c = ( boundXMax + boundXMin ) / 2;
			boundXMin = c - MIN_SIBLINGS_ON_CANVAS / 2;
			boundXMax = c + MIN_SIBLINGS_ON_CANVAS / 2;
		}

		updateMaxSizeX( transform.getScreenWidth() );

		if ( stayFullyZoomedOut )
			zoomOutFullyX( transform );
		constrainTransform( transform );

		transformState.set( transform );
	}

	private void updateMaxSizeX( final int screenWidth )
	{
		final double border = screenWidth * borderRatioX + borderAbsX;
		maxSizeX = ( boundXMax - boundXMin ) * maxSizeFactorX / ( 1.0 - 2.0 * border / ( screenWidth - 1 ) );
	}

	private void updateMaxSizeY( final int screenHeight )
	{
		final double border = screenHeight * borderRatioY + borderAbsY;
		maxSizeY = ( boundYMax - boundYMin ) * maxSizeFactorY / ( 1.0 - 2.0 * border / ( screenHeight - 1 ) );
	}

	public void setLayoutRangeY( final double layoutMinY, final double layoutMaxY )
	{
		boundYMin = layoutMinY - boundYLayoutBorder;
		boundYMax = layoutMaxY + boundYLayoutBorder;

		if ( boundYMax - boundYMin < MIN_TIMEPOINTS_ON_CANVAS )
		{
			final double c = ( boundYMax + boundYMin ) / 2;
			boundYMin = c - MIN_TIMEPOINTS_ON_CANVAS / 2;
			boundYMax = c + MIN_TIMEPOINTS_ON_CANVAS / 2;
		}

		updateMaxSizeY( transformState.get().getScreenHeight() );
	}

	private void constrainTransform( final ScreenTransform transform )
	{
		ConstrainScreenTransform.constrainTransform(
				transform,
				MIN_SIBLINGS_ON_CANVAS, MIN_TIMEPOINTS_ON_CANVAS,
				maxSizeX, maxSizeY,
				boundXMin, boundXMax, boundYMin, boundYMax,
				borderRatioX, borderRatioY,
				borderAbsX, borderAbsY );
	}

	private void zoomOutFullyX( final ScreenTransform transform )
	{
		ConstrainScreenTransform.zoomOutFullyX(
				transform,
				maxSizeX,
				boundXMin, boundXMax,
				borderRatioX,
				borderAbsX );
	}

	private void zoomOutFullyY( final ScreenTransform transform )
	{
		ConstrainScreenTransform.zoomOutFullyY(
				transform,
				maxSizeY,
				boundYMin, boundYMax,
				borderRatioY,
				borderAbsY );
	}

	private boolean hasMinSizeX( final ScreenTransform transform )
	{
		return ConstrainScreenTransform.hasMinSizeX( transform, MIN_SIBLINGS_ON_CANVAS + EPSILON );
	}

	private boolean hasMinSizeY( final ScreenTransform transform )
	{
		return ConstrainScreenTransform.hasMinSizeY( transform, MIN_TIMEPOINTS_ON_CANVAS + EPSILON );
	}

	private boolean hasMaxSizeX( final ScreenTransform transform )
	{
		return ConstrainScreenTransform.hasMaxSizeX( transform, maxSizeX );
	}

	private boolean hasMaxSizeY( final ScreenTransform transform )
	{
		return ConstrainScreenTransform.hasMaxSizeY( transform, maxSizeY );
	}

	private class TranslateDragBehaviour extends AbstractNamedBehaviour implements DragBehaviour
	{
		/**
		 * Coordinates where mouse dragging started.
		 */
		private int oX, oY;

		final private ScreenTransform previousTransform = new ScreenTransform();

		private long previousTime = 0;

		private long dt = 0;

		public TranslateDragBehaviour()
		{
			super( DRAG_TRANSLATE );
		}

		@Override
		public void init( final int x, final int y )
		{
			final long t = System.currentTimeMillis();
			oX = x;
			oY = y;
			transformState.get( previousTransform );
			previousTime = t;
			dt = 0;
		}

		@Override
		public void drag( final int x, final int y )
		{
			final ScreenTransform transform = transformState.get();

			// TODO: Revise?
			//  This seems a quite noisy way to estimate current motion speed.
			//  Instead, it would be better to average over dXY/dt over last few updates?
			final long t = System.currentTimeMillis();
			if ( t > previousTime )
			{
				previousTransform.set( transform );
				dt = t - previousTime;
				previousTime = t;
			}

			final int dX = oX - x;
			final int dY = oY - y;
			oX = x;
			oY = y;
			transform.shift( dX, dY );
			constrainTransform( transform );

			transformState.set( transform );
		}

		@Override
		public void end( final int x, final int y )
		{
			if ( enableIntertialTranslate && dt > 0 )
			{
				animator = new InertialScreenTransformAnimator( previousTransform, transformState.get(), dt, 400 );
				runAnimation();
			}
		}
	}

	private enum ScrollAxis
	{
		X, Y, XY
	}

	private class ZoomScrollBehaviour extends AbstractNamedBehaviour implements ScrollBehaviour
	{
		private final ScrollAxis axis;

		public ZoomScrollBehaviour( final String name, final ScrollAxis axis )
		{
			super( name );
			this.axis = axis;
		}

		@Override
		public void scroll( final double wheelRotation, final boolean isHorizontal, final int wx, final int wy )
		{
			if ( isHorizontal )
				return;

			final int x = wx - headerWidth;
			final int y = wy - headerHeight;

			final boolean zoomIn = wheelRotation < 0;
			double dScale = 1.0 + Math.abs( wheelRotation ) * zoomScrollSensitivity;
			if ( zoomIn )
				dScale = 1.0 / dScale;

			final ScreenTransform transform = transformState.get();
			final ScreenTransform previousTransform = transform.copy();

			if ( axis == ScrollAxis.X || axis == ScrollAxis.XY )
			{
				if ( zoomIn )
				{
					if ( !hasMinSizeX( transform ) )
						transform.zoomX( dScale, x );
				}
				else
				{
					if ( !hasMaxSizeX( transform ) )
						transform.zoomX( dScale, x );
				}
			}

			if ( axis == ScrollAxis.Y || axis == ScrollAxis.XY )
			{
				if ( zoomIn )
				{
					if ( !hasMinSizeY( transform ) )
						transform.zoomY( dScale, y );
				}
				else
				{
					if ( !hasMaxSizeY( transform ) )
						transform.zoomY( dScale, y );
				}
			}

			stayFullyZoomedOut = hasMaxSizeX( transform );

			constrainTransform( transform );
			ConstrainScreenTransform.removeJitter( transform, previousTransform );
			if ( !transform.equals( previousTransform ) )
			{
				if ( enableInertialZoom )
					animator = new InertialScreenTransformAnimator( previousTransform, transform, 50, 400 );
				else
					transformState.set( transform );
			}

			if ( enableInertialZoom )
				runAnimation();
		}
	}

	private class TranslateScrollBehaviour extends AbstractNamedBehaviour implements ScrollBehaviour
	{
		public TranslateScrollBehaviour()
		{
			super( SCROLL_TRANSLATE );
		}

		@Override
		public void scroll( final double wheelRotation, final boolean isHorizontal, final int x, final int y )
		{
			final ScreenTransform transform = transformState.get();
			final ScreenTransform previousTransform = transform.copy();
			final double d = wheelRotation * 15;
			if ( isHorizontal )
				transform.shiftX( d );
			else
				transform.shiftY( d );
			constrainTransform( transform );
			ConstrainScreenTransform.removeJitter( transform, previousTransform );
			transformState.set( transform );
		}
	}

	private final ScreenTransform tstart = new ScreenTransform();

	private final ScreenTransform tend = new ScreenTransform();

	public void centerOn( final double lx, final double ly )
	{
		transformState.get( tstart );

		final double minX = tstart.getMinX();
		final double maxX = tstart.getMaxX();
		final double cx = ( maxX + minX ) / 2;
		final double dx = lx - cx;

		final double minY = tstart.getMinY();
		final double maxY = tstart.getMaxY();
		final double cy = ( maxY + minY ) / 2;
		final double dy = ly - cy;

		tend.set( tstart );
		tend.shiftLayoutX( dx );
		tend.shiftLayoutY( dy );
		constrainTransform( tend );

		ConstrainScreenTransform.removeJitter( tend, tstart );
		if ( !tend.equals( tstart ) )
		{
			animator = new InterpolateScreenTransformAnimator( tstart, tend, 200 );
			runAnimation();
		}
	}

	/**
	 * Zooms the current view to the specified layout coordinates.
	 *
	 * @param lx1
	 *            left coordinate of the coordinates to zoom to.
	 * @param lx2
	 *            right coordinate of the coordinates to zoom to.
	 * @param ly1
	 *            top coordinate of the coordinates to zoom to.
	 * @param ly2
	 *            bottom coordinate of the coordinates to zoom to.
	 */
	public void zoomTo( final double lx1, final double lx2, final double ly1, final double ly2 )
	{
		transformState.get( tstart );
		final double xmin = Math.min( lx1, lx2 );
		final double xmax = Math.max( lx1, lx2 );
		final double ymin = Math.min( ly1, ly2 );
		final double ymax = Math.max( ly1, ly2 );
		if ( hasMinSizeX( tstart ) && hasMinSizeY( tstart )
				&& Math.max( xmax - xmin, ymax - ymin ) < ZOOM_LIMIT )
		{
			// Unzoom fully.
			tend.set( tstart );
			zoomOutFullyX( tend );
			zoomOutFullyY( tend );
		}
		else
		{
			tend.set( xmin, xmax, ymin, ymax, tstart.getScreenWidth(), tstart.getScreenHeight() );
		}

		constrainTransform( tend );
		ConstrainScreenTransform.removeJitter( tend, tstart );
		if ( !tend.equals( tstart ) )
		{
			stayFullyZoomedOut = false;
			animator = new InterpolateScreenTransformAnimator( tstart, tend, 200 );
			runAnimation();
		}
	}

	public void zoomOutFully()
	{
		transformState.get( tstart );
		tend.set( tstart );
		zoomOutFullyX( tend );
		zoomOutFullyY( tend );
		constrainTransform( tend );
		ConstrainScreenTransform.removeJitter( tend, tstart );
		if ( !tend.equals( tstart ) )
		{
			stayFullyZoomedOut = false;
			animator = new InterpolateScreenTransformAnimator( tstart, tend, 200 );
			runAnimation();
		}
	}

	private void animate()
	{
		final long t = System.currentTimeMillis();
		final ScreenTransform c = animator.getCurrent( t );
		if ( stayFullyZoomedOut )
			zoomOutFullyX( c );
		constrainTransform( c );
		transformState.set( c );
	}

	private synchronized void runAnimation()
	{
		if ( currentTimerTask != null )
			currentTimerTask.cancel();
		timer.purge();
		currentTimerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				if ( null == animator || animator.isComplete() )
				{
					cancel();
					stayFullyZoomedOut = hasMaxSizeX( transformState.get() );
					currentTimerTask = null;
				}
				else
				{
					animate();
				}
			}
		};
		timer.schedule( currentTimerTask, 0, INERTIAL_ANIMATION_PERIOD );
	}
}
