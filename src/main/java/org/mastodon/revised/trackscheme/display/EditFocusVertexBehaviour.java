package org.mastodon.revised.trackscheme.display;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.mastodon.revised.trackscheme.ScreenTransform;
import org.mastodon.revised.trackscheme.TrackSchemeEdge;
import org.mastodon.revised.trackscheme.TrackSchemeGraph;
import org.mastodon.revised.trackscheme.TrackSchemeVertex;
import org.mastodon.revised.trackscheme.display.OffsetHeaders.OffsetHeadersListener;
import org.mastodon.revised.ui.selection.FocusModel;
import org.mastodon.undo.UndoPointMarker;

import net.imglib2.ui.TransformListener;

/**
 *
 * @author Jean-Yves Tinevez &lt;jeanyves.tinevez@gmail.com&gt;
 */
public class EditFocusVertexBehaviour implements Runnable, TransformListener< ScreenTransform >, OffsetHeadersListener
{
	private static final Font FONT = new Font( "SansSerif", Font.BOLD, 10 );

	private final TrackSchemeGraph< ?, ? > graph;

	private final UndoPointMarker undoPointMarker;

	private final JComponent display;

	private final ScreenTransform screenTransform;

	private Editor editor;

	private final FontMetrics fontMetrics;

	private final FocusModel< TrackSchemeVertex, TrackSchemeEdge > focus;

	/**
	 * current width of vertical header.
	 */
	private int headerWidth;

	/**
	 * current height of horizontal header.
	 */
	private int headerHeight;

	public EditFocusVertexBehaviour(
			final FocusModel< TrackSchemeVertex, TrackSchemeEdge > focus,
			final TrackSchemeGraph< ?, ? > graph,
			final UndoPointMarker undoPointMarker,
			final JComponent display )
	{
		this.focus = focus;
		this.graph = graph;
		this.undoPointMarker = undoPointMarker;
		this.display = display;
		this.screenTransform = new ScreenTransform();
		this.fontMetrics = display.getFontMetrics( FONT );
	}

	@Override
	public void run()
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
		editor.blockKeys();
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

		/**
		 * Adapted from Jan Funke's code in
		 * https://github.com/saalfeldlab/bigcat/blob/janh5/src/main/java/bdv/bigcat/ui/BigCatTable.java#L112-L143
		 */
		private void blockKeys()
		{
			// Get all keystrokes that are mapped to actions in higher components
			final ArrayList< KeyStroke > allTableKeys = new ArrayList< KeyStroke >();
			for ( Container c = this; c != null; c = c.getParent() )
			{
				if ( c instanceof JComponent )
				{
					final InputMap inputMap = ( ( JComponent ) c ).getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
					final KeyStroke[] tableKeys = inputMap.allKeys();
					if ( tableKeys != null )
						allTableKeys.addAll( Arrays.asList( tableKeys ) );
				}
			}

			// An action that does nothing. We can not just map to "none",
			// as this is not interrupting the action-name -> action search.
			// We have to map to a proper action, "nothing" in this case.
			final Action nada = new AbstractAction()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed( final ActionEvent e )
				{}
			};
			getActionMap().put( "nothing", nada );

			// Replace every table key binding with nothing, thus creating an
			// event-barrier.
			final InputMap inputMap = getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
			for ( final KeyStroke key : allTableKeys )
				inputMap.put( key, "nothing" );
		}

		private void commit()
		{
			vertex.setLabel( getText().trim() );
			undoPointMarker.setUndoPoint();
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
