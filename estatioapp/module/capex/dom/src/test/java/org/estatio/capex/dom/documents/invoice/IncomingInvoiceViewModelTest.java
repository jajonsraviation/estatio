package org.estatio.capex.dom.documents.invoice;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.joda.time.LocalDate;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;

import org.estatio.capex.dom.order.Order;
import org.estatio.capex.dom.order.OrderItem;
import org.estatio.capex.dom.order.OrderRepository;
import org.estatio.capex.dom.project.Project;
import org.estatio.dom.charge.Charge;
import org.estatio.dom.party.Organisation;

public class IncomingInvoiceViewModelTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Mock
    private OrderRepository mockOrderRepository;

    @Test
    public void orderItemsForAutoComplete_Test(){

        List<OrderItem> result;

        // given
        IncomingInvoiceViewModel vm = new IncomingInvoiceViewModel();
        vm.orderRepository = mockOrderRepository;
        Charge someOtherCharge = new Charge();
        Charge charge = new Charge();
        Project project = new Project();
        Organisation seller = new Organisation();

        OrderItem oi1 = new OrderItem();
        oi1.setCharge(charge);
        oi1.setProject(project);

        OrderItem oi2 = new OrderItem();
        oi2.setCharge(charge);
        oi2.setProject(project);

        OrderItem oi3 = new OrderItem();
        oi3.setCharge(charge);

        OrderItem oi4 = new OrderItem();
        oi4.setCharge(someOtherCharge); // charge is mandatory on OrderItem

        Order o1 = new Order();
        o1.getItems().add(oi1);
        oi1.setOrdr(o1);
        o1.setSeller(seller);

        Order o2 = new Order();
        o2.getItems().add(oi2);
        oi2.setOrdr(o2);

        Order o3 = new Order();
        o3.getItems().add(oi3);
        oi3.setOrdr(o3);

        Order o4 = new Order();
        o4.getItems().add(oi4);
        oi4.setOrdr(o4);

        // expect
        context.checking(new Expectations() {
            {
                allowing(mockOrderRepository).matchByOrderNumber(with(any(String.class)));
                will(returnValue(Arrays.asList(
                        o1, o2, o3, o4
                )));
            }

        });

        // when
        result = vm.orderItemsForAutoComplete("***");

        // then
        Assertions.assertThat(result.size()).isEqualTo(4);

        // and when
        vm.setCharge(charge);
        result = vm.orderItemsForAutoComplete("***");

        // then
        Assertions.assertThat(result.size()).isEqualTo(3);

        // and when
        vm.setProject(project);
        result = vm.orderItemsForAutoComplete("***");

        // then
        Assertions.assertThat(result.size()).isEqualTo(2);

        // and when
        vm.setSeller(seller);
        result = vm.orderItemsForAutoComplete("***");

        // then
        Assertions.assertThat(result.size()).isEqualTo(1);

    }

    @Test
    public void minimalRequiredDataToComplete() throws Exception {

        // given
        IncomingInvoiceViewModel vm = new IncomingInvoiceViewModel();

        // when
        String result = vm.minimalRequiredDataToComplete();

        // then
        Assertions.assertThat(result).isEqualTo("invoice number, buyer, seller, date received, due date, net amount, gross amount required");

        // and when
        vm.setInvoiceNumber("123");
        vm.setNetAmount(new BigDecimal("100"));
        result = vm.minimalRequiredDataToComplete();

        // then
        Assertions.assertThat(result).isEqualTo("buyer, seller, date received, due date, gross amount required");

        // and when
        vm.setBuyer(new Organisation());
        vm.setSeller(new Organisation());
        vm.setDateReceived(new LocalDate());
        vm.setDueDate(new LocalDate());
        vm.setGrossAmount(BigDecimal.ZERO);
        result = vm.minimalRequiredDataToComplete();

        // then
        Assertions.assertThat(result).isNull();

    }

}