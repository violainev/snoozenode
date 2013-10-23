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
package org.inria.myriads.snoozenode.groupmanager.managerpolicies.comparators;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.inria.myriads.snoozecommon.communication.localcontroller.LocalControllerDescription;
import org.inria.myriads.snoozecommon.communication.localcontroller.LocalControllerStatus;
import org.inria.myriads.snoozecommon.datastructure.LRUCache;
import org.inria.myriads.snoozecommon.guard.Guard;
import org.inria.myriads.snoozecommon.metric.Metric;
import org.inria.myriads.snoozecommon.util.MathUtils;
import org.inria.myriads.snoozenode.groupmanager.estimator.ResourceDemandEstimator;
import org.inria.myriads.snoozenode.localcontroller.metrics.transport.AggregatedMetricData;

/**
 * L1 norm based local controller sorting in increasing temperature
 * 
 * @author Eugen Feller
 */
public class LocalControllerIncreasingTemperatureLast
    implements Comparator<LocalControllerDescription> 
{
    /** Resource demand estimator. */
    private ResourceDemandEstimator estimator_;
    
    /**
     * Constructor.
     * 
     * @param estimator     The resource demand estimator
     */
    public LocalControllerIncreasingTemperatureLast(ResourceDemandEstimator estimator)
    {
        Guard.check(estimator);
        estimator_ = estimator;
    }
    
    /**
     * Compares two local controllers.
     *  
     * @param localController1   First local controller
     * @param localController2   Second local controller
     * @return                   -1, 0, 1
     */
    public final int compare(LocalControllerDescription localController1, 
                             LocalControllerDescription localController2)
    {
        Guard.check(localController1, localController2);
        
        Map<String, LRUCache<Long,Metric>> metrics1 = localController1.getMetricData();
        Map<String, LRUCache<Long,Metric>> metrics2 = localController2.getMetricData();

        LRUCache<Long,Metric> temps1 = metrics1.get("cputemperature");
        LRUCache<Long,Metric> temps2 = metrics2.get("cputemperature");
        
        Double lTemp1 = getLastTemperature(temps1);
        Double lTemp2 = getLastTemperature(temps2);
        
     //   long lastShutdown1 = localController1.getLastTimeShutdown();
     //   long lastShutdown2 = localController2.getLastTimeShutdown();
        
        LocalControllerStatus status1 = localController1.getStatus();
        LocalControllerStatus status2 = localController2.getStatus();
        
        // if both active, return the coldest
        if (status1.equals(LocalControllerStatus.ACTIVE) && status2.equals(LocalControllerStatus.ACTIVE))
        {
            if (lTemp1 < lTemp2) 
            {
                return -1;
            }
            else
            {
                return 1;
            }
        }
        // if only one active, return this one 
        else if (status1.equals(LocalControllerStatus.ACTIVE) && status2.equals(LocalControllerStatus.PASSIVE))
        {
            return 1;
        }
        else if (status1.equals(LocalControllerStatus.PASSIVE) && status2.equals(LocalControllerStatus.ACTIVE))
        {
            return -1;
        }
        // if both passive, return the one shutdown a long time ago
        else if (status1.equals(LocalControllerStatus.PASSIVE) && status2.equals(LocalControllerStatus.PASSIVE))
        {
        /*	if (lastShutdown1 < lastShutdown2)
        	{
        		return -1;
        	}
        	else
        	{
        		return 1;
        	}
        	*/
        	return -1;
        }
        
        return 0;
    }
    
    
    private Double getLastTemperature(LRUCache<Long,Metric> temperatures)
    {
    	Double lastTemp = 0d;
        for (Entry<Long,Metric> entry : temperatures.entrySet())
        {
        	lastTemp = entry.getValue().getValue();
        }
    
        return lastTemp;
    }

    
    
}

