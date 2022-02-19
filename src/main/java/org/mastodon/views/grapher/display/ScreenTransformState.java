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
package org.mastodon.views.grapher.display;

import org.mastodon.views.grapher.datagraph.ScreenTransform;
import org.scijava.listeners.Listeners;

import bdv.viewer.TransformListener;

public class ScreenTransformState
{
	private final Listeners.List< TransformListener< ScreenTransform > > listeners;

	private final ScreenTransform transform;

	public ScreenTransformState( final ScreenTransform transform )
	{
		this.transform = new ScreenTransform( transform );
		listeners = new Listeners.List<>();
	}

	public ScreenTransformState()
	{
		this( new ScreenTransform() );
	}

	/**
	 * Get the current transform.
	 *
	 * @param transform
	 *     is set to the current transform
	 */
	public synchronized void get( final ScreenTransform transform )
	{
		transform.set( this.transform );
	}

	/**
	 * Get the current transform.
	 *
	 * @return a copy of the current transform
	 */
	public synchronized ScreenTransform get()
	{
		return new ScreenTransform( this.transform );
	}

	/**
	 * Set the transform.
	 * 
	 * @param transform
	 *            the transform to copy from.
	 */
	public synchronized void set( final ScreenTransform transform )
	{
		if ( !this.transform.equals( transform ) )
		{
			this.transform.set( transform );
			listeners.list.forEach( l -> l.transformChanged( this.transform ) );
		}
	}

	/**
	 * {@code TransformListener<ScreenTransform>} can be added/removed here.
	 * 
	 * @return the listeners.
	 */
	public Listeners< TransformListener< ScreenTransform > > listeners()
	{
		return listeners;
	}
}
