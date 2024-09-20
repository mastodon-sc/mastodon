package org.mastodon.model;

/**
 * Interface for objects that have a 3x3 covariance matrix, that can be set and
 * accessed.
 */
public interface HasCovariance
{

	/**
	 * Stores the covariance matrix in the specified <code>double[][]</code>
	 * arrays.
	 * 
	 * @param mat
	 *            a (at least) 3x3 <code>double[][]</code> array in which the
	 *            covariance matrix will be written.
	 */
	public void getCovariance( final double[][] mat );

	/**
	 * Sets the covariance matrix of this objects, reading values from the
	 * specified <code>double[][]</code> arrays.
	 * 
	 * @param mat
	 *            a (at least) 3x3 <code>double[][]</code> array from which
	 *            covariance values will be read.
	 */
	public void setCovariance( final double[][] mat );

}
