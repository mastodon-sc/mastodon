package net.trackmate;

import net.imglib2.RealLocalizable;

public interface Spot extends RealLocalizable
{
	public long getId();

	public double getX();

	public double getY();

	public double getZ();
}
