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

package org.opentripplanner.routing.car_rental;

import java.io.Serializable;
import java.util.HashMap;
import javax.xml.bind.annotation.XmlAttribute;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class Car2GoVehicle extends CarRentalVehicle implements Serializable {
	private static final long serialVersionUID = -5559496362882593038L;

	@XmlAttribute
	@JsonSerialize
	private String name;
	
	@XmlAttribute
	@JsonSerialize
	private String description;
	
	@XmlAttribute
	@JsonSerialize
	private HashMap<String, String> extended_fields;
	
	@XmlAttribute
	@JsonSerialize
	private double x,y;
	
	@Override
    public boolean equals(Object o) {
        if (!(o instanceof Car2GoVehicle)) {
            return false;
        }
        Car2GoVehicle other = (Car2GoVehicle) o;
        boolean same = false;
        
        if (null != this.extended_fields.get("vin")
        		&& null != other.extended_fields.get("vin")
        		&& this.extended_fields.get("vin").equals(other.extended_fields.get("vin"))) {
        	same = true;
        }
        
		return same;
    }
	
	@Override
	public int hashCode() {
		return this.extended_fields.get("vin").hashCode() + 1;
	}
}
