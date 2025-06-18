package com.company.gamespace.view.clientdetail;

import com.company.gamespace.entity.ClientDetails;
import com.company.gamespace.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.editor.EditorCancelEvent;
import com.vaadin.flow.component.grid.editor.EditorCloseEvent;
import com.vaadin.flow.component.grid.editor.EditorOpenEvent;
import com.vaadin.flow.component.grid.editor.EditorSaveEvent;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
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
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private Dialogs dialog;

    @Subscribe
    public void onInit(final InitEvent event) {
        configureGrid();
//        refreshBackground();
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

    private void saveClientDetails(ClientDetails clientDetails) {
        dataManager.save(clientDetails);
        clientDetailDl.load();
        clientDetailDataGrid.getSelectionModel().select(clientDetails);
        clientDetailDataGrid.getDataProvider().refreshAll();  // This refreshes the grid
        showNotification("Client data saved successfully!");
    }

    // Validation method
    private boolean validateClientDetails(ClientDetails clientDetails) {
        if (clientDetails.getFirstName() == null || clientDetails.getFirstName().isEmpty()) {
            return false; // First Name is mandatory
        }
        if (clientDetails.getEntryTime() == null) {
            OffsetDateTime nowWithSystemOffset = OffsetDateTime.now(ZoneId.systemDefault());
            clientDetails.setEntryTime(nowWithSystemOffset);
        }
        return true;
    }

    private void refreshBackground() {
        getUI().ifPresent(ui -> {
            ui.setPollInterval(60000); // 60 sec
            ui.addPollListener(e -> clientDetailDataGrid.getDataProvider().refreshAll());
        });
    }

    // Adding TimePicker for entryTime and exitTime
    public void configureGrid() {
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

        Objects.requireNonNull(clientDetailDataGrid.getColumnByKey("totalHours"))
                .setRenderer(new TextRenderer<>(client -> {
                    BigDecimal totalHours = client.getTotalHours();
                    if (totalHours == null) return "";

                    int hours = totalHours.intValue(); // Extract full hours
                    int minutes = totalHours
                            .subtract(BigDecimal.valueOf(hours)) // Decimal part
                            .multiply(BigDecimal.valueOf(60))     // Convert to minutes
                            .setScale(0, RoundingMode.DOWN)       // Don't round up
                            .intValue();                          // Convert to int

                    return String.format("%d hrs %d min", hours, minutes);
                }));

        Objects.requireNonNull(clientDetailDataGrid.getColumnByKey("finalCost"))
                .setRenderer(new TextRenderer<>(client -> {
                    if (client.getFinalCost() == null) return "";
                    NumberFormat indiaFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
                    return indiaFormat.format(client.getFinalCost());
                }));

        clientDetailDataGrid.addComponentColumn(client -> {
            Button deleteButton = uiComponents.create(Button.class);
            Icon deleteIcon = new Icon(VaadinIcon.TRASH);
            deleteButton.setIcon(deleteIcon);

            deleteButton.addClickListener(buttonClickEvent -> {
                dialog.createOptionDialog()
                        .withHeader("Confirm Deletion")
                        .withText("Are you sure you want to delete this client?")
                        .withActions(new DialogAction(DialogAction.Type.OK)
                                .withHandler(e -> {
                                    dataManager.remove(client);
                                    clientDetailDl.load();
                                    clientDetailDataGrid.getDataProvider().refreshAll();
                                    showNotification("Client deleted successfully!");

                                }), new DialogAction(DialogAction.Type.CANCEL))
                        .open();

            });
            deleteButton.getElement().getThemeList().add("error");
            return deleteButton;
        }).setHeader("Delete").setAutoWidth(true);

//        Objects.requireNonNull(clientDetailDataGrid.getColumnByKey("timeLeft"))
//                .setRenderer(new ComponentRenderer<>(client -> {
//                    Span timeLeftSpan = new Span();
//                    timeLeftSpan.setText(getTimeLeftDisplay(client));
//
//                    // Style based on time left
//                    OffsetDateTime now = OffsetDateTime.now();
//                    OffsetDateTime exit = client.getExitTime();
//
//                    if (exit == null) {
//                        timeLeftSpan.getStyle().set("color", "gray");
//                        return timeLeftSpan;
//                    }
//
//                    Duration duration = Duration.between(now, exit);
//                    if (duration.isNegative() || duration.isZero()) {
//                        timeLeftSpan.getStyle().set("color", "red");
//                    } else if (duration.toMinutes() < 30) {
//                        timeLeftSpan.getStyle().set("color", "orange");
//                    } else {
//                        timeLeftSpan.getStyle().set("color", "green");
//                    }
//
//                    return timeLeftSpan;
//                }));
    }

    private void initHeaderFilters(){
        HeaderRow filterRow = clientDetailDataGrid.appendHeaderRow();
        TextField firstNameFilter = uiComponents.create(TextField.class);
        TextField lastNameFilter = uiComponents.create(TextField.class);

        firstNameFilter.setPlaceholder("Filter...");
        firstNameFilter.setClearButtonVisible(true);
        firstNameFilter.setWidthFull();
        firstNameFilter.addValueChangeListener(e -> applyFilters(firstNameFilter.getValue(), lastNameFilter.getValue()));
        filterRow.getCell(clientDetailDataGrid.getColumnByKey("firstName")).setComponent(firstNameFilter);

        lastNameFilter.setPlaceholder("Filter...");
        lastNameFilter.setClearButtonVisible(true);
        lastNameFilter.setWidthFull();
        lastNameFilter.addValueChangeListener(e -> applyFilters(firstNameFilter.getValue(), lastNameFilter.getValue()));
        filterRow.getCell(clientDetailDataGrid.getColumnByKey("lastName")).setComponent(lastNameFilter);
    }

    private void applyFilters(String firstName, String lastName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            clientDetailDl.setParameter("firstName", null);
        } else {
            clientDetailDl.setParameter("firstName", "%" + firstName.trim().toLowerCase() + "%");
        }

        if (lastName == null || lastName.trim().isEmpty()) {
            clientDetailDl.setParameter("lastName", null);
        } else {
            clientDetailDl.setParameter("lastName", "%" + lastName.trim().toLowerCase() + "%");
        }
        clientDetailDl.load();
    }

    private String getTimeLeftDisplay(ClientDetails client) {
        if (client.getExitTime() == null) {
            return "N/A";
        }

        OffsetDateTime now = OffsetDateTime.now();
        Duration duration = Duration.between(now, client.getExitTime());

        if (duration.isNegative() || duration.isZero()) {
            return "Time’s up";
        }

        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();

        return String.format("%d hours %d minutes left", hours, minutes);
    }

    @Subscribe(id = "logoutBtn", subject = "clickListener")
    public void onLogoutBtnClick(final ClickEvent<JmixButton> event) {
        ClientDetails selectedClient = clientDetailDataGrid.asSingleSelect().getValue();

        if (selectedClient == null) {
            showNotification("Please select a client row first.");
            return;
        }

        if (Objects.isNull(selectedClient.getEntryTime())) {
            showNotification("Entry time is missing for the selected client.");
            return;
        }

        OffsetDateTime currentExitTime = OffsetDateTime.now(ZoneId.systemDefault());
        if (Objects.isNull(selectedClient.getExitTime())) {
            selectedClient.setExitTime(currentExitTime);
        }

        // ✅ Get duration and calculate exact decimal hours
        Duration duration = Duration.between(selectedClient.getEntryTime(), selectedClient.getExitTime());
        long totalMinutes = duration.toMinutes();

        BigDecimal decimalHours = BigDecimal.valueOf(totalMinutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP); // keep 2 decimal places

        BigDecimal costPerMinute = BigDecimal.valueOf(80)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        BigDecimal finalCost = decimalHours.multiply(BigDecimal.valueOf(80))
                .setScale(0, RoundingMode.HALF_UP);

        selectedClient.setTotalHours(decimalHours);
        selectedClient.setFinalCost(finalCost);

        dataManager.save(selectedClient);
        clientDetailDl.load();
        clientDetailDataGrid.getDataProvider().refreshAll();

        showNotification("Client logged out. Total Hours: " + decimalHours + ", Amount: ₹" + finalCost);
    }

}
