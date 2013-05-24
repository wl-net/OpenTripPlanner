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

package org.opentripplanner.routing.transit_index;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.Trip;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.edgetype.FrequencyBoard;
import org.opentripplanner.routing.edgetype.PreAlightEdge;
import org.opentripplanner.routing.edgetype.PreBoardEdge;
import org.opentripplanner.routing.edgetype.TransitBoardAlight;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.services.TransitIndexService;
import org.opentripplanner.util.MapUtils;

import com.google.common.collect.Maps;
import com.vividsolutions.jts.geom.Coordinate;

public class TransitIndexServiceImpl implements TransitIndexService, Serializable {
    
    private static final long serialVersionUID = -8147894489513820239L;
    private Map<String, List<RouteVariant>> variantsByAgency;
    private Map<AgencyAndId, List<RouteVariant>> variantsByRoute;
    private Map<AgencyAndId, RouteVariant> variantsByTrip;
    private Map<AgencyAndId, PreAlightEdge> preAlightEdgesByStop;
    private Map<AgencyAndId, PreBoardEdge> preBoardEdgesByStop;
    private Map<AgencyAndId, HashSet<String>> directionsForRoute;
    private Map<AgencyAndId, HashSet<Stop>> stopsForRoute;
    private List<TraverseMode> modes;
    private Map<String, List<ServiceCalendar>> calendarsByAgency = Maps.newHashMap();
    private Map<String, List<ServiceCalendarDate>> calendarDatesByAgency = Maps.newHashMap();
    private Map<String, Agency> agencies = Maps.newHashMap();
    private Coordinate center;
    private int overnightBreak;

    public TransitIndexServiceImpl(
            Map<String, List<RouteVariant>> variantsByAgency,
            Map<AgencyAndId, List<RouteVariant>> variantsByRoute,
            Map<AgencyAndId, RouteVariant> variantsByTrip,
            Map<AgencyAndId, PreBoardEdge> preBoardEdges,
            Map<AgencyAndId, PreAlightEdge> preAlightEdges,
            Map<AgencyAndId, HashSet<String>> directionsByRoute,
            Map<AgencyAndId, HashSet<Stop>> stopsByRoute, List<TraverseMode> modes) {
        this.variantsByAgency = variantsByAgency;
        this.variantsByRoute = variantsByRoute;
        this.variantsByTrip = variantsByTrip;
        this.preBoardEdgesByStop = preBoardEdges;
        this.preAlightEdgesByStop = preAlightEdges;
        this.directionsForRoute = directionsByRoute;
        this.stopsForRoute = stopsByRoute;
        this.modes = modes;
    }

    public void merge(
            Map<String, List<RouteVariant>> variantsByAgency,
            Map<AgencyAndId, List<RouteVariant>> variantsByRoute,
            Map<AgencyAndId, RouteVariant> variantsByTrip,
            Map<AgencyAndId, PreBoardEdge> preBoardEdges,
            Map<AgencyAndId, PreAlightEdge> preAlightEdges,
            Map<AgencyAndId, HashSet<String>> directionsByRoute,
            Map<AgencyAndId, HashSet<Stop>> stopsByRoute, List<TraverseMode> modes) {

        MapUtils.mergeInUnique(this.variantsByAgency, variantsByAgency);
        MapUtils.mergeInUnique(this.variantsByRoute, variantsByRoute);
        this.variantsByTrip.putAll(variantsByTrip);
        this.preBoardEdgesByStop.putAll(preBoardEdges);
        this.preAlightEdgesByStop.putAll(preAlightEdges);
        MapUtils.mergeInUnique(this.directionsForRoute, directionsByRoute);
        MapUtils.mergeInUnique(this.stopsForRoute, stopsByRoute);
        for (TraverseMode mode : modes) {
            if (!this.modes.contains(mode)) {
                this.modes.add(mode);
            }
        }
    }

    @Override
    public List<RouteVariant> getVariantsForAgency(String agency) {
        List<RouteVariant> variants = variantsByAgency.get(agency);
        if (variants == null) {
            return Collections.emptyList();
        }
        return variants;
    }

    @Override
    public List<RouteVariant> getVariantsForRoute(AgencyAndId route) {
        List<RouteVariant> variants = variantsByRoute.get(route);
        if (variants == null) {
            return Collections.emptyList();
        }
        return variants;
    }

    @Override
    public RouteVariant getVariantForTrip(AgencyAndId trip) {
        return variantsByTrip.get(trip);
    }

    @Override
    public PreAlightEdge getPreAlightEdge(AgencyAndId stop) {
        return preAlightEdgesByStop.get(stop);
    }

    @Override
    public PreBoardEdge getPreBoardEdge(AgencyAndId stop) {
        return preBoardEdgesByStop.get(stop);
    }

    @Override
    public Collection<String> getDirectionsForRoute(AgencyAndId route) {
        return directionsForRoute.get(route);
    }

    @Override
    public List<TraverseMode> getAllModes() {
        return modes;
    }

    @Override
    public List<String> getAllAgencies() {
        return new ArrayList<String>(variantsByAgency.keySet());
    }

    @Override
    public Collection<AgencyAndId> getAllRouteIds() {
        return variantsByRoute.keySet();
    }

    @Override
    public void addCalendars(Collection<ServiceCalendar> allCalendars) {
        for (ServiceCalendar calendar : allCalendars) {
            MapUtils.addToMapList(calendarsByAgency, calendar.getServiceId().getAgencyId(),
                    calendar);
        }
    }

    @Override
    public void addCalendarDates(Collection<ServiceCalendarDate> allDates) {
        for (ServiceCalendarDate date : allDates) {
            MapUtils.addToMapList(calendarDatesByAgency, date.getServiceId().getAgencyId(), date);
        }
    }

    @Override
    public List<ServiceCalendarDate> getCalendarDatesByAgency(String agency) {
        return calendarDatesByAgency.get(agency);
    }

    @Override
    public List<ServiceCalendar> getCalendarsByAgency(String agency) {
        return calendarsByAgency.get(agency);
    }

    @Override
    public Agency getAgency(String id) {
        return agencies.get(id);
    }

    public void addAgency(Agency agency) {
        agencies.put(agency.getId(), agency);
    }

    @Override
    public List<AgencyAndId> getRoutesForStop(AgencyAndId stop) {
        HashSet<AgencyAndId> out = new HashSet<AgencyAndId>();
        Edge edge = preBoardEdgesByStop.get(stop);
        if (edge == null)
            return new ArrayList<AgencyAndId>();
        for (Edge e: edge.getToVertex().getOutgoing()) {
            if (e instanceof TransitBoardAlight && ((TransitBoardAlight) e).isBoarding()) {
                TransitBoardAlight board = (TransitBoardAlight) e;
                for (Trip t : board.getPattern().getTrips()) {
                    out.add(t.getRoute().getId());
                }
            }
            if (e instanceof FrequencyBoard) {
                // this could benefit from all patterns being required to belong to a single route
                out.add(((FrequencyBoard)e).getPattern().getTrip().getRoute().getId());
            }
        }
        return new ArrayList<AgencyAndId>(out);
    }

    public void setCenter(Coordinate coord) {
        this.center = coord;
    }

    @Override
    public Coordinate getCenter() {
        return center;
    }

    public void setOvernightBreak(int overnightBreak) {
        this.overnightBreak = overnightBreak;
    }

    @Override
    public int getOvernightBreak() {
        return overnightBreak;
    }

    @Override
    public Collection<Stop> getStopsForRoute(AgencyAndId route) {
        return stopsForRoute.get(route);
    }
}
