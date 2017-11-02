package org.mastodon.app.ui.settings;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class SettingsPanel extends JPanel
{
	private final DefaultMutableTreeNode root;

	private final DefaultTreeModel model;

	private final FullWidthSelectionJTree tree;

	private final JPanel pages;

	private final JPanel breadcrumbs;

	public void addPage( final SettingsPage page )
	{
		final String path = page.getTreePath();
		final String[] parts = path.split( ">" );
		DefaultMutableTreeNode current = root;
		for ( final String part : parts )
		{
			final String text = part.trim();
			DefaultMutableTreeNode next = null;
			for ( int i = 0; i < current.getChildCount(); ++i )
			{
				DefaultMutableTreeNode child = ( DefaultMutableTreeNode ) current.getChildAt( i );
				final SettingsNodeData data = ( SettingsNodeData ) child.getUserObject();
				if ( text.equals( data.name ) )
				{
					next = child;
					break;
				}
			}

			if ( next == null )
			{
				SettingsNodeData data = new SettingsNodeData( text, null );
				next = new DefaultMutableTreeNode( data );
				model.insertNodeInto( next, current, current.getChildCount() );
			}

			current = next;
		}

		final SettingsNodeData data = ( SettingsNodeData ) current.getUserObject();
		data.page = page;
		tree.expandPath( new TreePath( root ) );

		pages.add( data.page.getTreePath(), data.page.getJPanel() );
		pages.revalidate();
		pages.repaint();
	}

	public SettingsPanel()
	{
		root = new DefaultMutableTreeNode( new SettingsNodeData( "root", null ) );
		model = new DefaultTreeModel( root );
		tree = new FullWidthSelectionJTree( model );

		breadcrumbs = new JPanel();
		breadcrumbs.setLayout( new BoxLayout( breadcrumbs, BoxLayout.LINE_AXIS ) );
		breadcrumbs.setBorder( new EmptyBorder( 5, 5, 5, 0 ) );
		setBreadCrumbs( root );

		final CardLayout cardLayout = new CardLayout();
		pages = new JPanel( cardLayout );

		tree.setEditable( false );
		tree.setSelectionRow( 0 );
		tree.setRootVisible( false );
		tree.setShowsRootHandles( true );
		tree.setExpandsSelectedPaths( true );
		tree.getSelectionModel().setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );

		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		renderer.setIcon( null );
		renderer.setLeafIcon( null );
		renderer.setOpenIcon( null );
		renderer.setClosedIcon( null );
		Color bg = renderer.getBackgroundSelectionColor();
		tree.setBackgroundSelectionColor( bg );
		tree.setCellRenderer( renderer );

		tree.getSelectionModel().addTreeSelectionListener(
				new TreeSelectionListener()
				{
					@Override
					public void valueChanged( final TreeSelectionEvent e )
					{
						final DefaultMutableTreeNode selectedNode = ( DefaultMutableTreeNode ) tree.getLastSelectedPathComponent();
						if ( selectedNode != null )
						{
							setBreadCrumbs( selectedNode );
							final SettingsNodeData data = ( SettingsNodeData ) selectedNode.getUserObject();
							if ( data.page != null )
								cardLayout.show( pages, data.page.getTreePath() );
						}
					}
				} );

		final JButton cancel = new JButton("Cancel");
		final JButton apply = new JButton("Apply");
		final JButton ok = new JButton("OK");
		final JPanel buttons = new JPanel();
		buttons.setLayout( new BoxLayout( buttons, BoxLayout.LINE_AXIS ) );
		buttons.add( Box.createHorizontalGlue() );
		buttons.add( cancel );
		buttons.add( apply );
		buttons.add( ok );

		final JPanel content = new JPanel( new BorderLayout() );
		content.add( breadcrumbs, BorderLayout.NORTH );
		content.add( pages, BorderLayout.CENTER );
		content.add( buttons, BorderLayout.SOUTH );

		final JScrollPane treeScrollPane = new JScrollPane( tree );
		treeScrollPane.setPreferredSize( new Dimension( 500, 500 ) );

		final JSplitPane splitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, content );
		splitPane.setResizeWeight( 0 );
		splitPane.setContinuousLayout( true );
		splitPane.setDividerSize( 5 );
		splitPane.setDividerLocation( 300 );

		this.setLayout( new BorderLayout() );
		this.add( splitPane, BorderLayout.CENTER );

		cancel.addActionListener( e -> getPages().forEach( SettingsPage::cancel ) );
		apply.addActionListener( e -> getPages().forEach( SettingsPage::apply ) );
		ok.addActionListener( e -> getPages().forEach( SettingsPage::apply ) );
	}

	private ArrayList< SettingsPage > getPages()
	{
		ArrayList< SettingsPage > list = new ArrayList<>();
		getPages( root, list );
		return list;
	}

	private void getPages( DefaultMutableTreeNode node, ArrayList< SettingsPage > pages )
	{
		final SettingsNodeData data = ( SettingsNodeData ) node.getUserObject();
		if ( data.page != null )
			pages.add( data.page );
		for ( int i = 0; i < node.getChildCount(); ++i )
			getPages( ( DefaultMutableTreeNode ) node.getChildAt( i ), pages );
	}

	private void setBreadCrumbs( final DefaultMutableTreeNode selectedNode )
	{
		breadcrumbs.removeAll();
		final Font font = new JLabel().getFont().deriveFont( Font.BOLD );
		DefaultMutableTreeNode current = selectedNode;
		while ( current != root )
		{
			final SettingsNodeData data = ( SettingsNodeData ) current.getUserObject();
			JLabel label = new JLabel( data.name );
			label.setFont( font );
			final TreePath tpath = new TreePath( model.getPathToRoot( current ) );
			label.addMouseListener( new MouseAdapter()
			{
				@Override
				public void mouseClicked( final MouseEvent e )
				{
					tree.getSelectionModel().setSelectionPath( tpath );
				}
			} );
			breadcrumbs.add( label, 0 );
			DefaultMutableTreeNode parent = ( DefaultMutableTreeNode ) current.getParent();
			if ( parent != root )
			{
				label = new JLabel( " \u25b8 " );
				label.setFont( font );
				breadcrumbs.add( label, 0 );
			}
			current = parent;
		}

		if ( breadcrumbs.getComponentCount() == 0 )
			breadcrumbs.add( new JLabel( " " ) );

		breadcrumbs.revalidate();
		breadcrumbs.repaint();
	}

	static class SettingsNodeData
	{
		private final String name;

		private SettingsPage page;

		SettingsNodeData( final String name, final SettingsPage page )
		{
			this.name = name;
			this.page = page;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}

	static class FullWidthSelectionJTree extends JTree
	{
		private Color backgroundSelectionColor;

		FullWidthSelectionJTree( final TreeModel newModel )
		{
			super( newModel );
			backgroundSelectionColor = new DefaultTreeCellRenderer().getBackgroundSelectionColor();
			setOpaque( false );
		}

		void setBackgroundSelectionColor( final Color backgroundSelectionColor )
		{
			this.backgroundSelectionColor = backgroundSelectionColor;
		}

		@Override
		public void paintComponent( Graphics g )
		{
			final int[] rows = getSelectionRows();
			if ( rows != null )
			{
				g.setColor( backgroundSelectionColor );
				for ( int i : rows )
				{
					Rectangle r = getRowBounds( i );
					g.fillRect( 0, r.y, getWidth(), r.height );
				}
			}
			super.paintComponent( g );
		}
	}
}
