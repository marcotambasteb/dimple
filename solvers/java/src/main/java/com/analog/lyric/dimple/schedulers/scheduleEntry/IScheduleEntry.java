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

package com.analog.lyric.dimple.schedulers.scheduleEntry;

import java.util.HashMap;

import com.analog.lyric.dimple.model.Port;


/**
 * @author jeffb
 * 
 *         All types of schedule entries must implement the update() method,
 *         which is to update the portion of the graph associated with the
 *         entry.
 */
public interface IScheduleEntry
{
	public abstract void update() ;
	
	public IScheduleEntry copy(HashMap<Object,Object> old2newObjs) ;
	public IScheduleEntry copyToRoot(HashMap<Object,Object> old2newObjs) ;
	
	public Iterable<Port> getPorts();
}
