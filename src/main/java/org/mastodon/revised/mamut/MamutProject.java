package org.mastodon.revised.mamut;

import java.io.File;

public class MamutProject
{
	/**
	 * Relative paths in the XML should be interpreted with respect to this.
	 */
	private File basePath;

	private final File datasetXmlFile;

	private File rawModelFile;

	public MamutProject(
			final File basePath,
			final File datasetXmlFile,
			final File rawModelFile )
	{
		this.basePath = basePath;
		this.datasetXmlFile = datasetXmlFile;
		this.rawModelFile = rawModelFile;
	}

	/**
	 * Get the base path of the project. Relative paths in the XML project
	 * description are interpreted with respect to this.
	 *
	 * @return the base path of the project
	 */
	public File getBasePath()
	{
		return basePath;
	}

	public void setBasePath( final File basePath )
	{
		this.basePath = basePath;
	}

	public File getDatasetXmlFile()
	{
		return datasetXmlFile;
	}

	public File getRawModelFile()
	{
		return rawModelFile;
	}

	public void setRawModelFile( final File rawModelFile )
	{
		this.rawModelFile = rawModelFile;
	}

	/**
	 * Derive a name for the raw model file from the name of the project file:
	 * Replace {@code .xml} extension with {@code .raw}. If the project name
	 * does not end in {@code .xml}, simply append {@code .raw}.
	 *
	 * @param projectFile
	 *            the project file.
	 * @return the proposed raw model file
	 */
	public static File deriveRawModelFile( final File projectFile )
	{
		final String name = projectFile.getAbsolutePath();
		if ( name.endsWith( ".xml" ) )
			return new File( name.substring( 0, name.length() - ".xml".length() ) + ".raw" );
		else
			return new File( name + ".raw" );
	}
}
