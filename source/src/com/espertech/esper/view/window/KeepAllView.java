/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.view.window;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.collection.ViewUpdatedCollection;
import com.espertech.esper.core.context.util.AgentInstanceViewFactoryChainContext;
import com.espertech.esper.view.CloneableView;
import com.espertech.esper.view.DataWindowView;
import com.espertech.esper.view.View;
import com.espertech.esper.view.ViewSupport;

import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * This view is a keep-all data window that simply keeps all events added.
 * It in addition allows to remove events efficiently for the remove-stream events received by the view.
 */
public class KeepAllView extends ViewSupport implements DataWindowView, CloneableView
{
    protected final AgentInstanceViewFactoryChainContext agentInstanceViewFactoryContext;
    private final KeepAllViewFactory keepAllViewFactory;
    protected LinkedHashSet<EventBean> indexedEvents;
    protected ViewUpdatedCollection viewUpdatedCollection;

    /**
     * Ctor.
     * @param keepAllViewFactory for copying this view in a group-by
     * @param viewUpdatedCollection for satisfying queries that select previous events in window order
     */
    public KeepAllView(AgentInstanceViewFactoryChainContext agentInstanceViewFactoryContext, KeepAllViewFactory keepAllViewFactory, ViewUpdatedCollection viewUpdatedCollection)
    {
        this.agentInstanceViewFactoryContext = agentInstanceViewFactoryContext;
        this.keepAllViewFactory = keepAllViewFactory;
        indexedEvents = new LinkedHashSet<EventBean>();
        this.viewUpdatedCollection = viewUpdatedCollection;
    }

    public View cloneView()
    {
        return keepAllViewFactory.makeView(agentInstanceViewFactoryContext);
    }

    /**
     * Returns true if the window is empty, or false if not empty.
     * @return true if empty
     */
    public boolean isEmpty()
    {
        return indexedEvents.isEmpty();
    }

    /**
     * Returns the (optional) collection handling random access to window contents for prior or previous events.
     * @return buffer for events
     */
    public ViewUpdatedCollection getViewUpdatedCollection()
    {
        return viewUpdatedCollection;
    }

    public final EventType getEventType()
    {
        // The event type is the parent view's event type
        return parent.getEventType();
    }

    public final void update(EventBean[] newData, EventBean[] oldData)
    {
        if (newData != null)
        {
            for (EventBean newEvent : newData) {
                indexedEvents.add(newEvent);
                internalHandleAdded(newEvent);
            }
        }

        if (oldData != null)
        {
            for (EventBean anOldData : oldData)
            {
                indexedEvents.remove(anOldData);
                internalHandleRemoved(anOldData);
            }
        }

        // update event buffer for access by expressions, if any
        if (viewUpdatedCollection != null)
        {
            viewUpdatedCollection.update(newData, oldData);
        }

        updateChildren(newData, oldData);
    }

    public final Iterator<EventBean> iterator()
    {
        return indexedEvents.iterator();
    }

    public void internalHandleAdded(EventBean newEvent) {
        // no action required
    }

    public void internalHandleRemoved(EventBean oldEvent) {
        // no action required
    }

}
