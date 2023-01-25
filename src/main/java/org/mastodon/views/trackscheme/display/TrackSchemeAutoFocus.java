/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
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

import bdv.viewer.TransformListener;
import org.mastodon.model.FocusListener;
import org.mastodon.model.FocusModel;
import org.mastodon.views.trackscheme.LineageTreeLayout;
import org.mastodon.views.trackscheme.ScreenTransform;
import org.mastodon.views.trackscheme.TrackSchemeEdge;
import org.mastodon.views.trackscheme.TrackSchemeVertex;
import org.scijava.listeners.Listeners;

import net.imglib2.RealPoint;

/**
 * A {@code FocusModel} for TrackScheme that automatically focuses a vertex near
 * the center of the window if none is focused (on {@code getFocusedVertex()}).
 */
public class TrackSchemeAutoFocus
		implements FocusModel< TrackSchemeVertex, TrackSchemeEdge >, TransformListener< ScreenTransform >
{
	private final LineageTreeLayout layout;

	private final FocusModel< TrackSchemeVertex, TrackSchemeEdge > focus;

	private final ScreenTransform screenTransform = new ScreenTransform();

	private final RealPoint centerPos = new RealPoint( 2 );

	private double ratioXtoY = 1;

	public TrackSchemeAutoFocus(
			final LineageTreeLayout layout,
			final FocusModel< TrackSchemeVertex, TrackSchemeEdge > focus )
	{
		this.layout = layout;
		this.focus = focus;
	}

	@Override
	public void focusVertex( final TrackSchemeVertex vertex )
	{
		focus.focusVertex( vertex );
	}

	@Override
	public TrackSchemeVertex getFocusedVertex( final TrackSchemeVertex ref )
	{
		TrackSchemeVertex vertex = focus.getFocusedVertex( ref );
		if ( vertex != null )
			return vertex;

		vertex = layout.getClosestActiveVertex( centerPos, ratioXtoY, ref );
		if ( vertex != null )
			focus.focusVertex( vertex );

		return vertex;
	}

	@Override
	public Listeners< FocusListener > listeners()
	{
		return focus.listeners();
	}

	@Override
	public void transformChanged( final ScreenTransform transform )
	{
		synchronized ( screenTransform )
		{
			screenTransform.set( transform );
			centerPos.setPosition( ( transform.getMaxX() + transform.getMinX() ) / 2., 0 );
			centerPos.setPosition( ( transform.getMaxY() + transform.getMinY() ) / 2., 1 );
			ratioXtoY = transform.getXtoYRatio();
		}
	}
}
