package net.trackmate.revised.trackscheme;

/**
 * A laid out column.
 *
 * @author Jean-Yves Tinevez
 */
public class ScreenColumn
{
	/**
	 * The label of the column.
	 */
	public String label;

	/**
	 * The screen X coordinate of the column left border.
	 */
	public int xLeft;

	/**
	 * The width of the column, in screen units.
	 */
	public int width;

	public ScreenColumn( final String label, final int xLeft, final int width )
	{
		this.label = label;
		this.xLeft = xLeft;
		this.width = width;
	}

}
