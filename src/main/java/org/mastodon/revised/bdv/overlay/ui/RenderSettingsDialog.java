package org.mastodon.revised.bdv.overlay.ui;

import java.awt.BorderLayout;
import java.awt.Component;
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

import org.mastodon.revised.bdv.overlay.RenderSettings;
import org.mastodon.revised.bdv.overlay.RenderSettings.UpdateListener;

/**
 * An editor and manager for BDV RenderSettings.
 *
 * @author Jean=Yves Tinevez &lt;jeanyves.tinevez@gmail.com&gt;
 */
class RenderSettingsDialog extends JDialog
{

	private static final long serialVersionUID = 1L;

	JButton buttonDeleteStyle;

	JButton buttonNewStyle;

	JButton buttonSetStyleName;

	JButton okButton;

	JButton saveButton;

	public RenderSettingsDialog( final Frame owner, final DefaultComboBoxModel< RenderSettings > model, final RenderSettings targetSettings )
	{
		super( owner, "BDV render settings chooser", false );

		final JPanel dialogPane = new JPanel();
		final JPanel contentPanel = new JPanel();
		final JPanel panelChooseStyle = new JPanel();
		final JLabel jlabelTitle = new JLabel();
		final RenderSettingsPanel renderSettingsPanel = new RenderSettingsPanel( targetSettings );

		final JComboBox< RenderSettings > comboBoxStyles = new JComboBox<>( model );
		// Update common render settings when a settings is chosen in the menu.
		comboBoxStyles.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				final RenderSettings rs = comboBoxStyles.getItemAt( comboBoxStyles.getSelectedIndex() );
				targetSettings.set( rs );
				enableComponents( renderSettingsPanel, !RenderSettings.defaults.contains( rs ) );
			}
		} );
		comboBoxStyles.setSelectedIndex( 0 );

		// Update menu settings when the common render settings is changed.
		targetSettings.addUpdateListener( new UpdateListener()
		{

			@Override
			public void renderSettingsChanged()
			{
				comboBoxStyles.getItemAt( comboBoxStyles.getSelectedIndex() ).set( targetSettings );
			}
		} );


		final JPanel panelStyleButtons = new JPanel();
		buttonDeleteStyle = new JButton();
		final JPanel hSpacer1 = new JPanel( null );
		buttonNewStyle = new JButton();
		buttonSetStyleName = new JButton();
		final JPanel buttonBar = new JPanel();
		okButton = new JButton();
		saveButton = new JButton();

		// ======== this ========
		setTitle( "BDV render settings" );
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
					jlabelTitle.setText( "BDV render settings." );
					jlabelTitle.setHorizontalAlignment( SwingConstants.CENTER );
					jlabelTitle.setFont( dialogPane.getFont().deriveFont( Font.BOLD ) );
					panelChooseStyle.add( jlabelTitle );

					// Combo box panel
					final JPanel comboBoxPanel = new JPanel();
					{
						final BorderLayout layout = new BorderLayout();
						comboBoxPanel.setLayout( layout );
						comboBoxPanel.add( new JLabel( "Render settings: " ), BorderLayout.WEST );
						comboBoxPanel.add( comboBoxStyles, BorderLayout.CENTER );
					}

					panelChooseStyle.add( comboBoxPanel );

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

					}
					panelChooseStyle.add( panelStyleButtons );
				}
				contentPanel.add( panelChooseStyle, BorderLayout.NORTH );
				contentPanel.add( renderSettingsPanel, BorderLayout.CENTER );


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

	private static final void enableComponents( final Container container, final boolean enable )
	{
		final Component[] components = container.getComponents();
		for ( final Component component : components )
		{
			component.setEnabled( enable );
			if ( component instanceof Container )
			{
				enableComponents( ( Container ) component, enable );
			}
		}
	}

}
