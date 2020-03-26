
# columns to include in every data obtained by (get_data_by_prefix)
meta_cols <- c("iteration", "tick")


# Operational capacity ----------------------------------------------------


data[["operational_capacities"]] <- raw_main_results %>% 
  get_var_from_single_column("powerplants.operational.capacity", value = "capacity")


data[["operational_capacities_by_tech_and_producer"]] <- raw_main_results %>% 
  get_vars_from_multiple_columns(
    prefix = "operational.capacity", 
    vars = c("market", "technology", "producer"), 
    value = "capacity")

data[["operational_capacities_by_tech"]] <- data[["operational_capacities_by_tech_and_producer"]] %>% 
  filter(producer == "all")
  

plots[["operational_capacities_by_tech"]] <- function(data, input, average = TRUE){

  data <- data %>%
    filter(technology %in% input$technologies_checked)
  
  if(average){
    # Average over all iterations
    plot <- data %>% 
      group_by(tick, market, technology) %>% 
      summarise(avg_capacity = mean(capacity)) %>% 
      ggplot(mapping = aes(y = avg_capacity * unit_factor())) +
        facet_grid(~ market)
  } else {
    # By Iterations
    plot <- data %>%
      ggplot(mapping = aes(y = capacity * unit_factor())) +
        facet_wrap(iteration ~ market)
  }
  plot +
    geom_area_shaded(mapping = aes(x = tick, fill = technology)) +
    scale_fill_technologies() + 
    labs_default(
      y = glue("Capacity ({input$unit_prefix}W)"),
      subtitle = default_subtitle(average),
      fill = "Technology")
}


data[["operational_capacities_by_producer"]] <- data[["operational_capacities_by_tech_and_producer"]] %>% 
  filter(producer != "all")

plots[["operational_capacities_by_producer"]] <- function(data, input, average = TRUE){
  
  data <- data %>%
    filter(technology %in% input$technologies_checked) %>% 
    filter(producer %in% input$producers_checked)
  
  
  if(average){
    # Average over all iterations
    plot <- data %>% 
      group_by(tick, market, producer, technology) %>% 
      summarise(avg_capacity = mean(capacity)) %>% 
      ggplot(mapping = aes(y = avg_capacity * unit_factor())) +
      facet_wrap(producer ~ market)
  } else {
    # By Iterations
    plot <- data %>%
      ggplot(mapping = aes(y = capacity * unit_factor())) +
      facet_wrap(iteration ~ market + producer)
  }
  plot +
    geom_area_shaded(mapping = aes(x = tick, fill = technology)) +
    scale_fill_technologies() + 
    labs_default(
      y = glue("Capacity ({input$unit_prefix}W)"),
      subtitle = default_subtitle(average),
      fill = "Technology"
      )
}


# Generation --------------------------------------------------------------

data[["generation_total"]] <- raw_main_results %>%
  get_data_by_prefix(col_prefix = "production", value = "generation") %>%
  separate(col = "key", into = c("var", "market", "technology"), sep = "\\.")

plots[["generation_total"]] <- function(data, input, average = TRUE){
  
  data <- data %>%
    filter(technology %in% input$technologies_checked) 
  
  if(average){
    # Average over all iterations
    plot <- data %>% 
      group_by(tick, market, technology) %>% 
      summarise(avg_generation = mean(generation)) %>%
      ggplot(mapping = aes(x = tick, y = avg_generation * unit_factor(), fill = technology)) +
        facet_grid(~ market)
  } else {
    # By Iterations
    plot <- data %>%
      ggplot(mapping = aes(x = tick, y = generation * unit_factor(), fill = technology)) +
        facet_wrap(iteration ~ market)
  }
  
  plot +
    geom_area_shaded() +
    scale_fill_technologies() + 
    labs_default(
      y = glue("Generation ({input$unit_prefix}Wh)"),
      subtitle = default_subtitle(average),
      fill = "Technology")
}

# Pipeline Capacity -------------------------------------------------------

data[["pipeline_capacities"]] <- raw_main_results %>%
  get_data_by_prefix("pipeline.capacity", value = "capacity") %>%
  separate(col = "key", into = c("var1", "var2", "market"), sep = "\\.") %>%
  select(-var1, -var2)

plots[["pipeline_capacities"]] <- function(data, input, average = TRUE){
  
  if(average){
    # Average over all iterations
    plot <- data %>% 
      group_by(tick, market) %>% 
      summarise(avg_capacity = mean(capacity)) %>% 
      ggplot(mapping = aes(x = tick, y = avg_capacity * unit_factor())) +
      facet_grid(~ market)
  } else {
    # By Iterations
    plot <- data %>%
      ggplot(mapping = aes(x = tick, y = capacity * unit_factor())) +
      facet_wrap(iteration ~ market)
  }
  
  plot +
    geom_line() +
    labs_default(
      y = glue("Pipeline capacity ({input$unit_prefix}W)"),
      subtitle = default_subtitle(average),
      fill = "Technology")
}


# Cash --------------------------------------------------------------------

data[["cash_by_producers"]] <- raw_main_results %>%
  get_data_by_prefix("cash", value = "cash") %>%
  separate(col = key, into = c("var", "producer"), sep = "\\.")

plots[["cash_by_producers"]] <- function(data, input, average = TRUE){
  
  data <- data %>%
    filter(producer %in% input$producers_checked)
    
  if(average){
    # Average over all iterations
    plot <- data %>% 
      group_by(tick, producer) %>%
      summarise(avg_cash = mean(cash)) %>%
      ggplot(mapping = aes(x = tick, y = avg_cash / 1e6, color = producer))
  } else {
    # By Iterations
    plot <- data %>%
      ggplot(mapping = aes(x = tick, y = cash / 1e6, color = producer)) +
      facet_wrap( ~ iteration)
  }
  
  plot +
    geom_line() +
    scale_color_custom("producer_colors") +
    labs_default(
        y = "Cash (Million EUR)",
        subtitle = default_subtitle(average),
        color = "Producer")
}

# Cashflow --------------------------------------------------------------------

data[["cashflows"]] <- raw_main_results %>%
  get_data_by_prefix("cashflow", value = "cash") %>%
  separate(col = key, into = c("var", "type"), sep = "\\.") %>% 
  select(-var)

plots[["cashflows"]] <- function(data, input, average = TRUE){
  
  if(average){
    # Average over all iterations
    plot <- data %>% 
      group_by(tick, type) %>%
      summarise(avg_cash = mean(cash)) %>%
      ggplot(mapping = aes(x = tick, y = avg_cash / 1e6, color = type))
  } else {
    # By Iterations
    plot <- data %>%
      ggplot(mapping = aes(x = tick, y = cash / 1e6, color = type)) +
      facet_wrap( ~ iteration)
  }
  
  plot +
    geom_line() +
    labs_default(
      y = "Cashflow (Million EUR)",
      subtitle = default_subtitle(average))
}


# Power plants ------------------------------------------------------------

data[["nr_powerplants"]] <- raw_main_results %>%
  get_data_by_prefix(col_prefix = "nr.of.powerplants", value = "N", suffix = "") %>%
  select(-key)

plots[["nr_powerplants"]] <- function(data, input, average = TRUE){
    
    if(average){
      # Average over all iterations
      plot <- data %>% 
        group_by(tick) %>%
        summarise(avg = mean(N)) %>%
        ggplot(mapping = aes(x = tick, y = avg))
    } else {
      # By Iterations
      plot <- data %>%
        ggplot(mapping = aes(x = tick, y = N)) +
        facet_wrap( ~ iteration)
    }
  
  plot +
    geom_line() +
    labs_default(
      y = "Nr of powerplants",
      subtitle = default_subtitle(average))
}



# Fuel Prices -------------------------------------------------------------


data[["fuel_prices"]] <- raw_main_results %>%
  get_data_by_prefix(col_prefix = "substance.price", value = "price") %>%
  separate(col = "key", into = c("var1","var2", "fuel"), sep = "\\.") %>%
  select(-var1, -var2)

plots[["fuel_prices"]] <- function(data, input, average = TRUE){
  
  data <- data %>%
    filter(fuel %in% input$fuels_checked)
    
    if(average){
      # Average over all iterations
      plot <- data %>% 
        group_by(tick, fuel) %>%
        summarise(avg_price = mean(price)) %>%
        ggplot(mapping = aes(x = tick, y = avg_price / 1e6, color = fuel))
    } else {
      # By Iterations
      plot <- data %>%
        ggplot(mapping = aes(x = tick, y = price / 1e6, color = fuel)) +
        facet_wrap( ~ iteration)
    }
  
  plot +
    geom_line() +
    scale_color_custom("fuel_colors") +
    labs_default(
      y = "Price (Million EUR/ton)",
      subtitle = default_subtitle(average),
      color = "Fuel")
}


# Fuel Volumes ------------------------------------------------------------

data[["fuel_volumes"]] <- raw_main_results %>%
  get_data_by_prefix(col_prefix = "substance.volume", value = "volume") %>%
  separate(col = "key", into = c("var1","var2", "fuel"), sep = "\\.") %>%
  select(-var1, -var2)

plots[["fuel_volumes"]] <- function(data, input, average = TRUE){
  
  data <- data %>%
    filter(fuel %in% input$fuels_checked)
  
  if(average){
    # Average over all iterations
    plot <- data %>% 
      group_by(tick, fuel) %>%
      summarise(avg_volume = mean(volume)) %>%
      ggplot(mapping = aes(x = tick, y = avg_volume, color = fuel))
    
  } else {
    # By Iterations
    plot <- data %>%
      ggplot(mapping = aes(x = tick, y = volume, color = fuel)) +
      facet_wrap( ~ iteration)
  }
  
  plot +
    geom_line() +
    scale_color_custom("fuel_colors") +
    labs_default(
      y = "Volume (tons)",
      subtitle = default_subtitle(average),
      color = "Fuel")
}

 
# CO2 Prices -------------------------------------------------------------

data[["CO2_prices"]] <- raw_main_results %>%
  get_data_by_prefix(col_prefix = "price.co2", value = "price", suffix = "") %>%
  select(-key)

plots[["CO2_prices"]] <- function(data, input, average = TRUE){

  if(average){
    # Average over all iterations
    plot <- data %>% 
      group_by(tick) %>%
      summarise(avg_price = mean(price)) %>%
      ggplot(mapping = aes(x = tick, y = avg_price))
  } else {
    # By Iterations
    plot <- data %>%
      ggplot(mapping = aes(x = tick, y = price)) +
      facet_wrap( ~ iteration)
  }
  
  plot +
    geom_line() +
    labs_default(
      y = "Price (EUR/ton)",
      subtitle = default_subtitle(average))
}



# CO2 Volumes ------------------------------------------------------------

data[["CO2_volumes"]] <- raw_main_results %>%
  get_data_by_prefix(col_prefix = "co2.emissions", value = "volume") %>%
  separate(col = "key", into = c("var1","var2", "type"), sep = "\\.") %>%
  select(-var1, -var2)

plots[["CO2_volumes"]] <- function(data, input, average = TRUE){
  
  if(average){
    # Average over all iterations
    plot <- data %>% 
      group_by(tick, type) %>%
      summarise(avg_volume = mean(volume)) %>%
      ggplot(mapping = aes(x = tick, y = avg_volume, linetype = type)) 
  } else {
    # By Iterations
    plot <- data %>%
      ggplot(mapping = aes(x = tick, y = volume, linetype = type)) +
      facet_wrap( ~ iteration)
    
  }
  
  plot +
    geom_line() +
    labs_default(
      y = "Volume (tons)",
      subtitle = default_subtitle(average))
}





# Market Average prices -------------------------------------------------------------


data[["average_prices"]] <- raw_main_results %>%
  get_data_by_prefix(col_prefix = "market.average price", value = "price") %>%
  separate(col = "key", into = c("var1","var2", "market"), sep = "\\.") %>%
  select(-var1, -var2)

plots[["average_prices"]] <- function(data, input, average = TRUE){
  
  if(average){
    # Average over all iterations
    plot <- data %>% 
      group_by(tick, market) %>%
      summarise(avg_price = mean(price)) %>% 
      ggplot(mapping = aes(x = tick, y = avg_price / unit_factor())) +
      facet_grid( ~ market)

  } else {
    # By Iterations
    plot <- data %>%
      ggplot(mapping = aes(x = tick, y = price / unit_factor())) +
      facet_wrap(iteration ~ market)
    
  }
  
  plot +
    geom_line() +
    labs_default(
      y = glue("Electricity price (EUR/{input$unit_prefix}Wh)"),
      subtitle = default_subtitle(average))
}

# Market Average Volumes -------------------------------------------------------------


data[["average_volumes"]] <- raw_main_results %>%
  get_data_by_prefix(col_prefix = "market.volume", value = "volume") %>%
  separate(col = "key", into = c("var1","var2", "market"), sep = "\\.") %>%
  select(-var1, -var2)

plots[["average_volumes"]] <- function(data, input, average = TRUE){
  
  if(average){
    # Average over all iterations
    plot <- data %>% 
      group_by(tick, market) %>%
      summarise(avg_volume = mean(volume)) %>% 
      ggplot(mapping = aes(x = tick, y = avg_volume)) +
      facet_grid( ~ market)
    
  } else {
    # By Iterations
    plot <- data %>%
      ggplot(mapping = aes(x = tick, y = volume)) +
      facet_wrap(iteration ~ market)
    
  }
  
  plot +
    geom_line() +
    labs_default(
      y = glue("Market Volume"),
      subtitle = default_subtitle(average))
}

# Segment prices -------------------------------------------------------------

# ..   segment.price.DutchMarket.segment20 = col_number(),
# ..   segment.hours.DutchMarket.segment20 = col_number(),
# ..   segment.load.DutchMarket.segment20 = col_number(),
# ..   segment.volume.DutchMarket.segment20

# structure for segments is always the same:

get_segment_data <- function(data, segment_value){

  data %>%
    get_data_by_prefix(col_prefix = paste0("segment.",segment_value), value = segment_value) %>%
    separate(col = "key", into = c("var1","var2", "market", "segment"), sep = "\\.") %>%
    mutate(
      segment = as.numeric(str_remove(segment, "segment")),
      segment = as.factor(segment)
    ) %>%
    select(-var1, -var2)

}

data[["segment_prices"]] <- raw_main_results %>%
  get_segment_data(segment_value = "price")

plots[["segment_prices"]] <- function(data, input, average = TRUE){
  
  data <- data %>%
    filter(segment %in% input$segments_checked)
  
  if(average){
    # Average over all iterations
    plot <- data %>% 
      group_by(tick, market, segment) %>%
      summarise(avg_price = mean(price)) %>% 
      ggplot(mapping = aes(x = tick, y = avg_price / unit_factor(), color = segment)) +

    if(input$all_in_one_plot){
      plot <- plot +
        facet_grid( ~ market)
      
    } else {
      plot <- plot +
        facet_grid(segment ~ market)
    }
    
    
  } else {
    # By Iterations
    plot <- data %>%
      ggplot(mapping = aes(x = tick, y = price, color = segment)) +
      facet_grid(segment + iteration ~ market)
    
  }
  
  plot +
    geom_line() +
    labs_default(
      y = glue("Electricity price (EUR/{input$unit_prefix}Wh)"),
      subtitle = default_subtitle(average))
}


# Electricity load -------------------------------------------------------------

data[["segment_load"]] <- raw_main_results %>%
  get_segment_data(segment_value = "load")

plots[["segment_load"]] <- function(data, input, average = TRUE){
  
  data <- data %>%
    filter(segment %in% input$segments_checked)

  label_color = "Segment"
  label_x = "Tick (year)"
    
  if(average){
    # Average over all iterations
    data <- data %>% 
      group_by(tick, market, segment) %>%
      summarise(avg_load = mean(load)) %>% 
      ungroup()

      if(input$all_in_one_plot){
        
        if(input$flip_tick_segment){
          
          plot <- data %>%
            mutate(
              segment = as.numeric(segment),
              tick = as.factor(tick)
            ) %>%
            ggplot(mapping = aes(x = segment, y = avg_load * unit_factor(), color = tick))
          
          label_color = "Tick (Year)"
          label_x = "Segment"
          
        } else {
          plot <- data %>%
            ggplot(mapping = aes(x = tick, y = avg_load * unit_factor(), color = segment))
        }
        
        plot <- plot +
          facet_wrap(~ market)
        
        
      } else {
        
        if(input$flip_tick_segment){
          plot <- data %>%
            mutate(
              segment = as.numeric(segment),
              tick = as.factor(tick)
            ) %>%
            ggplot(mapping = aes(x = segment, y = avg_load * unit_factor(), color = tick)) +
            facet_wrap(market ~ tick)
          
          label_color = "Tick (Year)"
          label_x = "Segment"
          
        } else {
          plot <- data %>%
            ggplot(mapping = aes(x = tick, y = avg_load * unit_factor(), color = segment)) +
            facet_grid(segment ~ market)
        }
      }
    
    
  } else {
    # By Iterations
    plot <- data %>%
      ggplot(mapping = aes(x = tick, y = load * unit_factor(), color = segment)) +
      geom_line() +
      facet_wrap(iteration ~ market)
  }
  
  plot +
    geom_line() +
    labs_default(
      y = glue("Load ({input$unit_prefix}W)"),
      x = label_x,
      color = label_color,
      subtitle = default_subtitle(average))
}



# Segment volume -------------------------------------------------------------

data[["segment_volume"]] <- raw_main_results %>%
  get_segment_data(segment_value = "volume")


plots[["segment_volume"]] <- function(data, input, average = TRUE){
  
  data <- data %>%
    filter(segment %in% input$segments_checked)
    
  if(average){
    # Average over all iterations
    plot <- data %>% 
      group_by(tick, market, segment) %>%
      summarise(avg_volume = mean(volume)) %>% 
      ggplot(mapping = aes(y = avg_volume * unit_factor()))
      
      if(input$all_in_one_plot){
        plot <- plot +
          facet_grid( ~ market)
        
      } else {
        plot <- plot +
          facet_grid(segment ~ market)
      }
    
    
  } else {
    # By Iterations
    plot <- data %>%
      ggplot(mapping = aes(y = volume * unit_factor())) +
      facet_grid(market ~ iteration)
    
  }
  
  plot +
    geom_line(x = tick,  color = segment) +
    labs_default(
      y = glue("Load ({input$unit_prefix}Wh)"),
      subtitle = default_subtitle(average))
}

# Segment volume -------------------------------------------------------------

data[["segment_hours"]] <- raw_main_results %>%
  get_segment_data(segment_value = "hours")


plots[["segment_hours"]] <- function(data, input, average = TRUE){
  
  data <- data %>%
    filter(segment %in% input$segments_checked)
  
  if(average){
    # Average over all iterations
    plot <- data %>% 
      group_by(tick, market, segment) %>%
      summarise(avg_hours = mean(hours)) %>% 
      ggplot(mapping = aes(x = tick, y = avg_hours, color = segment))
    
    if(input$all_in_one_plot){
      plot <- plot +
        facet_grid( ~ market)
      
    } else {
      plot <- plot +
        facet_grid(segment ~ market)
    }
    
    
  } else {
    # By Iterations
    plot <- data %>%
      ggplot(mapping = aes(x = tick, y = hours, color = segment)) +
      facet_grid(market ~ iteration)
    
  }
  
  plot +
    geom_line() +
    labs_default(
      y = glue("Hours (h)"),
      subtitle = default_subtitle(average))
}


# Variables for app  ------------------------------------------------------------

# variables for inputs based on data above


all_technologies <- get_sinlge_variable(data$operational_capacities_by_tech, technology)
all_producers <- get_sinlge_variable(data$cash_by_producers, producer)
all_fuels <- get_sinlge_variable(data$fuel_prices, fuel)
all_segments <- get_sinlge_variable(data$segment_prices, segment)


technology_colors <- set_colors(all_technologies, "custom_technology_colors", "technology_color_palette")
producer_colors <- set_colors(all_producers, "custom_producer_colors", "producer_color_palette")
fuel_colors <- set_colors(all_fuels, "custom_fuel_colors", "fuel_color_palette")
#segment_colors <- set_colors(all_segments, "custom_segment_colors", "segment_color_palette")



