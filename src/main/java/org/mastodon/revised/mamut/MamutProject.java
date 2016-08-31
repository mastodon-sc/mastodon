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
}
