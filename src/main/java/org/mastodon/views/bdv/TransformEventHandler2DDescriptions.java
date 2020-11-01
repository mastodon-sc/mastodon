package org.mastodon.views.bdv;

import bdv.TransformEventHandler2D;
import org.mastodon.ui.keymap.CommandDescriptionProvider;
import org.mastodon.ui.keymap.CommandDescriptions;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.plugin.Plugin;

/*
 * Command descriptions for all commands provided by {@link TransformEventHandler2D}
 */
@Plugin( type = CommandDescriptionProvider.class )
public class TransformEventHandler2DDescriptions extends CommandDescriptionProvider
{
	public TransformEventHandler2DDescriptions()
	{
		super( KeyConfigContexts.BIGDATAVIEWER );
	}

	@Override
	public void getCommandDescriptions( final CommandDescriptions descriptions )
	{
		descriptions.add( TransformEventHandler2D.DRAG_TRANSLATE, TransformEventHandler2D.DRAG_TRANSLATE_KEYS, "Pan the view by mouse-dragging." );
		descriptions.add( TransformEventHandler2D.DRAG_ROTATE, TransformEventHandler2D.DRAG_ROTATE_KEYS, "Rotate the view by mouse-dragging." );

		descriptions.add( TransformEventHandler2D.ZOOM_NORMAL, TransformEventHandler2D.ZOOM_NORMAL_KEYS, "Zoom in by scrolling." );
		descriptions.add( TransformEventHandler2D.ZOOM_FAST, TransformEventHandler2D.ZOOM_FAST_KEYS, "Zoom in by scrolling (fast)." );
		descriptions.add( TransformEventHandler2D.ZOOM_SLOW, TransformEventHandler2D.ZOOM_SLOW_KEYS, "Zoom in by scrolling (slow)." );

		descriptions.add( TransformEventHandler2D.SCROLL_TRANSLATE, TransformEventHandler2D.SCROLL_TRANSLATE_KEYS, "Translate by scrolling." );
		descriptions.add( TransformEventHandler2D.SCROLL_TRANSLATE_FAST, TransformEventHandler2D.SCROLL_TRANSLATE_FAST_KEYS, "Translate by scrolling (fast)." );
		descriptions.add( TransformEventHandler2D.SCROLL_TRANSLATE_SLOW, TransformEventHandler2D.SCROLL_TRANSLATE_SLOW_KEYS, "Translate by scrolling (slow)." );

		descriptions.add( TransformEventHandler2D.ROTATE_LEFT, TransformEventHandler2D.ROTATE_LEFT_KEYS, "Rotate left (counter-clockwise) by 1 degree." );
		descriptions.add( TransformEventHandler2D.ROTATE_RIGHT, TransformEventHandler2D.ROTATE_RIGHT_KEYS, "Rotate right (clockwise) by 1 degree." );
		descriptions.add( TransformEventHandler2D.KEY_ZOOM_IN, TransformEventHandler2D.KEY_ZOOM_IN_KEYS, "Zoom in." );
		descriptions.add( TransformEventHandler2D.KEY_ZOOM_OUT, TransformEventHandler2D.KEY_ZOOM_OUT_KEYS, "Zoom out." );

		descriptions.add( TransformEventHandler2D.ROTATE_LEFT_FAST, TransformEventHandler2D.ROTATE_LEFT_FAST_KEYS, "Rotate left (counter-clockwise) by 10 degrees." );
		descriptions.add( TransformEventHandler2D.ROTATE_RIGHT_FAST, TransformEventHandler2D.ROTATE_RIGHT_FAST_KEYS, "Rotate right (clockwise) by 10 degrees." );
		descriptions.add( TransformEventHandler2D.KEY_ZOOM_IN_FAST, TransformEventHandler2D.KEY_ZOOM_IN_FAST_KEYS, "Zoom in (fast)." );
		descriptions.add( TransformEventHandler2D.KEY_ZOOM_OUT_FAST, TransformEventHandler2D.KEY_ZOOM_OUT_FAST_KEYS, "Zoom out (fast)." );

		descriptions.add( TransformEventHandler2D.ROTATE_LEFT_SLOW, TransformEventHandler2D.ROTATE_LEFT_SLOW_KEYS, "Rotate left (counter-clockwise) by 0.1 degree." );
		descriptions.add( TransformEventHandler2D.ROTATE_RIGHT_SLOW, TransformEventHandler2D.ROTATE_RIGHT_SLOW_KEYS, "Rotate right (clockwise) by 0.1 degree." );
		descriptions.add( TransformEventHandler2D.KEY_ZOOM_IN_SLOW, TransformEventHandler2D.KEY_ZOOM_IN_SLOW_KEYS, "Zoom in (slow)." );
		descriptions.add( TransformEventHandler2D.KEY_ZOOM_OUT_SLOW, TransformEventHandler2D.KEY_ZOOM_OUT_SLOW_KEYS, "Zoom out (slow)." );

		descriptions.add( TransformEventHandler2D.SCROLL_ROTATE, TransformEventHandler2D.SCROLL_ROTATE_KEYS, "Rotate by scrolling." );
		descriptions.add( TransformEventHandler2D.SCROLL_ROTATE_FAST, TransformEventHandler2D.SCROLL_ROTATE_FAST_KEYS, "Rotate by scrolling (fast)." );
		descriptions.add( TransformEventHandler2D.SCROLL_ROTATE_SLOW, TransformEventHandler2D.SCROLL_ROTATE_SLOW_KEYS, "Rotate by scrolling (slow)." );
	}
}
