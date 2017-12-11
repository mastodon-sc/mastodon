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
package org.mastodon.revised.trackscheme.display;

import java.awt.event.KeyListener;

import org.mastodon.revised.trackscheme.display.AbstractTrackSchemeOverlay.TrackSchemeOverlayFactory;
import org.mastodon.revised.trackscheme.display.style.DefaultTrackSchemeOverlay;
import org.mastodon.revised.ui.selection.NavigationEtiquette;
import org.scijava.ui.behaviour.KeyPressedManager;
import org.scijava.ui.behaviour.io.InputTriggerConfig;

/**
 * Optional parameters for {@link TrackSchemePanel}.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class TrackSchemeOptions
{
	public final Values values = new Values();

	/**
	 * Create default {@link TrackSchemeOptions}.
	 * @return default {@link TrackSchemeOptions}.
	 */
	public static TrackSchemeOptions options()
	{
		return new TrackSchemeOptions();
	}

	/**
	 * Sets the width of {@link TrackSchemePanel} canvas.
	 *
	 * @param w
	 *            the width.
	 * @return this instance.
	 */
	public TrackSchemeOptions width( final int w )
	{
		values.width = w;
		return this;
	}

	/**
	 * Sets the height of {@link TrackSchemePanel} canvas.
	 *
	 * @param h
	 *            the height.
	 * @return this instance.
	 */
	public TrackSchemeOptions height( final int h )
	{
		values.height = h;
		return this;
	}

	/**
	 * Sets the animation time in milliseconds.
	 *
	 * @param ms
	 *            the animation time in milliseconds.
	 * @return this instance.
	 */
	public TrackSchemeOptions animationDurationMillis( final long ms )
	{
		values.animationDurationMillis = ms;
		return this;
	}

	/**
	 * Sets the input trigger config. TODO is this config option necessary?
	 *
	 * @param c
	 *            the input trigger config.
	 * @return this instance.
	 */
	public TrackSchemeOptions inputTriggerConfig( final InputTriggerConfig c )
	{
		values.inputTriggerConfig = c;
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
	public TrackSchemeOptions shareKeyPressedEvents( final KeyPressedManager manager )
	{
		values.keyPressedManager = manager;
		return this;
	}
	/**
	 * Sets the navigation etiquette.
	 *
	 * @param navigationEtiquette
	 *            the navigation etiquette.
	 * @return this instance.
	 */
	public TrackSchemeOptions navigationEtiquette( final NavigationEtiquette navigationEtiquette )
	{
		values.navigationEtiquette = navigationEtiquette;
		return this;
	}

	/**
	 * Sets the factory used to create this TrackScheme's overlay.
	 *
	 * @param factory
	 *            the factory.
	 * @return this instance.
	 */
	public TrackSchemeOptions trackSchemeOverlayFactory( final TrackSchemeOverlayFactory factory )
	{
		values.trackSchemeOverlayFactory = factory;
		return this;
	}

	/**
	 * Read-only {@link TrackSchemeOptions} values.
	 */
	public static class Values
	{
		private int width = 800;

		private int height = 600;

		private long animationDurationMillis = 250;

		private InputTriggerConfig inputTriggerConfig = null;

		private KeyPressedManager keyPressedManager = null;

		private NavigationEtiquette navigationEtiquette = NavigationEtiquette.MINIMAL;

		private TrackSchemeOverlayFactory trackSchemeOverlayFactory = new DefaultTrackSchemeOverlay.Factory();

		public TrackSchemeOptions optionsFromValues()
		{
			return new TrackSchemeOptions().
				width( width ).
				height( height ).
				animationDurationMillis( animationDurationMillis ).
				inputTriggerConfig( inputTriggerConfig ).
				navigationEtiquette( navigationEtiquette ).
				trackSchemeOverlayFactory( trackSchemeOverlayFactory );
		}

		public int getWidth()
		{
			return width;
		}

		public int getHeight()
		{
			return height;
		}

		public long getAnimationDurationMillis()
		{
			return animationDurationMillis;
		}

		public InputTriggerConfig getInputTriggerConfig()
		{
			return inputTriggerConfig;
		}

		public KeyPressedManager getKeyPressedManager()
		{
			return keyPressedManager;
		}

		public NavigationEtiquette getNavigationEtiquette()
		{
			return navigationEtiquette;
		}

		public TrackSchemeOverlayFactory getTrackSchemeOverlayFactory()
		{
			return trackSchemeOverlayFactory;
		}
	}
}
