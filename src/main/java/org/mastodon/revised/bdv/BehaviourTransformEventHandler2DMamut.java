package org.mastodon.revised.bdv;

import org.mastodon.revised.mamut.KeyConfigContexts;
import org.mastodon.revised.ui.keymap.CommandDescriptionProvider;
import org.mastodon.revised.ui.keymap.CommandDescriptions;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.ScrollBehaviour;
import org.scijava.ui.behaviour.util.Behaviours;

import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.TransformListener;

/**
 * Adapted from BehaviourTransformEventHandlerPlanar in BDV-vistools, by Tobias
 * Pietzsch.
 */
public class BehaviourTransformEventHandler2DMamut implements BehaviourTransformEventHandlerMamut
{

	private static final String DRAG_TRANSLATE = "2d drag translate";
	private static final String DRAG_ROTATE = "2d drag rotate";
	private static final String ZOOM_NORMAL = "2d scroll zoom";
	private static final String ZOOM_FAST = "2d scroll zoom fast";
	private static final String ZOOM_SLOW = "2d scroll zoom slow";
	private static final String SCROLL_TRANSLATE = "2d scroll translate";
	private static final String SCROLL_TRANSLATE_FAST = "2d scroll translate fast";
	private static final String SCROLL_TRANSLATE_SLOW = "2d scroll translate slow";
	private static final String SCROLL_ROTATE = "2d scroll rotate";
	private static final String SCROLL_ROTATE_FAST = "2d scroll rotate fast";
	private static final String SCROLL_ROTATE_SLOW = "2d scroll rotate slow";
	private static final String ROTATE_LEFT = "2d rotate left";
	private static final String ROTATE_LEFT_FAST = "2d rotate left fast";
	private static final String ROTATE_LEFT_SLOW = "2d rotate left slow";
	private static final String ROTATE_RIGHT = "2d rotate right";
	private static final String ROTATE_RIGHT_FAST = "2d rotate right fast";
	private static final String ROTATE_RIGHT_SLOW = "2d rotate right slow";
	private static final String KEY_ZOOM_IN = "2d zoom in";
	private static final String KEY_ZOOM_IN_FAST = "2d zoom in fast";
	private static final String KEY_ZOOM_IN_SLOW = "2d zoom in slow";
	private static final String KEY_ZOOM_OUT = "2d zoom out";
	private static final String KEY_ZOOM_OUT_FAST = "2d zoom out fast";
	private static final String KEY_ZOOM_OUT_SLOW = "2d zoom out slow";

	private static final String[] DRAG_TRANSLATE_KEYS = new String[] { "button2", "button3" };
	private static final String[] ZOOM_NORMAL_KEYS = new String[] { "meta scroll", "ctrl shift scroll" };
	private static final String[] ZOOM_FAST_KEYS = new String[] { "shift scroll" };
	private static final String[] ZOOM_SLOW_KEYS = new String[] { "ctrl scroll" };
	private static final String[] DRAG_ROTATE_KEYS = new String[] { "button1" };
	private static final String[] SCROLL_TRANSLATE_KEYS = new String[] { "not mapped" };
	private static final String[] SCROLL_TRANSLATE_FAST_KEYS = new String[] { "not mapped" };
	private static final String[] SCROLL_TRANSLATE_SLOW_KEYS = new String[] { "not mapped" };
	private static final String[] SCROLL_ROTATE_KEYS = new String[] { "scroll" };
	private static final String[] SCROLL_ROTATE_FAST_KEYS = new String[] { "shift scroll" };
	private static final String[] SCROLL_ROTATE_SLOW_KEYS = new String[] { "ctrl scroll" };
	private static final String[] ROTATE_LEFT_KEYS = new String[] { "LEFT" };
	private static final String[] ROTATE_RIGHT_KEYS = new String[] { "RIGHT" };
	private static final String[] ROTATE_LEFT_FAST_KEYS = new String[] { "shift LEFT" };
	private static final String[] ROTATE_RIGHT_FAST_KEYS = new String[] { "shift RIGHT" };
	private static final String[] ROTATE_LEFT_SLOW_KEYS = new String[] { "ctrl LEFT" };
	private static final String[] ROTATE_RIGHT_SLOW_KEYS = new String[] { "ctrl RIGHT" };

	private static final String[] KEY_ZOOM_IN_KEYS = new String[] { "UP" };
	private static final String[] KEY_ZOOM_OUT_KEYS = new String[] { "DOWN" };
	private static final String[] KEY_ZOOM_IN_FAST_KEYS = new String[] { "shift UP" };
	private static final String[] KEY_ZOOM_OUT_FAST_KEYS = new String[] { "shift DOWN" };
	private static final String[] KEY_ZOOM_IN_SLOW_KEYS = new String[] { "ctrl UP" };
	private static final String[] KEY_ZOOM_OUT_SLOW_KEYS = new String[] { "ctrl DOWN" };

	private static final double[] SPEED = { 1.0, 10.0, 0.1 };

	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = CommandDescriptionProvider.class )
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
			descriptions.add( DRAG_ROTATE, DRAG_ROTATE_KEYS, "Rotate the view by mouse-dragging." );

			descriptions.add( ZOOM_NORMAL, ZOOM_NORMAL_KEYS, "Zoom in by scrolling." );
			descriptions.add( ZOOM_FAST, ZOOM_FAST_KEYS, "Zoom in by scrolling (fast)." );
			descriptions.add( ZOOM_SLOW, ZOOM_SLOW_KEYS, "Zoom in by scrolling (slow)." );

			descriptions.add( SCROLL_TRANSLATE, SCROLL_TRANSLATE_KEYS, "Translate by scrolling." );
			descriptions.add( SCROLL_TRANSLATE_FAST, SCROLL_TRANSLATE_FAST_KEYS, "Translate by scrolling (fast)." );
			descriptions.add( SCROLL_TRANSLATE_SLOW, SCROLL_TRANSLATE_SLOW_KEYS, "Translate by scrolling (slow)." );

			descriptions.add( ROTATE_LEFT, ROTATE_LEFT_KEYS, "Rotate left (counter-clockwise) by 1 degree." );
			descriptions.add( ROTATE_RIGHT, ROTATE_RIGHT_KEYS, "Rotate right (clockwise) by 1 degree." );
			descriptions.add( KEY_ZOOM_IN, KEY_ZOOM_IN_KEYS, "Zoom in." );
			descriptions.add( KEY_ZOOM_OUT, KEY_ZOOM_OUT_KEYS, "Zoom out." );

			descriptions.add( ROTATE_LEFT_FAST, ROTATE_LEFT_FAST_KEYS, "Rotate left (counter-clockwise) by 10 degrees." );
			descriptions.add( ROTATE_RIGHT_FAST, ROTATE_RIGHT_FAST_KEYS, "Rotate right (clockwise) by 10 degrees." );
			descriptions.add( KEY_ZOOM_IN_FAST, KEY_ZOOM_IN_FAST_KEYS, "Zoom in (fast)." );
			descriptions.add( KEY_ZOOM_OUT_FAST, KEY_ZOOM_OUT_FAST_KEYS, "Zoom out (fast)." );

			descriptions.add( ROTATE_LEFT_SLOW, ROTATE_LEFT_SLOW_KEYS, "Rotate left (counter-clockwise) by 0.1 degree." );
			descriptions.add( ROTATE_RIGHT_SLOW, ROTATE_RIGHT_SLOW_KEYS, "Rotate right (clockwise) by 0.1 degree." );
			descriptions.add( KEY_ZOOM_IN_SLOW, KEY_ZOOM_IN_SLOW_KEYS, "Zoom in (slow)." );
			descriptions.add( KEY_ZOOM_OUT_SLOW, KEY_ZOOM_OUT_SLOW_KEYS, "Zoom out (slow)." );

			descriptions.add( SCROLL_ROTATE, SCROLL_ROTATE_KEYS, "Rotate by scrolling." );
			descriptions.add( SCROLL_ROTATE_FAST, SCROLL_ROTATE_FAST_KEYS, "Rotate by scrolling (fast)." );
			descriptions.add( SCROLL_ROTATE_SLOW, SCROLL_ROTATE_SLOW_KEYS, "Rotate by scrolling (slow)." );
		}
	}

	private final DragTranslate dragTranslate;
	private final DragRotate dragRotate;
	private final Zoom zoom;
	private final Zoom zoomFast;
	private final Zoom zoomSlow;
	private final ScrollTranslate scrollTranslateNormal;
	private final ScrollTranslate scrollTranslateFast;
	private final ScrollTranslate scrollTranslateSlow;
	private final ScrollRotate scrollRotateNormal;
	private final ScrollRotate scrollRotateFast;
	private final ScrollRotate scrollRotateSlow;
	private final KeyRotate keyRotateLeftNormal;
	private final KeyRotate keyRotateLeftFast;
	private final KeyRotate keyRotateLeftSlow;
	private final KeyRotate keyRotateRightNormal;
	private final KeyRotate keyRotateRightFast;
	private final KeyRotate keyRotateRightSlow;
	private final KeyZoom keyZoomInNormal;
	private final KeyZoom keyZoomInFast;
	private final KeyZoom keyZoomInSlow;
	private final KeyZoom keyZoomOutNormal;
	private final KeyZoom keyZoomOutFast;
	private final KeyZoom keyZoomOutSlow;

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
	private double oX;

	private double oY;

	/**
	 * The screen size of the canvas (the component displaying the image and
	 * generating mouse events).
	 */
	private int canvasW = 1;

	private int canvasH = 1;

	/**
	 * Screen coordinates to keep centered while zooming or rotating with the
	 * keyboard. These are set to <em>(canvasW/2, canvasH/2)</em>
	 */
	private int centerX = 0;

	private int centerY = 0;

	public BehaviourTransformEventHandler2DMamut( final TransformListener< AffineTransform3D > listener )
	{
		this.listener = listener;

		dragTranslate = new DragTranslate();
		dragRotate = new DragRotate();

		scrollTranslateNormal = new ScrollTranslate( SPEED[ 0 ] );
		scrollTranslateFast = new ScrollTranslate( SPEED[ 1 ] );
		scrollTranslateSlow = new ScrollTranslate( SPEED[ 2 ] );

		zoom = new Zoom( SPEED[ 0 ] );
		zoomFast = new Zoom( SPEED[ 1 ] );
		zoomSlow = new Zoom( SPEED[ 2 ] );

		scrollRotateNormal = new ScrollRotate( 2 * SPEED[ 0 ] );
		scrollRotateFast = new ScrollRotate( 2 * SPEED[ 1 ] );
		scrollRotateSlow = new ScrollRotate( 2 * SPEED[ 2 ] );

		keyRotateLeftNormal = new KeyRotate( SPEED[ 0 ] );
		keyRotateLeftFast = new KeyRotate( SPEED[ 1 ] );
		keyRotateLeftSlow = new KeyRotate( SPEED[ 2 ] );
		keyRotateRightNormal = new KeyRotate( -SPEED[ 0 ] );
		keyRotateRightFast = new KeyRotate( -SPEED[ 1 ] );
		keyRotateRightSlow = new KeyRotate( -SPEED[ 2 ] );

		keyZoomInNormal = new KeyZoom( SPEED[ 0 ] );
		keyZoomInFast = new KeyZoom( SPEED[ 1 ] );
		keyZoomInSlow = new KeyZoom( SPEED[ 2 ] );
		keyZoomOutNormal = new KeyZoom( -SPEED[ 0 ] );
		keyZoomOutFast = new KeyZoom( -SPEED[ 1 ] );
		keyZoomOutSlow = new KeyZoom( -SPEED[ 2 ] );
	}

	@Override
	public void install( final Behaviours behaviours )
	{
		behaviours.behaviour( dragTranslate, DRAG_TRANSLATE, DRAG_TRANSLATE_KEYS );
		behaviours.behaviour( dragRotate, DRAG_ROTATE,  DRAG_ROTATE_KEYS );

		behaviours.behaviour( scrollTranslateNormal, SCROLL_TRANSLATE, SCROLL_TRANSLATE_KEYS );
		behaviours.behaviour( scrollTranslateFast, SCROLL_TRANSLATE_FAST, SCROLL_TRANSLATE_FAST_KEYS );
		behaviours.behaviour( scrollTranslateSlow, SCROLL_TRANSLATE_SLOW, SCROLL_TRANSLATE_SLOW_KEYS );

		behaviours.behaviour( zoom, ZOOM_NORMAL, ZOOM_NORMAL_KEYS );
		behaviours.behaviour( zoomFast, ZOOM_FAST, ZOOM_FAST_KEYS );
		behaviours.behaviour( zoomSlow, ZOOM_SLOW, ZOOM_SLOW_KEYS );

		behaviours.behaviour( scrollRotateNormal, SCROLL_ROTATE, SCROLL_ROTATE_KEYS );
		behaviours.behaviour( scrollRotateFast, SCROLL_ROTATE_FAST, SCROLL_ROTATE_FAST_KEYS );
		behaviours.behaviour( scrollRotateSlow, SCROLL_ROTATE_SLOW, SCROLL_ROTATE_SLOW_KEYS );

		behaviours.behaviour( keyRotateLeftNormal, ROTATE_LEFT, ROTATE_LEFT_KEYS );
		behaviours.behaviour( keyRotateLeftFast, ROTATE_LEFT_FAST, ROTATE_LEFT_FAST_KEYS );
		behaviours.behaviour( keyRotateLeftSlow, ROTATE_LEFT_SLOW, ROTATE_LEFT_SLOW_KEYS );
		behaviours.behaviour( keyRotateRightNormal, ROTATE_RIGHT, ROTATE_RIGHT_KEYS );
		behaviours.behaviour( keyRotateRightFast, ROTATE_RIGHT_FAST, ROTATE_RIGHT_FAST_KEYS );
		behaviours.behaviour( keyRotateRightSlow, ROTATE_RIGHT_SLOW, ROTATE_RIGHT_SLOW_KEYS );

		behaviours.behaviour( keyZoomInNormal,  KEY_ZOOM_IN, KEY_ZOOM_IN_KEYS );
		behaviours.behaviour( keyZoomInFast, KEY_ZOOM_IN_FAST, KEY_ZOOM_IN_FAST_KEYS );
		behaviours.behaviour( keyZoomInSlow, KEY_ZOOM_IN_SLOW, KEY_ZOOM_IN_SLOW_KEYS );
		behaviours.behaviour( keyZoomOutNormal, KEY_ZOOM_OUT, KEY_ZOOM_OUT_KEYS );
		behaviours.behaviour( keyZoomOutFast, KEY_ZOOM_OUT_FAST, KEY_ZOOM_OUT_FAST_KEYS );
		behaviours.behaviour( keyZoomOutSlow, KEY_ZOOM_OUT_SLOW, KEY_ZOOM_OUT_SLOW_KEYS );
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
	final private static double step = Math.PI / 180;

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

	private class DragRotate implements DragBehaviour
	{
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
				final double dX = x - centerX;
				final double dY = y - centerY;
				final double odX = oX - centerX;
				final double odY = oY - centerY;
				final double theta = Math.atan2( dY, dX ) - Math.atan2( odY, odX );

				affine.set( affineDragStart );
				rotate( 2, theta );
				notifyListener();
			}
		}

		@Override
		public void end( final int x, final int y )
		{}
	}

	private class ScrollRotate implements ScrollBehaviour
	{
		private final double speed;

		public ScrollRotate( final double speed )
		{
			this.speed = speed;
		}

		@Override
		public void scroll( final double wheelRotation, final boolean isHorizontal, final int x, final int y )
		{
			synchronized ( affine )
			{
				final double theta = speed * wheelRotation * Math.PI / 180.0;

				// center shift
				affine.set( affine.get( 0, 3 ) - x, 0, 3 );
				affine.set( affine.get( 1, 3 ) - y, 1, 3 );

				affine.rotate( 2, theta );

				// center un-shift
				affine.set( affine.get( 0, 3 ) + x, 0, 3 );
				affine.set( affine.get( 1, 3 ) + y, 1, 3 );

				notifyListener();
			}
		}
	}

	private class DragTranslate implements DragBehaviour
	{
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

	private class ScrollTranslate implements ScrollBehaviour
	{

		private final double speed;

		public ScrollTranslate( final double speed )
		{
			this.speed = speed;
		}

		@Override
		public void scroll( final double wheelRotation, final boolean isHorizontal, final int x, final int y )
		{
			synchronized ( affine )
			{
				final double d = -wheelRotation * 10 * speed;
				if ( isHorizontal )
					affine.translate( d, 0, 0 );
				else
					affine.translate( 0, d, 0 );
				notifyListener();
			}
		}
	}

	private class Zoom implements ScrollBehaviour
	{

		private final double speed;

		public Zoom( final double speed )
		{
			this.speed = speed;
		}

		@Override
		public void scroll( final double wheelRotation, final boolean isHorizontal, final int x, final int y )
		{
			synchronized ( affine )
			{
				final double s = speed * wheelRotation;
				final double dScale = 1.0 + 0.05 * Math.abs( s );
				if ( s > 0 )
					scale( 1.0 / dScale, x, y );
				else
					scale( dScale, x, y );
				notifyListener();
			}
		}
	}

	private class KeyRotate implements ClickBehaviour
	{
		private final double speed;

		public KeyRotate( final double speed )
		{
			this.speed = speed;
		}

		@Override
		public void click( final int x, final int y )
		{
			synchronized ( affine )
			{
				rotate( 2, step * speed );
				notifyListener();
			}
		}
	}

	private class KeyZoom implements ClickBehaviour
	{
		private final double dScale;

		public KeyZoom( final double speed )
		{
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
}
