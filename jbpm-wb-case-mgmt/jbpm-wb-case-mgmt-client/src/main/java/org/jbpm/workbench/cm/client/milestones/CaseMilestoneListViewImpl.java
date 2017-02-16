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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.dom.Button;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.MouseEvent;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.jbpm.workbench.cm.model.CaseMilestoneSummary;
import org.jbpm.workbench.cm.client.util.AbstractView;

import static java.util.Comparator.comparing;
import static org.jboss.errai.common.client.dom.DOMUtil.addCSSClass;
import static org.jboss.errai.common.client.dom.DOMUtil.removeCSSClass;

@Dependent
@Templated
public class CaseMilestoneListViewImpl extends AbstractView<CaseMilestoneJS> implements CaseMilestoneJS.CaseMilestoneListView {

    @Inject
    @DataField("milestones")
    private Div milestonesContainer;

    @Inject
    @DataField("empty-list-item")
    private Div emptyContainer;

    @Inject
    @DataField("sort-alpha-asc")
    private Button sortAlphaAsc;

    @Inject
    @DataField("sort-alpha-desc")
    private Button sortAlphaDesc;

    @Inject
    @Bound
    @DataField("milestone-list")
    private ListComponent<CaseMilestoneSummary, CaseMilestoneItemView> milestones;

    @Inject
    @AutoBound
    private DataBinder<List<CaseMilestoneSummary>> caseMilestoneList;


    @Override
    public void init(final CaseMilestoneJS presenter) {
        super.init(presenter);
        milestones.addComponentCreationHandler(v -> v.init(presenter));
    }

    @Override
    public void setCaseMilestoneList(final List<CaseMilestoneSummary> caseMilestoneList) {
        this.caseMilestoneList.setModel(caseMilestoneList);
        if (caseMilestoneList.isEmpty()) {
            removeCSSClass(emptyContainer, "hidden");
        } else {
            addCSSClass(emptyContainer, "hidden");
        }
    }

    @Override
    public void removeAllMilestones() {
        caseMilestoneList.setModel(new ArrayList<>());
    }

    @Override
    public HTMLElement getElement() {
        return milestonesContainer;
    }

    @EventHandler("sort-alpha-asc")
    public void onSortAlphaAsc(final @ForEvent("click") MouseEvent event) {
        onSortChange(sortAlphaAsc, sortAlphaDesc, false);
    }

    @EventHandler("sort-alpha-desc")
    public void onSortAlphaDesc(final @ForEvent("click") MouseEvent event) {
        onSortChange(sortAlphaDesc, sortAlphaAsc, true);
    }

    private void onSortChange(final HTMLElement toHide, final HTMLElement toShow, final Boolean sortByAsc){
        addCSSClass(toHide, "hidden");
        removeCSSClass(toShow, "hidden");
        this.caseMilestoneList.getModel().sort(getCaseMilestoneSummaryComparator(sortByAsc));
    }

    protected Comparator<CaseMilestoneSummary> getCaseMilestoneSummaryComparator(final Boolean sortByAsc) {
        Comparator<CaseMilestoneSummary> comparatorByName = comparing(CaseMilestoneSummary::getName);
        return comparing(CaseMilestoneSummary::getStatus).thenComparing(sortByAsc ? comparatorByName: comparatorByName.reversed());
    }

}