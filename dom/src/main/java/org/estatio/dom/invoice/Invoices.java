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
package org.estatio.dom.invoice;

import java.math.BigInteger;
import java.util.List;
import org.estatio.dom.EstatioDomainService;
import org.estatio.dom.asset.FixedAsset;
import org.estatio.dom.asset.Property;
import org.estatio.dom.currency.Currency;
import org.estatio.dom.lease.Lease;
import org.estatio.dom.lease.invoicing.InvoiceCalculationParameters;
import org.estatio.dom.numerator.Numerator;
import org.estatio.dom.numerator.Numerators;
import org.estatio.dom.party.Party;
import org.estatio.dom.utils.StringUtils;
import org.estatio.services.settings.EstatioSettingsService;
import org.joda.time.LocalDate;
import org.apache.isis.applib.annotation.*;
import org.apache.isis.applib.annotation.ActionSemantics.Of;

public class Invoices extends EstatioDomainService<Invoice> {

    public Invoices() {
        super(Invoices.class, Invoice.class);
    }

    // //////////////////////////////////////

    @NotInServiceMenu
    @Named("Invoices")
    public List<Invoice> findInvoices(final Lease lease) {
        return allMatches("findByLease",
                "lease", lease);
    }

    @NotInServiceMenu
    @Named("Invoices")
    public List<Invoice> findInvoices(final Party party) {
        return allMatches("findByBuyer",
                "buyer", party);
    }

    public List<Invoice> findInvoicesByInvoiceNumber(
            final @Named("Invoice number") String invoiceNumber) {
        return allMatches("findByInvoiceNumber",
                "invoiceNumber", StringUtils.wildcardToCaseInsensitiveRegex(invoiceNumber));
    }

    // //////////////////////////////////////

    @Programmatic
    public List<Invoice> findInvoicesByRunId(final String runId) {
        return allMatches("findByRunId",
                "runId", runId);
    }

    @Programmatic
    public List<Invoice> findInvoices(
            final InvoiceStatus status) {
        return allMatches("findByStatus",
                "status", status);
    }

    @Programmatic
    public List<Invoice> findInvoices(
            final FixedAsset fixedAsset,
            final InvoiceStatus status) {
        return allMatches("findByFixedAssetAndStatus",
                "fixedAsset", fixedAsset,
                "status", status);
    }

    @Programmatic
    public List<Invoice> findInvoices(
            final FixedAsset fixedAsset,
            final LocalDate dueDate) {
        return allMatches("findByFixedAssetAndDueDate",
                "fixedAsset", fixedAsset,
                "dueDate", dueDate);
    }

    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "1")
    public List<Invoice> findInvoices(
            final FixedAsset fixedAsset,
            final @Named("Due Date") @Optional LocalDate dueDate,
            final @Optional InvoiceStatus status) {
        if (status == null) {
            return findInvoices(fixedAsset, dueDate);
        } else if (dueDate == null) {
            return findInvoices(fixedAsset, status);
        } else {
            return allMatches("findByFixedAssetAndDueDateAndStatus",
                    "fixedAsset", fixedAsset,
                    "dueDate", dueDate,
                    "status", status);
        }
    }

    // //////////////////////////////////////

    @NotContributed
    @ActionSemantics(Of.NON_IDEMPOTENT)
    @MemberOrder(sequence = "1")
    public Invoice newInvoiceForLease(
            final @Named("Lease") Lease lease,
            final @Named("Due date") LocalDate dueDate,
            final PaymentMethod paymentMethod,
            final Currency currency
            ) {
        return newInvoice(
                lease.getPrimaryParty(),
                lease.getSecondaryParty(),
                paymentMethod,
                currency,
                dueDate,
                lease,
                null);
    }

    // //////////////////////////////////////

    @Programmatic
    public Invoice newInvoice(
            final @Named("Seller") Party seller,
            final @Named("Buyer") Party buyer,
            final PaymentMethod paymentMethod,
            final Currency currency,
            final @Named("Due date") LocalDate dueDate,
            final Lease lease,
            final String interactionId
            ) {
        Invoice invoice = newTransientInstance();
        invoice.setBuyer(buyer);
        invoice.setSeller(seller);
        invoice.setPaymentMethod(paymentMethod);
        invoice.setStatus(InvoiceStatus.NEW);
        invoice.setCurrency(currency);
        invoice.setLease(lease);
        invoice.setDueDate(dueDate);
        invoice.setUuid(java.util.UUID.randomUUID().toString());
        invoice.setRunId(interactionId);

        // copy down form the agreement, we require all invoice items to relate
        // back to this (root) fixed asset
        invoice.setPaidBy(lease.getPaidBy());
        invoice.setFixedAsset(lease.getFixedAsset());

        persistIfNotAlready(invoice);
        getContainer().flush();
        return invoice;
    }

    @Programmatic
    public Invoice findOrCreateMatchingInvoice(
            final PaymentMethod paymentMethod,
            final Lease lease,
            final InvoiceStatus invoiceStatus,
            final LocalDate dueDate,
            final String interactionId) {
        Party buyer = lease.getSecondaryParty();
        Party seller = lease.getPrimaryParty();
        return findOrCreateMatchingInvoice(seller, buyer, paymentMethod, lease, invoiceStatus, dueDate, interactionId);
    }

    @Programmatic
    public Invoice findMatchingInvoice(
            final Party seller,
            final Party buyer,
            final PaymentMethod paymentMethod,
            final Lease lease,
            final InvoiceStatus invoiceStatus,
            final LocalDate dueDate) {
        final List<Invoice> invoices = findMatchingInvoices(
                seller, buyer, paymentMethod, lease, invoiceStatus, dueDate);
        if (invoices == null || invoices.size() == 0) {
            return null;
        }
        return invoices.get(0);
    }

    @Programmatic
    public Invoice findOrCreateMatchingInvoice(
            final Party seller,
            final Party buyer,
            final PaymentMethod paymentMethod,
            final Lease lease,
            final InvoiceStatus invoiceStatus,
            final LocalDate dueDate,
            final String interactionId) {
        final List<Invoice> invoices = findMatchingInvoices(
                seller, buyer, paymentMethod, lease, invoiceStatus, dueDate);
        if (invoices == null || invoices.size() == 0) {
            return newInvoice(seller, buyer, paymentMethod, settings.systemCurrency(), dueDate, lease, interactionId);
        }
        return invoices.get(0);
    }

    @Programmatic
    public List<Invoice> findMatchingInvoices(
            final Party seller,
            final Party buyer,
            final PaymentMethod paymentMethod,
            final Lease lease,
            final InvoiceStatus invoiceStatus,
            final LocalDate dueDate) {
        return allMatches(
                "findMatchingInvoices",
                "seller", seller,
                "buyer", buyer,
                "paymentMethod", paymentMethod,
                "lease", lease,
                "status", invoiceStatus,
                "dueDate", dueDate);
    }

    // //////////////////////////////////////

    @Prototype
    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "98")
    public List<Invoice> allInvoices() {
        return allInstances();
    }

    // //////////////////////////////////////

    @ActionSemantics(Of.IDEMPOTENT)
    @MemberOrder(name = "Administration", sequence = "numerators.invoices.1")
    public Numerator findCollectionNumberNumerator() {
        return numerators.findGlobalNumerator(Constants.COLLECTION_NUMBER_NUMERATOR_NAME);
    }

    // //////////////////////////////////////

    @ActionSemantics(Of.IDEMPOTENT)
    @MemberOrder(name = "Administration", sequence = "numerators.invoices.2")
    @NotContributed
    public Numerator createCollectionNumberNumerator(
            final @Named("Format") String format,
            final @Named("Last value") BigInteger lastIncrement) {

        return numerators.createGlobalNumerator(Constants.COLLECTION_NUMBER_NUMERATOR_NAME, format, lastIncrement);
    }

    public String default0CreateCollectionNumberNumerator() {
        return "%09d";
    }

    public BigInteger default1CreateCollectionNumberNumerator() {
        return BigInteger.ZERO;
    }

    // //////////////////////////////////////

    @ActionSemantics(Of.IDEMPOTENT)
    @MemberOrder(name = "Administration", sequence = "numerators.invoices.3")
    @NotContributed
    public Numerator findInvoiceNumberNumerator(
            final FixedAsset fixedAsset) {
        return numerators.findScopedNumerator(Constants.INVOICE_NUMBER_NUMERATOR_NAME, fixedAsset);
    }

    // //////////////////////////////////////

    @ActionSemantics(Of.IDEMPOTENT)
    @MemberOrder(name = "Administration", sequence = "numerators.invoices.4")
    @NotContributed
    public Numerator createInvoiceNumberNumerator(
            final Property property,
            final @Named("Format") String format,
            final @Named("Last value") BigInteger lastIncrement) {
        return numerators.createScopedNumerator(
                Constants.INVOICE_NUMBER_NUMERATOR_NAME, property, format, lastIncrement);
    }

    public String default1CreateInvoiceNumberNumerator() {
        return "XXX-%06d";
    }

    public BigInteger default2CreateInvoiceNumberNumerator() {
        return BigInteger.ZERO;
    }

    // //////////////////////////////////////

    @Programmatic
    public void removeRuns(InvoiceCalculationParameters parameters) {
        List<Invoice> invoices = findInvoices(parameters.property(), parameters.invoiceDueDate(), InvoiceStatus.NEW);
        for (Invoice invoice : invoices) {
            invoice.remove();
        }
    }

    // //////////////////////////////////////

    private Numerators numerators;

    public void injectNumerators(final Numerators numerators) {
        this.numerators = numerators;
    }

    private EstatioSettingsService settings;

    public void injectSettings(final EstatioSettingsService settings) {
        this.settings = settings;
    }

}
