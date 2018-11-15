package org.mastodon.feature.ui;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.feature.Multiplicity;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.util.Listeners;
import org.scijava.Context;

/**
 * A JPanel, one line, in which the user can select a pair of feature /
 * projection keys.
 *
 * @author Jean-Yves Tinevez
 */
public class FeatureSelectionPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	private static final int MAX_N_SOURCES = 10;

	private Set< FeatureSpec< ?, ? > > featureSpecs;

	private final JComboBox< FeatureSpec< ?, ? > > cbFeatures;

	private final JComboBox< FeatureProjectionSpec > cbProjections;

	private final JLabel lblArrow;

	private final JComboBox< Integer > cbSource1;

	private final JLabel lblAnd;

	private final JComboBox< Integer > cbSource2;

	private final JLabel lblSource1;

	private final JLabel lblSource2;

	private final Listeners.List< UpdateListener > updateListeners;

	private final Component projectionStrut;

	private final Component featureStrut;

	private final Component arrowStrut;

	private final Component source1Strut;

	private final Component andStrut;

	public FeatureSelectionPanel()
	{
		this( new HashSet<>() );
	}

	public FeatureSelectionPanel( final Set< FeatureSpec< ?, ? > > featureSpecs )
	{
		this.updateListeners = new Listeners.SynchronizedList<>();
		// Forwarding listener.
		final ItemListener forwardListener = new ItemListener()
		{

			@Override
			public void itemStateChanged( final ItemEvent e )
			{
				if ( e.getStateChange() == ItemEvent.SELECTED )
					notifyListeners();
			}
		};

		final BoxLayout layout = new BoxLayout( this, BoxLayout.LINE_AXIS );
		setLayout( layout );

		// Feature CB.
		this.cbFeatures = new JComboBox<>();
		cbFeatures.setRenderer( new FeatureSpecListCellRenderer() );
		cbFeatures.addItemListener( new ItemListener()
		{

			@Override
			public void itemStateChanged( final ItemEvent e )
			{
				if ( e.getStateChange() == ItemEvent.SELECTED )
					refreshProjections();
			}
		} );
		add( cbFeatures );
		this.featureStrut = add( Box.createHorizontalStrut( 5 ) );

		// Arrow label
		this.lblArrow = new JLabel( "\u2192" );
		add( lblArrow );
		this.arrowStrut = add( Box.createHorizontalStrut( 5 ) );

		// Projection CB.
		this.cbProjections = new JComboBox<>();
		cbProjections.setRenderer( new FeatureProjectionSpecListCellRenderer() );
		cbProjections.addItemListener( forwardListener );
		add( cbProjections );
		this.projectionStrut = add( Box.createHorizontalStrut( 5 ) );

		// N-sources. Careful! Their display is 1-based!
		final Integer[] sources = new Integer[ MAX_N_SOURCES ];
		for ( int i = 0; i < sources.length; i++ )
			sources[ i ] = i + 1;

		// Source index 1.
		this.lblSource1 = new JLabel( "ch" );
		add( lblSource1 );
		this.cbSource1 = new JComboBox<>( sources );
		cbSource1.addItemListener( forwardListener );
		add( cbSource1 );
		this.source1Strut = add( Box.createHorizontalStrut( 5 ) );

		// & label.
		this.lblAnd = new JLabel( "&" );
		add( lblAnd );
		this.andStrut = add( Box.createHorizontalStrut( 5 ) );

		// Source index 2.
		this.lblSource2 = new JLabel( "ch" );
		add( lblSource2 );
		this.cbSource2 = new JComboBox<>( sources );
		cbSource2.addItemListener( forwardListener );
		add( cbSource2 );

		setFeatureSpecs( featureSpecs );
	}

	private void notifyListeners()
	{
		for ( final UpdateListener l : updateListeners.list )
			l.featureKeyChanged();
	}

	/**
	 * Exposes the listeners that will be notified when a change is made in this
	 * panel.
	 *
	 * @return the listeners.
	 */
	public Listeners< UpdateListener > updateListeners()
	{
		return updateListeners;
	}

	/**
	 * Returns the key pair corresponding to the selection in this panel, as an
	 * 2-elements string array.
	 * <p>
	 * The first element is the feature key ({@link FeatureSpec#getKey()}) or the
	 * empty string if there is no selection.
	 * <p>
	 * The second element is the feature projection key, built from the selected
	 * feature projection name ({@link FeatureProjectionSpec#projectionName}), the
	 * feature multiplicity and the source indices selected in this panel. Or the
	 * empty string if there is no selection.
	 *
	 * @return a 2-element string array.
	 */
	public String[] getSelection()
	{
		final FeatureSpec< ?, ? > featureSpec = ( FeatureSpec< ?, ? > ) cbFeatures.getSelectedItem();
		if ( null == featureSpec )
			return new String[] { "", "" };

		final FeatureProjectionSpec projectionSpec = ( FeatureProjectionSpec ) cbProjections.getSelectedItem();
		if (null == projectionSpec)
			return new String[] { featureSpec.getKey(), "" };

		return new String[] {
				featureSpec.getKey(),
				projectionSpec.projectionKey(
						featureSpec.getMultiplicity(),
						// We use the index, since the display is 1-based.
						cbSource1.getSelectedIndex(),
						cbSource2.getSelectedIndex() ) };
	}

	public void setSelection( final String[] selection )
	{
		final String featureKey = selection[ 0 ];
		final FeatureSpec< ?, ? > featureSpec = getFeatureSpecFromKey( featureKey );
		cbFeatures.setSelectedItem( featureSpec );
		if ( null == featureSpec )
			return;

		final String projectionKey = selection[ 1 ];
		final String projectionName = featureSpec.getMultiplicity().nameFromKey( projectionKey );
		final FeatureProjectionSpec featureProjectionSpec = getFeatureProjectionSpecFromName( featureSpec, projectionName );
		cbProjections.setSelectedItem( featureProjectionSpec );

		final int[] indices = featureSpec.getMultiplicity().indicesFromKey( projectionKey );
		if ( indices.length > 0 )
		{
			cbSource1.setSelectedIndex( indices[ 0 ] );
			if ( indices.length > 1 )
				cbSource2.setSelectedIndex( indices[ 1 ] );
		}
	}

	private FeatureProjectionSpec getFeatureProjectionSpecFromName( final FeatureSpec< ?, ? > featureSpec, final String projectionName )
	{
		final FeatureProjectionSpec[] projectionSpecs = featureSpec.getProjectionSpecs();
		for ( final FeatureProjectionSpec featureProjectionSpec : projectionSpecs )
		{
			if ( featureProjectionSpec.projectionName.equals( projectionName ) )
				return featureProjectionSpec;
		}
		return null;
	}

	private FeatureSpec< ?, ? > getFeatureSpecFromKey( final String key )
	{
		for ( final FeatureSpec< ?, ? > featureSpec : featureSpecs )
		{
			if ( featureSpec.getKey().equals( key ) )
				return featureSpec;
		}
		return null;
	}

	private void refreshProjections()
	{
		final FeatureSpec< ?, ? > currentSelection = ( FeatureSpec< ?, ? > ) cbFeatures.getSelectedItem();

		// Projections.
		final FeatureProjectionSpec[] projectionSpecs = ( currentSelection == null )
				? new FeatureProjectionSpec[] {}
				: currentSelection.getProjectionSpecs();
		cbProjections.setModel( new DefaultComboBoxModel<>( projectionSpecs ) );

		// Visibility.
		final boolean projectionCBVisible = ( null != currentSelection ) &&
				( ( projectionSpecs.length > 1 )
						|| currentSelection.getMultiplicity() != Multiplicity.SINGLE );
		arrowStrut.setVisible( projectionCBVisible );
		cbProjections.setVisible( projectionCBVisible );
		lblArrow.setVisible( projectionCBVisible );
		featureStrut.setVisible( projectionCBVisible );

		projectionStrut.setVisible( projectionCBVisible && ( currentSelection.getMultiplicity() != Multiplicity.SINGLE ) );
		lblSource1.setVisible( projectionCBVisible && ( currentSelection.getMultiplicity() != Multiplicity.SINGLE ) );
		cbSource1.setVisible( projectionCBVisible && ( currentSelection.getMultiplicity() != Multiplicity.SINGLE ) );

		source1Strut.setVisible( projectionCBVisible && ( currentSelection.getMultiplicity() == Multiplicity.ON_SOURCE_PAIRS ) );
		lblAnd.setVisible( projectionCBVisible && ( currentSelection.getMultiplicity() == Multiplicity.ON_SOURCE_PAIRS ) );
		andStrut.setVisible( projectionCBVisible && ( currentSelection.getMultiplicity() == Multiplicity.ON_SOURCE_PAIRS ) );

		lblSource2.setVisible( projectionCBVisible && ( currentSelection.getMultiplicity() == Multiplicity.ON_SOURCE_PAIRS ) );
		cbSource2.setVisible( projectionCBVisible && ( currentSelection.getMultiplicity() == Multiplicity.ON_SOURCE_PAIRS ) );

		notifyListeners();
	}

	/**
	 * Sets the feature specifications to display in this panel.
	 *
	 * @param featureSpecs
	 *                         the feature specifications.
	 */
	public void setFeatureSpecs( final Set< FeatureSpec< ?, ? > > featureSpecs )
	{
		if ( null == this.featureSpecs || !this.featureSpecs.equals( featureSpecs ) )
		{
			this.featureSpecs = featureSpecs;
			final FeatureSpec< ?, ? > previousSelection = ( FeatureSpec< ?, ? > ) cbFeatures.getSelectedItem();
			cbFeatures.setModel( new FeatureSpecComboBoxModel( featureSpecs ) );
			if ( null == previousSelection && cbFeatures.getModel().getSize() > 0  )
				cbFeatures.setSelectedIndex( 0 );
			else
			{
				cbFeatures.setSelectedItem( previousSelection );
				final Object selectedItem = cbFeatures.getSelectedItem();
				if (null == selectedItem && cbFeatures.getModel().getSize() > 0)
					cbFeatures.setSelectedIndex( 0 );
			}
		}
	}

	private class FeatureSpecComboBoxModel extends AbstractListModel< FeatureSpec< ?, ? > > implements ComboBoxModel< FeatureSpec< ?, ? > >
	{

		private static final long serialVersionUID = 1L;

		private final List< FeatureSpec< ?, ? > > featureSpecs;

		private FeatureSpec< ?, ? > selectedItem;

		public FeatureSpecComboBoxModel( final Collection< FeatureSpec< ?, ? > > featureSpecs )
		{
			this.featureSpecs = new ArrayList<>( featureSpecs );
			this.featureSpecs.sort( Comparator.comparing( FeatureSpec::getKey ) );
		}

		@Override
		public int getSize()
		{
			return featureSpecs.size();
		}

		@Override
		public FeatureSpec< ?, ? > getElementAt( final int index )
		{
			if ( index < 0 || index >= featureSpecs.size() )
				return null;
			return featureSpecs.get( index );
		}

		@Override
		public FeatureSpec< ?, ? > getSelectedItem()
		{
			return selectedItem;
		}

		@Override
		public void setSelectedItem( final Object object )
		{
			// No item is selected and object is null, so no change required.
			if ( selectedItem == null && object == null )
				return;

			// Bad class
			if ( !( object instanceof FeatureSpec ) )
				return;

			// object is already selected so no change required.
			if ( selectedItem != null && selectedItem.equals( object ) )
				return;

			// Simply return if object is not in the list.
			if ( object != null && getIndexOf( object ) == -1 )
				return;

			// Here we know that object is either an item in the list or null.

			// Handle the three change cases: selectedItem is null, object is
			// non-null; selectedItem is non-null, object is null;
			// selectedItem is non-null, object is non-null and they're not
			// equal.
			selectedItem = ( FeatureSpec< ?, ? > ) object;
			fireContentsChanged( this, -1, -1 );
		}

		public int getIndexOf( final Object object )
		{
			// Maybe no need for binary search even if the list is sorted.
			return featureSpecs.indexOf( object );
		}
	}

	private static final class FeatureSpecListCellRenderer extends DefaultListCellRenderer
	{

		private static final long serialVersionUID = 1L;

		@Override
		public Component getListCellRendererComponent( final JList< ? > list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus )
		{
			final Component renderer = super.getListCellRendererComponent(
					list,
					( null == value ) ? "" : ( ( FeatureSpec< ?, ? > ) value ).getKey(),
					index,
					isSelected,
					cellHasFocus );

			return renderer;
		}
	}

	private static final class FeatureProjectionSpecListCellRenderer extends DefaultListCellRenderer
	{

		private static final long serialVersionUID = 1L;

		@Override
		public Component getListCellRendererComponent( final JList< ? > list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus )
		{
			final Component renderer = super.getListCellRendererComponent(
					list,
					( null == value ) ? "" : ( ( FeatureProjectionSpec ) value ).projectionName,
					index,
					isSelected,
					cellHasFocus );

			return renderer;
		}
	}

	public static interface UpdateListener
	{
		public void featureKeyChanged();
	}

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		setDefaultLocale( Locale.ROOT );

		final Context context = new Context( FeatureSpecsService.class );
		final FeatureSpecsService specsService = context.getService( FeatureSpecsService.class );

		final FeatureSelectionPanel selectionPanel = new FeatureSelectionPanel();
		selectionPanel.updateListeners().add( () -> System.out.println( Arrays.toString( selectionPanel.getSelection() ) ) );

		final JFrame frame = new JFrame( "Feature selection panel" );
		frame.getContentPane().add( selectionPanel );
		frame.setVisible( true );

		selectionPanel.setFeatureSpecs( new HashSet<>( specsService.getSpecs( Spot.class ) ) );
		frame.pack();
	}
}
