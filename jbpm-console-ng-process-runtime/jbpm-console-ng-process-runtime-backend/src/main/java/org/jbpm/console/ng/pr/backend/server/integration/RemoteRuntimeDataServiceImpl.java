/*
 * Copyright 2016 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.console.ng.pr.backend.server.integration;

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.bus.server.annotations.Service;
import org.jbpm.console.ng.bd.integration.KieServerIntegration;
import org.jbpm.console.ng.bd.model.NodeInstanceSummary;
import org.jbpm.console.ng.bd.model.ProcessDefinitionKey;
import org.jbpm.console.ng.bd.model.ProcessInstanceKey;
import org.jbpm.console.ng.bd.model.ProcessInstanceSummary;
import org.jbpm.console.ng.bd.model.ProcessSummary;
import org.jbpm.console.ng.bd.model.RuntimeLogSummary;
import org.jbpm.console.ng.bd.model.UserTaskSummary;
import org.jbpm.console.ng.pr.service.integration.RemoteRuntimeDataService;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.api.model.instance.NodeInstance;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskEventInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.ocpsoft.prettytime.PrettyTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@ApplicationScoped
public class RemoteRuntimeDataServiceImpl implements RemoteRuntimeDataService {

    private static final Logger logger = LoggerFactory.getLogger(RemoteRuntimeDataServiceImpl.class);

    @Inject
    private KieServerIntegration kieServerIntegration;


    public List<ProcessInstanceSummary> getProcessInstances(String serverTemplateId, List<Integer> statuses, Integer page, Integer pageSize) {

        List<ProcessInstanceSummary> instances = new ArrayList<ProcessInstanceSummary>();
        if (serverTemplateId == null || serverTemplateId.isEmpty()) {
            return instances;
        }

        QueryServicesClient queryServicesClient = getClient(serverTemplateId, QueryServicesClient.class);

        List<ProcessInstance> processInstances = queryServicesClient.findProcessInstancesByStatus(statuses, page, pageSize);

        for (ProcessInstance processInstance : processInstances) {
            ProcessInstanceSummary summary = build(processInstance);

            instances.add(summary);
        }

        return instances;
    }

    @Override
    public ProcessInstanceSummary getProcessInstance(String serverTemplateId, ProcessInstanceKey processInstanceKey) {

        QueryServicesClient queryServicesClient = getClient(serverTemplateId, QueryServicesClient.class);

        ProcessInstance processInstance = queryServicesClient.findProcessInstanceById(processInstanceKey.getProcessInstanceId());

        return build(processInstance);
    }

    @Override
    public List<NodeInstanceSummary> getProcessInstanceActiveNodes(String serverTemplateId, Long processInstanceId) {

        List<NodeInstanceSummary> instances = new ArrayList<NodeInstanceSummary>();
        QueryServicesClient queryServicesClient = getClient(serverTemplateId, QueryServicesClient.class);

        List<NodeInstance> nodeInstances = queryServicesClient.findActiveNodeInstances(processInstanceId, 0, 100);

        for (NodeInstance instance : nodeInstances) {
            NodeInstanceSummary summary = new NodeInstanceSummary(instance.getId(),
                    instance.getProcessInstanceId(),
                    instance.getName(),
                    instance.getNodeId(),
                    instance.getNodeType(),
                    instance.getDate().toString(),
                    instance.getConnection(),
                    false);

            instances.add(summary);
        }

        return instances;
    }

    @Override
    public List<RuntimeLogSummary> getBusinessLogs(String serverTemplateId, String processName, Long processInstanceId) {

        List<NodeInstance> processInstanceHistory = getProcessInstanceHistory(serverTemplateId, processInstanceId);
        List<TaskEventInstance> allTaskEventsByProcessInstanceId = new ArrayList<TaskEventInstance>();//taskAuditService.getAllTaskEventsByProcessInstanceId(processInstanceId, "");
        List<RuntimeLogSummary> logs = new ArrayList<RuntimeLogSummary>(processInstanceHistory.size() + allTaskEventsByProcessInstanceId.size());
        PrettyTime prettyDateFormatter = new PrettyTime();

        for(int i = processInstanceHistory.size() - 1 ; i >= 0; i--){
            NodeInstance nis = processInstanceHistory.get(i);

            if(nis.getNodeType().equals("HumanTaskNode")){
                logs.add(new RuntimeLogSummary(nis.getId(), prettyDateFormatter.format(nis.getDate()), "Task '" + nis.getName() + "' was created", "System"));
                for(TaskEventInstance te : allTaskEventsByProcessInstanceId){
                    if(te.getWorkItemId() != null && nis.getId() == te.getWorkItemId()){
                        if(te.getType().equals("CLAIMED") || te.getType().equals("RELEASED") || te.getType().equals("COMPLETED")){
                            logs.add(new RuntimeLogSummary(nis.getId(), "- " + prettyDateFormatter.format(te.getLogTime()), "Task '" + nis.getName() +
                                    "' was " + te.getType().toLowerCase() + " by user " + te.getUserId(), "Human"));
                        }
                    }
                }
            }else if(nis.getNodeType().equals("StartNode")){
                logs.add(new RuntimeLogSummary(nis.getId(), prettyDateFormatter.format(nis.getDate()), "Process '" + processName + "' was created", "Human"));
            }else if(nis.getNodeType().equals("EndNode")){
                logs.add(new RuntimeLogSummary(nis.getId(), prettyDateFormatter.format(nis.getDate()), "Process '" + processName + "' was completed", "System"));
            }


        }
        return logs;
    }

    @Override
    public List<ProcessSummary> getProcesses(String serverTemplateId, Integer page, Integer pageSize) {
        List<ProcessSummary> summaries = new ArrayList<ProcessSummary>();

        if (serverTemplateId == null || serverTemplateId.isEmpty()) {
            return summaries;
        }

        QueryServicesClient queryServicesClient = getClient(serverTemplateId, QueryServicesClient.class);

        List<ProcessDefinition> processes = queryServicesClient.findProcesses(page, pageSize);

        for (ProcessDefinition definition : processes) {

            ProcessSummary summary = new ProcessSummary(definition.getId(),
                    definition.getName(),
                    definition.getContainerId(),
                    definition.getPackageName(),
                    "",
                    definition.getVersion(),
                    "",
                    "");

            summaries.add(summary);
        }

        return summaries;
    }

    @Override
    public ProcessSummary getProcess(String serverTemplateId, ProcessDefinitionKey processDefinitionKey) {
        ProcessServicesClient queryServicesClient = getClient(serverTemplateId, ProcessServicesClient.class);

        ProcessDefinition definition = queryServicesClient.getProcessDefinition(processDefinitionKey.getDeploymentId(), processDefinitionKey.getProcessId());

        ProcessSummary summary = new ProcessSummary(definition.getId(),
                definition.getName(),
                definition.getContainerId(),
                definition.getPackageName(),
                "",
                definition.getVersion(),
                "",
                "");

        summary.setAssociatedEntities(definition.getAssociatedEntities());
        summary.setProcessVariables(definition.getProcessVariables());
        summary.setReusableSubProcesses(definition.getReusableSubProcesses());
        summary.setServiceTasks(definition.getServiceTasks());

        return summary;
    }

    @Override
    public List<RuntimeLogSummary> getRuntimeLogs(String serverTemplateId, Long processInstanceId) {
        List<NodeInstance> processInstanceHistory = getProcessInstanceHistory(serverTemplateId, processInstanceId);
        List<TaskEventInstance> allTaskEventsByProcessInstanceId = new ArrayList<TaskEventInstance>();//taskAuditService.getAllTaskEventsByProcessInstanceId(processInstanceId, "");
        List<RuntimeLogSummary> logs = new ArrayList<RuntimeLogSummary>(processInstanceHistory.size() + allTaskEventsByProcessInstanceId.size());
        PrettyTime prettyDateFormatter = new PrettyTime();

        for(int i = processInstanceHistory.size() - 1 ; i >= 0; i--){
            NodeInstance nis = processInstanceHistory.get(i);

                if(nis.getNodeType().equals("HumanTaskNode")){
                    logs.add(new RuntimeLogSummary(nis.getId(), prettyDateFormatter.format(nis.getDate()), nis.getName() + "("+nis.getNodeType()+")", "System"));
                    for(TaskEventInstance te : allTaskEventsByProcessInstanceId){
                        if(te.getWorkItemId() != null && nis.getId() == te.getWorkItemId()){
                            if(te.getType().equals("ADDED")){
                                logs.add(new RuntimeLogSummary(nis.getId(), "- " + prettyDateFormatter.format(te.getLogTime()), te.getUserId() + "->" +te.getType(), "System"));
                            }else{
                                logs.add(new RuntimeLogSummary(nis.getId(), "- " + prettyDateFormatter.format(te.getLogTime()), te.getUserId() + "->" +te.getType(), "Human"));
                            }
                        }
                    }
                }else if(nis.getNodeType().equals("StartNode")){
                    logs.add(new RuntimeLogSummary(nis.getId(), prettyDateFormatter.format(nis.getDate()), nis.getName() + "("+nis.getNodeType()+")", "Human"));
                }else {
                    logs.add(new RuntimeLogSummary(nis.getId(), prettyDateFormatter.format(nis.getDate()), nis.getName() + "("+nis.getNodeType()+")", "System"));
                }

        }
        return logs;
    }

    protected List<NodeInstance> getProcessInstanceHistory(String serverTemplateId, Long processInstanceId) {
        QueryServicesClient queryServicesClient = getClient(serverTemplateId, QueryServicesClient.class);

        return queryServicesClient.findNodeInstances(processInstanceId, 0, 100);

    }

    protected ProcessInstanceSummary build(ProcessInstance processInstance) {
        ProcessInstanceSummary summary = new ProcessInstanceSummary(
                processInstance.getId(),
                processInstance.getProcessId(),
                processInstance.getContainerId(),
                processInstance.getProcessName(),
                processInstance.getProcessVersion(),
                processInstance.getState(),
                processInstance.getDate(),
                processInstance.getInitiator(),
                processInstance.getProcessInstanceDescription(),
                processInstance.getCorrelationKey(),
                processInstance.getParentId()
        );

        if (processInstance.getActiveUserTasks() != null && processInstance.getActiveUserTasks().getTasks() != null) {
            List<TaskSummary> tasks = processInstance.getActiveUserTasks().getItems();

            List<UserTaskSummary> userTaskSummaries = new ArrayList<UserTaskSummary>();
            for (TaskSummary taskSummary : tasks) {
                UserTaskSummary userTaskSummary = new UserTaskSummary(taskSummary.getId(),
                        taskSummary.getName(),
                        taskSummary.getActualOwner(),
                        taskSummary.getStatus());

                userTaskSummaries.add(userTaskSummary);
            }
            summary.setActiveTasks(userTaskSummaries);
        }

        return summary;
    }

    protected <T> T getClient(String serverTemplateId, Class<T> serviceClass) {
        KieServicesClient client = kieServerIntegration.getServerClient(serverTemplateId);
        if (client == null) {
            throw new RuntimeException("No client to interact with server " + serverTemplateId);
        }

        return client.getServicesClient(serviceClass);
    }
}
