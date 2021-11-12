package org.mastodon.views.grapher.display;

/**
 * Data class that specifies what to plot in a grapher view.
 * 
 * @author Jean-Yves Tinevez
 */
public class FeatureGraphConfig
{

	private final FeatureSpecPair xFeature;

	private final FeatureSpecPair yFeature;

	private final boolean keepCurrent;

	private final boolean trackOfSelection;

	private final boolean connect;

	public FeatureGraphConfig(
			final FeatureSpecPair xFeature,
			final FeatureSpecPair yFeature,
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

	public FeatureSpecPair getXFeature()
	{
		return xFeature;
	}

	public FeatureSpecPair getYFeature()
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
