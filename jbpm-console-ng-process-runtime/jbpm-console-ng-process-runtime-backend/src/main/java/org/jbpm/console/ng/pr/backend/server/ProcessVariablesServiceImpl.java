/*
 * Copyright 2014 JBoss by Red Hat.
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
package org.jbpm.console.ng.pr.backend.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.bus.server.annotations.Service;
import org.jbpm.console.ng.bd.backend.server.VariableHelper;
import org.jbpm.console.ng.ga.model.QueryFilter;
import org.jbpm.console.ng.bd.integration.KieServerIntegration;
import org.jbpm.console.ng.bd.model.ProcessVariableKey;
import org.jbpm.console.ng.bd.model.ProcessVariableSummary;
import org.jbpm.console.ng.pr.service.ProcessVariablesService;
import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.RuntimeDataService;
import org.kie.server.api.model.definition.VariablesDefinition;
import org.kie.server.api.model.instance.VariableInstance;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.uberfire.paging.PageResponse;

/**
 * @author salaboy
 */
@Service
@ApplicationScoped
public class ProcessVariablesServiceImpl implements ProcessVariablesService {

    @Inject
    private RuntimeDataService dataService;

    @Inject
    private DefinitionService bpmn2Service;

    @Inject
    private KieServerIntegration kieServerIntegration;

    @Override
    public PageResponse<ProcessVariableSummary> getData(QueryFilter filter) {
        PageResponse<ProcessVariableSummary> response = new PageResponse<ProcessVariableSummary>();
        List<ProcessVariableSummary> processVariablesSums = getProcessVariables(filter);
        response.setStartRowIndex(filter.getOffset());
        response.setTotalRowSize(processVariablesSums.size() - 1);
        if (processVariablesSums.size() > filter.getCount()) {
            response.setTotalRowSizeExact(false);
        } else {
            response.setTotalRowSizeExact(true);
        }
        response.setTotalRowSizeExact( true );
        response.setTotalRowSize( processVariablesSums.size() );
        if (!processVariablesSums.isEmpty()){
            if (processVariablesSums.size() > (filter.getCount() + filter.getOffset())) {
                response.setPageRowList( new ArrayList<ProcessVariableSummary>( processVariablesSums.subList( filter.getOffset(), filter.getOffset() + filter.getCount() ) ) );
                response.setLastPage( false );
            } else {
                response.setPageRowList( new ArrayList<ProcessVariableSummary>( processVariablesSums.subList( filter.getOffset(),processVariablesSums.size()) ) );
                response.setLastPage( true );
            }

        } else {
            response.setPageRowList(new ArrayList<ProcessVariableSummary>(processVariablesSums));
            response.setLastPage(true);

        }
        return response;

    }

    private List<ProcessVariableSummary> getProcessVariables(QueryFilter filter) {
        Long processInstanceId = null;
        String processId = "";
        String deploymentId = "";
        String serverTemplateId = "";
        if (filter.getParams() != null) {
            processInstanceId = Long.valueOf((String) filter.getParams().get("processInstanceId"));
            processId = (String) filter.getParams().get("processDefId");
            deploymentId = (String) filter.getParams().get("deploymentId");
            serverTemplateId = (String) filter.getParams().get("serverTemplateId");
        }
        // append 1 to the count to check if there are further pages
        org.kie.internal.query.QueryFilter qf = new org.kie.internal.query.QueryFilter(filter.getOffset(), filter.getCount() + 1,
                filter.getOrderBy(), filter.isAscending());
        Collection<ProcessVariableSummary> processVariables = null;
        Map<String, String> properties = new HashMap<String, String>();
        if (serverTemplateId.equals("Local")) {
            properties.putAll(bpmn2Service.getProcessVariables(deploymentId, processId));

            processVariables = VariableHelper.adaptCollection(dataService.getVariablesCurrentState(processInstanceId), properties,
                    processInstanceId);
        } else {
            KieServicesClient client = kieServerIntegration.getServerClient(serverTemplateId);

            QueryServicesClient queryClient = client.getServicesClient(QueryServicesClient.class);
            ProcessServicesClient processClient = client.getServicesClient(ProcessServicesClient.class);

            VariablesDefinition vars = processClient.getProcessVariableDefinitions(deploymentId, processId);
            properties.putAll(vars.getVariables());

            List<VariableInstance> variables = queryClient.findVariablesCurrentState(processInstanceId);

            processVariables = VariableHelper.adaptCollection(variables, properties, processInstanceId);
        }



        List<ProcessVariableSummary> processVariablesSums = new ArrayList<ProcessVariableSummary>(processVariables.size());
        for (ProcessVariableSummary pv : processVariables) {

            if (filter.getParams().get("textSearch") == null || ((String) filter.getParams().get("textSearch")).isEmpty()) {
                processVariablesSums.add(pv);
            } else if (pv.getVariableId().toLowerCase().contains((String) filter.getParams().get("textSearch"))) {
                processVariablesSums.add(pv);
            }
        }
        return processVariablesSums;
    }

    @Override
    public ProcessVariableSummary getItem(ProcessVariableKey key) {
        return null;
    }

    @Override
    public List<ProcessVariableSummary> getAll(QueryFilter filter) {
        return getProcessVariables(filter);
    }

}
