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
package org.estatio.integtests.invoice;

import org.estatio.dom.invoice.Invoice;
import org.estatio.dom.invoice.InvoiceStatus;
import org.estatio.dom.invoice.Invoices;
import org.estatio.dom.invoice.PaymentMethod;
import org.estatio.dom.lease.Lease;
import org.estatio.dom.lease.Leases;
import org.estatio.dom.party.Parties;
import org.estatio.dom.party.Party;
import org.estatio.fixture.EstatioBaseLineFixture;
import org.estatio.fixture.asset.PropertiesAndUnitsFixture;
import org.estatio.fixture.invoice.InvoiceAndInvoiceItemFixture;
import org.estatio.fixture.lease.LeasesAndLeaseUnitsAndLeaseItemsAndLeaseTermsAndTagsAndBreakOptionsFixture;
import org.estatio.fixture.party.PersonsAndOrganisationsAndCommunicationChannelsFixture;
import org.estatio.integtests.EstatioIntegrationTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.apache.isis.applib.fixturescripts.CompositeFixtureScript;
import org.apache.isis.core.integtestsupport.IsisSystemForTest;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.transaction.IsisTransaction;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class InvoicesTest_findOrCreateMatchingInvoice extends EstatioIntegrationTest {

    @Before
    public void setupData() {
        scenarioExecution().install(new CompositeFixtureScript() {
            @Override
            protected void execute(ExecutionContext executionContext) {
                execute(new EstatioBaseLineFixture(), executionContext);
                execute("parties", new PersonsAndOrganisationsAndCommunicationChannelsFixture(), executionContext);
                execute("properties", new PropertiesAndUnitsFixture(), executionContext);
                execute("leases", new LeasesAndLeaseUnitsAndLeaseItemsAndLeaseTermsAndTagsAndBreakOptionsFixture(), executionContext);
                //execute("invoices", new InvoiceAndInvoiceItemFixture(), executionContext);
            }
        });
    }

    private Invoices invoices;
    private Parties parties;
    private Leases leases;
    private Party seller;
    private Party buyer;
    private Lease lease;

    @Before
    public void setUp() throws Exception {
        invoices = service(Invoices.class);
        parties = service(Parties.class);
        leases = service(Leases.class);

        seller = parties.findPartyByReference(InvoiceAndInvoiceItemFixture.SELLER_PARTY);
        buyer = parties.findPartyByReference(InvoiceAndInvoiceItemFixture.BUYER_PARTY);
        lease = leases.findLeaseByReference(InvoiceAndInvoiceItemFixture.LEASE);

        final IsisTransaction transaction = IsisContext.getTransactionManager().getTransaction();
        final IsisTransaction.State state = transaction.getState();

        IsisSystemForTest.get();

    }

    @Test
    public void whenDoesNotExist() {
        // given
        Assert.assertThat(invoices.allInvoices().isEmpty(), is(true));
        // when
        Invoice invoice = invoices.findOrCreateMatchingInvoice(seller, buyer, PaymentMethod.DIRECT_DEBIT, lease, InvoiceStatus.NEW, InvoiceAndInvoiceItemFixture.START_DATE, null);
        // then
        Assert.assertNotNull(invoice);
        Assert.assertThat(invoices.allInvoices().isEmpty(), is(false));
    }

    @Test
    public void whenExist() {
        // given
        Invoice invoice = invoices.findOrCreateMatchingInvoice(seller, buyer, PaymentMethod.DIRECT_DEBIT, lease, InvoiceStatus.NEW, InvoiceAndInvoiceItemFixture.START_DATE, null);
        // when
        Invoice invoice2 = invoices.findOrCreateMatchingInvoice(seller, buyer, PaymentMethod.DIRECT_DEBIT, lease, InvoiceStatus.NEW, InvoiceAndInvoiceItemFixture.START_DATE, null);
        // then
        Assert.assertThat(invoice2, is(sameInstance(invoice)));
    }

}
