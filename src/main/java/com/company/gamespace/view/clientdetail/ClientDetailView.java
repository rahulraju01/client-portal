package com.company.gamespace.view.clientdetail;

import com.company.gamespace.entity.ClientDetails;
import com.company.gamespace.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.grid.editor.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Route(value = "client-detail-view", layout = MainView.class)
@ViewController(id = "ClientDetailView")
@ViewDescriptor(path = "client-detail-view.xml")
public class ClientDetailView extends StandardView {
    @Autowired
    protected Notifications notifications;
    @ViewComponent
    private CollectionContainer<ClientDetails> clientDetailDc;

    @Autowired
    private DataManager dataManager;

    @ViewComponent
    private DataGrid<ClientDetails> clientDetailDataGrid;

    // TimePicker for entryTime and exitTime
    private TimePicker entryTimePicker = new TimePicker();
    private TimePicker exitTimePicker = new TimePicker();

    @ViewComponent
    private TypedTextField<Object> firstName;
    @ViewComponent
    private TypedTextField<Object> lastName;
    @ViewComponent
    private CollectionLoader<ClientDetails> clientDetailDl;

    @ViewComponent

    @Subscribe
    public void onInit(final InitEvent event) {
        configureTimePickers();
    }

    @Install(to = "clientDetailDataGrid.@editor", subject = "openListener")
    protected void onCustomersDataGridEditorOpened(EditorOpenEvent<ClientDetails> event) {
        entryTimePicker.setValue(Objects.nonNull(event.getItem().getEntryTime()) ? event.getItem().getEntryTime().toLocalTime() : LocalTime.of(10, 0));
        exitTimePicker.setValue(Objects.nonNull(event.getItem().getExitTime()) ? event.getItem().getExitTime().toLocalTime() : LocalTime.of(17, 0));
        showNotification("Editor opended");
    }

    @Install(to = "clientDetailDataGrid.@editor", subject = "cancelListener")
    protected void onCustomersDataGridEditorCanceled(EditorCancelEvent<ClientDetails> event) {
        showNotification("Edit canceled");
    }

    @Install(to = "clientDetailDataGrid.@editor", subject = "saveListener")
    protected void onCustomersDataGridEditorSaved(EditorSaveEvent<ClientDetails> event) {
        LocalDateTime entryTime = LocalDateTime.of(LocalDate.now(), entryTimePicker.getValue());
        LocalDateTime exitTime = LocalDateTime.of(LocalDate.now(), exitTimePicker.getValue());
        event.getItem().setEntryTime(entryTime.atOffset(ZoneOffset.systemDefault().getRules().getOffset(entryTime)));
        event.getItem().setExitTime(exitTime.atOffset(ZoneOffset.systemDefault().getRules().getOffset(exitTime)));

        if (validateClientDetails(event.getItem())) {
            saveClientDetails(event.getItem());
            showNotification("Changes saved to the database!");
        } else {
            showNotification("Validation Error");
        }
    }

    @Install(to = "clientDetailDataGrid.@editor", subject = "closeListener")
    protected void onCustomersDataGridEditorClosed(EditorCloseEvent<ClientDetails> event) {
        showNotification("Editor closed");
    }

    protected void showNotification(String message) {
        notifications.create(message)
                .withPosition(Notification.Position.BOTTOM_END)
                .show();
    }

    @Subscribe(id = "createClientData", subject = "clickListener")
    public void onCreateClientDataClick(final ClickEvent<JmixButton> event) {
        // Create a new ClientDetails object
        ClientDetails clientDetails = new ClientDetails();
        clientDetails.setId(UUID.randomUUID());
        clientDetails.setFirstName(firstName.getValue());
        clientDetails.setLastName(lastName.getValue());

        // Validation before saving data
        if (validateClientDetails(clientDetails)) {
            saveClientDetails(clientDetails);
        } else {
            showNotification("First Name is required.");
        }
    }

    private void saveClientDetails(ClientDetails clientDetails){
        dataManager.save(clientDetails);
        clientDetailDl.load();
        clientDetailDataGrid.getDataProvider().refreshAll();  // This refreshes the grid
        showNotification("Client data saved successfully!");
    }
    // Validation method
    private boolean validateClientDetails(ClientDetails clientDetails) {
        if (clientDetails.getFirstName() == null || clientDetails.getFirstName().isEmpty()) {
            return false; // First Name is mandatory
        }
        if (clientDetails.getEntryTime() == null) {
            clientDetails.setEntryTime(OffsetDateTime.now()); // Default to current time (with offset) if entryTime is empty
        }
        return true;
    }

    // Adding TimePicker for entryTime and exitTime
    public void configureTimePickers() {
        entryTimePicker.setStep(Duration.ofMinutes(30)); // 30-minute intervals
//        entryTimePicker.setValue(LocalTime.of(10, 0));  // default time value
        entryTimePicker.setWidth(100.0f, Unit.PERCENTAGE);

        exitTimePicker.setStep(Duration.ofMinutes(30)); // 30-minute intervals
//        exitTimePicker.setValue(LocalTime.of(17, 0));  // default time value
        exitTimePicker.setWidth(100.0f, Unit.PERCENTAGE);

        // Add time pickers to the columns
        Objects.requireNonNull(clientDetailDataGrid.getColumnByKey("entryTime"))
                .setEditorComponent(entryTimePicker)
                .setRenderer(new TextRenderer<>(clientDetails ->
                        clientDetails.getEntryTime() != null ? clientDetails.getEntryTime().toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a")) : ""));

        Objects.requireNonNull(clientDetailDataGrid.getColumnByKey("exitTime")).setEditorComponent(exitTimePicker)
                .setRenderer(new TextRenderer<>(clientDetails ->
                        clientDetails.getExitTime() != null ? clientDetails.getExitTime().toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a")) : ""));
    }
}
