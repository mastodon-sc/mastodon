package net.trackmate.revised.ui.context;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.trackmate.revised.context.ContextChooser;
import net.trackmate.revised.context.ContextProvider;
import net.trackmate.revised.context.ContextChooser.UpdateListener;

public class ContextChooserPanel< V > extends JPanel implements ContextChooser.UpdateListener
{
	private static final long serialVersionUID = 1L;

	private final ContextChooser< V > contextChooser;

	private class Entry
	{
		private final ContextProvider< V > provider;

		public Entry( final ContextProvider< V > provider )
		{
			this.provider = provider;
		}

		@Override
		public String toString()
		{
			return provider.getContextProviderName();
		}

		public ContextProvider< V > getProvider()
		{
			return provider;
		}
	}

	private final JComboBox< Entry > comboBox;

	public ContextChooserPanel( final ContextChooser< V > contextChooser )
	{
		super( new FlowLayout( FlowLayout.LEADING ) );
		this.contextChooser = contextChooser;
		comboBox = new JComboBox<>();
		comboBox.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				@SuppressWarnings( "unchecked" )
				final Entry entry = ( Entry ) comboBox.getSelectedItem();
				contextChooser.choose( entry.getProvider() );
			}
		} );
		add( new JLabel( "context:" ) );
		add( comboBox );
		contextChooser.addUpdateListener( this );
	}

	@Override
	public synchronized void contextChooserUpdated()
	{
		final DefaultComboBoxModel< Entry > model = new DefaultComboBoxModel<>();
		final ContextProvider< V > chosenProvider = contextChooser.getChosenProvider();
		for ( final ContextProvider< V > provider : contextChooser.getProviders() )
		{
			final Entry entry = new Entry( provider );
			model.addElement( entry );
			if ( provider.equals( chosenProvider ) )
				model.setSelectedItem( entry );
		}
		comboBox.setModel( model );
	}
}
