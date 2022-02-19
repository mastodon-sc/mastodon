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
