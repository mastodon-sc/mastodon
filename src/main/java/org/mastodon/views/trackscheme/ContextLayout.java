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
package org.mastodon.views.trackscheme;

import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.views.context.Context;

/**
 * Algorithm:
 * <ol>
 * <li>Mark vertices in context with {@code mark}.
 * <li>Mark vertices attached to them with {@code ghostmark = mark - 1}.
 * <li>Use {@link LineageTreeLayoutImp#layout(java.util.Collection, int)} to layout
 * vertices that have been marked like this. (After that, all active (laid out)
 * vertices (also ghosts) will have been marked with the
 * {@link LineageTreeLayoutImp#getCurrentLayoutTimestamp()}).
 * </ol>
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class ContextLayout
{
	private final TrackSchemeGraph< ?, ? > graph;

	/**
	 * layout the {@link TrackSchemeGraph} into layout coordinates.
	 */
	private final LineageTreeLayout layout;

	private int previousMinTimepoint;

	private int previousMaxTimepoint;

	public ContextLayout(
			final TrackSchemeGraph< ?, ? > graph,
			final LineageTreeLayout layout )
	{
		this.graph = graph;
		this.layout = layout;
		this.previousMinTimepoint = -1;
		this.previousMaxTimepoint = -1;
	}

	/**
	 * Layouts part of the graph covered by {@code context}.
	 * <p>
	 * The {@link ScreenTransform#getMinY()} and
	 * {@link ScreenTransform#getMaxY()} of {@code transform} determines the
	 * time-point range to cover. If the time-point range is the same as in the
	 * previous call nothing is updated, unless {@code forceUpdate == true}.
	 *
	 * @param context
	 *            the context to layout.
	 * @param transform
	 *            the transform used to determine the time-point range.
	 * @param forceUpdate
	 *            if {@code true}, will force an update regardless of whether
	 *            the time-point is the same as in the previous call to this
	 *            method.
	 * @return {@code true} if the layout was updated.
	 */
	public boolean buildContext(
			final Context< TrackSchemeVertex > context,
			final ScreenTransform transform,
			final boolean forceUpdate )
	{
		final int minTimepoint = ( int ) transform.getMinY();
		final int maxTimepoint = ( int ) transform.getMaxY() + 1;
		if ( minTimepoint == previousMinTimepoint && maxTimepoint == previousMaxTimepoint && !forceUpdate )
			return false;

		previousMinTimepoint = minTimepoint;
		previousMaxTimepoint = maxTimepoint;

		final int ghostmark = layout.nextLayoutTimestamp();
		final int mark = layout.nextLayoutTimestamp();
		final RefList< TrackSchemeVertex > roots = RefCollections.createRefList( graph.vertices() );

		context.readLock().lock();
		try
		{
			for ( int t = minTimepoint; t <= maxTimepoint; ++t )
			{
				for ( final TrackSchemeVertex tv : context.getInsideVertices( t ) )
				{
					tv.setLayoutTimestamp( mark );
					if ( t == minTimepoint )
						roots.add( tv );
					else
						buildContextTraceParents( tv, ghostmark, minTimepoint, roots );
				}
			}
		}
		finally
		{
			context.readLock().unlock();
		}

		layout.layout( LexicographicalVertexOrder.sort( graph, roots ), mark );

		return true;
	}

	/**
	 * Follow backwards along incoming edges until
	 * <ul>
	 * <li>(A) a vertex is reached that is already marked with ghostmark or
	 * mark, or
	 * <li>(B) vertex is reached that has timepoint &lt;= minTimepoint.
	 * </ul>
	 *
	 * Mark all recursively visited vertices as ghosts. In case (B), add the
	 * final vertex to set of roots.
	 */
	private void buildContextTraceParents( final TrackSchemeVertex tv, final int ghostmark, final int minTimepoint,
			final RefList< TrackSchemeVertex > roots )
	{
		if ( tv.incomingEdges().isEmpty() )
			roots.add( tv );
		else
		{
			final TrackSchemeVertex ref = graph.vertexRef();
			for ( final TrackSchemeEdge te : tv.incomingEdges() )
			{
				final TrackSchemeVertex parent = te.getSource( ref );
				if ( parent.getLayoutTimestamp() < ghostmark )
				{
					parent.setLayoutTimestamp( ghostmark );
					if ( parent.getTimepoint() <= minTimepoint )
						roots.add( parent );
					else
						buildContextTraceParents( parent, ghostmark, minTimepoint, roots );
				}
			}
			graph.releaseRef( ref );
		}
	}
}
