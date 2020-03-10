## Setting up all data and plot functions in this file

data <- list()


# Units

my_units <- tribble(
  ~name, ~label, ~factor,
  "Mega", "M", 1,
  "Giga", "G", 1/1000)
# TODO make selection later
my_unit <- my_units %>% 
  filter(name == "Giga")
unit_label <- pull(my_unit, label)
unit_factor <- pull(my_unit, factor)


my_labs <- c(
  "tick" = "Tick (year)",
  "average" = "Average over all selected iterations",
  "iterations" = "Selected iterations",
  "y_capacity" = glue("Capacity ({unit_label}W)"),
  "y_generation" = glue("Generation ({unit_label}Wh)")
)




# Operational capacity ----------------------------------------------------
# capacity is in MWh



data[["operational_capacities"]] <- raw_main_results %>% 
  get_data_by_prefix("operational.capacity", value = "capacity")

data[["operational_capacities_total"]] <- data$operational_capacities %>% 
  filter(key == "operational.capacity.powerplants")

data[["operational_capacities_by_tech"]] <- data$operational_capacities %>% 
  filter(key != "operational.capacity.powerplants") %>% 
  separate(col = "key", into = c("var1", "var2", "market", "technology"), sep = "\\.") %>% 
  select(-var1, -var2)


plot_operational_capacities_average <- function(my_technologies, my_iterations){
  
  data[["operational_capacities_by_tech"]] %>%
    filter(
      technology %in% my_technologies,
      iteration %in% my_iterations
    ) %>% 
    group_by(tick, market, technology) %>% 
    summarise(avg_capacity = mean(capacity)) %>% 
    ggplot(mapping = aes(x = tick, y = avg_capacity * unit_factor, fill = technology)) +
      geom_area_shaded() +
      scale_fill_manual(values = technology_colors) +
      scale_fill_technologies() +
      facet_grid(~ market) +
      labs(
        title = "Operational capacities",
        subtitle = my_labs["average"],
        x = my_labs["tick"],
        y = my_labs["y_capacity"]
      )
}

plot_operational_capacities_by_iterations <- function(my_technologies, my_iterations){
  
  data[["operational_capacities_by_tech"]] %>%
    filter(
      technology %in% my_technologies,
      iteration %in% my_iterations) %>%
    ggplot(mapping = aes(x = tick, y = capacity * unit_factor, fill = technology)) +
    geom_area_shaded() +
    scale_fill_technologies() +
    facet_wrap(iteration ~  market) +
    labs(
      title = "Operational capacities",
      subtitle = my_labs["iterations"],
      x = my_labs["tick"],
      y = my_labs["y_capacity"]
    )
}



# Pipeline Capacity -------------------------------------------------------


data[["pipeline_capacity"]] <- raw_main_results %>% 
  get_data_by_prefix("pipeline.capacity", value = "capacity") %>% 
  separate(col = "key", into = c("var1", "var2", "market"), sep = "\\.") %>% 
  select(-var1, -var2)
  


plot_pipeline_capacity_average <- function(my_iterations){
  
  data$pipeline_capacity %>%
    filter(
      iteration %in% my_iterations
    ) %>% 
    group_by(tick, market) %>% 
    summarise(avg_capacity = mean(capacity)) %>% 
    ggplot(mapping = aes(x = tick, y = avg_capacity * unit_factor)) +
  geom_line() +
  facet_grid(~ market) +
  labs(
    title = "Average pipeline capacity",
    subtitle = my_labs["average"],
    x = my_labs["tick"],
    y = my_labs["y_capacity"]
  ) 
}

plot_pipeline_capacity_by_iterations <- function(my_iterations){
  
  data$pipeline_capacity %>%
    filter(
      iteration %in% my_iterations
    ) %>% 
    ggplot(mapping = aes(x = tick, y = capacity * unit_factor)) +
    geom_line() +
    facet_grid(iteration ~ market) +
    labs(
      title = "Pipeline capacity by iterations",
      subtitle = my_labs["iterations"],
      x = my_labs["tick"],
      y = my_labs["y_capacity"]
    ) 
}



# Generation --------------------------------------------------------------

data[["generation_total"]] <- raw_main_results %>% 
  get_data_by_prefix(col_prefix = "production", value = "generation") %>% 
  separate(col = "key", into = c("var", "market", "technology"), sep = "\\.")


plot_generation_average <- function(my_technologies, my_iterations){
  
  data$generation_total %>%
    filter(
      technology %in% my_technologies,
      iteration %in% my_iterations
    ) %>% 
    group_by(tick, market, technology) %>% 
    summarise(avg_generation = mean(generation)) %>% 
    ggplot(mapping = aes(x = tick, y = avg_generation * unit_factor, fill = technology)) +
      geom_area_shaded() +
      scale_fill_technologies() +
      facet_grid(~ market) +
      labs(
        title = "Generation",
        subtitle = my_labs["average"],
        x = my_labs["tick"],
        y = my_labs["y_generation"]
    )
}

plot_generation_by_iterations <- function(my_technologies, my_iterations){
  
  data$generation_total %>%
    filter(
      technology %in% my_technologies,
      iteration %in% my_iterations) %>%
    ggplot(mapping = aes(x = tick, y = generation * unit_factor, fill = technology)) +
      geom_area_shaded() +
      scale_fill_technologies() +
      facet_wrap(iteration ~  market) +
      labs(
        title = "Generation",
        subtitle = my_labs["iterations"],
        x = my_labs["tick"],
        y = my_labs["y_generation"]
    )
}



# Cash --------------------------------------------------------------------


data[["cash_by_agents"]] <- raw_main_results %>% 
  get_data_by_prefix("cash", value = "cash") %>% 
  separate(col = key, into = c("var", "producer"), sep = "\\.")

plot_cash_by_agents_by_iterations <- function(my_producers, my_iterations){
  
  data[["cash_by_agents"]] %>% 
    filter(
      producer %in% my_producers,
      iteration %in% my_iterations) %>% 
    ggplot(mapping = aes(x = tick, y = cash, color = producer)) +
    geom_line() +
    facet_wrap(~ iteration)
}


plot_cash_by_agents_average <- function(my_producers, my_iterations){
  
  data[["cash_by_agents"]] %>% 
    filter(
      producer %in% my_producers,
      iteration %in% my_iterations) %>% 
    group_by(tick, var, producer) %>% 
    summarise(avg_cash = mean(cash)) %>% 
    ggplot(mapping = aes(x = tick, y = avg_cash, color = producer)) +
    geom_line()
}
