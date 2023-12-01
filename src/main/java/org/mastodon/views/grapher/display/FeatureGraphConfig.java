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
package org.mastodon.views.grapher.display;

/**
 * Data class that specifies what to plot in a grapher view.
 * 
 * @author Jean-Yves Tinevez
 */
public class FeatureGraphConfig
{

	public enum GraphDataItemsSource
	{
		SELECTION, TRACK_OF_SELECTION, KEEP_CURRENT, CONTEXT;
	}

	private final FeatureSpecPair xFeature;

	private final FeatureSpecPair yFeature;

	private final GraphDataItemsSource itemSource;

	private final boolean connect;

	public FeatureGraphConfig(
			final FeatureSpecPair xFeature,
			final FeatureSpecPair yFeature,
			final GraphDataItemsSource itemSource,
			final boolean connect )
	{
		this.xFeature = xFeature;
		this.yFeature = yFeature;
		this.itemSource = itemSource;
		this.connect = connect;
	}

	public FeatureSpecPair getXFeature()
	{
		return xFeature;
	}

	public FeatureSpecPair getYFeature()
	{
		return yFeature;
	}

	public GraphDataItemsSource itemSource()
	{
		return itemSource;
	}

	public boolean drawConnected()
	{
		return connect;
	}

	@Override
	public String toString()
	{
		return super.toString() +
				"\n - xFeature: " + xFeature +
				"\n - yFeature: " + yFeature +
				"\n - item source: " + itemSource +
				"\n - show edge: " + connect;
	}
}
