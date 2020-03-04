# -----------------------------------------------------------------------------------
# EMLab --
# -----------------------------------------------------------------------------------

library(tidyverse)

## config
id = "1583326255162"
#scenario = "Scenario_NL"
scenario = "Scenario_NL_intermittent"

directory = "/Users/marcmel/Development/java-projects/emlab-generation2/results/"
# Define your directory location (where data files are located and plots will be stored). Make sure you use '/', not '\'
# 
# filename = paste(id, scenario, "EMlabModelRole-DefaultReporter-main.csv", sep = "-")
# 
# # Read your main.csv data into a cleaned dataframe using
# data = ReadMainData(filename, directory)
# 
# plot(data$cash.Energy.Producer.NL.A)
# plot(data$cash.Energy.Producer.NL.B)
# plot(data$cash.Energy.Producer.International.A)
# plot(data$cash.Energy.Producer.International.B)
# plot(data$cash.Energy.Producer.International.C)
# 
# # Create a plot, for example operational generation capacity (for info about a function, hover over PlotCapacity and hit F1)
# PlotCapacity(data, detail=FALSE)
# # This will save the plot(s) to your directory. If you want to modify the graphs themselves, store the output in a variable:
# CapacityPlots = PlotCapacity(data, detail=FALSE)
# # You can then access the plots individually with CapacityPlots[[1]], CapacityPlots[[2]], etc.
# CapacityPlots[[1]]
# 
# # Another example function
# PlotNrPowerPlants(data)



# Marketinfo --------------------------------------------------------------

prefix = paste(id, scenario, sep ="-")

filename = paste(prefix, "EMlabModelRole-DefaultReporter-MarketInformation.csv", sep = "-")
marketForecast <- read_delim(paste0(directory, filename), delim = ";") %>% 
  arrange(iteration, tick, segment)


market_forecast_filtered <- marketForecast %>% 
  mutate(
    expectedSegmentLoadCausingVOLL = ifelse(result == 3, expectedSegmentLoad, NA)
  ) %>% 
  gather(
    expectedSegmentLoad, segmentSupply, totalCapacityAvailable, 
    key = "variable", value = "capacity") %>% 
  filter(
    # Just focus on one agent
    agent == "Energy Producer NL B",
    variable != "totalCapacityAvailable")

marketForecastPlot1 <-  market_forecast_filtered %>% 
  ggplot(mapping = aes(x = segment, y = capacity, color = variable, linetype = agent)) +
    geom_line() +
    geom_point(mapping = aes(y = expectedSegmentLoadCausingVOLL), pch = 1, col = "blue") +
    scale_y_log10() + 
    facet_wrap(~ tick) + 
    labs(
      title = "Expected Market Information")
marketForecastPlot1
  
marketForecastPlot2 <- market_forecast_filtered %>% 
  ggplot(mapping = aes(x = segment, y = expectedElectricityPrice, linetype = agent)) +
  geom_line() +
  scale_y_log10() + 
  facet_wrap(~ tick) + 
  labs(
    title = "Expected Electrictiy Prices")
marketForecastPlot2

ggsave(filename = paste0("output/", prefix, "-marketForecastPlot1.pdf"), plot = marketForecastPlot1, width = 10, height = 6)
ggsave(filename = paste0("output/", prefix, "-marketForecastPlot2.pdf"), plot = marketForecastPlot2, width = 10, height = 6)





