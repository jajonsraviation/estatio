/*
 *
 *  Copyright 2012-2014 Eurocommercial Properties NV
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.estatio.module.capex.fixtures.project.personas;

import java.math.BigDecimal;

import javax.inject.Inject;

import org.estatio.module.asset.dom.Property;
import org.estatio.module.asset.dom.PropertyRepository;
import org.estatio.module.asset.fixtures.property.personas.PropertyAndUnitsAndOwnerAndManagerForOxfGb;
import org.estatio.module.capex.dom.project.Project;
import org.estatio.module.charge.dom.Charge;
import org.estatio.module.charge.dom.ChargeRepository;

import static org.incode.module.base.integtests.VT.ld;

public class ProjectForOxf extends ProjectAbstract {

    public static final String PROJECT_REFERENCE = "OXF-02";

    @Override
    protected void execute(ExecutionContext executionContext) {

        // prereqs
        executionContext.executeChild(this, new PropertyAndUnitsAndOwnerAndManagerForOxfGb());

        // exec
        Project projectOxf2 = createProject(
        		PROJECT_REFERENCE, "New extension", ld(2016, 1, 1), ld(2019, 7, 1), null,
                "/GBR", null, executionContext);

        Charge charge = chargeRepository.findByReference("WORKS");
        Property Oxf = propertyRepository.findPropertyByReference(PropertyAndUnitsAndOwnerAndManagerForOxfGb.REF);
        projectOxf2.addItem(charge, "works", new BigDecimal("40000.00"), null, null,Oxf,null );

    }

    @Inject
    ChargeRepository chargeRepository;

    @Inject PropertyRepository propertyRepository;

}
