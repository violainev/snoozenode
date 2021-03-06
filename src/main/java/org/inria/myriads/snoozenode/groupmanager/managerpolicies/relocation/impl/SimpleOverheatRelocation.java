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
import java.util.List;

import org.inria.myriads.snoozecommon.communication.localcontroller.LocalControllerDescription;
import org.inria.myriads.snoozecommon.communication.virtualcluster.VirtualMachineMetaData;
import org.inria.myriads.snoozecommon.guard.Guard;
import org.inria.myriads.snoozecommon.util.MathUtils;
import org.inria.myriads.snoozenode.groupmanager.estimator.ResourceDemandEstimator;
import org.inria.myriads.snoozenode.groupmanager.managerpolicies.reconfiguration.ReconfigurationPlan;
import org.inria.myriads.snoozenode.groupmanager.managerpolicies.relocation.VirtualMachineRelocation;
import org.inria.myriads.snoozenode.groupmanager.managerpolicies.relocation.utility.RelocationUtility;
import org.inria.myriads.snoozenode.groupmanager.managerpolicies.util.SortUtils;
import org.inria.myriads.snoozenode.localcontroller.monitoring.enums.LocalControllerState;
import org.inria.myriads.snoozenode.util.OutputUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Overheat relocation policy.
 * 
 * @author Eugen Feller
 */
public final class SimpleOverheatRelocation 
    implements VirtualMachineRelocation 
{
    /** Define the logger. */
    private static final Logger log_ = LoggerFactory.getLogger(SimpleOverheatRelocation.class);
    
    /** Resource demand estimator. */
    private ResourceDemandEstimator estimator_;

    /**
     * Constructor.
     * 
     * @param estimator     The resource demand estimator
     */
    public SimpleOverheatRelocation(ResourceDemandEstimator estimator)
    {
        Guard.check(estimator);
        log_.debug("Initializing the overheat relocation policy");       
        estimator_ = estimator;
    }

    /**
     * Compute migration candidates.
     * 
     * @param virtualMachines       The virtual machines
     * @param overloadCapacity      The overload capacity
     * @return                      The migration candidates
     */
  /*  private List<VirtualMachineMetaData> getMigrationCandidates(List<VirtualMachineMetaData> virtualMachines,
                                                                List<Double> overloadCapacity)
    {
        log_.debug("Computing list of migration candidates");
        
        List<VirtualMachineMetaData> migrationCandidates = new ArrayList<VirtualMachineMetaData>();       
        for (VirtualMachineMetaData metaData : virtualMachines)
        {            
            String virtualMachineId = metaData.getVirtualMachineLocation().getVirtualMachineId();
            List<Double> virtualMachineUsage = estimator_.estimateVirtualMachineResourceDemand(metaData);
            log_.debug(String.format("Estimated virtual machine %s resource demand: %s. Overload capacity: %s", 
                                      virtualMachineId,
                                      virtualMachineUsage, 
                                      overloadCapacity));
            
            if (MathUtils.vectorCompareIsGreater(virtualMachineUsage, overloadCapacity))
            {
                log_.debug(String.format("Virtual machine %s added to the list of candidates!", virtualMachineId));
                migrationCandidates.add(metaData);
                return migrationCandidates;
            }
        }
        
        List<Double> tmpUsage = MathUtils.createEmptyVector();
        for (VirtualMachineMetaData metaData : virtualMachines)
        {
            migrationCandidates.add(metaData);         
            List<Double> virtualMachineUsage = estimator_.estimateVirtualMachineResourceDemand(metaData);            
            tmpUsage = MathUtils.addVectors(tmpUsage, virtualMachineUsage);
            log_.debug(String.format("Estimated virtual machine %s resource demand: %s. Total demand: %s", 
                                     metaData.getVirtualMachineLocation().getVirtualMachineId(),
                                     virtualMachineUsage, 
                                     tmpUsage));          
            if (MathUtils.vectorCompareIsGreater(tmpUsage, overloadCapacity))
            {
                break;
            }
        }
        
        return migrationCandidates;
    }*/
    
    /**
     * Computes a migration plan to relocates virtual machines.
     * 
     * @param sourceLocalController         The source local controller description
     * @param destinationLocalControllers   The destination local controller candidates
     * @return                              The migration plan
     */
    public ReconfigurationPlan relocateVirtualMachines(LocalControllerDescription sourceLocalController, 
                                                 List<LocalControllerDescription> destinationLocalControllers)
    {
        log_.debug("Starting to compute the moderate heat migration plan");

        List<VirtualMachineMetaData> virtualMachines = 
            new ArrayList<VirtualMachineMetaData>(sourceLocalController.getVirtualMachineMetaData().values());
        
      //  SortUtils.sortVirtualMachinesIncreasing(virtualMachines, estimator_);
        OutputUtils.printVirtualMachines(virtualMachines);
                            
     //   List<VirtualMachineMetaData> migrationCandidates = getMigrationCandidates(virtualMachines, overloadCapacity);  
        
     //   SortUtils.sortLocalControllersIncreasing(destinationLocalControllers, estimator_);
        

        
        
        ReconfigurationPlan reconfigurationPlan = 
                RelocationUtility.computeReconfigurationPlan(virtualMachines,  
                                                             destinationLocalControllers, 
                                                             estimator_,
                                                             LocalControllerState.OVERHEATED);
        return reconfigurationPlan;
    }


}
