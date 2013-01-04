package org.estatio.dom.asset;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.jdo.annotations.Element;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;


import org.joda.time.LocalDate;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.AutoComplete;
import org.apache.isis.applib.annotation.DescribedAs;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.Resolve;
import org.apache.isis.applib.annotation.Resolve.Type;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.core.objectstore.jdo.applib.annotations.Auditable;
import org.estatio.dom.communicationchannel.CommunicationChannel;
import org.estatio.dom.communicationchannel.CommunicationChannelType;
import org.estatio.dom.communicationchannel.PostalAddress;
import org.estatio.dom.party.Parties;
import org.estatio.dom.party.Party;

@PersistenceCapable
@Auditable
@AutoComplete(repository = Properties.class)
public class Property extends AbstractDomainObject {

    // {{ Reference (attribute, title)
    private String reference;

    @DescribedAs("Unique reference code for this property")
    @Title(sequence = "1", prepend = "[", append = "] ")
    @Disabled
    @MemberOrder(sequence = "1.1")
    public String getReference() {
        return reference;
    }

    public void setReference(final String code) {
        this.reference = code;
    }

    // }}

    // {{ Name (attribute, title)
    private String name;

    @DescribedAs("Unique reference code for this property")
    @Title(sequence = "2")
    @MemberOrder(sequence = "1.2")
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    // }}

    // {{ Type (attribute)
    private PropertyType type;

    @MemberOrder(sequence = "1.3")
    public PropertyType getType() {
        return type;
    }

    public void setType(final PropertyType type) {
        this.type = type;
    }

    // }}

    // {{ OpeningDate (attribute)
    private LocalDate openingDate;

    @javax.jdo.annotations.Persistent
    // required for applib.Date
    @MemberOrder(sequence = "1.4")
    public LocalDate getOpeningDate() {
        return openingDate;
    }

    public void setOpeningDate(final LocalDate openingDate) {
        this.openingDate = openingDate;
    }

    // }}

    // {{ AcquireDate (attribute)
    private LocalDate acquireDate;

    @javax.jdo.annotations.Persistent
    // required for applib.Date
    @MemberOrder(sequence = "1.5")
    @Optional
    public LocalDate getAcquireDate() {
        return acquireDate;
    }

    public void setAcquireDate(final LocalDate acquireDate) {
        this.acquireDate = acquireDate;
    }

    // }}

    // {{ Disposal LocalDate (attribute)
    private LocalDate disposalDate;

    @javax.jdo.annotations.Persistent
    // required for applib.Date
    @MemberOrder(sequence = "1.6")
    @Optional
    public LocalDate getDisposalDate() {
        return disposalDate;
    }

    public void setDisposalDate(final LocalDate disposalDate) {
        this.disposalDate = disposalDate;
    }

    // }}

    // {{ Area (attribute)
    // REVIEW: should a BigDecimal be used instead?
    private Double area;

    @MemberOrder(sequence = "1.7")
    public Double getArea() {
        return area;
    }

    public void setArea(final Double area) {
        this.area = area;
    }

    // }}

    // {{ AreaOfUnits (attribute)
    @MemberOrder(sequence = "1.8")
    public BigDecimal getAreaOfUnits() {
        BigDecimal area = BigDecimal.ZERO;
        for (Unit unit : getUnits()) {
            area.add(unit.getArea() != null ? unit.getArea() : BigDecimal.ZERO);
        }
        return area;
    }

    // }}

    // {{ City (derived attribute)
    @MemberOrder(sequence = "1.9")
    public String getCity() {
        // TODO: Ugly piece of code
        for (CommunicationChannel communicationChannel : getCommunicationChannels()) {
            if (communicationChannel instanceof PostalAddress) {
                return ((PostalAddress) communicationChannel).getCity();
            }
        }
        return "";
    }

    // }}
   
    // {{ Actors (list, unidir)
    @Persistent(mappedBy = "property")
    private Set<PropertyActor> actors = new LinkedHashSet<PropertyActor>();

    @Resolve(Type.EAGERLY)
    @MemberOrder(sequence = "2.1")
    public Set<PropertyActor> getActors() {
        return actors;
    }

    public void setActors(final Set<PropertyActor> actors) {
        this.actors = actors;
    }

    public void addToActors(final PropertyActor actor) {
        // check for no-op
        if (actor == null || getActors().contains(actor)) {
            return;
        }
        // associate new
        getActors().add(actor);
        // additional business logic
        onAddToActors(actor);
    }

    public void removeFromActors(final PropertyActor actor) {
        // check for no-op
        if (actor == null || !getActors().contains(actor)) {
            return;
        }
        // dissociate existing
        getActors().remove(actor);
        // additional business logic
        onRemoveFromActors(actor);
    }

    protected void onAddToActors(final PropertyActor actor) {
    }

    protected void onRemoveFromActors(final PropertyActor actor) {
    }

    // }}

    // {{ CommunicationChannels (list, unidir)
    @Join(column="PROPERTY_ID", generateForeignKey = "false")
    @Element(column = "COMMUNICATIONCHANNEL_ID", generateForeignKey = "false")
    private Set<CommunicationChannel> communicationChannels = new LinkedHashSet<CommunicationChannel>();

    @Resolve(Type.EAGERLY)
    @MemberOrder(sequence = "1")
    public Set<CommunicationChannel> getCommunicationChannels() {
        return communicationChannels;
    }

    public void setCommunicationChannels(final Set<CommunicationChannel> communicationChannels) {
        this.communicationChannels = communicationChannels;
    }

    @MemberOrder(name="CommunicationChannels", sequence="1")
    public CommunicationChannel addCommunicationChannel(final CommunicationChannelType communicationChannelType) {
        CommunicationChannel communicationChannel = communicationChannelType.create(getContainer());
        communicationChannels.add(communicationChannel);
        return communicationChannel;
    }
    
    @Hidden
    public CommunicationChannel findCommunicationChannelForType(CommunicationChannelType type){
        for (CommunicationChannel c : communicationChannels){
            if (c.getType().equals(type)){
                return c;
            }
        }
        return null;
    }

    // }}

    // {{ Units (list, bidir)
    @Persistent(mappedBy = "property")
    private Set<Unit> units = new LinkedHashSet<Unit>();

    @Resolve(Type.EAGERLY)
    @MemberOrder(sequence = "2.2")
    public Set<Unit> getUnits() {
        return units;
    }

    public void setUnits(final Set<Unit> units) {
        this.units = units;
    }

    // }}

    // {{ NewUnit (action)
    @MemberOrder(name = "Units", sequence = "1")
    public Unit newUnit(@Named("Code") final String code, @Named("Name") final String name) {
        Unit unit = unitsRepo.newUnit(code, name);
        unit.setProperty(this);
        getUnits().add(unit);
        return unit;
    }

    // }}

    // {{ addActor (action)
    @MemberOrder(name="Actors", sequence = "1")
    public PropertyActor addActor(
           @Named("party") Party party, 
           @Named("type") PropertyActorType type, 
           @Named("startDate") @Optional LocalDate startDate, 
           @Named("endDate") @Optional LocalDate endDate) {
        PropertyActor propertyActor = propertyActorsRepo.findPropertyActor(this, party, type, startDate, endDate);
        if (propertyActor ==  null) { 
            propertyActor = propertyActorsRepo.newPropertyActor(this, party, type, startDate, endDate);
            actors.add(propertyActor);
        }
        return propertyActor;
    }
    
    public List<Party> choices0AddActor() {
        return parties.allParties();
    }
    
    // }}
    
    // {{ injected: Units
    
    private Units unitsRepo;

    public void setUnits(final Units unitsRepo) {
        this.unitsRepo = unitsRepo;
    }

    private Parties parties;

    public void setParties(Parties parties) {
        this.parties = parties;
    }
    
    // }}

    // {{ injected: PropertyActors
    private PropertyActors propertyActorsRepo;

    public void setPropertyActorsRepo(final PropertyActors propertyActors) {
        this.propertyActorsRepo = propertyActors;
    }

    // }}

}
