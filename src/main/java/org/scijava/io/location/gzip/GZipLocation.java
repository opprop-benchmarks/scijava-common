
package org.scijava.io.location.gzip;

import org.scijava.io.DataHandle;
import org.scijava.io.Location;
import org.scijava.io.location.AbstractHigherOrderLocation;

/**
 * {@link Location} backed by a {@link DataHandle} that is <code>gzip</code>
 * compressed.
 * 
 * @author Gabriel Einsdorf
 * @see GZipHandle
 */
public class GZipLocation extends AbstractHigherOrderLocation {

	public GZipLocation(Location location) {
		super(location);
	}

}
