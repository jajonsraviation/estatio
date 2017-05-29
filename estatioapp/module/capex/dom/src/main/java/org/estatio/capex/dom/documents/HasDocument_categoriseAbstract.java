package org.estatio.capex.dom.documents;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.message.MessageService;

import org.incode.module.document.dom.impl.docs.Document;
import org.incode.module.document.dom.impl.docs.DocumentRepository;

import org.estatio.capex.dom.EstatioCapexDomModule;
import org.estatio.dom.asset.Property;
import org.estatio.dom.invoice.DocumentTypeData;

public abstract class HasDocument_categoriseAbstract extends DocumentOrHasDocument_categoriseAsAbstract {

    protected final HasDocument hasDocument;

    public HasDocument_categoriseAbstract(
            final HasDocument hasDocument,
            final DocumentTypeData documentTypeData) {
        super(documentTypeData);
        this.hasDocument = hasDocument;
    }

    @Override
    protected Document getDocument() {
        return hasDocument.getDocument();
    }

    public static class DomainEvent
            extends EstatioCapexDomModule.ActionDomainEvent<HasDocument_categoriseAbstract>{}

    @Action(
            semantics = SemanticsOf.IDEMPOTENT,
            domainEvent = DomainEvent.class
    )
    @ActionLayout(cssClassFa = "folder-open-o")
    public HasDocument act(
            final Property property,
            final boolean goToNext) {
        final HasDocument viewModel = categoriseAndAttachPaperclip(property);

        if (goToNext){
            final Document nextDocument = nextDocument();
            if (nextDocument != null) {
                return viewModelFactory.createFor(nextDocument);
            }
            // fall through to returning the view model for this document
            messageService.informUser("No more documents to categorise");
        }

        return viewModel;
    }

    @Override
    public Property default0Act() {
        return super.default0Act();
    }

    public boolean default1Act(){
        return true;
    }

    @Override
    public boolean hideAct() {
        return super.hideAct();
    }

    private Document nextDocument() {
        List<Document> incomingDocuments = documentRepository.findWithNoPaperclips();
        return incomingDocuments.size() > 0 ? incomingDocuments.get(0) : null;
    }


    @Inject
    MessageService messageService;

    @Inject
    DocumentRepository documentRepository;

    @Inject
    HasDocumentAbstract.Factory viewModelFactory;

}