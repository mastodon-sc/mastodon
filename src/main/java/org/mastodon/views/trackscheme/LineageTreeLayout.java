/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2023 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.views.trackscheme;

import java.util.Collection;

import org.mastodon.collection.RefSet;
import org.mastodon.model.FadingModel;
import org.mastodon.model.RootsModel;
import org.mastodon.model.SelectionModel;
import org.mastodon.ui.coloring.GraphColorGenerator;
import org.scijava.listeners.Listeners;

import net.imglib2.RealLocalizable;

public interface LineageTreeLayout
{
	void layout();

	void layout( Collection< TrackSchemeVertex > layoutRoots );

	void layout( Collection< TrackSchemeVertex > layoutRoots, int mark );

	double getCurrentLayoutMinX();

	double getCurrentLayoutMaxX();

	int getCurrentLayoutTimestamp();

	int nextLayoutTimestamp();

	void cropAndScale( ScreenTransform transform, ScreenEntities screenEntities, int decorationsOffsetX,
			int decorationsOffsetY );

	TrackSchemeVertex getClosestActiveVertex( RealLocalizable layoutPos, double aspectRatioXtoY,
			TrackSchemeVertex ref );

	TrackSchemeVertex getClosestActiveVertexWithin( double lx1, double ly1, double lx2, double ly2,
			double aspectRatioXtoY, TrackSchemeVertex ref );

	RefSet< TrackSchemeVertex > getActiveVerticesWithin( double lx1, double ly1, double lx2, double ly2 );

	TrackSchemeVertex getFirstActiveChild( TrackSchemeVertex vertex, TrackSchemeVertex ref );

	TrackSchemeVertex getFirstActiveParent( TrackSchemeVertex vertex, TrackSchemeVertex ref );

	TrackSchemeVertex getLeftSibling( TrackSchemeVertex vertex, TrackSchemeVertex ref );

	TrackSchemeVertex getRightSibling( TrackSchemeVertex vertex, TrackSchemeVertex ref );

	Listeners< LayoutListener > layoutListeners();

	interface LayoutListener
	{

		/**
		 * Notifies after the layout has been done.
		 *
		 * @param layout
		 *            the layout.
		 */
		public void layoutChanged( LineageTreeLayout layout );
	}

	interface LineageTreeLayoutFactory
	{
		LineageTreeLayout create( final RootsModel< TrackSchemeVertex > rootsModel,
				final TrackSchemeGraph< ?, ? > graph,
				final SelectionModel< TrackSchemeVertex, TrackSchemeEdge > selection,
				final GraphColorGenerator< TrackSchemeVertex, TrackSchemeEdge > colorGenerator,
				final FadingModel< TrackSchemeVertex, TrackSchemeEdge > fadingModel );
	}
}
