/*
 * Created by JFormDesigner on Mon Jul 04 15:49:29 EDT 2016
 */

package net.trackmate.revised.trackscheme.display.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import net.trackmate.graph.GraphIdBimap;
import net.trackmate.revised.trackscheme.DefaultModelFocusProperties;
import net.trackmate.revised.trackscheme.DefaultModelGraphProperties;
import net.trackmate.revised.trackscheme.DefaultModelHighlightProperties;
import net.trackmate.revised.trackscheme.DefaultModelNavigationProperties;
import net.trackmate.revised.trackscheme.DefaultModelSelectionProperties;
import net.trackmate.revised.trackscheme.ModelGraphProperties;
import net.trackmate.revised.trackscheme.TrackSchemeFocus;
import net.trackmate.revised.trackscheme.TrackSchemeGraph;
import net.trackmate.revised.trackscheme.TrackSchemeHighlight;
import net.trackmate.revised.trackscheme.TrackSchemeNavigation;
import net.trackmate.revised.trackscheme.TrackSchemeSelection;
import net.trackmate.revised.trackscheme.display.TrackSchemeOptions;
import net.trackmate.revised.trackscheme.display.TrackSchemePanel;
import net.trackmate.revised.trackscheme.display.laf.TrackSchemeStyle;
import net.trackmate.revised.trackscheme.display.ui.dummygraph.DummyEdge;
import net.trackmate.revised.trackscheme.display.ui.dummygraph.DummyGraph;
import net.trackmate.revised.trackscheme.display.ui.dummygraph.DummyVertex;
import net.trackmate.revised.ui.grouping.GroupManager;
import net.trackmate.revised.ui.selection.FocusModel;
import net.trackmate.revised.ui.selection.HighlightModel;
import net.trackmate.revised.ui.selection.NavigationHandler;
import net.trackmate.revised.ui.selection.Selection;

/**
 */
public class TrackSchemeStyleChooserFrame extends JFrame
{

	private TrackSchemePanel panelPreview;

	private final TrackSchemeStyleChooserModel model;

	public TrackSchemeStyleChooserFrame( final TrackSchemeStyleChooserModel model )
	{
		this.model = model;
		initComponents();
	}

	private void initComponents()
	{
		final DummyGraph example = DummyGraph.example();
		final GraphIdBimap< DummyVertex, DummyEdge > idmap = example.getIdBimap();
		final Selection< DummyVertex, DummyEdge > selection = new Selection<>( example, idmap );
		final ModelGraphProperties dummyProps = new DefaultModelGraphProperties< DummyVertex, DummyEdge >( example, idmap, selection );
		final TrackSchemeGraph< DummyVertex, DummyEdge > graph = new TrackSchemeGraph<>( example, idmap, dummyProps );
		final TrackSchemeHighlight highlight = new TrackSchemeHighlight( new DefaultModelHighlightProperties<>( example, idmap, new HighlightModel<>( idmap ) ), graph );
		final TrackSchemeFocus focus = new TrackSchemeFocus( new DefaultModelFocusProperties<>( example, idmap, new FocusModel<>( idmap ) ), graph );
		final TrackSchemeSelection tsSelection = new TrackSchemeSelection( new DefaultModelSelectionProperties<>( example, idmap, selection ) );
		final TrackSchemeNavigation navigation = new TrackSchemeNavigation( new DefaultModelNavigationProperties<>( example, idmap, new NavigationHandler<>( new GroupManager().createGroupHandle() ) ), graph );
		panelPreview = new TrackSchemePanel( graph, highlight, focus, tsSelection, navigation, TrackSchemeOptions.options() );
		panelPreview.setTimepointRange( 0, 6 );
		panelPreview.graphChanged();

		final JPanel dialogPane = new JPanel();
		final JPanel contentPanel = new JPanel();
		final JPanel panelChooseStyle = new JPanel();
		final JLabel jlabelTitle = new JLabel();
		final JComboBox< TrackSchemeStyle > comboBoxStyles = new JComboBox<>( model );
		final JPanel panelStyleButtons = new JPanel();
		final JButton buttonDeleteStyle = new JButton();
		final JPanel hSpacer1 = new JPanel( null );
		final JButton buttonEditStyle = new JButton();
		final JButton buttonNewStyle = new JButton();
		final JPanel buttonBar = new JPanel();
		final JButton okButton = new JButton();

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

						// ---- buttonEditStyle ----
						buttonEditStyle.setText( "Edit" );
						panelStyleButtons.add( buttonEditStyle );

						// ---- buttonNewStyle ----
						buttonNewStyle.setText( "New" );
						panelStyleButtons.add( buttonNewStyle );
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
				( ( GridBagLayout ) buttonBar.getLayout() ).columnWidths = new int[] { 244, 80 };
				( ( GridBagLayout ) buttonBar.getLayout() ).columnWeights = new double[] { 1.0, 0.0 };

				// ---- okButton ----
				okButton.setText( "OK" );
				buttonBar.add( okButton, new GridBagConstraints( 1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets( 0, 0, 0, 0 ), 0, 0 ) );
			}
			dialogPane.add( buttonBar, BorderLayout.SOUTH );
		}
		contentPane.add( dialogPane, BorderLayout.CENTER );
		pack();
		setLocationRelativeTo( getOwner() );
	}

	public static void main( final String[] args )
	{
		try
		{
			javax.swing.UIManager.setLookAndFeel(
					javax.swing.UIManager.getSystemLookAndFeelClassName() );
		}
		catch ( final javax.swing.UnsupportedLookAndFeelException e )
		{}
		catch ( final ClassNotFoundException e )
		{}
		catch ( final InstantiationException e )
		{}
		catch ( final IllegalAccessException e )
		{}
		final TrackSchemeStyleChooserModel m = new TrackSchemeStyleChooserModel();
		final TrackSchemeStyleChooserFrame frame = new TrackSchemeStyleChooserFrame( m );
		frame.setVisible( true );
	}
}
