/*
 * This file is part of Track It!.
 * Copyright (C) 2013 Henrique Malheiro
 * Copyright (C) 2015 Pedro Gomes
 * 
 * TrackIt! is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Track It! is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Track It!. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.henriquemalheiro.trackit.business.operation.event;

import java.util.HashMap;
import java.util.Map;

import com.henriquemalheiro.trackit.business.common.Messages;

public enum OperationEvent implements OperationEventPublisher {
    OPERATION_STARTED() {
    	@Override
    	public void publishEvent(OperationEventListener listener, Object... value) {
    		listener.operationStarted(value[0].toString(), value[1].toString());
    	}
    },
    OPERATION_FINISHED() {
    	@Override
    	public void publishEvent(OperationEventListener listener, Object... value) {
    		listener.operationFinished(value[0].toString());
    	}
    },
    OPERATION_PROGRESS() {
    	@Override
    	public void publishEvent(OperationEventListener listener, Object... value) {
    		listener.operationProgress(((Integer) value[0]).intValue(), value[1].toString());
    	}
    };

    private static Map<OperationEvent, String> names;
    
    static {
    	names = new HashMap<>();
    	names.put(OPERATION_STARTED, Messages.getMessage("event.operationStarted"));
    	names.put(OPERATION_FINISHED, Messages.getMessage("event.operationFinished"));
    	names.put(OPERATION_PROGRESS, Messages.getMessage("event.operationProgress"));
    }

    private OperationEvent() {
    }

    public String getName() {
        return names.get(this);
    }
    
    @Override
    public String toString() {
        return getName();
    }
}