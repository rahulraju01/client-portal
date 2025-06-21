package com.company.gamespace.view.main;

import com.company.gamespace.dto.TotalRevenueStatistics;
import com.company.gamespace.service.ClientDetailService;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;
import io.jmix.chartsflowui.component.Chart;
import io.jmix.chartsflowui.data.item.SimpleDataItem;
import io.jmix.chartsflowui.kit.component.model.DataSet;
import io.jmix.chartsflowui.kit.data.chart.ListChartItems;
import io.jmix.flowui.app.main.StandardMainView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Year;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Route("")
@ViewController(id = "MainView")
@ViewDescriptor(path = "main-view.xml")
@Slf4j
public class MainView extends StandardMainView {
    @ViewComponent
    private Chart revenueBarChartStatistics;

    @Autowired
    private ClientDetailService clientDetailService;
    @ViewComponent
    private Span totalRevenueLabel;

    @Subscribe
    public void onInit(final InitEvent event) {
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        updateChartData();
    }

    private void updateChartData(){
        List<TotalRevenueStatistics> revenueStatistics = clientDetailService.getTotalRevenueStats();
        List<SimpleDataItem> simpleDataItems = revenueStatistics
                .stream()
                .map(SimpleDataItem::new)
                .toList();

        ListChartItems<SimpleDataItem> chartItems = new ListChartItems<>(simpleDataItems);

        revenueBarChartStatistics.withDataSet(
                new DataSet().withSource(
                        new DataSet.Source<SimpleDataItem>()
                                .withDataProvider(chartItems)
                                .withCategoryField("month")
                                .withValueField("totalRevenue")
                )
        );

        BigDecimal yearlyTotal = revenueStatistics.stream()
                .map(TotalRevenueStatistics::getTotalRevenue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        NumberFormat indiaFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        String formattedRevenue = indiaFormat.format(yearlyTotal);
        int currentYear = Year.now().getValue();
        totalRevenueLabel.setText(String.format("Total Revenue for %s: %s", currentYear, formattedRevenue));
    }
}
