package org.mastodon.views.grapher.display;

import org.mastodon.views.grapher.SpecPair;

public class GraphConfig
{

	private final SpecPair xFeature;

	private final SpecPair yFeature;

	private final boolean keepCurrent;

	private final boolean trackOfSelection;

	private final boolean connect;

	public GraphConfig(
			final SpecPair xFeature,
			final SpecPair yFeature,
			final boolean keepCurrent,
			final boolean trackOfSelection,
			final boolean connect )
	{
		this.xFeature = xFeature;
		this.yFeature = yFeature;
		this.keepCurrent = keepCurrent;
		this.trackOfSelection = trackOfSelection;
		this.connect = connect;
	}

	public SpecPair getXFeature()
	{
		return xFeature;
	}

	public SpecPair getYFeature()
	{
		return yFeature;
	}

	public boolean graphTrackOfSelection()
	{
		return trackOfSelection;
	}

	public boolean keepCurrent()
	{
		return keepCurrent;
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
				"\n - keep current: " + keepCurrent +
				"\n - track of selection: " + trackOfSelection +
				"\n - do connect: " + connect;
	}
}
