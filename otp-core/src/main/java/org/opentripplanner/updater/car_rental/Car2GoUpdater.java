/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package org.opentripplanner.updater.car_rental;

import java.util.List;
import java.util.prefs.Preferences;

import org.opentripplanner.routing.car_rental.CarRentalVehicle;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.updater.PreferencesConfigurable;

public class Car2GoUpdater implements PreferencesConfigurable, CarRentalDataSource {

	@Override
	public void configure(Graph graph, Preferences preferences)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean update() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<CarRentalVehicle> getVehicles() {
		// TODO Auto-generated method stub
		return null;
	}

}
