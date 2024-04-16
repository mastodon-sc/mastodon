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
package org.mastodon.views.bdv;

import org.mastodon.ui.keymap.KeyConfigScopes;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;

import bdv.TransformEventHandler3D;

/*
 * Command descriptions for all commands provided by {@link TransformEventHandler3D}
 */
@Plugin( type = CommandDescriptionProvider.class )
public class TransformEventHandler3DDescriptions extends CommandDescriptionProvider
{
	public TransformEventHandler3DDescriptions()
	{
		super( KeyConfigScopes.MASTODON, KeyConfigContexts.BIGDATAVIEWER );
	}

	@Override
	public void getCommandDescriptions( final CommandDescriptions descriptions )
	{
		descriptions.add( TransformEventHandler3D.DRAG_TRANSLATE, TransformEventHandler3D.DRAG_TRANSLATE_KEYS,
				"Pan the view by mouse-dragging." );
		descriptions.add( TransformEventHandler3D.ZOOM_NORMAL, TransformEventHandler3D.ZOOM_NORMAL_KEYS,
				"Zoom in by scrolling." );
		descriptions.add( TransformEventHandler3D.SELECT_AXIS_X, TransformEventHandler3D.SELECT_AXIS_X_KEYS,
				"Select X as the rotation axis for keyboard rotation." );
		descriptions.add( TransformEventHandler3D.SELECT_AXIS_Y, TransformEventHandler3D.SELECT_AXIS_Y_KEYS,
				"Select Y as the rotation axis for keyboard rotation." );
		descriptions.add( TransformEventHandler3D.SELECT_AXIS_Z, TransformEventHandler3D.SELECT_AXIS_Z_KEYS,
				"Select Z as the rotation axis for keyboard rotation." );

		descriptions.add( TransformEventHandler3D.DRAG_ROTATE, TransformEventHandler3D.DRAG_ROTATE_KEYS,
				"Rotate the view by mouse-dragging." );
		descriptions.add( TransformEventHandler3D.SCROLL_Z, TransformEventHandler3D.SCROLL_Z_KEYS,
				"Translate in Z by scrolling." );
		descriptions.add( TransformEventHandler3D.ROTATE_LEFT, TransformEventHandler3D.ROTATE_LEFT_KEYS,
				"Rotate left (counter-clockwise) by 1 degree." );
		descriptions.add( TransformEventHandler3D.ROTATE_RIGHT, TransformEventHandler3D.ROTATE_RIGHT_KEYS,
				"Rotate right (clockwise) by 1 degree." );
		descriptions.add( TransformEventHandler3D.KEY_ZOOM_IN, TransformEventHandler3D.KEY_ZOOM_IN_KEYS, "Zoom in." );
		descriptions.add( TransformEventHandler3D.KEY_ZOOM_OUT, TransformEventHandler3D.KEY_ZOOM_OUT_KEYS,
				"Zoom out." );
		descriptions.add( TransformEventHandler3D.KEY_FORWARD_Z, TransformEventHandler3D.KEY_FORWARD_Z_KEYS,
				"Translate forward in Z." );
		descriptions.add( TransformEventHandler3D.KEY_BACKWARD_Z, TransformEventHandler3D.KEY_BACKWARD_Z_KEYS,
				"Translate backward in Z." );

		descriptions.add( TransformEventHandler3D.DRAG_ROTATE_FAST, TransformEventHandler3D.DRAG_ROTATE_FAST_KEYS,
				"Rotate the view by mouse-dragging (fast)." );
		descriptions.add( TransformEventHandler3D.SCROLL_Z_FAST, TransformEventHandler3D.SCROLL_Z_FAST_KEYS,
				"Translate in Z by scrolling (fast)." );
		descriptions.add( TransformEventHandler3D.ROTATE_LEFT_FAST, TransformEventHandler3D.ROTATE_LEFT_FAST_KEYS,
				"Rotate left (counter-clockwise) by 10 degrees." );
		descriptions.add( TransformEventHandler3D.ROTATE_RIGHT_FAST, TransformEventHandler3D.ROTATE_RIGHT_FAST_KEYS,
				"Rotate right (clockwise) by 10 degrees." );
		descriptions.add( TransformEventHandler3D.KEY_ZOOM_IN_FAST, TransformEventHandler3D.KEY_ZOOM_IN_FAST_KEYS,
				"Zoom in (fast)." );
		descriptions.add( TransformEventHandler3D.KEY_ZOOM_OUT_FAST, TransformEventHandler3D.KEY_ZOOM_OUT_FAST_KEYS,
				"Zoom out (fast)." );
		descriptions.add( TransformEventHandler3D.KEY_FORWARD_Z_FAST, TransformEventHandler3D.KEY_FORWARD_Z_FAST_KEYS,
				"Translate forward in Z (fast)." );
		descriptions.add( TransformEventHandler3D.KEY_BACKWARD_Z_FAST, TransformEventHandler3D.KEY_BACKWARD_Z_FAST_KEYS,
				"Translate backward in Z (fast)." );

		descriptions.add( TransformEventHandler3D.DRAG_ROTATE_SLOW, TransformEventHandler3D.DRAG_ROTATE_SLOW_KEYS,
				"Rotate the view by mouse-dragging (slow)." );
		descriptions.add( TransformEventHandler3D.SCROLL_Z_SLOW, TransformEventHandler3D.SCROLL_Z_SLOW_KEYS,
				"Translate in Z by scrolling (slow)." );
		descriptions.add( TransformEventHandler3D.ROTATE_LEFT_SLOW, TransformEventHandler3D.ROTATE_LEFT_SLOW_KEYS,
				"Rotate left (counter-clockwise) by 0.1 degree." );
		descriptions.add( TransformEventHandler3D.ROTATE_RIGHT_SLOW, TransformEventHandler3D.ROTATE_RIGHT_SLOW_KEYS,
				"Rotate right (clockwise) by 0.1 degree." );
		descriptions.add( TransformEventHandler3D.KEY_ZOOM_IN_SLOW, TransformEventHandler3D.KEY_ZOOM_IN_SLOW_KEYS,
				"Zoom in (slow)." );
		descriptions.add( TransformEventHandler3D.KEY_ZOOM_OUT_SLOW, TransformEventHandler3D.KEY_ZOOM_OUT_SLOW_KEYS,
				"Zoom out (slow)." );
		descriptions.add( TransformEventHandler3D.KEY_FORWARD_Z_SLOW, TransformEventHandler3D.KEY_FORWARD_Z_SLOW_KEYS,
				"Translate forward in Z (slow)." );
		descriptions.add( TransformEventHandler3D.KEY_BACKWARD_Z_SLOW, TransformEventHandler3D.KEY_BACKWARD_Z_SLOW_KEYS,
				"Translate backward in Z (slow)." );
	}
}
