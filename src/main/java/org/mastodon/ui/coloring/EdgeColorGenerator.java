/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.ui.coloring;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;

/**
 * Interface that can associate colors to a graph's edges.
 *
 * @param <V>
 *            the type of the vertices.
 * @param <E>
 *            the type of the edges.
 *
 * @author Jean-Yves Tinevez.
 * @author Tobias Pietzsch
 */
public interface EdgeColorGenerator< V extends Vertex< E >, E extends Edge< V > >
{
	/**
	 * Gets the color for the specified edge (ARGB bytes packed into
	 * {@code int}).
	 * <p>
	 * The {@code source} and {@code target} vertices of the edge are provided
	 * for convenience, in case the edge coloring is determined by its vertices.
	 * <p>
	 * The special value {@code 0x00000000} is used to denote that no color is
	 * assigned to the edge (which should be drawn in default color then).
	 *
	 * @param edge
	 *            the edge
	 * @param source
	 *            the source vertex of the edge
	 * @param target
	 *            the target vertex of the edge
	 * @return a color (as ARGB bytes packed into {@code int}).
	 */
	int color( E edge, V source, V target );
}
