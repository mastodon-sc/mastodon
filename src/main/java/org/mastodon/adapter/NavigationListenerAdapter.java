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
package org.mastodon.adapter;

import org.mastodon.model.NavigationListener;

public class NavigationListenerAdapter< V, E, WV, WE >
		implements NavigationListener< V, E >
{
	private final NavigationListener< WV, WE > listener;

	private final RefBimap< V, WV > vertexMap;

	private final RefBimap< E, WE > edgeMap;

	public NavigationListenerAdapter(
			final NavigationListener< WV, WE > listener,
			final RefBimap< V, WV > vertexMap,
			final RefBimap< E, WE > edgeMap )
	{
		this.listener = listener;
		this.vertexMap = vertexMap;
		this.edgeMap = edgeMap;
	}

	@Override
	public void navigateToVertex( final V vertex )
	{
		final WV ref = vertexMap.reusableRightRef();
		listener.navigateToVertex( vertexMap.getRight( vertex, ref ) );
		vertexMap.releaseRef( ref );
	}

	@Override
	public void navigateToEdge( final E edge )
	{
		final WE ref = edgeMap.reusableRightRef();
		listener.navigateToEdge( edgeMap.getRight( edge, ref ) );
		edgeMap.releaseRef( ref );
	}

	@Override
	public int hashCode()
	{
		return listener.hashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		return ( obj instanceof NavigationListenerAdapter )
				? listener.equals( ( ( NavigationListenerAdapter< ?, ?, ?, ? > ) obj ).listener )
				: false;
	}
}
