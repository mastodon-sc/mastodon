package net.trackmate.revised.bdv.overlay.wrap;

import gnu.trove.set.hash.TIntHashSet;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import net.imglib2.realtransform.AffineTransform3D;
import net.trackmate.graph.Edge;
import net.trackmate.graph.Vertex;
import net.trackmate.revised.trackscheme.display.NavigationLocksPanel;
import net.trackmate.revised.ui.selection.NavigationListener;
import bdv.viewer.ViewerPanel;
import bdv.viewer.animate.TranslationAnimator;

public class BDVNavigationLock< V extends Vertex< E >, E extends Edge< V > > extends JPanel implements NavigationListener
{
	private static final long serialVersionUID = 1L;

	private static final ImageIcon LOCK_ICON = new ImageIcon( NavigationLocksPanel.class.getResource( "lock.png" ) );

	private static final ImageIcon UNLOCK_ICON = new ImageIcon( NavigationLocksPanel.class.getResource( "lock_open_grey.png" ) );

	private static final Font FONT = new Font( "Arial", Font.PLAIN, 10 );

	private static final int N_LOCKS = 3;

	private final TIntHashSet groups;

	private final ViewerPanel panel;

	private final OverlayGraphWrapper< V, E > graph;

	public BDVNavigationLock( final ViewerPanel panel, final OverlayGraphWrapper< V, E > graph )
	{
		this.panel = panel;
		this.graph = graph;
		this.groups = new TIntHashSet();

		setLayout( new FlowLayout( FlowLayout.LEADING, 0, 0 ) );
		for ( int i = 0; i < N_LOCKS; i++ )
		{
			final int lockId = i;
			final JToggleButton button = new JToggleButton( "" + ( i + 1 ), UNLOCK_ICON, false );
			button.setPreferredSize( new Dimension( 60, 20 ) );
			button.setHorizontalAlignment( SwingConstants.LEFT );
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
	public void navigateToVertex( final int modelVertexId )
	{

		final V ref = graph.wrappedGraph.vertexRef();
		final V v = graph.idmap.getVertex( modelVertexId, ref );

		final OverlayVertexWrapper< V, E > ov = graph.vertexRef();
		ov.wv = v;
		final double[] gPos = new double[ 3 ];
		ov.localize( gPos );

		final int tp = ov.getTimepoint();
		panel.setTimepoint( tp );

		final AffineTransform3D t = panel.getDisplay().getTransformEventHandler().getTransform();
		final int width = panel.getWidth();
		final int height = panel.getHeight();

		final double dx = width / 2 - ( t.get( 0, 0 ) * gPos[ 0 ] + t.get( 0, 1 ) * gPos[ 1 ] + t.get( 0, 2 ) * gPos[ 2 ] );
		final double dy = height / 2 - ( t.get( 1, 0 ) * gPos[ 0 ] + t.get( 1, 1 ) * gPos[ 1 ] + t.get( 1, 2 ) * gPos[ 2 ] );
		final double dz = -( t.get( 2, 0 ) * gPos[ 0 ] + t.get( 2, 1 ) * gPos[ 1 ] + t.get( 2, 2 ) * gPos[ 2 ] );

		final double[] target = new double[] { dx, dy, dz };
		final TranslationAnimator animator = new TranslationAnimator( t, target, 300 );
		animator.setTime( System.currentTimeMillis() );
		panel.setTransformAnimator( animator );
		panel.requestRepaint();

		graph.releaseRef( ov );
		graph.wrappedGraph.releaseRef( ref );
	}

	@Override
	public boolean isInGroup( final int group )
	{
		return groups.contains( group );
	}

}
