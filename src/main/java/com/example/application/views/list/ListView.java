package com.example.application.views.list;

import com.example.application.data.Contact;
import com.example.application.services.CrmService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route(value = "", layout = MainLayout.class)
@PageTitle("Contacts | Vaadin CRM")
public class ListView extends VerticalLayout {

    final CrmService crmService;
    final Grid<Contact> grid = new Grid<>(Contact.class);
    final TextField filterText = new TextField();
    final ContactForm contactForm;

    public ListView(final CrmService crmService) {
        this.crmService = crmService;
        contactForm = new ContactForm(crmService.findAllCompanies(), crmService.findAllStatuses());

        addClassName("list-view");
        setSizeFull();
        configureGrid();
        configureForm();

        add(getToolbar(), getContent());
        updateList();
        closeEditor();
    }

    private void configureGrid() {
        grid.addClassNames("contact-grid");
        grid.setSizeFull();
        grid.setColumns("firstName", "lastName", "email");
        grid.addColumn(contact -> contact.getStatus().getName()).setHeader("Status");
        grid.addColumn(contact -> contact.getCompany().getName()).setHeader("Company");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
        grid.asSingleSelect().addValueChangeListener(event -> editContact(event.getValue()));
    }

    private void configureForm() {
        contactForm.setWidth("25vw");
        contactForm.addSaveListener(this::saveContact);
        contactForm.addDeleteListener(this::deleteContact);
        contactForm.addCloseListener(event -> closeEditor());
    }

    private void saveContact(final ContactForm.SaveEvent saveEvent) {
        crmService.saveContact(saveEvent.getContact());
        updateList();
        closeEditor();
    }

    private void deleteContact(final ContactForm.DeleteEvent deleteEvent) {
        crmService.deleteContact(deleteEvent.getContact());
        updateList();
        closeEditor();
    }

    private HorizontalLayout getToolbar() {
        filterText.setPlaceholder("Filter by name...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(event -> updateList());

        final Button addContactButton = new Button("Add contact");
        addContactButton.addClickListener(event -> addContact());

        final var toolbar = new HorizontalLayout(filterText, addContactButton);
        toolbar.addClassName("toolbar");
        return toolbar;
    }

    private Component getContent() {
        final HorizontalLayout content = new HorizontalLayout(grid, contactForm);
        content.setFlexGrow(2, grid);


        content.setFlexGrow(1, contactForm);
        content.addClassNames("content");
        content.setSizeFull();
        return content;
    }

    private void updateList() {
        grid.setItems(crmService.findAllContacts(filterText.getValue()));
    }

    private void closeEditor() {
        contactForm.setContact(null);
        contactForm.setVisible(false);
        addClassName("editing");
    }

    private void editContact(final Contact contact) {
        if (contact == null) {
            closeEditor();
        } else {
            contactForm.setContact(contact);
            contactForm.setVisible(true);
            addClassName("editing");
        }
    }

    private void addContact() {
        grid.asSingleSelect().clear();
        editContact(new Contact());
    }
}
