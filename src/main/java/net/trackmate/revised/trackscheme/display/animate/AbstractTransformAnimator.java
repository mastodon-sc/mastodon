package net.trackmate.revised.trackscheme.display.animate;


/**
 * Mother abstract class for animators that animate the current view by
 * modifying the viewer transform. The time unit for animation duration, start
 * time and current time is not specified, or example you can use <b>ms</b>
 * obtained from {@link System#currentTimeMillis()} or a frame number when
 * rendering movies.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 * @author Jean-Yves Tinevez &lt;jeanyves.tinevez@gmail.com&gt;
 * @param <A>
 *            the type of the transform animated.
 */
public abstract class AbstractTransformAnimator< A > extends AbstractAnimator
{
	/**
	 * Create new animator with the given duration. The animation will start
	 * with the first call to {@link #setTime(long)}.
	 *
	 * @param duration
	 *            animation duration (in time units)
	 */
	public AbstractTransformAnimator( final long duration )
	{
		super( duration );
	}

	public A getCurrent( final long time )
	{
		setTime( time );
		return get( ratioComplete() );
	}

	/**
	 * Returns a transform for the specified completion factor. For values below
	 * 0, that starting transform should be returned. For values larger than 1,
	 * the final transform should be returned. Values below 0 and 1 should
	 * interpolate between the two, depending on the concrete animation
	 * implementation.
	 *
	 * @param t
	 *            the completion factor, ranging from 0 to 1.
	 * @return the viewer transform for the specified completion factor.
	 */
	protected abstract A get( double t );
}
