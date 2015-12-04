package net.trackmate.spatial;

/**
 * Something (e.g., a spot) that has a timepoint.
 * Timepoints are alway &ge; 0.
 *
 * <p>
 * TODO: in which package should this be?
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public interface HasTimepoint
{
	/**
	 * Get the timepoint.
	 * @return the timepoint.
	 */
	public int getTimepoint();
}
