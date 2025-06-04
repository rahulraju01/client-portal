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
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.LocalTime;
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

    @Subscribe
    public void onInit(final InitEvent event) {
        configureTimePickers();
    }

    @Install(to = "clientDetailDataGrid.@editor", subject = "openListener")
    protected void onCustomersDataGridEditorOpened(EditorOpenEvent<ClientDetails> event) {
        if(event.getGrid().getEditor().isOpen()) {
            showNotification("Close Editor");
        } else {
            showNotification("Editor opened");
        }
    }

    @Install(to = "clientDetailDataGrid.@editor", subject = "cancelListener")
    protected void onCustomersDataGridEditorCanceled(EditorCancelEvent<ClientDetails> event) {
        List<ClientDetails> mutableItems = new ArrayList<>(clientDetailDc.getMutableItems());

        // Remove any empty/unsaved rows (customize this condition as needed)
        mutableItems.removeIf(cd -> cd.getFirstName() == null && cd.getLastName() == null);

        // Update the container with the modified list
        clientDetailDc.setItems(mutableItems);
        showNotification("Edit canceled");
    }

    @Install(to = "clientDetailDataGrid.@editor", subject = "saveListener")
    protected void onCustomersDataGridEditorSaved(EditorSaveEvent<ClientDetails> event) {
        ClientDetails clientDetails = event.getItem();

        // Manually save the entity to DB
        dataManager.save(clientDetails);

        // Optionally update any related fields
        showNotification("Changes saved to the database");
    }

    @Subscribe(id = "saveButton", subject = "clickListener")
    public void onSaveButtonClick(final ClickEvent<JmixButton> event) {
        showNotification("Save button invoked !!!!!");
    }
    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        // Custom initialization logic
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
        if(clientDetailDataGrid.getEditor().isOpen()) {
            showNotification("Close the Editor before creating new data");
            return;
        }

        // Create a new ClientDetails object
        ClientDetails clientDetails = new ClientDetails();
        clientDetails.setId(UUID.randomUUID());

        // Add the clientDetails to the container
        clientDetailDc.getMutableItems().add(clientDetails);

        // Refresh the grid
        clientDetailDataGrid.getDataProvider().refreshAll();

        // Open the editor for the new row
        clientDetailDataGrid.getEditor().editItem(clientDetails);
        clientDetailDataGrid.focus();
    }
    // Adding TimePicker for entryTime and exitTime
    public void configureTimePickers() {
        entryTimePicker.setStep(Duration.ofMinutes(30)); // 30-minute intervals
        entryTimePicker.setValue(LocalTime.of(10, 0));  // default time value
        entryTimePicker.setWidth(90.0f, Unit.PERCENTAGE);

        exitTimePicker.setStep(Duration.ofMinutes(30)); // 30-minute intervals
        exitTimePicker.setValue(LocalTime.of(17, 0));  // default time value
        exitTimePicker.setWidth(90.0f, Unit.PERCENTAGE);

        // Add time pickers to the columns
        Objects.requireNonNull(clientDetailDataGrid.getColumnByKey("entryTime"))
                .setEditorComponent(entryTimePicker)
                .setRenderer(new TextRenderer<>(clientDetails ->
                        clientDetails.getEntryTime() != null ? clientDetails.getEntryTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm a")) : ""));

        Objects.requireNonNull(clientDetailDataGrid.getColumnByKey("exitTime")).setEditorComponent(exitTimePicker)
                .setRenderer(new TextRenderer<>(clientDetails ->
                        clientDetails.getExitTime() != null ? clientDetails.getExitTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm a")) : ""));
    }
}
