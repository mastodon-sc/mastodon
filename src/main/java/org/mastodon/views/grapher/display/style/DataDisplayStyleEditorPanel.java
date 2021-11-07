/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.views.grapher.display.style;

import static org.mastodon.app.ui.settings.StyleElements.booleanElement;
import static org.mastodon.app.ui.settings.StyleElements.colorElement;
import static org.mastodon.app.ui.settings.StyleElements.linkedCheckBox;
import static org.mastodon.app.ui.settings.StyleElements.linkedColorButton;
import static org.mastodon.app.ui.settings.StyleElements.separator;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import org.mastodon.app.ui.settings.StyleElements.BooleanElement;
import org.mastodon.app.ui.settings.StyleElements.ColorElement;
import org.mastodon.app.ui.settings.StyleElements.Separator;
import org.mastodon.app.ui.settings.StyleElements.StyleElement;
import org.mastodon.app.ui.settings.StyleElements.StyleElementVisitor;

public class DataDisplayStyleEditorPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	private final JColorChooser colorChooser;

	private final List< StyleElement > styleElements;

	public DataDisplayStyleEditorPanel( final DataDisplayStyle style )
	{
		super( new BorderLayout() );
		colorChooser = new JColorChooser();
		styleElements = styleElements( style );

		style.updateListeners().add( () -> {
			styleElements.forEach( StyleElement::update );
			repaint();
		} );

		final JPanel editPanel = new JPanel( new GridBagLayout() );
		final GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets( 0, 5, 0, 5 );
		c.ipadx = 0;
		c.ipady = 0;
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;

		/*
		 * Fixed color setters.
		 */

		final int numCols = 2;

		c.gridwidth = 2;
		editPanel.add( Box.createVerticalStrut( 5 ), c );
		c.gridy++;
		c.gridwidth = 1;

		c.gridx = 0;

		styleElements.forEach( element -> element.accept(
				new StyleElementVisitor()
				{
					@Override
					public void visit( final Separator separator )
					{
						if ( c.gridx != 0 )
						{
							c.gridx = 0;
							++c.gridy;
						}
						c.gridwidth = 2;
						editPanel.add( Box.createVerticalStrut( 10 ), c );
						++c.gridy;
						c.gridwidth = 1;
					}

					@Override
					public void visit( final ColorElement element )
					{
						final JButton button = linkedColorButton( element, element.getLabel(), colorChooser );
						editPanel.add( button, c );
						if ( ++c.gridx == numCols )
						{
							c.gridx = 0;
							++c.gridy;
						}
					}

					@Override
					public void visit( final BooleanElement element )
					{
						final JCheckBox checkbox = linkedCheckBox( element, element.getLabel() );
						editPanel.add( checkbox, c );
						if ( ++c.gridx == numCols )
						{
							c.gridx = 0;
							++c.gridy;
						}
					}
				} ) );

		add( editPanel, BorderLayout.SOUTH );
	}

	private List< StyleElement > styleElements( final DataDisplayStyle style )
	{
		return Arrays.asList(
				colorElement( "edge", style::getEdgeColor, style::edgeColor ),
				colorElement( "selected edge", style::getSelectedEdgeColor, style::selectedEdgeColor ),
				colorElement( "vertex fill", style::getVertexFillColor, style::vertexFillColor ),
				colorElement( "selected vertex fill", style::getSelectedVertexFillColor, style::selectedVertexFillColor ),
				colorElement( "vertex draw", style::getVertexDrawColor, style::vertexDrawColor ),
				colorElement( "selected vertex draw", style::getSelectedVertexDrawColor, style::selectedVertexDrawColor ),
				colorElement( "simplified vertex fill", style::getSimplifiedVertexFillColor, style::simplifiedVertexFillColor ),
				colorElement( "selected simplified vertex fill", style::getSelectedSimplifiedVertexFillColor, style::selectedSimplifiedVertexFillColor ),

				separator(),

				colorElement( "vertex range", style::getVertexRangeColor, style::vertexRangeColor ),

				separator(),

				colorElement( "background", style::getBackgroundColor, style::backgroundColor ),
				colorElement( "header background", style::getHeaderBackgroundColor, style::headerBackgroundColor ),
				colorElement( "decoration", style::getDecorationColor, style::decorationColor ),
				colorElement( "header decoration", style::getHeaderDecorationColor, style::headerDecorationColor ),
				colorElement( "current timepoint", style::getCurrentTimepointColor, style::currentTimepointColor ),
				colorElement( "header current timepoint", style::getHeaderCurrentTimepointColor, style::headerCurrentTimepointColor ),

				separator(),

				booleanElement( "paint rows", style::isPaintRows, style::paintRows ),
				booleanElement( "highlight current timepoint", style::isHighlightCurrentTimepoint, style::highlightCurrentTimepoint ),
				booleanElement( "paint columns", style::isPaintColumns, style::paintColumns ),
				booleanElement( "paint header shadow", style::isPaintHeaderShadow, style::paintHeaderShadow )
		);
	}

	public static void main( final String[] args )
	{
		final DataDisplayStyle style = DataDisplayStyle.defaultStyle();
		new DataDisplayStyleEditorDialog( null, style ).setVisible( true );
	}

	public static class DataDisplayStyleEditorDialog extends JDialog
	{
		private static final long serialVersionUID = 1L;

		private final DataDisplayStyleEditorPanel stylePanel;

		public DataDisplayStyleEditorDialog( final JDialog dialog, final DataDisplayStyle style )
		{
			super( dialog, "Data graph style editor", false );

			stylePanel = new DataDisplayStyleEditorPanel( style );

			getContentPane().add( stylePanel, BorderLayout.CENTER );

			final ActionMap am = getRootPane().getActionMap();
			final InputMap im = getRootPane().getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
			final Object hideKey = new Object();
			final Action hideAction = new AbstractAction()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed( final ActionEvent e )
				{
					setVisible( false );
				}
			};
			im.put( KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ), hideKey );
			am.put( hideKey, hideAction );

			pack();
			setDefaultCloseOperation( WindowConstants.HIDE_ON_CLOSE );
		}
	}
}
