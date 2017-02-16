/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.workbench.cm.client.milestones;

//@RunWith(MockitoJUnitRunner.class)
public class CaseMilestonesPresenterTest {

//    @Mock
//    CaseMilestoneListPresenter.CaseMilestoneListView caseMilestoneListView;
//
//    @InjectMocks
//    CaseMilestoneListPresenter presenter;
//
//    @Override
//    public CaseMilestoneListPresenter getPresenter() {
//        return presenter;
//    }
//
//    List<CaseMilestoneSummary> caseMilestonesSummaryList = Lists.newArrayList(createCaseMilestone());
//    CaseInstanceSummary cis;
//    final String serverTemplateId = "serverTemplateId",
//            containerId = "containerId",
//            caseDefId ="caseDefinitionId",
//            caseId = "caseId";
//
//    private static CaseMilestoneSummary createCaseMilestone() {
//        return CaseMilestoneSummary.builder()
//                .identifier("identifier")
//                .name("milestoneName")
//                .status(CaseMilestoneStatus.AVAILABLE.getStatus())
//                .achieved(false)
//                .build();
//    }
//
//    @Before
//    public void init() {
//        caseService = new CallerMock<>(caseManagementService);
//        when(caseManagementService.getCaseMilestones(anyString(),anyString(),any(CaseMilestoneSearchRequest.class))).thenReturn(caseMilestonesSummaryList);
//        when(caseMilestoneListView.getCaseMilestoneSearchRequest()).thenReturn(new CaseMilestoneSearchRequest());
//        presenter.setCaseService(caseService);
//
//        cis = CaseInstanceSummary.builder().containerId(containerId).caseId(caseId).caseDefinitionId(caseDefId).build();
//        final CaseDefinitionSummary cds = CaseDefinitionSummary.builder().id(caseDefId).build();
//
//        when(caseManagementService.getCaseDefinition(serverTemplateId, cis.getContainerId(), cis.getCaseDefinitionId())).thenReturn(cds);
//
//        List<CaseMilestoneSummary> milestones = singletonList(createCaseMilestone());
//        when(caseManagementService.getCaseMilestones(anyString(), anyString(), any(CaseMilestoneSearchRequest.class))).thenReturn(milestones);
//    }
//
//    @Test
//    public void testClearCaseInstance() {
//        presenter.clearCaseInstance();
//
//        verifyClearCaseInstance();
//    }
//
//    private void verifyClearCaseInstance() {
//        verify(caseMilestoneListView).removeAllMilestones();
//    }
//
//    @Test
//    public void testLoadCaseInstance() {
//        List<CaseMilestoneSummary> milestones = singletonList(createCaseMilestone());
//        when(caseManagementService.getCaseMilestones(anyString(), anyString(), any(CaseMilestoneSearchRequest.class))).thenReturn(milestones);
//
//        setupCaseInstance(cis, serverTemplateId);
//
//        verifyClearCaseInstance();
//        verify(caseMilestoneListView).setCaseMilestoneList(milestones);
//    }
//
//    @Test
//    public void testRefreshData() {
//        setupCaseInstance(cis, serverTemplateId);
//        presenter.searchCaseMilestones();
//
//        verify(caseManagementService,times(2)).getCaseMilestones(cis.getContainerId(), cis.getCaseId(), caseMilestoneListView.getCaseMilestoneSearchRequest());
//        final ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
//        verify(caseMilestoneListView,times(2)).setCaseMilestoneList(captor.capture());
//        assertEquals(caseMilestonesSummaryList.size(), captor.getValue().size());
//    }

}