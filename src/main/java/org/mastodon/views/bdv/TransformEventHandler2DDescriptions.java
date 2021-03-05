/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
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
