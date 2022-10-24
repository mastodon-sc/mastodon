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

import java.util.Iterator;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.branch.BranchGraph;
import org.mastodon.model.FocusModel;
import org.mastodon.model.HasLabel;
import org.mastodon.undo.UndoPointMarker;
import org.mastodon.views.trackscheme.TrackSchemeEdge;
import org.mastodon.views.trackscheme.TrackSchemeGraph;
import org.mastodon.views.trackscheme.TrackSchemeVertex;
import org.scijava.ui.behaviour.util.Actions;

import net.imglib2.util.Cast;

public class BranchTrackSchemeEditLabelAction
{

	private static final String[] EDIT_FOCUS_LABEL_KEYS = new String[] { "ENTER" };

	public static < BV extends Vertex< BE >, BE extends Edge< BV >, V extends Vertex< E > & HasLabel, E extends Edge< V > > void install(
			final Actions actions,
			final TrackSchemePanel panel,
			final FocusModel< TrackSchemeVertex, TrackSchemeEdge > focus,
			final UndoPointMarker undoPointMarker,
			final BranchGraph< BV, BE, V, E > branchGraph )
	{
		final BranchTrackSchemeEditLabelActionImp< BV, BE, V, E > editBranchVerticesLabelAction =
				new BranchTrackSchemeEditLabelActionImp<>( focus, undoPointMarker, panel, branchGraph );
		panel.getScreenTransform().listeners().add( editBranchVerticesLabelAction );
		panel.getOffsetHeaders().listeners().add( editBranchVerticesLabelAction );
		actions.namedAction( editBranchVerticesLabelAction, EDIT_FOCUS_LABEL_KEYS );
	}

	private static class BranchTrackSchemeEditLabelActionImp< BV extends Vertex< BE >, BE extends Edge< BV >, V extends Vertex< E > & HasLabel, E extends Edge< V > >
			extends EditFocusVertexLabelAction
	{

		private static final long serialVersionUID = 1L;

		private final TrackSchemeGraph< BV, BE > trackSchemeGraph;

		private final BranchGraph< BV, BE, V, E > branchGraph;

		protected BranchTrackSchemeEditLabelActionImp(
				final FocusModel< TrackSchemeVertex, TrackSchemeEdge > focus,
				final UndoPointMarker undoPointMarker,
				final TrackSchemePanel panel,
				final BranchGraph< BV, BE, V, E > branchGraph )
		{
			super( focus, undoPointMarker, panel );
			this.branchGraph = branchGraph;
			this.trackSchemeGraph = Cast.unchecked( panel.getGraph() );
		}

		@Override
		protected void changeLabel( final TrackSchemeVertex vertex, final String label )
		{
			final BV branchSpot = trackSchemeGraph.getVertexMap().getLeft( vertex );
			final Iterator< V > it = branchGraph.vertexBranchIterator( branchSpot );
			while ( it.hasNext() )
			{
				final V v = it.next();
				v.setLabel( label );
			}
			undoPointMarker.setUndoPoint();
		}
	}
}
