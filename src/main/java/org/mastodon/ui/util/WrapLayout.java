/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.ui.util;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * FlowLayout subclass that fully supports wrapping of components. Taken from
 * http://www.camick.com/java/source/WrapLayout.java
 */
public class WrapLayout extends FlowLayout
{

	private static final long serialVersionUID = 1L;

	/**
	* Constructs a new <code>WrapLayout</code> with a left
	* alignment and a default 5-unit horizontal and vertical gap.
	*/
	public WrapLayout()
	{
		super();
	}

	/**
	* Constructs a new <code>FlowLayout</code> with the specified
	* alignment and a default 5-unit horizontal and vertical gap.
	* The value of the alignment argument must be one of
	* <code>WrapLayout</code>, <code>WrapLayout</code>,
	* or <code>WrapLayout</code>.
	* @param align the alignment value
	*/
	public WrapLayout(final int align)
	{
		super(align);
	}

	/**
	* Creates a new flow layout manager with the indicated alignment
	* and the indicated horizontal and vertical gaps.
	* <p>
	* The value of the alignment argument must be one of
	* <code>WrapLayout</code>, <code>WrapLayout</code>,
	* or <code>WrapLayout</code>.
	* @param align the alignment value
	* @param hgap the horizontal gap between components
	* @param vgap the vertical gap between components
	*/
	public WrapLayout(final int align, final int hgap, final int vgap)
	{
		super(align, hgap, vgap);
	}

	/**
	* Returns the preferred dimensions for this layout given the
	* <i>visible</i> components in the specified target container.
	* @param target the component which needs to be laid out
	* @return the preferred dimensions to lay out the
	* subcomponents of the specified container
	*/
	@Override
	public Dimension preferredLayoutSize(final Container target)
	{
		return layoutSize(target, true);
	}

	/**
	* Returns the minimum dimensions needed to layout the <i>visible</i>
	* components contained in the specified target container.
	* @param target the component which needs to be laid out
	* @return the minimum dimensions to lay out the
	* subcomponents of the specified container
	*/
	@Override
	public Dimension minimumLayoutSize(final Container target)
	{
		final Dimension minimum = layoutSize(target, false);
		minimum.width -= (getHgap() + 1);
		return minimum;
	}

	/**
	* Returns the minimum or preferred dimension needed to layout the target
	* container.
	*
	* @param target target to get layout size for
	* @param preferred should preferred size be calculated
	* @return the dimension to layout the target container
	*/
	private Dimension layoutSize(final Container target, final boolean preferred)
	{
	synchronized (target.getTreeLock())
	{
		//  Each row must fit with the width allocated to the containter.
		//  When the container width = 0, the preferred width of the container
		//  has not yet been calculated so lets ask for the maximum.

		int targetWidth = target.getSize().width;
		Container container = target;

		while (container.getSize().width == 0 && container.getParent() != null)
		{
			container = container.getParent();
		}

		targetWidth = container.getSize().width;

		if (targetWidth == 0)
			targetWidth = Integer.MAX_VALUE;

		final int hgap = getHgap();
		final int vgap = getVgap();
		final Insets insets = target.getInsets();
		final int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2);
		final int maxWidth = targetWidth - horizontalInsetsAndGap;

		//  Fit components into the allowed width

		final Dimension dim = new Dimension(0, 0);
		int rowWidth = 0;
		int rowHeight = 0;

		final int nmembers = target.getComponentCount();

		for (int i = 0; i < nmembers; i++)
		{
			final Component m = target.getComponent(i);

			if (m.isVisible())
			{
				final Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();

				//  Can't add the component to current row. Start a new row.

				if (rowWidth + d.width > maxWidth)
				{
					addRow(dim, rowWidth, rowHeight);
					rowWidth = 0;
					rowHeight = 0;
				}

				//  Add a horizontal gap for all components after the first

				if (rowWidth != 0)
				{
					rowWidth += hgap;
				}

				rowWidth += d.width;
				rowHeight = Math.max(rowHeight, d.height);
			}
		}

		addRow(dim, rowWidth, rowHeight);

		dim.width += horizontalInsetsAndGap;
		dim.height += insets.top + insets.bottom + vgap * 2;

		//	When using a scroll pane or the DecoratedLookAndFeel we need to
		//  make sure the preferred size is less than the size of the
		//  target containter so shrinking the container size works
		//  correctly. Removing the horizontal gap is an easy way to do this.

		final Container scrollPane = SwingUtilities.getAncestorOfClass(JScrollPane.class, target);

		if (scrollPane != null && target.isValid())
		{
			dim.width -= (hgap + 1);
		}

		return dim;
	}
	}

	/*
	 *  A new row has been completed. Use the dimensions of this row
	 *  to update the preferred size for the container.
	 *
	 *  @param dim update the width and height when appropriate
	 *  @param rowWidth the width of the row to add
	 *  @param rowHeight the height of the row to add
	 */
	private void addRow(final Dimension dim, final int rowWidth, final int rowHeight)
	{
		dim.width = Math.max(dim.width, rowWidth);

		if (dim.height > 0)
		{
			dim.height += getVgap();
		}

		dim.height += rowHeight;
	}
}
