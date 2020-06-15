
# columns to include in every data obtained by (get_data_by_prefix)
meta_cols <- c("iteration", "tick", "market", "producer", "segment")

# Segment energy ----------------------------------------------------

data[["expected_power_per_segment"]] <- raw_marketinformation_results %>% 
  get_vars_from_multiple_columns(prefix = "segment", vars = "type", value = "energy")


show_filters[["expected_power_per_segment"]] <- c("segment", "producer", "tick_expected")


plots[["expected_power_per_segment"]] <- function(data, input, average = TRUE){
  
  data <- data %>%
    filter(producer %in% input$producers_checked,
           tick == seq(input$tick_expected[1],input$tick_expected[2]),
           segment %in% input$segments_checked)
  
  if(average){
    # Average over all iterations
    plot <- data %>% 
      group_by(tick, market, producer, type, segment) %>% 
      summarise(avg_energy = mean(energy)) %>% 
      ggplot(mapping = aes(y = avg_energy  * unit_factor())) +
      facet_grid(tick ~ market)
    
  } else {
    # By Iterations
    plot <- data %>%
      ggplot(mapping = aes(y = energy  * unit_factor())) +
      facet_wrap(vars(iteration, market, tick))
  }
  
  plot +
    geom_line(mapping = aes(x = segment, linetype = type, color = producer)) +
    scale_color_custom("producer_colors") +
    labs_default(
      y = glue("Expected power ({input$unit_prefix}Wh)"),
      x = "Segment",
      title = get_title_of_selected_plot(input),
      subtitle = default_subtitle(average),
      linetype = "Type",
      color = "Energy producer")
}

# prices  ----------------------------------------------------

data[["marketinfo_prices"]] <- raw_marketinformation_results %>% 
  get_vars_from_multiple_columns(prefix = "price", vars = "type", value = "price")



get_marketinfo_prices <- function(data, input, average = TRUE){
  
  data <- data %>%
    filter(
      producer %in% input$producers_checked,
      tick == seq(input$tick_expected[1],input$tick_expected[2]),
      segment %in% input$segments_checked)
  
  if(average){
    # Average over all iterations
    plot <- data %>% 
      group_by(tick, market, producer, type, segment) %>% 
      summarise(avg_price = mean(price)) %>% 
      ggplot(mapping = aes(y = avg_price)) +
      facet_grid(tick ~ market, labeller = label_both)
    
  } else {
    # By Iterations
    plot <- data %>%
      ggplot(mapping = aes(y = price)) +
      facet_wrap(vars(iteration, market, tick), labeller = label_both)
    }
  
  plot +
    #geom_line(mapping = aes(x = segment, color = producer)) +
    geom_col(mapping = aes(x = segment, fill = producer), position = "dodge2") +
    scale_fill_custom("producer_colors") +
    #scale_color_custom("producer_colors") +
    labs_default(
      y = "Price (Euro/MWh)",
      x = "Segment",
      title = get_title_of_selected_plot(input),
      subtitle = default_subtitle(average),
      color = "Energy producer")
}


data[["expected_electricity_prices_per_segment"]] <- data[["marketinfo_prices"]] %>% 
  filter(type ==  "electricity")

show_filters[["expected_electricity_prices_per_segment"]] <- c("segment", "producer", "tick_expected")

plots[["expected_electricity_prices_per_segment"]] <- function(data, input, average = TRUE){
  get_marketinfo_prices(data, input, average)
}


data[["expected_CO2_prices_per_segment"]] <- data[["marketinfo_prices"]] %>% 
  filter(type ==  "co2")

show_filters[["expected_CO2_prices_per_segment"]] <- c("segment", "producer", "tick_expected")

plots[["expected_CO2_prices_per_segment"]] <- function(data, input, average = TRUE){
  get_marketinfo_prices(data, input, average)
}



# available capacity ------------------------------------------------------

data[["expected_available_capacity_per_segment"]] <- raw_marketinformation_results %>% 
  get_var_from_single_column(prefix = "capacity.available", value = "capacity")

show_filters[["expected_available_capacity_per_segment"]] <- c("segment", "producer", "tick_expected")


plots[["expected_available_capacity_per_segment"]] <- function(data, input, average = TRUE){
  
  data <- data %>%
    filter(
      producer %in% input$producers_checked,
      tick == seq(input$tick_expected[1],input$tick_expected[2]),
      segment %in% input$segments_checked)
  
  if(average){
    # Average over all iterations
    plot <- data %>% 
      group_by(tick, market, producer, segment) %>% 
      summarise(avg_capacity = mean(capacity)) %>% 
      ggplot(mapping = aes(y = avg_capacity  * unit_factor())) +
      facet_grid(tick ~ market)
    
  } else {
    # By Iterations
    plot <- data %>%
      ggplot(mapping = aes(y = capacity  * unit_factor())) +
      facet_wrap(vars(iteration, market, tick))
  }
  
  plot +
    geom_line(mapping = aes(x = segment, color = producer)) +
    scale_color_custom("producer_colors") +
    labs_default(
      y = glue("Expected available capacity in year ({input$unit_prefix}W)"),
      x = "Segment",
      title = get_title_of_selected_plot(input),
      subtitle = default_subtitle(average))
}


# Variables for app  ------------------------------------------------------------

# variables for inputs based on data above


# all_technologies <- get_sinlge_variable(data$operational_capacities_by_tech, technology)
# all_producers <- get_sinlge_variable(data$cash_by_producers, producer)
# all_fuels <- get_sinlge_variable(data$fuel_prices, fuel)
# all_segments <- get_sinlge_variable(data$segment_prices, segment)
# 
# 
# technology_colors <- set_colors(all_technologies, "custom_technology_colors", "technology_color_palette")
# producer_colors <- set_colors(all_producers, "custom_producer_colors", "producer_color_palette")
# fuel_colors <- set_colors(all_fuels, "custom_fuel_colors", "fuel_color_palette")
# #segment_colors <- set_colors(all_segments, "custom_segment_colors", "segment_color_palette")



