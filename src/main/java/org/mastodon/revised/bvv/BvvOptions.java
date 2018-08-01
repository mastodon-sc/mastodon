/*
 * #%L
 * BigDataViewer core classes with minimal dependencies
 * %%
 * Copyright (C) 2012 - 2015 BigDataViewer authors
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
package org.mastodon.revised.bvv;

import java.awt.event.KeyListener;
import org.scijava.ui.behaviour.KeyPressedManager;

/**
 * Optional parameters for {@link BvvPanel}.
 *
 * @author Tobias Pietzsch
 */
public class BvvOptions
{
	public final Values values = new Values();

	/**
	 * Create default {@link BvvOptions}.
	 * @return default {@link BvvOptions}.
	 */
	public static BvvOptions options()
	{
		return new BvvOptions();
	}

	/**
	 * Sets the width of {@link BvvPanel} canvas.
	 *
	 * @param w
	 *            the width.
	 * @return this instance.
	 */
	public BvvOptions width( final int w )
	{
		values.width = w;
		return this;
	}

	/**
	 * Sets the height of {@link BvvPanel} canvas.
	 *
	 * @param h
	 *            the height.
	 * @return this instance.
	 */
	public BvvOptions height( final int h )
	{
		values.height = h;
		return this;
	}

	/**
	 * Set the {@link KeyPressedManager} to share
	 * {@link KeyListener#keyPressed(java.awt.event.KeyEvent)} events with other
	 * ui-behaviour windows.
	 * <p>
	 * The goal is to make keyboard click/drag behaviours work like mouse
	 * click/drag: When a behaviour is initiated with a key press, the window
	 * under the mouse receives focus and the behaviour is handled there.
	 * </p>
	 *
	 * @param manager
	 * @return
	 */
	public BvvOptions shareKeyPressedEvents( final KeyPressedManager manager )
	{
		values.keyPressedManager = manager;
		return this;
	}

	/**
	 * Read-only {@link BvvOptions} values.
	 */
	public static class Values
	{
		private int width = 800;

		private int height = 600;

		private KeyPressedManager keyPressedManager = null;

		public BvvOptions optionsFromValues()
		{
			return new BvvOptions().
					width( width ).
					height( height ).
					shareKeyPressedEvents( keyPressedManager );
		}

		public int getWidth()
		{
			return width;
		}

		public int getHeight()
		{
			return height;
		}

		public KeyPressedManager getKeyPressedManager()
		{
			return keyPressedManager;
		}
	}
}
