package org.mastodon.revised.trackscheme.display.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.mastodon.adapter.FocusAdapter;
import org.mastodon.adapter.HighlightAdapter;
import org.mastodon.adapter.NavigationHandlerAdapter;
import org.mastodon.adapter.RefBimap;
import org.mastodon.adapter.SelectionAdapter;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.revised.trackscheme.TrackSchemeEdge;
import org.mastodon.revised.trackscheme.TrackSchemeEdgeBimap;
import org.mastodon.revised.trackscheme.TrackSchemeGraph;
import org.mastodon.revised.trackscheme.TrackSchemeVertex;
import org.mastodon.revised.trackscheme.TrackSchemeVertexBimap;
import org.mastodon.revised.trackscheme.display.TrackSchemeOptions;
import org.mastodon.revised.trackscheme.display.TrackSchemePanel;
import org.mastodon.revised.trackscheme.display.laf.TrackSchemeStyle;
import org.mastodon.revised.trackscheme.display.ui.dummygraph.DummyEdge;
import org.mastodon.revised.trackscheme.display.ui.dummygraph.DummyGraph;
import org.mastodon.revised.trackscheme.display.ui.dummygraph.DummyGraph.Examples;
import org.mastodon.revised.trackscheme.display.ui.dummygraph.DummyVertex;
import org.mastodon.revised.trackscheme.wrap.DefaultModelGraphProperties;
import org.mastodon.revised.trackscheme.wrap.ModelGraphProperties;
import org.mastodon.revised.ui.grouping.GroupManager;
import org.mastodon.revised.ui.selection.FocusModel;
import org.mastodon.revised.ui.selection.FocusModelImp;
import org.mastodon.revised.ui.selection.HighlightModel;
import org.mastodon.revised.ui.selection.HighlightModelImp;
import org.mastodon.revised.ui.selection.NavigationHandler;
import org.mastodon.revised.ui.selection.NavigationHandlerImp;
import org.mastodon.revised.ui.selection.Selection;
import org.mastodon.revised.ui.selection.TimepointModel;
import org.mastodon.revised.ui.selection.TimepointModelImp;

/**
 * A previewer, editor and managers for TrackScheme styles.
 *
 * @author Jean=Yves Tinevez &lt;jeanyves.tinevez@gmail.com&gt;
 */
class TrackSchemeStyleChooserDialog extends JDialog
{

	private static final long serialVersionUID = 1L;

	JButton buttonDeleteStyle;

	JButton buttonEditStyle;

	JButton buttonNewStyle;

	JButton buttonSetStyleName;

	JButton okButton;

	JButton saveButton;

	TrackSchemePanel panelPreview;

	public TrackSchemeStyleChooserDialog( final Frame owner, final DefaultComboBoxModel< TrackSchemeStyle > model )
	{
		super( owner, "TrackScheme style chooser", false );

		final Examples ex = DummyGraph.Examples.CELEGANS;
		final DummyGraph example = ex.getGraph();
		final GraphIdBimap< DummyVertex, DummyEdge > idmap = example.getIdBimap();
		final ModelGraphProperties< DummyVertex, DummyEdge > dummyProps = new DefaultModelGraphProperties<>();
		final TrackSchemeGraph< DummyVertex, DummyEdge > graph = new TrackSchemeGraph<>( example, idmap, dummyProps );
		final RefBimap< DummyVertex, TrackSchemeVertex > vertexMap = new TrackSchemeVertexBimap<>( idmap, graph );
		final RefBimap< DummyEdge, TrackSchemeEdge > edgeMap = new TrackSchemeEdgeBimap<>( idmap, graph );
		final HighlightModel< TrackSchemeVertex, TrackSchemeEdge > highlight = new HighlightAdapter<>( new HighlightModelImp<>( idmap ), vertexMap, edgeMap );
		final FocusModel< TrackSchemeVertex, TrackSchemeEdge > focus = new FocusAdapter<>( new FocusModelImp<>( idmap ), vertexMap, edgeMap );
		final TimepointModel timepoint = new TimepointModelImp();
		timepoint.setTimepoint( 2 );
		final Selection< TrackSchemeVertex, TrackSchemeEdge > selection = new SelectionAdapter<>( ex.getSelection(), vertexMap, edgeMap );
		final NavigationHandler< TrackSchemeVertex, TrackSchemeEdge > navigation = new NavigationHandlerAdapter<>( new NavigationHandlerImp<>( new GroupManager( 1 ).createGroupHandle() ), vertexMap, edgeMap );
		panelPreview = new TrackSchemePanel( graph, highlight, focus, timepoint, selection, navigation, TrackSchemeOptions.options() );
		panelPreview.setTimepointRange( 0, 7 );
		panelPreview.graphChanged();

		final JPanel dialogPane = new JPanel();
		final JPanel contentPanel = new JPanel();
		final JPanel panelChooseStyle = new JPanel();
		final JLabel jlabelTitle = new JLabel();
		final JComboBox< TrackSchemeStyle > comboBoxStyles = new JComboBox<>( model );
		comboBoxStyles.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				panelPreview.setTrackSchemeStyle( comboBoxStyles.getItemAt( comboBoxStyles.getSelectedIndex() ) );
				panelPreview.repaint();
			}
		} );
		final JPanel panelStyleButtons = new JPanel();
		buttonDeleteStyle = new JButton();
		final JPanel hSpacer1 = new JPanel( null );
		buttonEditStyle = new JButton();
		buttonNewStyle = new JButton();
		buttonSetStyleName = new JButton();
		final JPanel buttonBar = new JPanel();
		okButton = new JButton();
		saveButton = new JButton();

		// ======== this ========
		setTitle( "TrackScheme styles" );
		final Container contentPane = getContentPane();
		contentPane.setLayout( new BorderLayout() );

		// ======== dialogPane ========
		{
			dialogPane.setBorder( new EmptyBorder( 12, 12, 12, 12 ) );
			dialogPane.setLayout( new BorderLayout() );

			// ======== contentPanel ========
			{
				contentPanel.setLayout( new BorderLayout() );

				// ======== panelChooseStyle ========
				{
					panelChooseStyle.setLayout( new GridLayout( 3, 0, 0, 10 ) );

					// ---- jlabelTitle ----
					jlabelTitle.setText( "TrackScheme display styles." );
					jlabelTitle.setHorizontalAlignment( SwingConstants.CENTER );
					jlabelTitle.setFont( dialogPane.getFont().deriveFont( Font.BOLD ) );
					panelChooseStyle.add( jlabelTitle );
					panelChooseStyle.add( comboBoxStyles );

					// ======== panelStyleButtons ========
					{
						panelStyleButtons.setLayout( new BoxLayout( panelStyleButtons, BoxLayout.LINE_AXIS ) );

						// ---- buttonDeleteStyle ----
						buttonDeleteStyle.setText( "Delete" );
						panelStyleButtons.add( buttonDeleteStyle );
						panelStyleButtons.add( hSpacer1 );

						// ---- buttonNewStyle ----
						buttonNewStyle.setText( "New" );
						panelStyleButtons.add( buttonNewStyle );

						// ---- buttonSetStyleName ----
						buttonSetStyleName.setText( "Set name" );
						panelStyleButtons.add( buttonSetStyleName );

						// ---- buttonEditStyle ----
						buttonEditStyle.setText( "Edit" );
						panelStyleButtons.add( buttonEditStyle );

					}
					panelChooseStyle.add( panelStyleButtons );
				}
				contentPanel.add( panelChooseStyle, BorderLayout.NORTH );

				// ======== panelPreview ========
				contentPanel.add( panelPreview, BorderLayout.CENTER );
			}
			dialogPane.add( contentPanel, BorderLayout.CENTER );

			// ======== buttonBar ========
			{
				buttonBar.setBorder( new EmptyBorder( 12, 0, 0, 0 ) );
				buttonBar.setLayout( new GridBagLayout() );
				( ( GridBagLayout ) buttonBar.getLayout() ).columnWidths = new int[] { 80, 164, 80 };
				( ( GridBagLayout ) buttonBar.getLayout() ).columnWeights = new double[] { 0.0, 1.0, 0.0 };

				// ---- okButton ----
				okButton.setText( "OK" );
				buttonBar.add( okButton, new GridBagConstraints( 2, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets( 0, 0, 0, 0 ), 0, 0 ) );

				// ---- saveButton -----
				saveButton.setText( "Save styles" );
				buttonBar.add( saveButton, new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets( 0, 0, 0, 0 ), 0, 0 ) );
			}
			dialogPane.add( buttonBar, BorderLayout.SOUTH );
		}
		contentPane.add( dialogPane, BorderLayout.CENTER );
		pack();
	}
}
