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
import javax.swing.border.MatteBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * Main panel for preferences dialogs.
 * <p>
 * On the right, one of several {@link SettingsPage}s is shown. On the left, a
 * {@code JTree} is used to select between pages. On the top, a breadcrumbs
 * trail shows the tree path of the current {@link SettingsPage}. On the bottom,
 * "Cancel", "Apply", and "OK" buttons are shown.
 * </p>
 *
 * @author Tobias Pietzsch
 */
public class SettingsPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	private final DefaultMutableTreeNode root;

	private final DefaultTreeModel model;

	private final FullWidthSelectionJTree tree;

	private final JPanel pages;

	private final JPanel breadcrumbs;

	private final ModificationListener modificationListener;

	private final ArrayList< Runnable > runOnOk;

	private final ArrayList< Runnable > runOnCancel;

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
				final DefaultMutableTreeNode child = ( DefaultMutableTreeNode ) current.getChildAt( i );
				final SettingsNodeData data = ( SettingsNodeData ) child.getUserObject();
				if ( text.equals( data.name ) )
				{
					next = child;
					break;
				}
			}

			if ( next == null )
			{
				final SettingsNodeData data = new SettingsNodeData( text, null );
				next = new DefaultMutableTreeNode( data );
				model.insertNodeInto( next, current, current.getChildCount() );
			}

			current = next;
		}

		page.modificationListeners().add( modificationListener );

		final SettingsNodeData data = ( SettingsNodeData ) current.getUserObject();
		data.page = page;
		tree.expandPath( new TreePath( root ) );

		if ( pages.getComponents().length == 0 )
			tree.getSelectionModel().setSelectionPath( new TreePath( model.getPathToRoot( current ) ) );

		pages.add( data.page.getTreePath(), data.page.getJPanel() );
		pages.revalidate();
		pages.repaint();
	}

	/**
	 * Removes the settings page with the specified path. Does nothing if there
	 * is no settings page for the path.
	 *
	 * @param path
	 *            the path of the settings page to remove. Example:
	 *            {@code "Analyze > Tables"}
	 */
	public void removePage( final String path )
	{
		final String[] parts = path.split( ">" );
		DefaultMutableTreeNode current = root;
		for ( final String part : parts )
		{
			final String text = part.trim();
			DefaultMutableTreeNode next = null;
			for ( int i = 0; i < current.getChildCount(); ++i )
			{
				final DefaultMutableTreeNode child = ( DefaultMutableTreeNode ) current.getChildAt( i );
				final SettingsNodeData data = ( SettingsNodeData ) child.getUserObject();
				if ( text.equals( data.name ) )
				{
					next = child;
					break;
				}
			}
			current = next;
		}
		if ( null == current )
			return; // Path not found in the tree.

		model.removeNodeFromParent( current );
		for ( final SettingsPage page : getPages() )
		{
			if ( page.getTreePath().equals( path ) )
				pages.remove( page.getJPanel() );
		}
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

		final DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		renderer.setIcon( null );
		renderer.setLeafIcon( null );
		renderer.setOpenIcon( null );
		renderer.setClosedIcon( null );
		final Color bg = renderer.getBackgroundSelectionColor();
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
		content.setBorder( new EmptyBorder( 10, 0, 10, 10 ) );

		final JScrollPane treeScrollPane = new JScrollPane( tree );
		treeScrollPane.setPreferredSize( new Dimension( 200, 500 ) );
		treeScrollPane.setMinimumSize( new Dimension( 150, 200 ) );
		treeScrollPane.setBorder( new MatteBorder( 0, 0, 0, 1, Color.LIGHT_GRAY ) );
//		treeScrollPane.setBorder( new EmptyBorder( 0, 0, 0, 0 ) );
		renderer.setBackgroundNonSelectionColor( treeScrollPane.getBackground() );

		final JSplitPane splitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, content );
		splitPane.setResizeWeight( 0 );
		splitPane.setContinuousLayout( true );
		splitPane.setDividerSize( 10 );
		splitPane.setDividerLocation( treeScrollPane.getPreferredSize().width );
		splitPane.setBorder( new MatteBorder( 0, 0, 1, 0, Color.LIGHT_GRAY ) );
//		splitPane.setBorder( new EmptyBorder( 0, 0, 0, 0 ) );

		this.setLayout( new BorderLayout() );
		this.add( splitPane, BorderLayout.CENTER );

		buttons.setBorder( new EmptyBorder( 10, 0, 5, 10 ) );
		this.add( buttons, BorderLayout.SOUTH );

		runOnCancel = new ArrayList<>();
		runOnOk = new ArrayList<>();

		cancel.addActionListener( e -> cancel() );
		ok.addActionListener( e -> {
			getPages().forEach( SettingsPage::apply );
			runOnOk.forEach( Runnable::run );
		} );

		apply.setEnabled( false );
		modificationListener = () -> apply.setEnabled( true );
		apply.addActionListener( e -> {
			apply.setEnabled( false );
			getPages().forEach( SettingsPage::apply );
		} );
	}

	public void cancel()
	{
		getPages().forEach( SettingsPage::cancel );
		runOnCancel.forEach( Runnable::run );
	}

	public synchronized void onOk( final Runnable runnable )
	{
		runOnOk.add( runnable );
	}

	public synchronized void onCancel( final Runnable runnable )
	{
		runOnCancel.add( runnable );
	}

	private ArrayList< SettingsPage > getPages()
	{
		final ArrayList< SettingsPage > list = new ArrayList<>();
		getPages( root, list );
		return list;
	}

	private void getPages( final DefaultMutableTreeNode node, final ArrayList< SettingsPage > pages )
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
			final DefaultMutableTreeNode parent = ( DefaultMutableTreeNode ) current.getParent();
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
		private static final long serialVersionUID = 1L;

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
		public void paintComponent( final Graphics g )
		{
			final int[] rows = getSelectionRows();
			if ( rows != null )
			{
				g.setColor( backgroundSelectionColor );
				for ( final int i : rows )
				{
					final Rectangle r = getRowBounds( i );
					g.fillRect( 0, r.y, getWidth(), r.height );
				}
			}
			super.paintComponent( g );
		}
	}
}
