package org.mastodon.revised.trackscheme.display.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import org.mastodon.revised.trackscheme.display.style.TrackSchemeStyle;
import org.mastodon.revised.trackscheme.display.style.TrackSchemeStyle.UpdateListener;

public class TrackSchemeStyleEditorPanel extends JPanel implements UpdateListener
{
	private static final long serialVersionUID = 1L;

	private final JColorChooser colorChooser;

	private final TrackSchemeStyle style;

	private final List< StyleElement > styleElements;

	public TrackSchemeStyleEditorPanel( final TrackSchemeStyle style )
	{
		super( new GridBagLayout() );
		this.style = style;
		style.addUpdateListener( this );
		colorChooser = new JColorChooser();

		final GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets( 0, 5, 0, 5 );
		c.ipadx = 0;
		c.ipady = 0;
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;

		styleElements = styleElements( style );

		/*
		 * Fixed color setters.
		 */

		final int numCols = 2;

		c.gridwidth = 2;
		add( Box.createVerticalStrut( 5 ), c );
		c.gridy++;
		c.gridwidth = 1;

		c.gridx = 0;
		for ( final StyleElement element : styleElements )
		{
			if ( element.fillsRow() )
			{
				if ( c.gridx != 0 )
				{
					c.gridx = 0;
					++c.gridy;
				}
				c.gridwidth = 2;
				add( element.getComponent(), c );
				++c.gridy;
				c.gridwidth = 1;
			}
			else
			{
				add( element.getComponent(), c );
				if ( ++c.gridx == numCols )
				{
					c.gridx = 0;
					++c.gridy;
				}
			}
		}
	}

	@Override
	public void trackSchemeStyleChanged()
	{
		styleElements.forEach( StyleElement::update );
		repaint();
	}

	private interface StyleElement
	{
		public Component getComponent();

		public default void update()
		{}

		public default boolean fillsRow()
		{
			return false;
		}
	}

	private class Separator implements StyleElement
	{
		@Override
		public Component getComponent()
		{
			return Box.createVerticalStrut( 10 );
		}

		@Override
		public boolean fillsRow()
		{
			return true;
		}
	}

	private Separator separator()
	{
		return new Separator();
	}

	private abstract class ColorSetter implements StyleElement
	{
		private final String label;

		private final ColorIcon icon;

		private JButton button;

		public ColorSetter( final String label )
		{
			this.label = label;

			icon = new ColorIcon( getColor() );
			button = new JButton( label, icon );
			button.setOpaque( false );
			button.setContentAreaFilled( false );
			button.setBorderPainted( false );
			button.setFont( new JButton().getFont() );
			button.setMargin( new Insets( 0, 0, 0, 0 ) );
			button.setBorder( new EmptyBorder( 2, 5, 2, 2 ) );
			button.setHorizontalAlignment( SwingConstants.LEFT );
			button.addActionListener( new ActionListener()
			{
				@Override
				public void actionPerformed( final ActionEvent e )
				{
					colorChooser.setColor( getColor() );
					final JDialog d = JColorChooser.createDialog( button, "Choose a color", true, colorChooser, new ActionListener()
					{
						@Override
						public void actionPerformed( final ActionEvent arg0 )
						{
							final Color c = colorChooser.getColor();
							if ( c != null )
							{
								icon.setColor( c );
								button.repaint();
								setColor( c );
							}
						}
					}, null );
					d.setVisible( true );
				}
			} );
		}

		public String getLabel()
		{
			return label;
		}

		public abstract Color getColor();

		public abstract void setColor( Color c );

		@Override
		public JButton getComponent()
		{
			return button;
		}

		@Override
		public void update()
		{
//			if ( ! icon.color.equals( getColor() ) )
				icon.setColor( getColor() );
		}
	}

	private ColorSetter colorSetter( String label, Supplier< Color > get, Consumer< Color > set )
	{
		return new ColorSetter( label )
		{
			@Override
			public Color getColor()
			{
				return get.get();
			}

			@Override
			public void setColor( final Color c )
			{
				set.accept( c );
			}
		};
	}

	private abstract class BooleanSetter implements StyleElement
	{
		private final String label;

		private JCheckBox checkbox;

		public BooleanSetter( final String label )
		{
			this.label = label;

			checkbox = new JCheckBox( label, get() );
			checkbox.addActionListener( ( e ) -> set( checkbox.isSelected() ) );
		}

		public String getLabel()
		{
			return label;
		}

		public abstract boolean get();

		public abstract void set( boolean b );

		@Override
		public JCheckBox getComponent()
		{
			return checkbox;
		}

		@Override
		public void update()
		{
			if ( get() != checkbox.isSelected() )
				checkbox.setSelected( get() );
		}
	}

	private BooleanSetter booleanSetter( String label, BooleanSupplier get, Consumer< Boolean > set )
	{
		return new BooleanSetter( label )
		{
			@Override
			public boolean get()
			{
				return get.getAsBoolean();
			}

			@Override
			public void set( final boolean b )
			{
				set.accept( b );
			}
		};
	}

	private List< StyleElement > styleElements( final TrackSchemeStyle style )
	{
		return Arrays.asList(
				colorSetter( "edge", style::getEdgeColor, style::edgeColor ),
				colorSetter( "selected edge", style::getSelectedEdgeColor, style::selectedEdgeColor ),
				colorSetter( "vertex fill", style::getVertexFillColor, style::vertexFillColor ),
				colorSetter( "selected vertex fill", style::getSelectedVertexFillColor, style::selectedVertexFillColor ),
				colorSetter( "vertex draw", style::getVertexDrawColor, style::vertexDrawColor ),
				colorSetter( "selected vertex draw", style::getSelectedVertexDrawColor, style::selectedVertexDrawColor ),
				colorSetter( "simplified vertex fill", style::getSimplifiedVertexFillColor, style::simplifiedVertexFillColor ),
				colorSetter( "selected simplified vertex fill", style::getSelectedSimplifiedVertexFillColor, style::selectedSimplifiedVertexFillColor ),

				separator(),

				colorSetter( "vertex range", style::getVertexRangeColor, style::vertexRangeColor ),

				separator(),

				colorSetter( "background", style::getBackgroundColor, style::backgroundColor ),
				colorSetter( "header background", style::getHeaderBackgroundColor, style::headerBackgroundColor ),
				colorSetter( "decoration", style::getDecorationColor, style::decorationColor ),
				colorSetter( "header decoration", style::getHeaderDecorationColor, style::headerDecorationColor ),
				colorSetter( "current timepoint", style::getCurrentTimepointColor, style::currentTimepointColor ),
				colorSetter( "header current timepoint", style::getHeaderCurrentTimepointColor, style::headerCurrentTimepointColor ),

				separator(),

				booleanSetter( "paint rows", style::isPaintRows, style::paintRows ),
				booleanSetter( "highlight current timepoint", style::isHighlightCurrentTimepoint, style::highlightCurrentTimepoint ),
				booleanSetter( "paint columns", style::isPaintColumns, style::paintColumns ),
				booleanSetter( "paint header shadow", style::isPaintHeaderShadow, style::paintHeaderShadow )
		);
	}

	/**
	 * Adapted from http://stackoverflow.com/a/3072979/230513
	 */
	private static class ColorIcon implements Icon
	{
		private final int size = 16;

		private final int pad = 2;

		private Color color;

		public ColorIcon( final Color color )
		{
			this.color = color;
		}

		@Override
		public void paintIcon( final Component c, final Graphics g, final int x, final int y )
		{
			final Graphics2D g2d = ( Graphics2D ) g;
			g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
			g2d.setColor( color );
			// g2d.fillOval( x, y, size, size );
			g2d.fill( new RoundRectangle2D.Float( x + pad, y + pad, size, size, 5, 5 ) );
		}

		public void setColor( final Color color )
		{
			this.color = color;
		}

		@Override
		public int getIconWidth()
		{
			return size + 2 * pad;
		}

		@Override
		public int getIconHeight()
		{
			return size + 2 * pad;
		}
	}

	public static void main( final String[] args )
	{
		final TrackSchemeStyle style = TrackSchemeStyle.defaultStyle();
		new TrackSchemeStyleEditorDialog( null, style ).setVisible( true );
	}

	public static class TrackSchemeStyleEditorDialog extends JDialog
	{
		private static final long serialVersionUID = 1L;

		private final TrackSchemeStyleEditorPanel stylePanel;

		public TrackSchemeStyleEditorDialog( final JDialog dialog, final TrackSchemeStyle style )
		{
			super( dialog, "TrackScheme style editor", false );

			stylePanel = new TrackSchemeStyleEditorPanel( style );

			final JPanel content = new JPanel();
			content.setLayout( new BoxLayout( content, BoxLayout.PAGE_AXIS ) );
			content.add( stylePanel );
			getContentPane().add( content, BorderLayout.NORTH );

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
