package net.trackmate.revised.trackscheme.display.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
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

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import net.trackmate.revised.trackscheme.display.laf.TrackSchemeStyle;

public class TrackSchemeStylePanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	private final TrackSchemeStyle style;

	private final JColorChooser colorChooser;

	public TrackSchemeStylePanel( final TrackSchemeStyle style )
	{
		super( new GridBagLayout() );
		this.style = style;

		colorChooser = new JColorChooser();

		final GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets( 0, 5, 0, 5 );
		c.ipadx = 0;
		c.ipady = 0;
		c.gridwidth = 1;

		final List< ColorSetter > styleColors = styleColors( style );

		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		for ( final ColorSetter colorSetter : styleColors )
		{
			final JButton button = new JButton( colorSetter.getLabel(), new ColorIcon( colorSetter.getColor() ) );
			button.setMargin( new Insets( 0, 0, 0, 0 ) );
			button.setBorder( new EmptyBorder( 2, 2, 2, 2 ) );
			button.setHorizontalAlignment( SwingConstants.LEFT );
			button.addActionListener( new ActionListener()
			{
				@Override
				public void actionPerformed( final ActionEvent e )
				{
					colorChooser.setColor( colorSetter.getColor() );
					final JDialog d = JColorChooser.createDialog( button, "Choose a color", true, colorChooser, new ActionListener()
					{
						@Override
						public void actionPerformed( final ActionEvent arg0 )
						{
							final Color c = colorChooser.getColor();
							if ( c != null )
							{
								button.setIcon( new ColorIcon( c ) );
								colorSetter.setColor( c );
							}
						}
					}, null );
					d.setVisible( true );
				}
			} );
			c.anchor = GridBagConstraints.LINE_END;
			c.gridx = 0;
			add( button, c );
//			button.setFont( new JLabel().getFont() );
//			c.anchor = GridBagConstraints.LINE_START;
//			c.gridx = 1;
//			add( new JLabel( colorSetter.getLabel() ), c );
			c.gridy++;

			if ( colorSetter.skip > 0 )
			{
				add( Box.createVerticalStrut( colorSetter.skip ), c );
				c.gridy++;
			}
		}
	}

	private static abstract class ColorSetter
	{
		private final String label;

		private final int skip;

		public ColorSetter( final String label )
		{
			this( label, 0 );
		}

		public ColorSetter( final String label, final boolean skip )
		{
			this( label, skip ? 15 : 0 );
		}

		public ColorSetter( final String label, final int skip )
		{
			this.label = label;
			this.skip = skip;
		}

		public String getLabel()
		{
			return label;
		}

		public abstract Color getColor();

		public abstract void setColor( Color c );
	}

	private static List< ColorSetter > styleColors( final TrackSchemeStyle style )
	{
		return Arrays.asList( new ColorSetter[] {
				new ColorSetter( "edgeColor" ) {
					@Override public Color getColor() { return style.edgeColor; }
					@Override public void setColor( final Color c ) { style.edgeColor( c ); }
				},
				new ColorSetter( "vertexFillColor" ) {
					@Override public Color getColor() { return style.vertexFillColor; }
					@Override public void setColor( final Color c ) { style.vertexFillColor( c ); }
				},
				new ColorSetter( "vertexDrawColor" ) {
					@Override public Color getColor() { return style.vertexDrawColor; }
					@Override public void setColor( final Color c ) { style.vertexDrawColor( c ); }
				},
				new ColorSetter( "simplifiedVertexFillColor", true ) {
					@Override public Color getColor() { return style.simplifiedVertexFillColor; }
					@Override public void setColor( final Color c ) { style.simplifiedVertexFillColor( c ); }
				},
				new ColorSetter( "selectedEdgeColor" ) {
					@Override public Color getColor() { return style.selectedEdgeColor; }
					@Override public void setColor( final Color c ) { style.selectedEdgeColor( c ); }
				},
				new ColorSetter( "selectedVertexDrawColor" ) {
					@Override public Color getColor() { return style.selectedVertexDrawColor; }
					@Override public void setColor( final Color c ) { style.selectedVertexDrawColor( c ); }
				},
				new ColorSetter( "selectedVertexFillColor" ) {
					@Override public Color getColor() { return style.selectedVertexFillColor; }
					@Override public void setColor( final Color c ) { style.selectedVertexFillColor( c ); }
				},
				new ColorSetter( "selectedSimplifiedVertexFillColor", true ) {
					@Override public Color getColor() { return style.selectedSimplifiedVertexFillColor; }
					@Override public void setColor( final Color c ) { style.selectedSimplifiedVertexFillColor( c ); }
				},
				new ColorSetter( "backgroundColor" ) {
					@Override public Color getColor() { return style.backgroundColor; }
					@Override public void setColor( final Color c ) { style.backgroundColor( c ); }
				},
				new ColorSetter( "currentTimepointColor" ) {
					@Override public Color getColor() { return style.currentTimepointColor; }
					@Override public void setColor( final Color c ) { style.currentTimepointColor( c ); }
				},
				new ColorSetter( "decorationColor" ) {
					@Override public Color getColor() { return style.decorationColor; }
					@Override public void setColor( final Color c ) { style.decorationColor( c ); }
				},
				new ColorSetter( "vertexRangeColor", true ) {
					@Override public Color getColor() { return style.vertexRangeColor; }
					@Override public void setColor( final Color c ) { style.vertexRangeColor( c ); }
				},
				new ColorSetter( "headerBackgroundColor" ) {
					@Override public Color getColor() { return style.headerBackgroundColor; }
					@Override public void setColor( final Color c ) { style.headerBackgroundColor( c ); }
				},
				new ColorSetter( "headerDecorationColor" ) {
					@Override public Color getColor() { return style.headerDecorationColor; }
					@Override public void setColor( final Color c ) { style.headerDecorationColor( c ); }
				},
				new ColorSetter( "headerCurrentTimepointColor" ) {
					@Override public Color getColor() { return style.headerCurrentTimepointColor; }
					@Override public void setColor( final Color c ) { style.headerCurrentTimepointColor( c ); }
				}
		} );
	}

	/**
	 * Adapted from http://stackoverflow.com/a/3072979/230513
	 */
	private static class ColorIcon implements Icon
	{
		private final int size = 32;

		private final Color color;

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
//			g2d.fillOval( x, y, size, size );
	        g2d.fill(new RoundRectangle2D.Float(x, y, size, size, 5, 5));
		}

		@Override
		public int getIconWidth()
		{
			return size;
		}

		@Override
		public int getIconHeight()
		{
			return size;
		}
	}

	public static void main( final String[] args )
	{
		final TrackSchemeStyle style = TrackSchemeStyle.defaultStyle();
		new TrackSchemeStyleDialog( null, style ).setVisible( true );
	}

	public static class TrackSchemeStyleDialog extends JDialog
	{
		private static final long serialVersionUID = 1L;

		private final TrackSchemeStyle style;

		private final TrackSchemeStylePanel stylePanel;

		public TrackSchemeStyleDialog( final Frame owner, final TrackSchemeStyle style )
		{
			super( owner, "trackscheme style", false );
			this.style = style;

			stylePanel = new TrackSchemeStylePanel( style );

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
