package org.mastodon.revised.model.tag;

import java.awt.Color;
import java.io.Serializable;

/**
 * Serializable class that just stores a mutable label and a color.
 *
 * @author Jean-Yves Tinevez
 *
 */
public class Tag implements Serializable
{

	private static final long serialVersionUID = 926673562042881490L;

	private String label;

	private Color color;

	Tag( final String label, final Color color )
	{
		this.label = label;
		this.color = color;
	}

	public void setColor( final Color color )
	{
		this.color = color;
	}

	public void setLabel( final String label )
	{
		this.label = label;
	}

	public Color color()
	{
		return color;
	}

	public String label()
	{
		return label;
	}
}