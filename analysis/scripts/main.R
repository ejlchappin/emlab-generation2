## Setting up all data and plot functions in this file

data <- list()

# Operational capacity ----------------------------------------------------
# capacity is in MWh



data[["operational_capacities"]] <- raw_main_results %>% 
  get_data_by_prefix("operational.capacity", value = "capacity")

data[["operational_capacities_total"]] <- data$operational_capacities %>% 
  filter(key == "operational.capacity.powerplants")

data[["operational_capacities_by_tech"]] <- data$operational_capacities %>% 
  filter(key != "operational.capacity.powerplants") %>% 
  separate(col = "key", into = c("var1", "var2", "market", "technology"), sep = "\\.") %>% 
  select(-var1, -var2) %>% 
  mutate(technology = as.factor(technology))

plot_operational_capacities_average <- function(technologies, my_iterations){
  
  data[["operational_capacities_by_tech"]] %>%
    filter(
      technology %in% technologies,
      iteration %in% my_iterations
    ) %>% 
    group_by(tick, market, technology) %>% 
    summarise(avg_capacity = mean(capacity)) %>% 
    ggplot(mapping = aes(x = tick, y = avg_capacity, fill = technology)) +
    geom_area_shaded() +
    scale_fill_technologies() +
    facet_grid(~ market)
}

plot_operational_capacities_by_iterations <- function(technologies, my_iterations){
  
  data[["operational_capacities_by_tech"]] %>%
    filter(
      technology %in% technologies,
      iteration %in% my_iterations) %>%
    ggplot(mapping = aes(x = tick, y = capacity, fill = technology)) +
    geom_area_shaded() +
    scale_fill_technologies() +
    facet_wrap(iteration ~  market)
}


# Generation --------------------------------------------------------------

data[["generation_total"]] <- raw_main_results %>% 
  get_data_by_prefix(col_prefix = "production", value = "generation") %>% 
  separate(col = "key", into = c("var", "market", "technology"), sep = "\\.")

# TODO work on that
plot_generation <- function(){
  
  data[["generation_total"]] %>%
    ggplot(mapping = aes(x = tick, y = generation, fill = technology)) +
    geom_area_shaded() +
    scale_fill_technologies() +
    facet_wrap(iteration ~ market)
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
