package com.company.gamespace.view.main;

import com.company.gamespace.service.ClientDetailService;
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
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static java.lang.Boolean.TRUE;

@Route("")
@ViewController(id = "MainView")
@ViewDescriptor(path = "main-view.xml")
public class MainView extends StandardMainView {
    @ViewComponent
    private Chart revenueBarChartStatistics;

    @Autowired
    private ClientDetailService clientDetailService;

    @Subscribe
    public void onInit(final InitEvent event) {
        List<SimpleDataItem> simpleDataItems = clientDetailService.getTotalRevenueStats()
                .stream()
                .map(SimpleDataItem::new)
                .toList();

        ListChartItems<SimpleDataItem> chartItems = new ListChartItems<>(simpleDataItems);

        revenueBarChartStatistics.withDataSet(
                new DataSet().withSource(
                        new DataSet.Source<SimpleDataItem>()
                                .withDataProvider(chartItems)
                                .withCategoryField("month")
                                .withValueFields("totalRevenue")
                )
        );

//        if(initialize) {
//            typeChangeStatisticBarChart.setVisible(TRUE);
//            documentStatisticBarChart.add(typeChangeStatisticBarChart);
//        }
    }


}
