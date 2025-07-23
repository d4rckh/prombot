package org.prombot.commands;

import com.google.inject.Inject;
import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.utils.FileUpload;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.markers.None;
import org.prombot.config.ConfigService;
import org.prombot.config.domain.BotConfig;
import org.prombot.config.domain.NamedQuery;
import org.prombot.prom.PromFetcher;
import org.prombot.utils.FormatUtil;

@Slf4j
public class PlotCommand implements Command {
    @Getter
    private final CommandData commandData = Commands.slash("plot", "Plot a Prometheus metric.")
            .addOption(OptionType.STRING, "metric", "The metric name");

    @Inject
    ConfigService configService;

    @Inject
    PromFetcher promFetcher;

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        BotConfig config = configService.getBotConfig();

        OptionMapping queryOptionMapping = event.getOption("metric");

        if (queryOptionMapping == null) {
            event.reply("Couldn't find metric option");
            return;
        }

        String queryName = queryOptionMapping.getAsString();

        NamedQuery namedQuery = config.getMetrics().stream()
                .filter(nq -> nq.getName().toLowerCase().contains(queryName.toLowerCase()))
                .findFirst()
                .orElseThrow();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Duration du = Duration.ofDays(1);
        String step = promFetcher.calculateStep(du);

        List<Double> values = promFetcher.fetchValuesOverDuration(namedQuery.getQuery(), du, step);
        List<Integer> xData = IntStream.range(0, values.size()).boxed().collect(Collectors.toList());

        XYChart chart = new XYChartBuilder()
                .width(800)
                .height(500)
                .title("Metric: " + namedQuery.getName() + " (last 24 hours)")
                .xAxisTitle("Time")
                .yAxisTitle("Value")
                .build();

        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setChartTitleFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Line);
        chart.getStyler().setyAxisTickLabelsFormattingFunction((r) -> {
            return FormatUtil.formatValue(r, namedQuery.getFormat());
        });

        chart.addSeries(namedQuery.getName(), xData, values).setMarker(new None());

        try {
            BitmapEncoder.saveBitmap(chart, out, BitmapEncoder.BitmapFormat.PNG);

            event.reply("Here's your plot for **" + namedQuery.getName() + "**")
                    .addFiles(FileUpload.fromData(new ByteArrayInputStream(out.toByteArray()), "plot.png"))
                    .queue();
        } catch (Exception e) {
            log.error("Error saving graph: ", e);

            event.reply("Couldn't save graph").queue();
        }
    }
}
