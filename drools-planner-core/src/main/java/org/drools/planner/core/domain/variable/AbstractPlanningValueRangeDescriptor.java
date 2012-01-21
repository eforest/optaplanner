/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.planner.core.domain.variable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.drools.planner.api.domain.entity.PlanningEntity;
import org.drools.planner.api.domain.variable.ValueRange;
import org.drools.planner.core.domain.entity.PlanningEntityDescriptor;

public abstract class AbstractPlanningValueRangeDescriptor implements PlanningValueRangeDescriptor {

    protected PlanningVariableDescriptor variableDescriptor;
    protected boolean excludeUninitializedPlanningEntity;

    public AbstractPlanningValueRangeDescriptor(PlanningVariableDescriptor variableDescriptor) {
        this.variableDescriptor = variableDescriptor;
    }

    protected void processExcludeUninitializedPlanningEntity(ValueRange valueRangeAnnotation) {
        excludeUninitializedPlanningEntity = valueRangeAnnotation.excludeUninitializedPlanningEntity();
        if (excludeUninitializedPlanningEntity) {
            Class<?> variablePropertyType = variableDescriptor.getVariablePropertyType();
            Set<Class<?>> entityClassSet = variableDescriptor.getPlanningEntityDescriptor().getSolutionDescriptor()
                    .getPlanningEntityImplementationClassSet();
            boolean assignableFrom = false;
            for (Class<?> entityClass : entityClassSet) {
                if (variablePropertyType.isAssignableFrom(entityClass)) {
                    assignableFrom = true;
                    break;
                }
            }
            if (!assignableFrom) {
                throw new IllegalArgumentException("The planningEntityClass ("
                        + variableDescriptor.getPlanningEntityDescriptor().getPlanningEntityClass()
                        + ") has a PlanningVariable annotated property (" + variableDescriptor.getVariablePropertyName()
                        + ") with excludeUninitializedPlanningEntity (true), but there is no planning entity class"
                        + " that extends the variablePropertyType (" + variablePropertyType + ").");
            }
        }
    }

    protected Collection<?> applyFiltering(Collection<?> values) {
        if (!excludeUninitializedPlanningEntity) {
            return values;
        }
        Collection<Object> filteredValues = new ArrayList<Object>(values.size());
        for (Object value : values) {
            if (value.getClass().isAnnotationPresent(PlanningEntity.class)) {
                PlanningEntityDescriptor entityDescriptor = variableDescriptor.getPlanningEntityDescriptor()
                        .getSolutionDescriptor().getPlanningEntityDescriptor(value.getClass());
                if (entityDescriptor == null) {
                    throw new IllegalArgumentException("The planningEntityClass ("
                            + variableDescriptor.getPlanningEntityDescriptor().getPlanningEntityClass()
                            + ") has a PlanningVariable annotated property ("
                            + variableDescriptor.getVariablePropertyName()
                            + ") with excludeUninitializedPlanningEntity (true),"
                            + " but a planning value class (" + value.getClass()
                            + ") annotated with PlanningEntity is a non configured as a planning entity.");
                }
                if (entityDescriptor.isInitialized(value)) {
                    filteredValues.add(value);
                }
            }
        }
        return filteredValues;
    }

    public boolean isValuesCacheable() {
        return false;
    }

}
