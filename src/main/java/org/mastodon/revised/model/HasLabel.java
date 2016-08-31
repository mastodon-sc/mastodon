package org.mastodon.revised.model;

/**
* Something (e.g., a spot) that has a label.
*
* <p>
* TODO: in which package should this be?
*
* @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
*/
public interface HasLabel
{
	/**
	 * Get the label.
	 *
	 * @return the label.
	 */
	public String getLabel();

	/**
	 * Set the label.
	 *
	 * @param label
	 *            the label.
	 */
	public void setLabel( String label );
}
