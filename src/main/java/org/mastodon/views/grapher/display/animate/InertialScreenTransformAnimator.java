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
package org.mastodon.views.grapher.display.animate;

import org.mastodon.views.grapher.datagraph.ScreenTransform;
import org.mastodon.views.trackscheme.display.animate.AbstractTransformAnimator;

public class InertialScreenTransformAnimator extends AbstractTransformAnimator< ScreenTransform >
{
	private final ScreenTransform t0;

	private final ScreenTransform t1;

	private final ScreenTransform transform = new ScreenTransform();

	private static final double s = 3.;

	private final double tau;

	/**
	 * Extrapolate {@link ScreenTransform} that changed from {@code t0} to
	 * {@code t1} in time interval {@code dt}, beyond {@code t1}.
	 *
	 * @param t0
	 *            transform at begin of interval.
	 * @param t1
	 *            transform at end of interval.
	 * @param dt
	 *            duration of interval.
	 * @param duration
	 *            duration of inertial animation (same units as {@code dt}).
	 */
	public InertialScreenTransformAnimator( final ScreenTransform t0, final ScreenTransform t1, final long dt,
			final long duration )
	{
		super( duration );
		this.t0 = t0.copy();
		this.t1 = t1.copy();
		this.tau = duration / ( s * dt );
	}

	@Override
	protected ScreenTransform get( double t )
	{
		if ( t <= 0 )
			return t1;

		if ( t > 1 )
			t = 1;

		final double inc = tau * ( 1 - Math.exp( -t * s ) );
		transform.interpolate( t0, t1, 1 + inc );
		return transform;
	}
}
