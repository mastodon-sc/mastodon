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
package org.mastodon.views.grapher.display;

import org.mastodon.model.FocusListener;
import org.mastodon.model.FocusModel;
import org.mastodon.views.grapher.datagraph.DataEdge;
import org.mastodon.views.grapher.datagraph.DataGraphLayout;
import org.mastodon.views.grapher.datagraph.DataVertex;
import org.mastodon.views.grapher.datagraph.ScreenTransform;
import org.scijava.listeners.Listeners;

import bdv.viewer.TransformListener;
import net.imglib2.RealPoint;

/**
 * A {@code FocusModel} for Data that automatically focuses a vertex near the
 * center of the window if none is focused (on {@code getFocusedVertex()}).
 */
public class DataDisplayAutoFocus implements FocusModel< DataVertex, DataEdge >, TransformListener< ScreenTransform >
{
	private final DataGraphLayout< ?, ? > layout;

	private final FocusModel< DataVertex, DataEdge > focus;

	private final ScreenTransform screenTransform = new ScreenTransform();

	private final RealPoint centerPos = new RealPoint( 2 );

	public DataDisplayAutoFocus(
			final DataGraphLayout< ?, ? > layout,
			final FocusModel< DataVertex, DataEdge > focus )
	{
		this.layout = layout;
		this.focus = focus;
	}

	@Override
	public void focusVertex( final DataVertex vertex )
	{
		focus.focusVertex( vertex );
	}

	@Override
	public DataVertex getFocusedVertex( final DataVertex ref )
	{
		DataVertex vertex = focus.getFocusedVertex( ref );
		if ( vertex != null )
			return vertex;

		vertex = layout.getClosestActiveVertex( centerPos, ref );
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
			centerPos.setPosition( transform.getScreenWidth() / 2., 0 );
			centerPos.setPosition( transform.getScreenHeight() / 2., 1 );
		}
	}
}
