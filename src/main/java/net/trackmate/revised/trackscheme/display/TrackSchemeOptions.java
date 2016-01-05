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
package net.trackmate.revised.trackscheme.display;

import bdv.behaviour.io.InputTriggerConfig;
import net.imglib2.ui.TransformEventHandlerFactory;
import net.trackmate.revised.trackscheme.ScreenTransform;

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
	 * Set width of {@link TrackSchemePanel} canvas.
	 */
	public TrackSchemeOptions width( final int w )
	{
		values.width = w;
		return this;
	}

	/**
	 * Set height of {@link TrackSchemePanel} canvas.
	 */
	public TrackSchemeOptions height( final int h )
	{
		values.height = h;
		return this;
	}

	/**
	 * TODO
	 */
	public TrackSchemeOptions transformEventHandlerFactory( final TransformEventHandlerFactory< ScreenTransform > f )
	{
		values.transformEventHandlerFactory = f;
		return this;
	}

	/**
	 * TODO
	 */
	public TrackSchemeOptions animationDurationMillis( final long ms )
	{
		values.animationDurationMillis = ms;
		return this;
	}

	/**
	 * TODO javadoc
	 * TODO is this config option necessary?
	 *
	 * @param c
	 * @return
	 */
	public TrackSchemeOptions inputTriggerConfig( final InputTriggerConfig c )
	{
		values.inputTriggerConfig = c;
		return this;
	}

	/**
	 * Read-only {@link TrackSchemeOptions} values.
	 */
	public static class Values
	{
		private int width = 800;

		private int height = 600;

		private TransformEventHandlerFactory< ScreenTransform > transformEventHandlerFactory = InertialScreenTransformEventHandler.factory( new InputTriggerConfig() );

		private long animationDurationMillis = 250;

		private InputTriggerConfig inputTriggerConfig = null;

		public TrackSchemeOptions optionsFromValues()
		{
			return new TrackSchemeOptions().
				width( width ).
				height( height ).
				transformEventHandlerFactory( transformEventHandlerFactory ).
				animationDurationMillis( animationDurationMillis ).
				inputTriggerConfig( inputTriggerConfig );
		}

		public int getWidth()
		{
			return width;
		}

		public int getHeight()
		{
			return height;
		}

		public TransformEventHandlerFactory< ScreenTransform > getTransformEventHandlerFactory()
		{
			return transformEventHandlerFactory;
		}

		public long getAnimationDurationMillis()
		{
			return animationDurationMillis;
		}

		public InputTriggerConfig getInputTriggerConfig()
		{
			return inputTriggerConfig;
		}
	}
}
