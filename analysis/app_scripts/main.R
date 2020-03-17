## Setting up all data and plot functions in this file

# Container for data
data <- list()

# Containter for plot functions
plots <- list(
  average = list(),
  iterations = list()
)

# Definition of data and plots for area plots --------------------------------------------

# Operational capacity

data[["operational_capacities"]] <- raw_main_results %>% 
  get_data_by_prefix("operational.capacity", value = "capacity")

data[["operational_capacities_total"]] <- data$operational_capacities %>% 
  filter(key == "operational.capacity.powerplants")

data[["operational_capacities_by_tech"]] <- data$operational_capacities %>% 
  filter(key != "operational.capacity.powerplants") %>% 
  separate(col = "key", into = c("var1", "var2", "market", "technology"), sep = "\\.") %>% 
  select(-var1, -var2)



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

# Generation 

data[["generation_total"]] <- raw_main_results %>%
  get_data_by_prefix(col_prefix = "production", value = "generation") %>%
  separate(col = "key", into = c("var", "market", "technology"), sep = "\\.")

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


# Definition of data and plots for line plots --------------------------------------------

# 
# Pipeline Capacity
# 

data[["pipeline_capacities"]] <- raw_main_results %>%
  get_data_by_prefix("pipeline.capacity", value = "capacity") %>%
  separate(col = "key", into = c("var1", "var2", "market"), sep = "\\.") %>%
  select(-var1, -var2)

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


# Cash 

data[["cash_by_producers"]] <- raw_main_results %>%
  get_data_by_prefix("cash", value = "cash") %>%
  separate(col = key, into = c("var", "producer"), sep = "\\.")

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
    geom_line()
    #scale_fill_producer()
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
    #scale_fill_producer() +
    facet_wrap( ~ iteration)
}


# Power plants

data[["nr_powerplants"]] <- raw_main_results %>%
  get_data_by_prefix(col_prefix = "nr.of.powerplants", value = "N", suffix = "") %>%
  select(-key)

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



# Variables for app  ------------------------------------------------------------


# variables for inputs based on results
all_technologies <- get_sinlge_variable(data$operational_capacities_by_tech, technology)
all_producers <- get_sinlge_variable(data$cash_by_producers, producer)

# Other
technology_colors <- set_technology_colors(all_technologies)

all_my_units <- available_units$prefix
names(all_my_units) <- available_units$name


# These plots will be generated in the app
# the *data_name* needs to correspond to the entries in data[] below
app_plots <- list(
  list(
    data_name = "operational_capacities_by_tech",
    y_label = "Capacity ({unit_prefix}W)"
   ),
  list(
    data_name = "pipeline_capacities",
    y_label = "Pipeline capacity ({unit_prefix}W)"
  ),
  list(
    data_name = "generation_total",
    y_label = "Generation ({unit_prefix}Wh)"
  ),
  list(
    data_name = "cash_by_producers",
    y_label = "Cash (Euro)"
  ),
  list(
    data_name = "nr_powerplants",
    y_label = "Nr of powerplants"
  )
)

# This is the main menu a adds the plot pages to the app
app_menu <- list(
  navbarMenu(
    "Energy",
    tabPanel("Powerplants", source("app_pages/tab_powerplants.R")$value),
    tabPanel("Capacity", source("app_pages/tab_capacity.R")$value),
    tabPanel("Pipeline capacity", source("app_pages/tab_pipeline_capacity.R")$value),
    tabPanel("Generation", source("app_pages/tab_generation.R")$value)
  ),
  navbarMenu(
    "Cash",
    tabPanel("Cash", source("app_pages/tab_cash_producers.R")$value)
  )
)
