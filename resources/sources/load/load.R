
library(tidyverse)
library(lubridate)

load_countries = c(
  BE = "BE", 
  DE= "DE", 
  FR = "FR", 
  LU = "LU", 
  NL = "NL")

# Parse data --------------------------------------------------------------

# data from https://transparency.entsoe.eu/


# some data was missing, and hence estimated from the values for an hour before
# date        time average_load country
# <date>     <dbl>        <dbl> <fct>  
#   1 2015-03-29     2          NaN BE     
# 2 2015-03-29     2          NaN DE     
# 3 2015-03-29     2          NaN FR     
# 4 2015-05-12    17          NaN FR     
# 5 2015-03-29     2          NaN LU     
# 6 2015-03-29     2          NaN NL 

parse_load_data <- function(country){
  data <- read_csv(
    file = paste0("ENTSOE data/ENTSOE_load_2015_", country, ".csv"), 
    col_names = c("time_interval", "forecast", "load"), # times all in CET
    skip = 1) %>% 
    
    select(time_interval, load) 
  
  # taking average over every hour (from quarterly hours)
  data <- data %>% 
    mutate(
      date = as.Date(str_sub(time_interval, 1, 10), format = "%d.%m.%Y"),
      time = as.numeric(str_sub(time_interval, 12, 13))) %>% 
    group_by(date, time) %>% 
    summarise(average_load = mean(load, na.rm = TRUE))
  
  data %>% 
    ungroup() %>% 
    add_column(country = !!country)
}



all_data <- map(load_countries, parse_load_data) %>% 
  reduce(bind_rows) %>% 
  mutate(country = as_factor(country))

summary(all_data)
#8760 days are ok for a normal year

# Combine for paper -------------------------------------------------------

all_data_and_aggregate <- all_data %>% 
  bind_rows(
    all_data %>% 
      filter(country %in% c("BE", "NL", "LU", "FR")) %>% 
      group_by(date, time) %>% 
      summarise(average_load = sum(average_load, na.rm = TRUE)) %>%
      ungroup() %>% 
      add_column(country  = "BENELUX_FR")
  )


final_load_df <- map_dfr(c(load_countries, BENELUX_FR = "BENELUX_FR"), function(country){
  all_data_and_aggregate %>% 
    filter(country == !!country) %>%
    add_column(hour = seq(1:8760)) %>% 
    select(hour, average_load) %>% 
    spread(key = hour, value = average_load)
  }, .id = "country"
)


# Prepare for emlab data structure ----------------------------------------

write_csv(x = final_load_df, path = "entsoe_hourly_load_2015_DE_FR_BENELUX_1.csv")
