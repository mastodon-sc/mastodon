package net.trackmate.trackscheme.animate;

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
	private final long duration;

	/** Start time of the animation (in time units). */
	private long startTime;

	/** Boolean flag stating whether the animation started. */
	private boolean started;

	/** Completion factor, ranging from 0 to 1. If >= 1, the animation is done. */
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

		complete = ( time - startTime ) / ( double ) duration;
		if ( complete >= 1 )
			complete = 1;
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
