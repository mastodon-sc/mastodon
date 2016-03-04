package net.trackmate.revised.trackscheme.display;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.scijava.ui.behaviour.KeyStrokeAdder;

import bdv.util.AbstractNamedAction;
import bdv.viewer.InputActionBindings;
import net.imglib2.ui.TransformListener;
import net.trackmate.revised.trackscheme.ScreenTransform;
import net.trackmate.revised.trackscheme.TrackSchemeFocus;
import net.trackmate.revised.trackscheme.TrackSchemeGraph;
import net.trackmate.revised.trackscheme.TrackSchemeVertex;
import net.trackmate.revised.trackscheme.display.OffsetHeaders.OffsetHeadersListener;

/**
 *
 * @author Jean-Yves Tinevez &lt;jeanyves.tinevez@gmail.com&gt;
 */
public class EditFocusVertexBehaviour extends AbstractNamedAction implements TransformListener< ScreenTransform >, OffsetHeadersListener
{
	public static final String EDIT_FOCUS_NAME = "ts edit focused vertex label";

	private static final Font FONT = new Font( "SansSerif", Font.BOLD, 10 );

	private final TrackSchemeGraph< ?, ? > graph;

	private final JComponent display;

	private final ScreenTransform screenTransform;

	private Editor editor;

	private final FontMetrics fontMetrics;

	private final TrackSchemeFocus focus;

	/**
	 * current width of vertical header.
	 */
	private int headerWidth;

	/**
	 * current height of horizontal header.
	 */
	private int headerHeight;

	public EditFocusVertexBehaviour(
			final TrackSchemeFocus focus,
			final TrackSchemeGraph< ?, ? > graph,
			final JComponent display )
	{
		super( EDIT_FOCUS_NAME );
		this.focus = focus;
		this.graph = graph;
		this.display = display;
		this.screenTransform = new ScreenTransform();
		this.fontMetrics = display.getFontMetrics( FONT );
	}

	public void installActionBindings( final InputActionBindings keybindings, final KeyStrokeAdder.Factory keyConfig )
	{
		final ActionMap actionMap = new ActionMap();
		new NamedActionAdder( actionMap ).put( this );

		final InputMap inputMap = new InputMap();
		keyConfig.keyStrokeAdder( inputMap, "ts" ).put( EDIT_FOCUS_NAME, "ENTER" );

		keybindings.addActionMap( "edit label", actionMap );
		keybindings.addInputMap( "edit label", inputMap );
	}

	@Override
	public void actionPerformed( final ActionEvent e )
	{
		final TrackSchemeVertex ref = graph.vertexRef();
		final TrackSchemeVertex vertex = focus.getFocusedVertex( ref );
		if ( vertex == null )
		{
			graph.releaseRef( ref );
			return;
		}
		if ( null != editor )
		{
			editor.kill();
		}

		editor = new Editor( vertex );
		// vertex ref will have to be released in the editor class.
		display.add( editor );
		display.repaint();
	}

	@Override
	public void transformChanged( final ScreenTransform transform )
	{
		synchronized ( screenTransform )
		{
			screenTransform.set( transform );
		}
		if ( null != editor )
		{
			editor.reposition();
			display.repaint();
		}
	}

	@Override
	public void updateHeadersVisibility( final boolean isVisibleX, final int width, final boolean isVisibleY, final int height )
	{
		headerWidth = isVisibleX ? width : 0;
		headerHeight = isVisibleY ? height : 0;
	}

	private class Editor extends JTextField
	{
		private static final long serialVersionUID = 1L;

		private final TrackSchemeVertex vertex;

		private boolean killed = false;

		public Editor( final TrackSchemeVertex vertex )
		{
			this.vertex = vertex;
			final String label = vertex.getLabel();
			setText( label );
			setHorizontalAlignment( SwingConstants.CENTER );

			reposition();
			setFocusable( true );
			setEditable( true );
			setOpaque( true );
			setBorder( BorderFactory.createLineBorder( Color.ORANGE, 1 ) );
			setFont( FONT );
			selectAll();
			SwingUtilities.invokeLater( new Runnable()
			{
				@Override
				public void run()
				{
					requestFocusInWindow();
				}
			} );

			addActionListener( new ActionListener()
			{
				@Override
				public void actionPerformed( final ActionEvent e )
				{
					commit();
					kill();
				}
			} );
			addKeyListener( new KeyListener()
			{
				@Override
				public void keyTyped( final KeyEvent e )
				{
					reposition();
				}

				@Override
				public void keyReleased( final KeyEvent e )
				{}

				@Override
				public void keyPressed( final KeyEvent e )
				{
					if ( e.getExtendedKeyCode() == KeyEvent.VK_ESCAPE )
						kill();
				}
			} );
			addFocusListener( new FocusListener()
			{
				@Override
				public void focusLost( final FocusEvent e )
				{
					kill();
				}

				@Override
				public void focusGained( final FocusEvent e )
				{}
			} );

		}

		private void commit()
		{
			System.out.println( "TODO Change TrackScheme vertex " + vertex.getLabel() + " label to " + getText() );// DEBUG
		}

		private void kill()
		{
			if ( killed )
				return;
			display.remove( editor );
			display.repaint();
			display.requestFocusInWindow();
			graph.releaseRef( vertex );
			killed = true;
		}

		private void reposition()
		{
			final int ly = vertex.getTimepoint();
			final double lx = vertex.getLayoutX();

			final double[] screenPos = new double[ 2 ];
			screenTransform.apply( new double[] { lx, ly }, screenPos );
			screenPos[ 0 ] += headerWidth;
			screenPos[ 1 ] += headerHeight;

			final int h = fontMetrics.getHeight() + 10;
			final int w = fontMetrics.stringWidth( getText() ) + 30;
			final int x = Math.min( display.getWidth() - w, Math.max( 0, ( int ) screenPos[ 0 ] - w / 2 ) );
			final int y = Math.min( display.getHeight() - h, Math.max( 0, ( int ) screenPos[ 1 ] - h / 2 ) );
			setBounds( x, y, w, h );
		}
	}
}
