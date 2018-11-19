package org.mastodon.revised.ui.coloring.feature;

public interface FeatureRangeCalculator
{
	/**
	 * Returns the current min and max for the specified feature projection.
	 *
	 * @param projection
	 *            the feature projection id.
	 * @return the min and max as a new <code>double[]</code> 2-elements array,
	 *         or <code>null</code> if the calculation could not proceed because
	 *         the feature was not found the specified class, the projection
	 *         does not exist for the feature or there are no objects to compute
	 *         feature on.
	 */
	public double[] computeMinMax( final FeatureProjectionId projection );
}
