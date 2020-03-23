
# columns to include in every data obtained by (get_data_by_prefix)
meta_cols <- c("iteration", "tick", "market", "producer", "segment")

# Segment energy ----------------------------------------------------

data[["marketinfo_segment_energy"]] <- raw_marketinformation_results %>% 
  get_vars_from_multiple_columns(prefix = "segment", vars = "type", value = "energy")

plots[["marketinfo_segment_energy"]] <- function(data, input, average = TRUE){
  
  data <- data %>%
    filter(producer %in% input$producers_checked)
  
  if(average){
    # Average over all iterations
    plot <- data %>% 
      group_by(tick, market, producer, type, segment) %>% 
      summarise(avg_energy = mean(energy)) %>% 
      ggplot(mapping = aes(y = avg_energy  * unit_factor())) +
        facet_wrap(~ tick)

  } else {
    # By Iterations
    plot <- data %>%
      ggplot(mapping = aes(y = energy  * unit_factor())) +
      facet_grid(iteration ~ tick)
  }
  
  plot +
    geom_line(mapping = aes(x = segment, linetype = type, color = producer)) +
    scale_color_custom("producer_colors") +
    labs_default(
      y = glue("Expected power ({input$unit_prefix}Wh)"),
      x = "Segment",
      subtitle = default_subtitle(average),
      linetype = "Type",
      color = "Energy proudcer")
}

# prices  ----------------------------------------------------

data[["marketinfo_prices"]] <- raw_marketinformation_results %>% 
  get_vars_from_multiple_columns(prefix = "price", vars = "type", value = "price")


get_marketinfo_prices <- function(data, input, average = TRUE){
  
  data <- data %>%
    filter(
      producer %in% input$producers_checked)
  
  if(average){
    # Average over all iterations
    plot <- data %>% 
      group_by(tick, market, producer, type, segment) %>% 
      summarise(avg_price = mean(price)) %>% 
      ggplot(mapping = aes(y = avg_price)) +
      facet_wrap(~ tick)
    
  } else {
    # By Iterations
    plot <- data %>%
      ggplot(mapping = aes(y = price)) +
      facet_grid(iteration ~ tick)
  }
  
  plot +
    geom_line(mapping = aes(x = segment, color = producer)) +
    scale_color_custom("producer_colors") +
    labs_default(
      y = "Price (Euro)",
      x = "Segment",
      subtitle = default_subtitle(average),
      color = "Energy proudcer")
}


data[["marketinfo_electricity_prices"]] <- data[["marketinfo_prices"]] %>% 
  filter(type ==  "electricity")


plots[["marketinfo_electricity_prices"]] <- function(data, input, average = TRUE){
  get_marketinfo_prices(data, input, average)
}


data[["marketinfo_co2_prices"]] <- data[["marketinfo_prices"]] %>% 
  filter(type ==  "co2")

plots[["marketinfo_co2_prices"]] <- function(data, input, average = TRUE){
  get_marketinfo_prices(data, input, average)
}



# available capacity ------------------------------------------------------

data[["marketinfo_capacity_available"]] <- raw_marketinformation_results %>% 
  get_var_from_single_column(prefix = "capacity.available", value = "capacity")

plots[["marketinfo_capacity_available"]] <- function(data, input, average = TRUE){
  
  if(average){
    # Average over all iterations
    plot <- data %>% 
      group_by(tick, market, producer, segment) %>% 
      summarise(avg_capacity = mean(capacity)) %>% 
      ggplot(mapping = aes(y = avg_capacity  * unit_factor())) +
      facet_wrap(~ tick)
    
  } else {
    # By Iterations
    plot <- data %>%
      ggplot(mapping = aes(y = capacity  * unit_factor())) +
      facet_grid(iteration ~ tick)
  }
  
  plot +
    geom_line(mapping = aes(x = segment, color = producer)) +
    scale_color_custom("producer_colors") +
    labs_default(
      y = glue("Expected available capacity ({input$unit_prefix}W)"),
      x = "Segment",
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



