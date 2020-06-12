library(tidyverse)
library(readxl)

# The aim of this script is to read current power plant data and transform it into a format suitable for emlab:
# Name, Technology, Location, Age, Owner, Capacity, Efficiency


my_countries <- c("NL", "FR", "BE", "LU", "DE")

raw_data <- list()
plants <- list()

technologies <- list()
technologies[["conventional"]] <- c("Biomass CHP", "Coal PSC", "Hydroelectric", "OCGT", "CCGT","Nuclear PGT", "Fuel oil PGT")
technologies[["renewables"]] <- c("Offshore wind PGT", "Onshore wind PGT", "Hydroelectric", "Photovoltaic PGT", "Biomass CHP")

# ! make sure it's the same as in main thing
all_owners <- c(
  "Pref Investor Small",
  "Pref Investor Medium",
  "Pref Investor Large",
  "Pref Investor Very Large"
  # "Pref Investor Small FrBe", # = Westblock
  # "Pref Investor Medium FrBe",
  # "Pref Investor Large FrBe",
  # "Pref Investor Very Large FrBe"
)

all_owners_df <- tibble(Owner = all_owners)

technology_translations <- read_excel(path = "translation table.xlsx")

normalise_technology_names <- function(technologies_vector, from, to = "emlab_name", translation_table = technology_translations){

  recode_var <- technology_translations %>% pull(from)
  names(recode_var) <- technology_translations %>% pull(to)
  
  fct_recode(technologies_vector, !!!recode_var) %>% 
    as.character()
  
}

typical_age_for_plants<- tribble(
  ~technology, ~typical_age,
  "Offshore wind PGT", 25,
  "Onshore wind PGT", 25,
  "Hydroelectric", 50,
  "Photovoltaic PGT", 25,
  "Biomass CHP", 30,
  "Coal PSC", 40,
  "Lignite PSC", 40,
  "CCGT", 30,
  "OCGT", 30,
  "Fuel oil PGT", 30,
  "Nuclear PGT", 40
)

typical_capacities_for_renewables<- tribble(
  ~technology, ~typical_capacity,
  "Offshore wind PGT", 600,
  "Onshore wind PGT", 600,
  "Hydroelectric", 250,
  "Photovoltaic PGT", 500,
  "Biomass CHP", 500
)

# typical_efficiencies <- read_csv(file = "~/Development/java-projects/emlab-generation2/resources/data/learningCurves.csv") %>% 
#   select(Technology = 1, Efficiency = `2015`) %>%
#   filter(grepl("Eff",Technology)) %>% 
#   mutate(Technology = fct_recode(Technology, !!!recode_technologies_from_eff_to_emlab))

typical_efficiencies <- tribble(
  ~technology, ~efficiency,
  "Photovoltaic PGT", 1.10, 
  "Onshore wind PGT", 1,    
  "Offshore wind PGT", 1.02,
  "Biomass CHP", 0.35, 
#  "OCGT", 0.489,
  "OCGT", 0.38, #Difference?
  "Coal PSC", 0.44,
  "Lignite PSC", 0.45,
  "CCGT", 0.59, 
  "Fuel oil PGT", 0.35,
  "Nuclear PGT", 0.33
)


# Data --------------------------------------------------------------------


national_generation_capacity <- read_csv(file = "sources/opsd-national_generation_capacity-2019-12-02/national_generation_capacity_stacked.csv")

raw_data[["opsd_renewable_power_plants"]] <- read_csv("sources/opsd-renewable_power_plants-2019-04-05/renewable_power_plants_EU.csv")

raw_data[["opsd_conventional_power_plants_DE"]] <- read_csv("sources/opsd-conventional_power_plants-2018-12-20/conventional_power_plants_DE.csv")

raw_data[["opsd_conventional_power_plants_EU"]] <- read_csv(
  file = "sources/opsd-conventional_power_plants-2018-12-20/conventional_power_plants_EU.csv", 
  col_types = cols(
    chp = col_character(),
    type = col_character(),
    additional_info = col_character()))

raw_data[["elia_conventional_power_plants_BE"]] <- read_xls(path = "sources/elia-be-ProductionParkOverview-Belgium-2020_edited.xls", range = "B2:M112")


# Total capacity (OPSD) ----------------------------------------------------------

national_generation_capacity_data <- national_generation_capacity %>% 
  select(technology, source, year, type, country, capacity) %>% 
  filter(
    country %in% my_countries,
    year == 2015, source == "ENTSO-E SOAF") %>%
  filter(technology %in% c("Solar", "Onshore", "Offshore", "Nuclear", "Oil", "Natural gas", "Lignite", "Hydro", "Hard coal", "Biomass and biogas")) %>% 
  mutate(technology = normalise_technology_names(technology, from = "opsd_stats_name"))
  

national_generation_capacity_data %>% 
  group_by(technology, country) %>% 
  summarise(total_cap = sum(capacity)) %>% 
  ggplot(mapping = aes(x = technology, y = total_cap, fill = country)) + 
    geom_col() +
    coord_flip()


# Powerplant Lists --------------------------------------------------------

add_owners_and_age <- function(plants_df, owners_df){
  # adding Age and Owners randomly
  # expects:  technology age
  
  
  n <- nrow(plants_df)
  
  random_owners <- sample_n(owners_df, size = n, replace = TRUE) %>% pull(Owner)
  
  plants_df %>% 
    left_join(typical_efficiencies, by = "technology") %>% 
    left_join(typical_age_for_plants, by = "technology") %>% 
    mutate(
      random_age = sample(1:30, n , replace = T), # TODO: actually use typical age
      final_age = ifelse(is.na(age), random_age, age)) %>%
    add_column(Owner = random_owners)
  
}



# Renewables ALL ----------------------------------------------------------

raw_data[["opsd_renewable_power_plants"]] %>% 
  filter(
    country %in%  c("NL", "FR", "BE", "LU", "DE")) %>% 
  group_by(country, energy_source_level_2) %>% 
  count() %>% 
  arrange(n)


# proble,: way too many plants to handle efficiently probably!
# Hence, estimate plants based on capacity


# generate renewable for plants for onshore, and solar. 
# TODO: Take real ones for offshore?


#' Generates a list of renewable plants
new_renewables_list <- function(technology, country, typical_capacity, number_of_plants, last_plant_capacity, owners = all_owners){
  
  tibble(
    name = paste(technology, "plant", country, seq(number_of_plants)),
    country,
    capacity = typical_capacity,
    technology,
    age = NA
    ) %>% 
    add_row(
      name = paste("Last", technology, "plant", country),
      country,
      capacity = last_plant_capacity,
      technology,
      age = NA
          )
}

plants[["renewables"]] <- national_generation_capacity_data %>% 
  filter(technology %in% technologies[["renewables"]]) %>% 
  left_join(typical_capacities_for_renewables, by = "technology") %>% 
  mutate(
    number_of_plants = floor(capacity / typical_capacity), # number of plants without last one
    last_plant_capacity = capacity %% typical_capacity) %>% 
  select(technology, country, typical_capacity, number_of_plants, last_plant_capacity) %>% 
  pmap_dfr(new_renewables_list)

plants[["renewables_final"]] <- plants[["renewables"]] %>% 
  add_owners_and_age(all_owners_df) %>% 
  select(name, technology, age = final_age, capacity, efficiency, Owner, country)

plants[["renewables_final_DE"]] <- plants[["renewables_final"]] %>% 
  filter(country == "DE") %>% 
  add_column(node = "deNode")

plants[["renewables_final_FR_Benelux"]] <- plants[["renewables_final"]] %>% 
  filter(country %in% c("FR", "BE", "NL", "LU")) %>% 
  add_column(node = "frBeneluxNode")


plants[["renewables_final_FR_Benelux"]] %>% 
  group_by(technology,node) %>% 
  count()


# 
# random_owners <- sample_n(all_owners_df, size = nrow(renewable_plants_emlab), replace = TRUE) # TODO: improve somehow
# 
# renewable_plant_emlab_final <- renewable_plants_emlab %>% 
#   add_column(Owner = random_owners$Owner) %>% 
#   left_join(typical_efficiencies) %>% 
#   select(Name, Technology, Location, Age, Owner, Capacity, Efficiency)
# 
# renewable_plant_emlab_final
# 
# #

  


# Conventional DE -----------------------------------------------------------------

# taking net capacity because gross is less relevant (includes capacity used by power plant istself)

plants[["opsd_conventional_power_plants_DE"]] <-  raw_data[["opsd_conventional_power_plants_DE"]] %>% 
  mutate(
    technology = normalise_technology_names(fuel, from = "opsd_stats_name"),
    age = 2015-commissioned
  ) %>%
  filter(
    age >= 0,
    status == "operating",
    technology %in% technologies[["conventional"]]) %>%
  select(
    name = name_bnetza,
    technology,
    age,
    capacity = capacity_net_bnetza,
    efficiency_estimate = efficiency_estimate) 


# Conventional FR, NL ----------------------------------------------------------------------


plants[["opsd_conventional_power_plants_EU"]] <- raw_data[["opsd_conventional_power_plants_EU"]] %>% 
  select(
    name,
    country,
    capacity, 
    commissioned,
    technology = energy_source
  ) %>% 
  filter(
    country %in% c("NL", "FR"),
    commissioned < 2015 | is.na(commissioned)
    ) %>% 
  mutate(
    technology = normalise_technology_names(technology, from = "opsd_stats_name"),
    age = 2015 - commissioned
  ) %>% 
  filter(    
    technology %in% technologies[["conventional"]]) 




# Conventional BE ----------------------------------------------------------------------


raw_data[["elia_conventional_power_plants_BE"]] %>% 
  group_by(`Fuel for publication`,`Plant Type`) %>% 
  count()

plants[["elia_conventional_power_plants_BE"]] <- raw_data[["elia_conventional_power_plants_BE"]] %>% 
  select(
    name = `Generation plant`, 
    technology = `Fuel for publication`, 
    capacity = `Technical Nominal Power (MW)`) %>% 
  mutate(
    technology = normalise_technology_names(technology, from = "elia_be"),
    technology = fct_recode(technology, `Biomass CHP` = "Other"),
    age = NA,
    commissioned = NA,
    country = "BE")

# Name, Technology, Location, Age, Owner, Capacity, Efficiency


# All Conventional (add missing data) --------------------------------------------------------

plants[["opsd_conventional_power_plants_DE"]]  # TODO add german ones but with other owners.


plants[["FR_and_benelux"]] <- bind_rows(
  plants[["opsd_conventional_power_plants_EU"]], plants[["elia_conventional_power_plants_BE"]])



plants[["FR_and_benelux_final"]] <- plants[["FR_and_benelux"]] %>% 
  add_owners_and_age(all_owners_df) %>% 
  select(name, technology, age = final_age, capacity, efficiency, Owner, country)


plants[["opsd_conventional_power_plants_DE_final"]] <- plants[["opsd_conventional_power_plants_DE"]] %>% 
  add_owners_and_age(all_owners_df) %>% 
  mutate(
    final_efficiency = ifelse(is.na(efficiency_estimate), efficiency, efficiency_estimate)) %>% 
  select(name, technology, age = final_age, capacity, efficiency = final_efficiency, Owner) %>% 
  add_column(country = "DE")



#' Tests difference to capacity statistics and scales the power plants values to reach those stats
#'
#' @param plants_df 
#' @param countries 
#' @param scale 
#'
#' @return plants_df
calculate_difference_in_capacities <- function(plants_df, countries, scale = TRUE){
  
  capacities <- list()
  
  # Verify capacity with opsd stats
  capacities[["actual"]] <- national_generation_capacity_data %>% 
    filter(
      country %in% countries,
      technology %in% technologies[["conventional"]]) %>%
    group_by(technology) %>% 
    summarise(total_capacity = sum(capacity))
  
  capacities[["generated"]] <- plants_df %>% 
    group_by(technology) %>% 
    summarise(total_capacity = sum(capacity))
  
  capacities[["diff"]] <- capacities$actual %>% 
    left_join(capacities$generated, by = c("technology"), suffix = c(".actual", ".generated")) %>% 
    mutate(difference = total_capacity.actual / total_capacity.generated)
  
  print("Differences for")
  print(capacities[["diff"]])
  
  if(scale){
    plants_df <- plants_df %>% 
      left_join(capacities$diff %>% select(technology, difference), by = "technology") %>% 
      mutate(capacity_scaled = capacity * difference)
  } else {
    plants_df <- plants_df %>% 
      mutate(capacity_scaled = capacity)
  }
  
  plants_df
  
}

plants[["FR_and_benelux_scaled"]] <- calculate_difference_in_capacities(
  plants[["FR_and_benelux_final"]], countries = c("NL", "FR", "BE"), scale = TRUE) %>% 
  rename(capacity = capacity_scaled) %>% 
  add_column(node = "frBeneluxNode")

plants[["opsd_conventional_power_plants_DE_scaled"]] <- calculate_difference_in_capacities(
  plants[["opsd_conventional_power_plants_DE_final"]], countries = c("DE"), scale = TRUE) %>% 
  rename(capacity = capacity_scaled) %>% 
  add_column(node = "deNode")


plants[["all"]] <- bind_rows(
  plants[["FR_and_benelux_scaled"]], 
  plants[["opsd_conventional_power_plants_DE_scaled"]],
  plants[["renewables_final_DE"]],
  plants[["renewables_final_FR_Benelux"]]) %>%
  select(Name = name, Technology = technology, Location = node, Age = age, Owner, Capacity = capacity, Efficiency = efficiency) #format for emlab: # Name, Technology, Location, Age, Owner, Capacity, Efficiency

# Ignore luxembourg

plants[["all"]] %>% 
  group_by(Technology, Location, Owner) %>% 
  summarise(capacity_total = sum(Capacity, na.rm = TRUE)) %>% 
  View()
