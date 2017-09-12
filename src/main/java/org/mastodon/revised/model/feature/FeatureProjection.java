package org.mastodon.revised.model.feature;

/**
 * Interface for feature projections.
 * <p>
 * Feature projections are read-only projections of a {@link Feature} on a real
 * axis. A feature projection is a real, scalar function, mapped on
 * <code>double</code>s. They help plotting and representing multi-dimensional
 * features.
 * <p>
 * For instance, the velocity vector is a feature calculated for an edge.
 * Adequate feature projections for this feature would be:
 * <ul>
 * <li>the speed (velocity vector magnitude).
 * <li>the signed velocity along X axis.
 * <li>the signed velocity along Y axis.
 * <li>the signed velocity along Z axis.
 * <li><code>theta</code> the velocity vector angle measured in the OXY plane
 * with respect to X axis (0º to 360º).
 * <li><code>phi</code> the velocity vector angle measured in the OMZ plane with
 * respect to Z axis (0º to 180º).
 * </ul>
 * 
 * @param <K>
 *            the feature target (vertex, edge, ...).
 * @author Jean-Yves Tinevez
 */
public interface FeatureProjection< K >
{
	/**
	 * Returns whether the feature value is set for the specified object.
	 * 
	 * @param obj
	 *            the object.
	 * @return <code>true</code> if a value is present for the specified object,
	 *         <code>false</code> otherwise.
	 */
	public boolean isSet( K obj );

	/**
	 * Returns the value of this projection for the specified object.
	 * 
	 * @param obj
	 *            the object.
	 * @return the feature projection value as a <code>double</code>.
	 */
	public double value( K obj );

}
