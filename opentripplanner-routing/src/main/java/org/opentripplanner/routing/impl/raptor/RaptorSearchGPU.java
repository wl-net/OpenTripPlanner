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

package org.opentripplanner.routing.impl.raptor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opentripplanner.common.pqueue.BinHeap;
import org.opentripplanner.common.pqueue.OTPPriorityQueue;
import org.opentripplanner.common.pqueue.OTPPriorityQueueFactory;
import org.opentripplanner.routing.algorithm.GenericDijkstra;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateEditor;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.spt.ShortestPathTree;
import org.opentripplanner.routing.vertextype.TransitStop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.syr.pcpratts.rootbeer.runtime.Kernel;
import edu.syr.pcpratts.rootbeer.runtime.Rootbeer;

class LocalSearchKernel implements Kernel {
    private GenericDijkstra search;

    private RaptorSearchGPU raptor;

    private State initialState;

    private int nBoardings;

    public LocalSearchKernel(int nBoardings, State state, GenericDijkstra search, RaptorSearchGPU raptor) {
        this.nBoardings = nBoardings;
        this.initialState = state;
        this.search = search;
        this.raptor = raptor;
    }

    @Override
    public void gpuMethod() {
        ShortestPathTree spt = search.getShortestPathTree(initialState);
        SPTSTATE: for (State state : spt.getAllStates()) {
            final Vertex vertex = state.getVertex();
            if (!(vertex instanceof TransitStop))
                continue;

            RaptorStop stop = raptor.data.raptorStopsForStopId.get(((TransitStop) vertex).getStopId());
            if (stop == null)
                // we have found a stop is totally unused, so skip it
                continue;

            RaptorState parent = (RaptorState) state.getExtension("raptorParent");
            RaptorState newState;
            if (parent != null) {
                newState = new RaptorState(parent);
            } else {
                // this only happens in round 0
                newState = new RaptorState(state.getOptions().arriveBy);
            }
            newState.weight = state.getWeight();
            newState.nBoardings = nBoardings;
            newState.walkDistance = state.getWalkDistance();
            newState.arrivalTime = (int) state.getTime();
            newState.walkPath = state;
            newState.stop = stop;

            synchronized(raptor) {

                List<RaptorState> states = raptor.statesByStop[stop.index];
                if (states == null) {
                    states = new ArrayList<RaptorState>();
                    raptor.statesByStop[stop.index] = states;
                }

                for (RaptorState oldState : states) {
                    if (oldState.eDominates(newState)) {
                        continue SPTSTATE;
                    }
                }
                states.add(newState);
            }
            synchronized(raptor.visitedLastRound) {
                raptor.visitedLastRound.add(stop);
                raptor.visitedEver.add(stop);
            }
        }
    }
    
}

public class RaptorSearchGPU extends RaptorSearch {
    private static final Logger log = LoggerFactory.getLogger(RaptorSearchGPU.class);

    @SuppressWarnings("unchecked")
    RaptorSearchGPU(RaptorData data, RoutingRequest options) {
        super(data,options);
    }
    public void walkPhase(RoutingRequest options, RoutingRequest walkOptions, int nBoardings,
            List<RaptorState> createdStates) {

        final double distanceToNearestTransitStop = options.rctx.target
                .getDistanceToNearestTransitStop();
        ShortestPathTree spt;
        GenericDijkstra dijkstra = new GenericDijkstra(walkOptions);
        //dijkstra.setShortestPathTreeFactory(bounder);

        if (nBoardings == 0) {
            //TODO: retry min-time bounding with this and with maxtime

            if (bounder.getTargetDistance(options.rctx.origin) < options.getMaxWalkDistance())
                dijkstra.setHeuristic(bounder);

            MaxWalkState start = new MaxWalkState(options.rctx.origin, walkOptions);
            spt = dijkstra.getShortestPathTree(start);
            SPTSTATE: for (State state : spt.getAllStates()) {
                final Vertex vertex = state.getVertex();
                if (vertex instanceof TransitStop) {
                    RaptorStop stop = data.raptorStopsForStopId.get(((TransitStop) vertex).getStopId());
                    if (stop == null) {
                        // we have found a stop is totally unused, so skip it
                        continue;
                    }

                    List<RaptorState> states = statesByStop[stop.index];
                    if (states == null) {
                        states = new ArrayList<RaptorState>();
                        statesByStop[stop.index] = states;
                    }

                    RaptorState newState = new RaptorState(options.arriveBy);
                    newState.weight = state.getWeight();
                    newState.nBoardings = nBoardings;
                    newState.walkDistance = state.getWalkDistance();
                    newState.arrivalTime = (int) state.getTime();
                    newState.walkPath = state;
                    newState.stop = stop;

                    for (RaptorState oldState : states) {
                        if (oldState.eDominates(newState)) {
                            continue SPTSTATE;
                        }
                    }

                    visitedLastRound.add(stop);
                    visitedEver.add(stop);
                    states.add(newState);
                }
            }
            
        } else {

            final List<MaxWalkState> startPoints = new ArrayList<MaxWalkState>();

            List<Kernel> jobs = new ArrayList<Kernel>();
            for (RaptorState state : createdStates) {

                // bounding states
                // this reduces the number of initial vertices
                // and the state space size

                Vertex stopVertex = state.stop.stopVertex;

                double minWalk = distanceToNearestTransitStop;

                double targetDistance = bounder.getTargetDistance(stopVertex);

                if (targetDistance + state.walkDistance > options.getMaxWalkDistance()) {
                    // can't walk to destination, so we can't alight at a local vertex
                    if (state.stop.stopVertex.isLocal())
                        continue;
                }

                if (minWalk + state.walkDistance > options.getMaxWalkDistance()) {
                    continue;
                }

                StateEditor dijkstraState = new MaxWalkState.MaxWalkStateEditor(walkOptions,
                        stopVertex);
                dijkstraState.setStartTime(options.dateTime);
                dijkstraState.setNumBoardings(state.nBoardings);
                dijkstraState.setWalkDistance(state.walkDistance);
                dijkstraState.setTime(state.arrivalTime);
                dijkstraState.setExtension("raptorParent", state);
                dijkstraState.setOptions(walkOptions);
                dijkstraState.incrementWeight(state.weight);
                MaxWalkState newState = (MaxWalkState) dijkstraState.makeState();
                jobs.add(new LocalSearchKernel(nBoardings, newState, dijkstra, this));

            }
            System.out.println("walk starts: " + startPoints.size() + " / " + visitedEver.size());
//            dijkstra.setPriorityQueueFactory(new PrefilledPriorityQueueFactory(startPoints.subList(
//                    1, startPoints.size())));

//          bounder.addSptStates(startPoints.subList(1, startPoints.size()));

//            bounder.prepareForSearch();

            dijkstra.setSearchTerminationStrategy(bounder);
            dijkstra.setSkipTraverseResultStrategy(bounder);
            
            Rootbeer rootbeer = new Rootbeer();
            rootbeer.runAll(jobs);
            System.out.println("Ran " + jobs.size() + " jobs");
        }

        final List<? extends State> targetStates = bounder.bounders;
        if (targetStates != null) {
            TARGET: for (State targetState : targetStates) {
                RaptorState parent = (RaptorState) targetState.getExtension("raptorParent");
                RaptorState state;
                if (parent != null) {
                    state = new RaptorState(parent);
                    state.nBoardings = parent.nBoardings;
                } else {
                    state = new RaptorState(options.arriveBy);
                }
                state.weight = targetState.getWeight();
                state.walkDistance = targetState.getWalkDistance();
                state.arrivalTime = (int) targetState.getTime();
                state.walkPath = targetState;
                for (Iterator<RaptorState> it = getTargetStates().iterator(); it.hasNext();) {
                    RaptorState oldState = it.next();
                    if (oldState.eDominates(state)) {
                        continue TARGET;
                    } else if (state.eDominates(oldState)) {
                        it.remove();
                    }
                }
                addTargetState(state);
                log.debug("Found target at: " + state);
            }
        }
        for (State state : bounder.removedBoundingStates) {
            removeTargetState(state);
        }
    }

    class PrefilledPriorityQueueFactory implements OTPPriorityQueueFactory {

        private List<? extends State> startPoints;

        public PrefilledPriorityQueueFactory(List<? extends State> startPoints) {
            this.startPoints = startPoints;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public <T> OTPPriorityQueue<T> create(int maxSize) {
            BinHeap heap = new BinHeap<T>();
            for (State state : startPoints) {
                heap.insert(state, state.getWeight());
            }
            return heap;
        }

    }

}
