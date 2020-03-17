
# Definition of data and plots 

# Operational capacity ----------------------------------------------------


data[["operational_capacities"]] <- raw_main_results %>% 
  get_data_by_prefix("operational.capacity", value = "capacity")

data[["operational_capacities_total"]] <- data$operational_capacities %>% 
  filter(key == "operational.capacity.powerplants")

data[["operational_capacities_by_tech"]] <- data$operational_capacities %>% 
  filter(key != "operational.capacity.powerplants") %>% 
  separate(col = "key", into = c("var1", "var2", "market", "technology"), sep = "\\.") %>% 
  select(-var1, -var2)


plots$meta[["operational_capacities_by_tech"]] <- list(
  data_name = "operational_capacities_by_tech",
  y_label = "Capacity ({unit_prefix}W)"
)

#' Generates an area plot of the operational capacities of selected technologies.
#' The mean is take over all iterations
#'
#' @param data data frame with data
#' @param unit factor to divide unit
#' @param input a list with the character vector *technologies_checked* listing all technologies to be plotted
#'
#' @return ggplot
plots$average[["operational_capacities_by_tech"]] <- function(data, unit_factor, input){
  
  data %>%
    filter(technology %in% input$technologies_checked) %>% 
    group_by(tick, market, technology) %>% 
    summarise(avg_capacity = mean(capacity)) %>% 
    ggplot(mapping = aes(x = tick, y = avg_capacity * unit_factor, fill = technology)) +
      geom_area_shaded() +
      scale_fill_technologies() +
      facet_grid(~ market)
}

#' Generates an area plot of the operational capacities of selected technologies.
#' By Iterations
#'
#' @param data data frame with data
#' @param unit factor to divide unit
#' @param input a list with the character vector *technologies_checked* listing all technologies to be plotted
#'
#' @return ggplot
plots$iterations[["operational_capacities_by_tech"]] <- function(data, unit_factor, input){
  
  data %>%
    filter(technology %in% input$technologies_checked) %>% 
    ggplot(mapping = aes(x = tick, y = capacity * unit_factor, fill = technology)) +
      geom_area_shaded() +
      scale_fill_technologies() +
      facet_wrap(iteration ~ market)
}

 

# Generation --------------------------------------------------------------

data[["generation_total"]] <- raw_main_results %>%
  get_data_by_prefix(col_prefix = "production", value = "generation") %>%
  separate(col = "key", into = c("var", "market", "technology"), sep = "\\.")

plots$meta[["generation_total"]] = list(
  data_name = "generation_total",
  y_label = "Generation ({unit_prefix}Wh)"
  )
  
#' Generates an area plot of the generation of selected technologies.
#' The mean is take over all iterations
#'
#' @param data data frame with data
#' @param unit factor to divide unit
#' @param input a list with the character vector *technologies_checked* listing all technologies to be plotted
#'
#' @return ggplot
plots$average[["generation_total"]] <- function(data, unit_factor, input){

  data %>%
    filter(technology %in% input$technologies_checked_gen) %>% 
    group_by(tick, market, technology) %>% 
    summarise(avg_generation = mean(generation)) %>% 
    ggplot(mapping = aes(x = tick, y = avg_generation * unit_factor, fill = technology)) +
      geom_area_shaded() +
      scale_fill_technologies() +
      facet_grid(~ market)
}

#' Generates an area plot of the generation of selected technologies.
#' By Iterations
#'
#' @param data data frame with data
#' @param unit factor to divide unit
#' @param input a list with the character vector *technologies_checked* listing all technologies to be plotted
#'
#' @return ggplot
plots$iterations[["generation_total"]] <- function(data, unit_factor, input){
  
  data %>%
    filter(technology %in% input$technologies_checked_gen) %>% 
    ggplot(mapping = aes(x = tick, y = generation * unit_factor, fill = technology))  +
      geom_area_shaded() +
      scale_fill_technologies() +
      facet_wrap(iteration ~ market)
}



# Pipeline Capacity -------------------------------------------------------

data[["pipeline_capacities"]] <- raw_main_results %>%
  get_data_by_prefix("pipeline.capacity", value = "capacity") %>%
  separate(col = "key", into = c("var1", "var2", "market"), sep = "\\.") %>%
  select(-var1, -var2)

plots$meta[["pipeline_capacities"]] <- list(
  data_name = "pipeline_capacities",
  y_label = "Pipeline capacity ({unit_prefix}W)"
)

#' Generates a line plot of the total pipeline capacities.
#' The mean is take over all iterations
#'
#' @param data data frame with data
#' @param unit factor to divide unit
#' @param input can be NULL
#'
#' @return ggplot
plots$average[["pipeline_capacities"]] <- function(data, unit_factor, input){
  
  data %>%
    group_by(tick, market) %>% 
    summarise(avg_capacity = mean(capacity)) %>% 
    ggplot(mapping = aes(x = tick, y = avg_capacity * unit_factor)) +
    geom_line() +
    scale_fill_technologies() +
    facet_grid(~ market)
}

#' Generates a line plot of the total pipeline capacities
#' By Iterations
#'
#' @param data data frame with data
#' @param unit factor to divide unit
#' @param input can be NULL
#'
#' @return ggplot
plots$iterations[["pipeline_capacities"]] <- function(data, unit_factor, input){
  
  data %>%
    ggplot(mapping = aes(x = tick, y = capacity * unit_factor)) +
    geom_line() +
    scale_fill_technologies() +
    facet_wrap(iteration ~ market)
}



# Cash --------------------------------------------------------------------


data[["cash_by_producers"]] <- raw_main_results %>%
  get_data_by_prefix("cash", value = "cash") %>%
  separate(col = key, into = c("var", "producer"), sep = "\\.")
  
plots$meta[["cash_by_producers"]] <- list(
    data_name = "cash_by_producers",
    y_label = "Cash (EUR)"
  )

#' Generates a line plot of the total cash by producers
#' The mean is take over all iterations
#'
#' @param data data frame with data
#' @param unit factor to divide unit
#' @param input a list with the character vector *producers_checked* listing all technologies to be plotted
#'
#' @return ggplot
plots$average[["cash_by_producers"]] <- function(data, unit_factor, input){
  
  data %>%
    filter(producer %in% input$producers_checked) %>% 
    group_by(tick, producer) %>% 
    summarise(avg_cash = mean(cash)) %>% 
    ggplot(mapping = aes(x = tick, y = avg_cash * unit_factor, color = producer)) +
    geom_line() +
    scale_color_custom("producer_colors")
}

#' Generates a line plot of the total cash by producers
#' By Iterations
#'
#' @param data data frame with data
#' @param unit factor to divide unit
#' @param input a list with the character vector *producers_checked* listing all technologies to be plotted
#'
#' @return ggplot
plots$iterations[["cash_by_producers"]] <- function(data, unit_factor, input){
  
  data %>%
    filter(producer %in% input$producers_checked) %>% 
    ggplot(mapping = aes(x = tick, y = cash * unit_factor, color = producer)) +
    geom_line() +
    scale_color_custom("producer_colors") +
    facet_wrap( ~ iteration)
}



# Power plants ------------------------------------------------------------

data[["nr_powerplants"]] <- raw_main_results %>%
  get_data_by_prefix(col_prefix = "nr.of.powerplants", value = "N", suffix = "") %>%
  select(-key)
  
plots$meta[["nr_powerplants"]] <- list(
    data_name = "nr_powerplants",
    y_label = "Nr of powerplants")

#' Generates a line plot of the nr of total powerplants
#' The mean is taken over all iterations
#'
#' @param data data frame with data
#' @param unit (not used)
#' @param input (not used)
#'
#' @return ggplot
plots$average[["nr_powerplants"]] <- function(data, unit_factor, input){
  
  data %>%
    group_by(tick) %>% 
    summarise(avg = mean(N)) %>% 
    ggplot(mapping = aes(x = tick, y = avg)) +
      geom_line()
}

#' Generates a line plot of the nr of total powerplants
#' By Iterations
#'
#' @param data data frame with data
#' @param unit (not used)
#' @param input (not used)
#' 
#' @return ggplot
plots$iterations[["nr_powerplants"]] <- function(data, unit_factor, input){
  
  data %>%
    ggplot(mapping = aes(x = tick, y = N)) +
    geom_line() +
    facet_wrap(~ iteration)
}



# Fuel Prices -------------------------------------------------------------


data[["fuel_prices"]] <- raw_main_results %>%
  get_data_by_prefix(col_prefix = "substance.price", value = "price") %>%
  separate(col = "key", into = c("var1","var2", "fuel"), sep = "\\.") %>%
  select(-var1, -var2)


plots$meta[["fuel_prices"]] = list(
    data_name = "fuel_prices",
    y_label = "Price (EUR)"
)

#' Generates a line plot of fuel prices
#' The mean is take over all iterations
#'
#' @param data data frame with data
#' @param unit (not used)
#' @param input a list with the character vector *fuels_checked* listing all fuels to be plotted
#'
#' @return ggplot
plots$average[["fuel_prices"]] <- function(data, unit_factor, input){
  
  data %>%
    filter(fuel %in% input$fuels_checked) %>% 
    group_by(tick, fuel) %>% 
    summarise(avg_price = mean(price)) %>% 
    ggplot(mapping = aes(x = tick, y = avg_price, color = fuel)) +
    geom_line() +
    scale_color_custom("fuel_colors")
}

#' Generates a line plot of fuel prices
#' By Iterations
#'
#' @param data data frame with data
#' @param unit (not used)
#' @param input a list with the character vector *fuels_checked* listing all fuels to be plotted
#'
#' @return ggplot
plots$iterations[["fuel_prices"]] <- function(data, unit_factor, input){
  
  data %>%
    filter(fuel %in% input$fuels_checked) %>% 
    ggplot(mapping = aes(x = tick, y = price, color = fuel)) +
    geom_line() +
    scale_color_custom("fuel_colors") +
    facet_wrap( ~ iteration)
}

# Fuel Volumes ------------------------------------------------------------

data[["fuel_volumes"]] <- raw_main_results %>%
  get_data_by_prefix(col_prefix = "substance.volume", value = "volume") %>%
  separate(col = "key", into = c("var1","var2", "fuel"), sep = "\\.") %>%
  select(-var1, -var2)


plots$meta[["fuel_volumes"]] = list(
  data_name = "fuel_volumes",
  y_label = "Volume"
)

#' Generates a line plot of fuel volumes
#' The mean is take over all iterations
#'
#' @param data data frame with data
#' @param unit (not used)
#' @param input a list with the character vector *fuels_checked_vol* listing all fuels to be plotted
#'
#' @return ggplot
plots$average[["fuel_volumes"]] <- function(data, unit_factor, input){
  
  data %>%
    filter(fuel %in% input$fuels_checked_vol) %>% 
    group_by(tick, fuel) %>% 
    summarise(avg_volume = mean(volume)) %>% 
    ggplot(mapping = aes(x = tick, y = avg_volume, color = fuel)) +
    geom_line() +
    scale_color_custom("fuel_colors")
}

#' Generates a line plot of fuel prices
#' By Iterations
#'
#' @param data data frame with data
#' @param unit (not used)
#' @param input a list with the character vector *fuels_checked_vol* listing all fuels to be plotted
#'
#' @return ggplot
plots$iterations[["fuel_volumes"]] <- function(data, unit_factor, input){
  
  data %>%
    filter(fuel %in% input$fuels_checked_vol) %>% 
    ggplot(mapping = aes(x = tick, y = volume, color = fuel)) +
    geom_line() +
    scale_color_custom("fuel_colors") +
    facet_wrap( ~ iteration)
}



# CO2 Prices -------------------------------------------------------------

data[["CO2_prices"]] <- raw_main_results %>%
  get_data_by_prefix(col_prefix = "price.co2", value = "price", suffix = "") %>%
  select(-key)


plots$meta[["CO2_prices"]] = list(
  data_name = "CO2_prices",
  #title = "CO2 Prices",
  y_label = "Price (EUR)"
)

#' Generates a line plot of fuel prices
#' The mean is take over all iterations
#'
#' @param data data frame with data
#' @param unit (not used)
#' @param input (not used)
#'
#' @return ggplot
plots$average[["CO2_prices"]] <- function(data, unit_factor, input){
  
  data %>%
    group_by(tick) %>% 
    summarise(avg_price = mean(price)) %>% 
    ggplot(mapping = aes(x = tick, y = avg_price)) +
    geom_line()
}

#' Generates a line plot of fuel prices
#' By Iterations
#'
#' @param data data frame with data
#' @param unit (not used)
#' @param input (not used)
#'
#' @return ggplot
plots$iterations[["CO2_prices"]] <- function(data, unit_factor, input){
  
  data %>%
    ggplot(mapping = aes(x = tick, y = price)) +
    geom_line() +
    facet_wrap( ~ iteration)
}


# CO2 Volumes ------------------------------------------------------------

data[["CO2_volumes"]] <- raw_main_results %>%
  get_data_by_prefix(col_prefix = "co2.emissions", value = "volume") %>%
  separate(col = "key", into = c("var1","var2", "type"), sep = "\\.") %>%
  select(-var1, -var2)

plots$meta[["CO2_volumes"]] = list(
  data_name = "CO2_volumes",
  y_label = "Volume [t]"
)

#' Generates a line plot of CO2 volumes
#' The mean is take over all iterations
#'
#' @param data data frame with data
#' @param unit (not used)
#' @param input (not used)
#'
#' @return ggplot
plots$average[["CO2_volumes"]] <- function(data, unit_factor, input){
  
  data %>%
    group_by(tick, type) %>% 
    summarise(avg_volume = mean(volume)) %>% 
    ggplot(mapping = aes(x = tick, y = avg_volume, linetype = type)) +
    geom_line()
}

#' Generates a line plot of CO2 volumes
#' By Iterations
#'
#' @param data data frame with data
#' @param unit (not used)
#' @param input a list with the character vector *fuels_checked_vol* listing all fuels to be plotted
#'
#' @return ggplot
plots$iterations[["CO2_volumes"]] <- function(data, unit_factor, input){
  
  data %>%
    group_by(tick, type) %>% 
    ggplot(mapping = aes(x = tick, y = volume, linetype = type)) +
    geom_line() +
    facet_wrap( ~ iteration)
}

# Variables for app  ------------------------------------------------------------

# variables for inputs based on data above


all_technologies <- get_sinlge_variable(data$operational_capacities_by_tech, technology)
all_producers <- get_sinlge_variable(data$cash_by_producers, producer)
all_fuels <- get_sinlge_variable(data$fuel_prices, fuel)

technology_colors <- set_colors(all_technologies, "custom_technology_colors", "technology_color_palette")
producer_colors <- set_colors(all_producers, "custom_producer_colors", "producer_color_palette")
fuel_colors <- set_colors(all_fuels, "custom_fuel_colors", "fuel_color_palette")

all_my_units <- available_units$prefix
names(all_my_units) <- available_units$name

