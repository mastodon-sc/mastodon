/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.views.trackscheme.display.animate;

/**
 * Mother abstract class for animators that can animate the current view. The
 * time unit for animation duration, start time and current time is not
 * specified, or example you can use <b>ms</b> obtained from
 * {@link System#currentTimeMillis()} or a frame number when rendering movies.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 * @author Jean-Yves Tinevez &lt;jeanyves.tinevez@gmail.com&gt;
 */
public class AbstractAnimator
{
	/** Expected duration length of the animation (in time units). */
	private long duration;

	/** Start time of the animation (in time units). */
	private long startTime;

	/** Boolean flag stating whether the animation started. */
	private boolean started;

	/**
	 * Completion factor, ranging from 0 to 1. If &gt;= 1, the animation is
	 * done.
	 */
	private double complete;

	/**
	 * Create new animator with the given duration. The animation will start
	 * with the first call to {@link #setTime(long)}.
	 *
	 * @param duration
	 *            animation duration (in time units)
	 */
	public AbstractAnimator( final long duration )
	{
		this.duration = duration;
		started = false;
		complete = 0;
	}

	protected void reset( final long duration )
	{
		this.duration = duration;
		started = false;
		complete = 0;
	}

	/**
	 * Sets the current time for the animation. The first call starts the
	 * animation.
	 *
	 * @param time
	 *            current time (in time units)
	 */
	public void setTime( final long time )
	{
		if ( !started )
		{
			started = true;
			startTime = time;
		}

		if ( duration <= 0 )
		{
			complete = 1;
		}
		else
		{
			complete = ( time - startTime ) / ( double ) duration;
			if ( complete >= 1 )
				complete = 1;
		}
	}

	/**
	 * Returns true if the animation is completed at the {@link #setTime(long)
	 * current time}.
	 *
	 * @return true if the animation completed.
	 */
	public boolean isComplete()
	{
		return complete == 1;
	}

	/**
	 * Returns the completion ratio. It is a double ranging from 0 to 1, 0
	 * indicating that the animation just started, 1 indicating that it
	 * completed.
	 *
	 * @return the completion ratio.
	 */
	public double ratioComplete()
	{
		return complete;
	}
}
