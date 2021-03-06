/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
********************************************************************************/

package com.analog.lyric.dimple.schedulers.schedule;

import java.util.HashMap;

import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;


/**
 * @author jeffb
 * 
 *         Any schedule produced by a scheduler must implement this interface. A
 *         schedule is required to implement the Iterable interface, with each
 *         iteration, returning a sequence of ScheduleEntry objects.
 */
public interface ISchedule extends Iterable<IScheduleEntry>
{
	
	/*
	 * This method is called when setSchedule is called on the FactorGraph.
	 */
	public void attach(FactorGraph factorGraph) ;	
	public FactorGraph getFactorGraph();
	public ISchedule copy(HashMap<Object,Object> old2newObjs) ;
	public ISchedule copyToRoot(HashMap<Object,Object> old2newObjs) ;
}
