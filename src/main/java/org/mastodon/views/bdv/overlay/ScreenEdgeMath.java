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
package org.mastodon.views.bdv.overlay;

import net.imglib2.realtransform.AffineTransform3D;

/**
 * Computations to extract information from {@link OverlayEdge} and current
 * viewer transform for selecting.
 * <p>
 * One instance is used repeatedly for multiple {@link OverlayEdge OverlayEdges}
 * as follows:
 * <ol>
 * <li>Call {@link #init(OverlayEdge, AffineTransform3D)} with the
 * {@link OverlayEdge} and the current viewer transform. This resets internal
 * state.
 * <li>Call any of the getters. This triggers all necessary computations to
 * provide the requested value. Intermediate results are cached.
 * </ol>
 *
 * @param <V>
 *            OverlayVertex type.
 * @param <E>
 *            OverlayEdge type.
 *
 * @author Tobias Pietzsch
 */
public class ScreenEdgeMath< V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > >
{
	private final V ref1;

	private final V ref2;

	private final ScreenVertexMath svmSource = new ScreenVertexMath();

	private final ScreenVertexMath svmTarget = new ScreenVertexMath();

	/**
	 * min of edge bounding box in viewer coordinate system.
	 */
	private final double[] min = new double[ 3 ];

	/**
	 * max of edge bounding box in viewer coordinate system.
	 */
	private final double[] max = new double[ 3 ];

	/**
	 * edge position in viewer coordinate system. (center, between the
	 * source and target position)
	 */
	private final double[] c = new double[ 3 ];

	public ScreenEdgeMath( final V tempRef1, final V tempRef2 )
	{
		this.ref1 = tempRef1;
		this.ref2 = tempRef2;
	}

	public void init( final E edge, final AffineTransform3D viewerTransform )
	{
		svmSource.init( edge.getSource( ref1 ), viewerTransform );
		svmTarget.init( edge.getTarget( ref2 ), viewerTransform );
		final double[] vPosSource = svmSource.getViewPos();
		final double[] vPosTarget = svmTarget.getViewPos();
		for ( int d = 0; d < 3; ++d )
		{
			if ( vPosSource[ d ] < vPosTarget[ d ] )
			{
				min[ d ] = vPosSource[ d ];
				max[ d ] = vPosTarget[ d ];
			}
			else
			{
				max[ d ] = vPosSource[ d ];
				min[ d ] = vPosTarget[ d ];
			}
			c[ d ] = 0.5 * ( min[ d ] + max[ d ] );
		}
	}

	/**
	 * Get min of edge bounding box in viewer coordinate system.
	 *
	 * @return min of edge bounding box in viewer coordinate system.
	 */
	public double[] getViewMin()
	{
		return min;
	}

	/**
	 * Get max of edge bounding box in viewer coordinate system.
	 *
	 * @return max of edge bounding box in viewer coordinate system.
	 */
	public double[] getViewMax()
	{
		return max;
	}

	/**
	 * Get edge position in viewer coordinate system.
	 *
	 * @return edge position in viewer coordinate system.
	 */
	public double[] getViewPos()
	{
		return c;
	}
}
