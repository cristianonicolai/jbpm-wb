/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.console.ng.ht.client.editors.tasklogs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jbpm.console.ng.ga.model.PortableQueryFilter;
import org.jbpm.console.ng.ga.model.QueryFilter;
import org.jbpm.console.ng.gc.client.util.DateUtils;
import org.jbpm.console.ng.ht.client.i18n.Constants;
import org.jbpm.console.ng.ht.model.TaskEventSummary;
import org.jbpm.console.ng.ht.model.events.TaskRefreshedEvent;
import org.jbpm.console.ng.ht.model.events.TaskSelectionEvent;
import org.jbpm.console.ng.ht.service.integration.RemoteTaskService;
import org.uberfire.ext.widgets.common.client.callbacks.DefaultErrorCallback;

@Dependent
public class TaskLogsPresenter {

    public interface TaskLogsView extends IsWidget {

        void init( final TaskLogsPresenter presenter );

        void displayNotification( final String text );

        void setLogTextAreaText( final String text );

    }

    private TaskLogsView view;

    private Caller<RemoteTaskService> taskAuditService;

    private long currentTaskId = 0;
    private String serverTemplateId;
    private String containerId;

    private Constants constants = Constants.INSTANCE;

    @Inject
    public TaskLogsPresenter( final TaskLogsView view, final Caller<RemoteTaskService> taskAuditService ) {
        this.view = view;
        this.taskAuditService = taskAuditService;
    }

    @PostConstruct
    public void init() {
        view.init( this );
    }

    public IsWidget getView() {
        return view;
    }

    public void refreshLogs() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put( "taskId", currentTaskId );
        QueryFilter filter = new PortableQueryFilter( 0, 0, false, "", "", false, "", params );

        taskAuditService.call(new RemoteCallback<List<TaskEventSummary>>() {
            @Override
            public void callback(List<TaskEventSummary>events) {
                SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
                for (TaskEventSummary tes : events) {
                    String summaryStr = summaryToString(tes);
                    safeHtmlBuilder.appendEscapedLines(summaryStr);
                }
                view.setLogTextAreaText(safeHtmlBuilder.toSafeHtml().asString());
            }

            public String summaryToString(TaskEventSummary tes) {
                String timeStamp = DateUtils.getDateTimeStr(tes.getLogTime());
                String additionalDetail = "UPDATED".equals(tes.getType()) ? tes.getMessage() : tes.getUserId();
                return timeStamp + ": Task " + tes.getType() + " (" + additionalDetail + ")\n";
            }
        }, new DefaultErrorCallback() ).getTaskEvents( serverTemplateId, containerId, currentTaskId );

    }

    public void onTaskSelectionEvent( @Observes final TaskSelectionEvent event ) {
        this.currentTaskId = event.getTaskId();
        this.containerId = event.getContainerId();
        this.serverTemplateId = event.getServerTemplateId();
        refreshLogs();
    }

    public void onTaskRefreshedEvent( @Observes final TaskRefreshedEvent event ) {
        if ( currentTaskId == event.getTaskId() ) {
            refreshLogs();
        }
    }
}
