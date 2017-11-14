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
package org.estatio.module.lease.fixtures.invoice;

import java.math.BigInteger;
import java.util.SortedSet;

import javax.inject.Inject;

import org.joda.time.LocalDate;

import org.apache.isis.applib.fixturescripts.FixtureScript;

import org.isisaddons.module.security.dom.tenancy.ApplicationTenancies;
import org.isisaddons.module.security.dom.tenancy.ApplicationTenancy;

import org.incode.module.base.dom.valuetypes.LocalDateInterval;

import org.estatio.module.asset.dom.Property;
import org.estatio.module.currency.dom.Currency;
import org.estatio.module.currency.dom.CurrencyRepository;
import org.estatio.module.invoice.dom.Invoice;
import org.estatio.module.invoice.dom.InvoiceItem;
import org.estatio.module.lease.dom.invoicing.NumeratorForCollectionRepository;
import org.estatio.module.invoice.dom.PaymentMethod;
import org.estatio.module.lease.dom.Lease;
import org.estatio.module.lease.dom.LeaseItem;
import org.estatio.module.lease.dom.LeaseItemType;
import org.estatio.module.lease.dom.LeaseRepository;
import org.estatio.module.lease.dom.LeaseTerm;
import org.estatio.module.lease.dom.invoicing.InvoiceForLease;
import org.estatio.module.lease.dom.invoicing.InvoiceForLeaseRepository;
import org.estatio.module.lease.dom.invoicing.InvoiceItemForLease;
import org.estatio.module.lease.dom.invoicing.InvoiceItemForLeaseRepository;
import org.estatio.module.party.dom.Party;
import org.estatio.module.party.dom.PartyRepository;

/**
 * Creates {@link Invoice} and associated {@link InvoiceItem}s.
 */
public abstract class InvoiceAbstract extends FixtureScript {

    protected InvoiceAbstract(String friendlyName, String localName) {
        super(friendlyName, localName);
    }

    protected InvoiceForLease createInvoiceAndNumerator(
            final ApplicationTenancy applicationTenancy,
            Lease lease,
            String sellerStr,
            String buyerStr,
            PaymentMethod paymentMethod,
            String currencyStr,
            LocalDate startDate,
            ExecutionContext executionContext) {
        final Party buyer = partyRepository.findPartyByReference(buyerStr);
        final Party seller = partyRepository.findPartyByReference(sellerStr);
        final Currency currency = currencyRepository.findCurrency(currencyStr);

        final String interactionId = null;

        final InvoiceForLease invoice = invoiceForLeaseRepository
                .newInvoice(applicationTenancy, seller, buyer, paymentMethod, currency, startDate, lease, interactionId);
        invoice.setInvoiceDate(startDate);

        final Property property = lease.getProperty();
        final String format = property.getReference() + "-%06d";
        numeratorForCollectionRepository.createInvoiceNumberNumerator(property, format, BigInteger.ZERO, applicationTenancy);

        return executionContext.addResult(this, invoice);
    }

    protected void createInvoiceItemsForTermsOfFirstLeaseItemOfType(
            final InvoiceForLease invoice, final LeaseItemType leaseItemType,
            final LocalDate startDate, final LocalDateInterval interval,
            final ExecutionContext executionContext) {

        final Lease lease = invoice.getLease();
        final LeaseItem firstLeaseItem = lease.findFirstItemOfType(leaseItemType);
        final SortedSet<LeaseTerm> terms = firstLeaseItem.getTerms();
        for (final LeaseTerm term : terms) {
            InvoiceItemForLease item = invoiceItemForLeaseRepository.newInvoiceItem(term, interval, startDate, null);
            item.setInvoice(invoice);
            item.setSequence(invoice.nextItemSequence());

            executionContext.addResult(this, item);
        }
    }

    // //////////////////////////////////////

    @Inject
    private PartyRepository partyRepository;

    @Inject
    private CurrencyRepository currencyRepository;

    @Inject
    private InvoiceForLeaseRepository invoiceForLeaseRepository;

    @Inject
    private InvoiceItemForLeaseRepository invoiceItemForLeaseRepository;

    @Inject
    protected LeaseRepository leaseRepository;

    @Inject
    protected ApplicationTenancies applicationTenancies;

    @Inject
    protected NumeratorForCollectionRepository numeratorForCollectionRepository;

}