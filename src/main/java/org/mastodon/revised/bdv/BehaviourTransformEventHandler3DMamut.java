/*-
 * #%L
 * BigDataViewer core classes with minimal dependencies
 * %%
 * Copyright (C) 2012 - 2016 Tobias Pietzsch, Stephan Saalfeld, Stephan Preibisch,
 * Jean-Yves Tinevez, HongKee Moon, Johannes Schindelin, Curtis Rueden, John Bogovic
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
package org.mastodon.revised.bdv;

import org.mastodon.revised.mamut.KeyConfigContexts;
import org.mastodon.revised.ui.keymap.CommandDescriptionProvider;
import org.mastodon.revised.ui.keymap.CommandDescriptions;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.Behaviour;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.ScrollBehaviour;
import org.scijava.ui.behaviour.util.AbstractNamedBehaviour;
import org.scijava.ui.behaviour.util.Behaviours;

import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.TransformEventHandler;
import net.imglib2.ui.TransformListener;

/**
 * A {@link TransformEventHandler} that changes an {@link AffineTransform3D}
 * through a set of {@link Behaviour}s.
 *
 * @author Stephan Saalfeld
 * @author Tobias Pietzsch
 */
public class BehaviourTransformEventHandler3DMamut implements BehaviourTransformEventHandlerMamut
{
	public static final String DRAG_TRANSLATE = "drag translate";
	public static final String ZOOM_NORMAL = "scroll zoom";
	public static final String SELECT_AXIS_X = "axis x";
	public static final String SELECT_AXIS_Y = "axis y";
	public static final String SELECT_AXIS_Z = "axis z";

	public static final String DRAG_ROTATE = "drag rotate";
	public static final String SCROLL_Z = "scroll browse z";
	public static final String ROTATE_LEFT = "rotate left";
	public static final String ROTATE_RIGHT = "rotate right";
	public static final String KEY_ZOOM_IN = "zoom in";
	public static final String KEY_ZOOM_OUT = "zoom out";
	public static final String KEY_FORWARD_Z = "forward z";
	public static final String KEY_BACKWARD_Z = "backward z";

	public static final String DRAG_ROTATE_FAST = "drag rotate fast";
	public static final String SCROLL_Z_FAST = "scroll browse z fast";
	public static final String ROTATE_LEFT_FAST = "rotate left fast";
	public static final String ROTATE_RIGHT_FAST = "rotate right fast";
	public static final String KEY_ZOOM_IN_FAST = "zoom in fast";
	public static final String KEY_ZOOM_OUT_FAST = "zoom out fast";
	public static final String KEY_FORWARD_Z_FAST = "forward z fast";
	public static final String KEY_BACKWARD_Z_FAST = "backward z fast";

	public static final String DRAG_ROTATE_SLOW = "drag rotate slow";
	public static final String SCROLL_Z_SLOW = "scroll browse z slow";
	public static final String ROTATE_LEFT_SLOW = "rotate left slow";
	public static final String ROTATE_RIGHT_SLOW = "rotate right slow";
	public static final String KEY_ZOOM_IN_SLOW = "zoom in slow";
	public static final String KEY_ZOOM_OUT_SLOW = "zoom out slow";
	public static final String KEY_FORWARD_Z_SLOW = "forward z slow";
	public static final String KEY_BACKWARD_Z_SLOW = "backward z slow";

	private static final String[] DRAG_TRANSLATE_KEYS = new String[] { "button2", "button3" };
	private static final String[] ZOOM_NORMAL_KEYS = new String[] { "meta scroll", "ctrl shift scroll" };
	private static final String[] SELECT_AXIS_X_KEYS = new String[] { "X" };
	private static final String[] SELECT_AXIS_Y_KEYS = new String[] { "Y" };
	private static final String[] SELECT_AXIS_Z_KEYS = new String[] { "Z" };

	private static final String[] DRAG_ROTATE_KEYS = new String[] { "button1" };
	private static final String[] SCROLL_Z_KEYS = new String[] { "scroll" };
	private static final String[] ROTATE_LEFT_KEYS = new String[] { "LEFT" };
	private static final String[] ROTATE_RIGHT_KEYS = new String[] { "RIGHT" };
	private static final String[] KEY_ZOOM_IN_KEYS = new String[] { "UP" };
	private static final String[] KEY_ZOOM_OUT_KEYS = new String[] { "DOWN" };
	private static final String[] KEY_FORWARD_Z_KEYS = new String[] { "COMMA" };
	private static final String[] KEY_BACKWARD_Z_KEYS = new String[] { "PERIOD" };

	private static final String[] DRAG_ROTATE_FAST_KEYS = new String[] { "shift button1" };
	private static final String[] SCROLL_Z_FAST_KEYS = new String[] { "shift scroll" };
	private static final String[] ROTATE_LEFT_FAST_KEYS = new String[] { "shift LEFT" };
	private static final String[] ROTATE_RIGHT_FAST_KEYS = new String[] { "shift RIGHT" };
	private static final String[] KEY_ZOOM_IN_FAST_KEYS = new String[] { "shift UP" };
	private static final String[] KEY_ZOOM_OUT_FAST_KEYS = new String[] { "shift DOWN" };
	private static final String[] KEY_FORWARD_Z_FAST_KEYS = new String[] { "shift COMMA" };
	private static final String[] KEY_BACKWARD_Z_FAST_KEYS = new String[] { "shift PERIOD" };

	private static final String[] DRAG_ROTATE_SLOW_KEYS = new String[] { "ctrl button1" };
	private static final String[] SCROLL_Z_SLOW_KEYS = new String[] { "ctrl scroll" };
	private static final String[] ROTATE_LEFT_SLOW_KEYS = new String[] { "ctrl LEFT" };
	private static final String[] ROTATE_RIGHT_SLOW_KEYS = new String[] { "ctrl RIGHT" };
	private static final String[] KEY_ZOOM_IN_SLOW_KEYS = new String[] { "ctrl UP" };
	private static final String[] KEY_ZOOM_OUT_SLOW_KEYS = new String[] { "ctrl DOWN" };
	private static final String[] KEY_FORWARD_Z_SLOW_KEYS = new String[] { "ctrl COMMA" };
	private static final String[] KEY_BACKWARD_Z_SLOW_KEYS = new String[] { "ctrl PERIOD" };

	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = Descriptions.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigContexts.BIGDATAVIEWER );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( DRAG_TRANSLATE, DRAG_TRANSLATE_KEYS, "Pan the view by mouse-dragging." );
			descriptions.add( ZOOM_NORMAL, ZOOM_NORMAL_KEYS, "Zoom in by scrolling." );
			descriptions.add( SELECT_AXIS_X, SELECT_AXIS_X_KEYS, "Select X as the rotation axis for keyboard rotation." );
			descriptions.add( SELECT_AXIS_Y, SELECT_AXIS_Y_KEYS, "Select Y as the rotation axis for keyboard rotation." );
			descriptions.add( SELECT_AXIS_Z, SELECT_AXIS_Z_KEYS, "Select Z as the rotation axis for keyboard rotation." );

			descriptions.add( DRAG_ROTATE, DRAG_ROTATE_KEYS, "Rotate the view by mouse-dragging." );
			descriptions.add( SCROLL_Z, SCROLL_Z_KEYS, "Translate in Z by scrolling." );
			descriptions.add( ROTATE_LEFT, ROTATE_LEFT_KEYS, "Rotate left (counter-clockwise) by 1 degree." );
			descriptions.add( ROTATE_RIGHT, ROTATE_RIGHT_KEYS, "Rotate right (clockwise) by 1 degree." );
			descriptions.add( KEY_ZOOM_IN, KEY_ZOOM_IN_KEYS, "Zoom in." );
			descriptions.add( KEY_ZOOM_OUT, KEY_ZOOM_OUT_KEYS, "Zoom out." );
			descriptions.add( KEY_FORWARD_Z, KEY_FORWARD_Z_KEYS, "Translate forward in Z." );
			descriptions.add( KEY_BACKWARD_Z, KEY_BACKWARD_Z_KEYS, "Translate backward in Z." );

			descriptions.add( DRAG_ROTATE_FAST, DRAG_ROTATE_FAST_KEYS, "Rotate the view by mouse-dragging (fast)." );
			descriptions.add( SCROLL_Z_FAST, SCROLL_Z_FAST_KEYS, "Translate in Z by scrolling (fast)." );
			descriptions.add( ROTATE_LEFT_FAST, ROTATE_LEFT_FAST_KEYS, "Rotate left (counter-clockwise) by 10 degrees." );
			descriptions.add( ROTATE_RIGHT_FAST, ROTATE_RIGHT_FAST_KEYS, "Rotate right (clockwise) by 10 degrees." );
			descriptions.add( KEY_ZOOM_IN_FAST, KEY_ZOOM_IN_FAST_KEYS, "Zoom in (fast)." );
			descriptions.add( KEY_ZOOM_OUT_FAST, KEY_ZOOM_OUT_FAST_KEYS, "Zoom out (fast)." );
			descriptions.add( KEY_FORWARD_Z_FAST, KEY_FORWARD_Z_FAST_KEYS, "Translate forward in Z (fast)." );
			descriptions.add( KEY_BACKWARD_Z_FAST, KEY_BACKWARD_Z_FAST_KEYS, "Translate backward in Z (fast)." );

			descriptions.add( DRAG_ROTATE_SLOW, DRAG_ROTATE_SLOW_KEYS, "Rotate the view by mouse-dragging (slow)." );
			descriptions.add( SCROLL_Z_SLOW, SCROLL_Z_SLOW_KEYS, "Translate in Z by scrolling (slow)." );
			descriptions.add( ROTATE_LEFT_SLOW, ROTATE_LEFT_SLOW_KEYS, "Rotate left (counter-clockwise) by 0.1 degree." );
			descriptions.add( ROTATE_RIGHT_SLOW, ROTATE_RIGHT_SLOW_KEYS, "Rotate right (clockwise) by 0.1 degree." );
			descriptions.add( KEY_ZOOM_IN_SLOW, KEY_ZOOM_IN_SLOW_KEYS, "Zoom in (slow)." );
			descriptions.add( KEY_ZOOM_OUT_SLOW, KEY_ZOOM_OUT_SLOW_KEYS, "Zoom out (slow)." );
			descriptions.add( KEY_FORWARD_Z_SLOW, KEY_FORWARD_Z_SLOW_KEYS, "Translate forward in Z (slow)." );
			descriptions.add( KEY_BACKWARD_Z_SLOW, KEY_BACKWARD_Z_SLOW_KEYS, "Translate backward in Z (slow)." );
		}
	}

	private final TranslateXY drageTranslateBehaviour;
	private final Zoom zoomBehaviour;
	private final SelectRotationAxis selectRotationAxisXBehaviour;
	private final SelectRotationAxis selectRotationAxisYBehaviour;
	private final SelectRotationAxis selectRotationAxisZBehaviour;
	private final Rotate dragRotateBehaviour;
	private final Rotate dragRotateFastBehaviour;
	private final Rotate dragRotateSlowBehaviour;
	private final TranslateZ translateZBehaviour;
	private final TranslateZ translateZFastBehaviour;
	private final TranslateZ translateZSlowBehaviour;
	private final KeyRotate rotateLeftBehaviour;
	private final KeyRotate rotateLeftFastBehaviour;
	private final KeyRotate rotateLeftSlowBehaviour;
	private final KeyRotate rotateRightBehaviour;
	private final KeyRotate rotateRightFastBehaviour;
	private final KeyRotate rotateRightSlowBehaviour;
	private final KeyZoom keyZoomInBehaviour;
	private final KeyZoom keyZoomInFastBehaviour;
	private final KeyZoom keyZoomInSlowBehaviour;
	private final KeyZoom keyZoomOutBehaviour;
	private final KeyZoom keyZoomOutFastBehaviour;
	private final KeyZoom keyZoomOutSlowBehaviour;
	private final KeyTranslateZ keyForwardZBehaviour;
	private final KeyTranslateZ keyForwardZFastBehaviour;
	private final KeyTranslateZ keyForwardZSlowBehaviour;
	private final KeyTranslateZ keyBackwardZBehaviour;
	private final KeyTranslateZ keyBackwardZFastBehaviour;
	private final KeyTranslateZ keyBackwardZSlowBehaviour;

	private final double[] speed = { 1.0, 10.0, 0.1 };

	/**
	 * Current source to screen transform.
	 */
	private final AffineTransform3D affine = new AffineTransform3D();

	/**
	 * Whom to notify when the {@link #affine current transform} is changed.
	 */
	private TransformListener< AffineTransform3D > listener;

	/**
	 * Copy of {@link #affine current transform} when mouse dragging started.
	 */
	private final AffineTransform3D affineDragStart = new AffineTransform3D();

	/**
	 * Coordinates where mouse dragging started.
	 */
	private double oX, oY;

	/**
	 * Current rotation axis for rotating with keyboard, indexed {@code x->0, y->1,
	 * z->2}.
	 */
	private int axis = 0;

	/**
	 * The screen size of the canvas (the component displaying the image and
	 * generating mouse events).
	 */
	private int canvasW = 1, canvasH = 1;

	/**
	 * Screen coordinates to keep centered while zooming or rotating with the
	 * keyboard. These are set to <em>(canvasW/2, canvasH/2)</em>
	 */
	private int centerX = 0, centerY = 0;

	public BehaviourTransformEventHandler3DMamut( final TransformListener< AffineTransform3D > listener )
	{
		this.listener = listener;

		drageTranslateBehaviour = new TranslateXY( DRAG_TRANSLATE );
		zoomBehaviour = new Zoom( ZOOM_NORMAL );
		selectRotationAxisXBehaviour = new SelectRotationAxis( SELECT_AXIS_X, 0 );
		selectRotationAxisYBehaviour = new SelectRotationAxis( SELECT_AXIS_Y, 1 );
		selectRotationAxisZBehaviour = new SelectRotationAxis( SELECT_AXIS_Z, 2 );

		dragRotateBehaviour = new Rotate( DRAG_ROTATE, speed[ 0 ] );
		dragRotateFastBehaviour = new Rotate( DRAG_ROTATE_FAST, speed[ 1 ] );
		dragRotateSlowBehaviour = new Rotate( DRAG_ROTATE_SLOW, speed[ 2 ] );

		translateZBehaviour = new TranslateZ( SCROLL_Z, speed[ 0 ] );
		translateZFastBehaviour = new TranslateZ( SCROLL_Z_FAST, speed[ 1 ] );
		translateZSlowBehaviour = new TranslateZ( SCROLL_Z_SLOW, speed[ 2 ] );

		rotateLeftBehaviour = new KeyRotate( ROTATE_LEFT, speed[ 0 ] );
		rotateLeftFastBehaviour = new KeyRotate( ROTATE_LEFT_FAST, speed[ 1 ] );
		rotateLeftSlowBehaviour = new KeyRotate( ROTATE_LEFT_SLOW, speed[ 2 ] );

		rotateRightBehaviour = new KeyRotate( ROTATE_RIGHT, -speed[ 0 ] );
		rotateRightFastBehaviour = new KeyRotate( ROTATE_RIGHT_FAST, -speed[ 1 ] );
		rotateRightSlowBehaviour = new KeyRotate( ROTATE_RIGHT_SLOW, -speed[ 2 ] );

		keyZoomInBehaviour = new KeyZoom( KEY_ZOOM_IN, speed[ 0 ] );
		keyZoomInFastBehaviour = new KeyZoom( KEY_ZOOM_IN_FAST, speed[ 1 ] );
		keyZoomInSlowBehaviour = new KeyZoom( KEY_ZOOM_IN_SLOW, speed[ 2 ] );

		keyZoomOutBehaviour = new KeyZoom( KEY_ZOOM_OUT, -speed[ 0 ] );
		keyZoomOutFastBehaviour = new KeyZoom( KEY_ZOOM_OUT_FAST, -speed[ 1 ] );
		keyZoomOutSlowBehaviour = new KeyZoom( KEY_ZOOM_OUT_SLOW, -speed[ 2 ] );

		keyForwardZBehaviour = new KeyTranslateZ( KEY_FORWARD_Z, speed[ 0 ] );
		keyForwardZFastBehaviour = new KeyTranslateZ( KEY_FORWARD_Z_FAST, speed[ 1 ] );
		keyForwardZSlowBehaviour = new KeyTranslateZ( KEY_FORWARD_Z_SLOW, speed[ 2 ] );

		keyBackwardZBehaviour = new KeyTranslateZ( KEY_BACKWARD_Z, -speed[ 0 ] );
		keyBackwardZFastBehaviour = new KeyTranslateZ( KEY_BACKWARD_Z_FAST, -speed[ 1 ] );
		keyBackwardZSlowBehaviour = new KeyTranslateZ( KEY_BACKWARD_Z_SLOW, -speed[ 2 ] );
	}

	@Override
	public void install( final Behaviours behaviours )
	{
		behaviours.namedBehaviour( drageTranslateBehaviour, DRAG_TRANSLATE_KEYS );
		behaviours.namedBehaviour( zoomBehaviour, ZOOM_NORMAL_KEYS );
		behaviours.namedBehaviour( selectRotationAxisXBehaviour, SELECT_AXIS_X_KEYS );
		behaviours.namedBehaviour( selectRotationAxisYBehaviour, SELECT_AXIS_Y_KEYS );
		behaviours.namedBehaviour( selectRotationAxisZBehaviour, SELECT_AXIS_Z_KEYS );
		behaviours.namedBehaviour( dragRotateBehaviour, DRAG_ROTATE_KEYS );
		behaviours.namedBehaviour( dragRotateFastBehaviour, DRAG_ROTATE_FAST_KEYS );
		behaviours.namedBehaviour( dragRotateSlowBehaviour, DRAG_ROTATE_SLOW_KEYS );
		behaviours.namedBehaviour( translateZBehaviour, SCROLL_Z_KEYS );
		behaviours.namedBehaviour( translateZFastBehaviour, SCROLL_Z_FAST_KEYS );
		behaviours.namedBehaviour( translateZSlowBehaviour, SCROLL_Z_SLOW_KEYS );
		behaviours.namedBehaviour( rotateLeftBehaviour, ROTATE_LEFT_KEYS );
		behaviours.namedBehaviour( rotateLeftFastBehaviour, ROTATE_LEFT_FAST_KEYS );
		behaviours.namedBehaviour( rotateLeftSlowBehaviour, ROTATE_LEFT_SLOW_KEYS );
		behaviours.namedBehaviour( rotateRightBehaviour, ROTATE_RIGHT_KEYS );
		behaviours.namedBehaviour( rotateRightFastBehaviour, ROTATE_RIGHT_FAST_KEYS );
		behaviours.namedBehaviour( rotateRightSlowBehaviour, ROTATE_RIGHT_SLOW_KEYS );
		behaviours.namedBehaviour( keyZoomInBehaviour, KEY_ZOOM_IN_KEYS );
		behaviours.namedBehaviour( keyZoomInFastBehaviour, KEY_ZOOM_IN_FAST_KEYS );
		behaviours.namedBehaviour( keyZoomInSlowBehaviour, KEY_ZOOM_IN_SLOW_KEYS );
		behaviours.namedBehaviour( keyZoomOutBehaviour, KEY_ZOOM_OUT_KEYS );
		behaviours.namedBehaviour( keyZoomOutFastBehaviour, KEY_ZOOM_OUT_FAST_KEYS );
		behaviours.namedBehaviour( keyZoomOutSlowBehaviour, KEY_ZOOM_OUT_SLOW_KEYS );
		behaviours.namedBehaviour( keyForwardZBehaviour, KEY_FORWARD_Z_KEYS );
		behaviours.namedBehaviour( keyForwardZFastBehaviour, KEY_FORWARD_Z_SLOW_KEYS );
		behaviours.namedBehaviour( keyForwardZSlowBehaviour, KEY_FORWARD_Z_FAST_KEYS );
		behaviours.namedBehaviour( keyBackwardZBehaviour, KEY_BACKWARD_Z_KEYS );
		behaviours.namedBehaviour( keyBackwardZFastBehaviour, KEY_BACKWARD_Z_FAST_KEYS );
		behaviours.namedBehaviour( keyBackwardZSlowBehaviour, KEY_BACKWARD_Z_SLOW_KEYS );
	}

	@Override
	public AffineTransform3D getTransform()
	{
		synchronized ( affine )
		{
			return affine.copy();
		}
	}

	@Override
	public void setTransform( final AffineTransform3D transform )
	{
		synchronized ( affine )
		{
			affine.set( transform );
		}
	}

	@Override
	public void setCanvasSize( final int width, final int height, final boolean updateTransform )
	{
		if ( width == 0 || height == 0 ) {
			// NB: We are probably in some intermediate layout scenario.
			// Attempting to trigger a transform update with 0 size will result
			// in the exception "Matrix is singular" from imglib2-realtrasform.
			return;
		}
		if ( updateTransform )
		{
			synchronized ( affine )
			{
				affine.set( affine.get( 0, 3 ) - canvasW / 2, 0, 3 );
				affine.set( affine.get( 1, 3 ) - canvasH / 2, 1, 3 );
				affine.scale( ( double ) width / canvasW );
				affine.set( affine.get( 0, 3 ) + width / 2, 0, 3 );
				affine.set( affine.get( 1, 3 ) + height / 2, 1, 3 );
				notifyListener();
			}
		}
		canvasW = width;
		canvasH = height;
		centerX = width / 2;
		centerY = height / 2;
	}

	@Override
	public void setTransformListener( final TransformListener< AffineTransform3D > transformListener )
	{
		listener = transformListener;
	}

	@Override
	public String getHelpString()
	{
		return null;
	}

	/**
	 * notifies {@link #listener} that the current transform changed.
	 */
	private void notifyListener()
	{
		if ( listener != null )
			listener.transformChanged( affine );
	}

	/**
	 * One step of rotation (radian).
	 */
	final protected static double step = Math.PI / 180;

	private void scale( final double s, final double x, final double y )
	{
		// center shift
		affine.set( affine.get( 0, 3 ) - x, 0, 3 );
		affine.set( affine.get( 1, 3 ) - y, 1, 3 );

		// scale
		affine.scale( s );

		// center un-shift
		affine.set( affine.get( 0, 3 ) + x, 0, 3 );
		affine.set( affine.get( 1, 3 ) + y, 1, 3 );
	}

	/**
	 * Rotate by d radians around axis. Keep screen coordinates (
	 * {@link #centerX}, {@link #centerY}) fixed.
	 */
	private void rotate( final int axis, final double d )
	{
		// center shift
		affine.set( affine.get( 0, 3 ) - centerX, 0, 3 );
		affine.set( affine.get( 1, 3 ) - centerY, 1, 3 );

		// rotate
		affine.rotate( axis, d );

		// center un-shift
		affine.set( affine.get( 0, 3 ) + centerX, 0, 3 );
		affine.set( affine.get( 1, 3 ) + centerY, 1, 3 );
	}

	private class Rotate extends AbstractNamedBehaviour implements DragBehaviour
	{
		private final double speed;

		public Rotate( final String name, final double speed )
		{
			super( name );
			this.speed = speed;
		}

		@Override
		public void init( final int x, final int y )
		{
			synchronized ( affine )
			{
				oX = x;
				oY = y;
				affineDragStart.set( affine );
			}
		}

		@Override
		public void drag( final int x, final int y )
		{
			synchronized ( affine )
			{
				final double dX = oX - x;
				final double dY = oY - y;

				affine.set( affineDragStart );

				// center shift
				affine.set( affine.get( 0, 3 ) - oX, 0, 3 );
				affine.set( affine.get( 1, 3 ) - oY, 1, 3 );

				final double v = step * speed;
				affine.rotate( 0, -dY * v );
				affine.rotate( 1, dX * v );

				// center un-shift
				affine.set( affine.get( 0, 3 ) + oX, 0, 3 );
				affine.set( affine.get( 1, 3 ) + oY, 1, 3 );
				notifyListener();
			}
		}

		@Override
		public void end( final int x, final int y )
		{}
	}

	private class TranslateXY extends AbstractNamedBehaviour implements DragBehaviour
	{
		public TranslateXY( final String name )
		{
			super( name );
		}

		@Override
		public void init( final int x, final int y )
		{
			synchronized ( affine )
			{
				oX = x;
				oY = y;
				affineDragStart.set( affine );
			}
		}

		@Override
		public void drag( final int x, final int y )
		{
			synchronized ( affine )
			{
				final double dX = oX - x;
				final double dY = oY - y;

				affine.set( affineDragStart );
				affine.set( affine.get( 0, 3 ) - dX, 0, 3 );
				affine.set( affine.get( 1, 3 ) - dY, 1, 3 );
				notifyListener();
			}
		}

		@Override
		public void end( final int x, final int y )
		{}
	}

	private class TranslateZ extends AbstractNamedBehaviour implements ScrollBehaviour
	{
		private final double speed;

		public TranslateZ( final String name, final double speed )
		{
			super( name );
			this.speed = speed;
		}

		@Override
		public void scroll( final double wheelRotation, final boolean isHorizontal, final int x, final int y )
		{
			synchronized ( affine )
			{
				final double dZ = speed * -wheelRotation;
				// TODO (optionally) correct for zoom
				affine.set( affine.get( 2, 3 ) - dZ, 2, 3 );
				notifyListener();
			}
		}
	}

	private class Zoom extends AbstractNamedBehaviour implements ScrollBehaviour
	{
		private final double speed = 1.0;

		public Zoom( final String name )
		{
			super( name );
		}

		@Override
		public void scroll( final double wheelRotation, final boolean isHorizontal, final int x, final int y )
		{
			synchronized ( affine )
			{
				final double s = speed * wheelRotation;
				final double dScale = 1.0 + 0.05;
				if ( s > 0 )
					scale( 1.0 / dScale, x, y );
				else
					scale( dScale, x, y );
				notifyListener();
			}
		}
	}

	private class SelectRotationAxis extends AbstractNamedBehaviour implements ClickBehaviour
	{
		private final int axis;

		public SelectRotationAxis( final String name, final int axis )
		{
			super ( name );
			this.axis = axis;
		}

		@Override
		public void click( final int x, final int y )
		{
			BehaviourTransformEventHandler3DMamut.this.axis = axis;
		}
	}

	private class KeyRotate extends AbstractNamedBehaviour implements ClickBehaviour
	{
		private final double speed;

		public KeyRotate( final String name, final double speed )
		{
			super( name );
			this.speed = speed;
		}

		@Override
		public void click( final int x, final int y )
		{
			synchronized ( affine )
			{
				rotate( axis, step * speed );
				notifyListener();
			}
		}
	}

	private class KeyZoom extends AbstractNamedBehaviour implements ClickBehaviour
	{
		private final double dScale;

		public KeyZoom( final String name, final double speed )
		{
			super( name );
			if ( speed > 0 )
				dScale = 1.0 + 0.1 * speed;
			else
				dScale = 1.0 / ( 1.0 - 0.1 * speed );
		}

		@Override
		public void click( final int x, final int y )
		{
			synchronized ( affine )
			{
				scale( dScale, centerX, centerY );
				notifyListener();
			}
		}
	}

	private class KeyTranslateZ extends AbstractNamedBehaviour implements ClickBehaviour
	{
		private final double speed;

		public KeyTranslateZ( final String name, final double speed )
		{
			super( name );
			this.speed = speed;
		}

		@Override
		public void click( final int x, final int y )
		{
			synchronized ( affine )
			{
				affine.set( affine.get( 2, 3 ) + speed, 2, 3 );
				notifyListener();
			}
		}
	}
}
