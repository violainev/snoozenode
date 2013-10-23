/**
 * Copyright (C) 2010-2013 Eugen Feller, INRIA <eugen.feller@inria.fr>
 *
 * This file is part of Snooze, a scalable, autonomic, and
 * energy-aware virtual machine (VM) management framework.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 */
package org.inria.myriads.snoozenode.groupmanager.managerpolicies.relocation.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.inria.myriads.snoozecommon.communication.localcontroller.LocalControllerDescription;
import org.inria.myriads.snoozecommon.communication.localcontroller.LocalControllerStatus;
import org.inria.myriads.snoozecommon.communication.virtualcluster.VirtualMachineMetaData;
import org.inria.myriads.snoozecommon.datastructure.LRUCache;
import org.inria.myriads.snoozecommon.guard.Guard;
import org.inria.myriads.snoozecommon.metric.Metric;
import org.inria.myriads.snoozenode.groupmanager.estimator.ResourceDemandEstimator;
import org.inria.myriads.snoozenode.groupmanager.managerpolicies.reconfiguration.ReconfigurationPlan;
import org.inria.myriads.snoozenode.groupmanager.managerpolicies.relocation.VirtualMachineRelocation;
import org.inria.myriads.snoozenode.groupmanager.managerpolicies.relocation.utility.RelocationUtility;
import org.inria.myriads.snoozenode.groupmanager.managerpolicies.util.SortUtils;
import org.inria.myriads.snoozenode.localcontroller.metrics.transport.AggregatedMetricData;
import org.inria.myriads.snoozecommon.communication.localcontroller.LocalControllerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Most loaded server relocation policy.
 * 
 * @author Eugen Feller
 */
public final class UnderloadRelocation 
    implements VirtualMachineRelocation 
{
    /** Define the logger. */
    private static final Logger log_ = LoggerFactory.getLogger(UnderloadRelocation.class);
    
    /** Resource demand estimator. */
    private ResourceDemandEstimator estimator_;
    
    /**
     * Constructor.
     * 
     * @param estimator     The resource demand estimator
     */
    public UnderloadRelocation(ResourceDemandEstimator estimator)
    {
        Guard.check(estimator);
        estimator_ = estimator;
        log_.debug("Underload relocation policy started");
    }
    
    /**
     * Relocates virtual machines.
     * 
     * @param sourceLocalController         The source local controller description
     * @param destinationLocalControllers   The destination local controller candidates
     * @return                              The migration plan
     */
    public ReconfigurationPlan relocateVirtualMachines(LocalControllerDescription sourceLocalController, 
                                                 List<LocalControllerDescription> destinationLocalControllers)
    {
        Guard.check(sourceLocalController, destinationLocalControllers);
        
        log_.debug(String.format("UnderloadButNoMem: Underload detected local controller: %s",sourceLocalController.getHostname()));
        log_.debug(String.format("Starting to compute the migration plan for local controller: %s", 
                                 sourceLocalController.getId()));
                
      List<VirtualMachineMetaData> candidatevirtualMachines = 
            new ArrayList<VirtualMachineMetaData>(sourceLocalController.getVirtualMachineMetaData().values());
        
        if (candidatevirtualMachines.size() == 0)
        {
        	log_.debug("No VM -> nothing to do");
        	return null;
        }
                
        ArrayList<LocalControllerDescription> candidateLC = new ArrayList<LocalControllerDescription>();
        for (LocalControllerDescription lcd : destinationLocalControllers)
        {
        	LocalControllerDescription copy = new LocalControllerDescription(lcd, 10);
        	if (!copy.getId().equals(sourceLocalController.getId()))
        	{
	           	 List<VirtualMachineMetaData> vms = new ArrayList<VirtualMachineMetaData>(copy.getVirtualMachineMetaData().values());
	           	
	           	 log_.debug(String.format("Other LC %s has %d VM and state : %s",copy.getHostname()
	           			 														, vms.size()
	           			 														, copy.getState()));
	           	 
	           	 LocalControllerState lcState = copy.getState();
	           	 
	         
	         // PRIORITY TO OVERHEAT !
	          
	         	if (lcState.equals(LocalControllerState.OVERHEATED))
	           	 {
	             	log_.debug("At least one LC is overheat, better deal with this anomaly first");
	            	return null;
	           	 }
	          	 
	           	 if (vms.size() > 0 && !lcState.equals(LocalControllerState.OVERHEATED) && !lcState.equals(LocalControllerState.OVERLOADED))
	           	 {
	           		 candidateLC.add(copy);
	           	 }
        	}
        }
        
        if (candidateLC.size() == 0)
        {
        	log_.debug("Only LC with VM (or some LC are overheat/load) -> nothing to do");
        	return null;
        }
        
        
        SortUtils.sortVirtualMachinesDecreasing(candidatevirtualMachines, estimator_);
        
        SortUtils.sortLocalControllersIncreasingTemperature(candidateLC, estimator_, false);
        
        
        ReconfigurationPlan reconfigurationPlan = 
                RelocationUtility.computeReconfigurationPlan(candidatevirtualMachines,
                											 candidateLC, 
                                                             estimator_,
                                                             LocalControllerState.UNDERLOADED);
                                                             
        return reconfigurationPlan;
	           
    }
    
   
    
    
}
