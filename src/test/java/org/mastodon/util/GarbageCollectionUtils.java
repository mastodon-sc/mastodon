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
package org.mastodon.util;

import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class GarbageCollectionUtils
{

	private GarbageCollectionUtils()
	{
		// prevent from instantiation
	}

	/**
	 * Reliable way to trigger a garbage collection.
	 * <p>
	 * (More reliably than {@code System.gc()}).
	 */
	public static void triggerGarbageCollection()
	{
		if ( !GraphicsEnvironment.isHeadless() )
			workAroundSwingMemoryLeak();

		// NB: The garbage collection needs to be triggered twice
		// in order to cause a full garbage collection.
		triggerGarbageCollectionOnce();
		triggerGarbageCollectionOnce();
	}

	private static void triggerGarbageCollectionOnce()
	{
		// allocate more and ore memory until OutOfMemoryError is thrown.
		// (This will trigger GC.)
		try
		{
			final List< byte[] > memory = new ArrayList<>();
			while ( true )
				memory.add( new byte[ 100 * 1024 * 1024 ] ); // add 100MB
		}
		catch ( final OutOfMemoryError e )
		{
			// continue
		}
	}

	/**
	 * The Swing UI framework easily causes memory leaks. This is a workaround
	 * for one those memory leaks.
	 * <p>
	 * The BufferStrategyPaintManager would keep a reference to the latest
	 * painted window. So in order to allow other windows to be garbage
	 * collected, we paint a dummy window.
	 * <p>
	 * (The problem occurred on Ubuntu with OpenJDK 11.)
	 */
	private static void workAroundSwingMemoryLeak()
	{
		final JFrame dummy = new JFrame()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void paint( final Graphics g )
			{
				super.paint( g );
				SwingUtilities.invokeLater( this::dispose );
			}
		};
		dummy.setSize( 100, 100 );
		dummy.setVisible( true );
	}
}
