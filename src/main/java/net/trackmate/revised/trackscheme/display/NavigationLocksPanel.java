package net.trackmate.revised.trackscheme.display;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import net.trackmate.graph.RefPool;
import net.trackmate.revised.NavigationHandler;
import net.trackmate.revised.NavigationListener;
import net.trackmate.revised.trackscheme.TrackSchemeSelection;
import net.trackmate.revised.trackscheme.TrackSchemeVertex;
import net.trackmate.revised.ui.selection.SelectionListener;

public class NavigationLocksPanel extends JPanel implements SelectionListener, NavigationListener
{

	private static final long serialVersionUID = 1L;

	private static final ImageIcon LOCK_ICON = new ImageIcon( NavigationLocksPanel.class.getResource( "lock.png" ) );

	private static final ImageIcon UNLOCK_ICON = new ImageIcon( NavigationLocksPanel.class.getResource( "lock_open_grey.png" ) );

	private static final Font FONT = new Font( "Arial", Font.PLAIN, 10 );

	private static final int N_LOCKS = 5;

	private final NavigationHandler handler;

	private final TIntHashSet groups;

	private final TrackSchemeSelection selection;

	private final RefPool< TrackSchemeVertex > vertexPool;

	private final TrackSchemePanel panel;

	public NavigationLocksPanel( final TrackSchemePanel panel, final NavigationHandler handler, final TrackSchemeSelection selection, final RefPool< TrackSchemeVertex > vertexPool )
	{
		this.panel = panel;
		this.handler = handler;
		this.selection = selection;
		this.vertexPool = vertexPool;
		this.groups = new TIntHashSet();

		setLayout( new FlowLayout( FlowLayout.LEADING ) );
		for ( int i = 0; i < N_LOCKS; i++ )
		{
			final int lockId = i;
			final JToggleButton button = new JToggleButton( "" + ( i + 1 ), UNLOCK_ICON, false );
			button.setFont( FONT );
			button.setOpaque( false );
			button.setContentAreaFilled( false );
			button.setBorderPainted( false );
			button.addActionListener( new ActionListener()
			{
				@Override
				public void actionPerformed( final ActionEvent e )
				{
					if ( button.isSelected() )
						button.setIcon( LOCK_ICON );
					else
						button.setIcon( UNLOCK_ICON );
					activateGroup( lockId, button.isSelected() );
				}
			} );
			add( button );
		}
	}

	protected void activateGroup( final int lockId, final boolean activate )
	{
		if ( activate )
			groups.add( lockId );
		else
			groups.remove( lockId );
	}

	@Override
	public void selectionChanged()
	{
		final TIntSet vertices = selection.getSelectedVertexIds();
		if ( vertices.size() != 1 )
			return;

		final TrackSchemeVertex ref = vertexPool.createRef();
		final int trackSchemeVertexId = vertices.iterator().next();
		vertexPool.getByInternalPoolIndex( trackSchemeVertexId, ref );

		final int modelVertexId = ref.getModelVertexId();
		handler.notifyListeners( groups, modelVertexId );
	}

	@Override
	public void navigateToVertex( final int modelVertexId )
	{
		panel.centerOn( modelVertexId );
	}

	@Override
	public boolean isInGroup( final int group )
	{
		return groups.contains( group );
	}

}
