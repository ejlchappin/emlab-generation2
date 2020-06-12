library(tidyverse)
library(readxl)

# The aim of this script is to read current power plant data and transform it into a format suitable for emlab:
# Name, Technology, Location, Age, Owner, Capacity, Efficiency


my_countries <- c("NL", "FR", "BE", "LU", "DE")


n <- list()
raw_data <- list()
plants <- list()

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

country_to_node <- tribble(
  ~country, ~Location,
  "NL", "nlNode",
  "FR", "nlNode",
  "BE", "nlNode",
  "LU", "nlNode",
  "DE", "deNode"
)

technology_translations <- read_excel(path = "translation table.xlsx")

normalise_technology_names <- function(technologies_vector, from, to = "emlab_name", translation_table = technology_translations){

  recode_var <- technology_translations %>% pull(from)
  names(recode_var) <- technology_translations %>% pull(to)
  fct_recode(technologies_vector, !!!recode_var)
  
}


typical_age_for_plants<- tribble(
  ~Technology, ~typical_age,
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

# typical_efficiencies_for_renewables <- read_csv(file = "~/Development/java-projects/emlab-generation2/resources/data/learningCurves.csv") %>% 
#   select(Technology = 1, Efficiency = `2015`) %>%
#   filter(grepl("Eff",Technology)) %>% 
#   mutate(Technology = fct_recode(Technology, !!!recode_technologies_from_eff_to_emlab))

typical_efficiencies_for_renewables <- tribble(
  ~Technology, ~Efficiency,
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

#  c("Solar", "Onshore", "Offshore", "Nuclear", "Oil", "Natural gas", "Lignite", "Hydro", "Hard coal", "Biomass and biogas"))
#)

# idea: analyse typical sizes

renewables_technologies <- typical_capacities_for_renewables %>% pull(technology)


# Total capacity (OPSD) ----------------------------------------------------------


national_generation_capacity <- read_csv(file = "opsd-national_generation_capacity-2019-12-02/national_generation_capacity_stacked.csv")


national_generation_capacity_data <- national_generation_capacity %>% 
  select(technology, source, year, type, country, capacity) %>% 
  filter(
    country %in% my_countries,
    year == 2015, source == "ENTSO-E SOAF") %>%
  filter(technology %in% c("Solar", "Onshore", "Offshore", "Nuclear", "Oil", "Natural gas", "Lignite", "Hydro", "Hard coal", "Biomass and biogas")) %>% 
  mutate(technology = fct_recode(technology, !!!recode_technologies_from_stats_to_emlab))
  

national_generation_capacity_data %>% 
  group_by(technology, country) %>% 
  summarise(total_cap = sum(capacity)) %>% 
  ggplot(mapping = aes(x = technology, y = total_cap, fill = country)) + 
    geom_col() +
    coord_flip()

# Und jetzt?

# generate renewable dummy plants for onshore, and solar. Take real ones for offshore?
# but how big? -> Same as in model. -> also discuss with emile.

# make some plants out of the capacity, using the typical_capacities

# mapping the number of plants to the function that generates the new list 

# make list for power plants




new_renewables_list <- function(technology, country, typical_capacity, number_of_plants, last_plant_capacity, owners = all_owners){
  
  # n <- 6
  # my_technology <- "Onshore wind PGT"
  # my_country <- "nlNode"
  my_producer <- "Energy Producer DE C"
  # my_capacity <- 500
  # my_capacity_last <- 323
  my_efficiency <- 0.3
  
  tibble(
    Name = paste(technology, "plant", seq(number_of_plants)),
    Technology = technology,
    country = country,
    Age = 4, # how?? random?
    Capacity = typical_capacity
    ) %>% 
    add_row(
      Name = paste("Last", technology, "plant"),
      Technology = technology,
      country = country,
      Age = 4, # how?? random?
      Capacity = last_plant_capacity    )
}


renewable_plant_emlab <- national_generation_capacity_data %>% 
  filter(technology %in% renewables_technologies) %>% 
  left_join(typical_capacities_for_renewables) %>% 
  mutate(
    number_of_plants = floor(capacity / typical_capacity), # number of plants without last one
    last_plant_capacity = capacity %% typical_capacity) %>% 
  select(technology, country, typical_capacity, number_of_plants, last_plant_capacity) %>% 
  pmap_dfr(new_renewables_list)


random_owners <- sample_n(all_owners_df, size = nrow(renewable_plant_emlab), replace = TRUE)
# TODO: improve somehow

renewable_plant_emlab_final <- renewable_plant_emlab %>% 
  add_column(Owner =  random_owners$Owner) %>% 
  left_join(country_to_node) %>% 
  left_join(typical_efficiencies_for_renewables) %>% 
  select(Name, Technology, Location, Age, Owner, Capacity, Efficiency)

renewable_plant_emlab_final

#

  

# Renewables for all (OPSD) ------------------------------------------------------


renewable_plants[["EU"]] <- read_csv("opsd-renewable_power_plants-2019-04-05/renewable_power_plants_EU.csv")

renewable_plants$EU %>% 
  filter(
    country %in%  c("NL", "FR", "BE", "LU", "DE"),
    commissioning_date ) %>% 
  group_by(country) %>% 
  count()

# way too many plants!
renewable_plants$EU$commissioning_date

# Problem: decentral power plants.
# This list is useless. Need just to distribute to some plants to the total capacity



# Conventional Germany (OPSD) -----------------------------------------------------------------

# conventional plants DE


plants <- list()
conventional_plants <- list()
renewable_plants <- list()

conventional_plants[["DE"]] <- read_csv("opsd-conventional_power_plants-2018-12-20/conventional_power_plants_DE.csv")
 
# taking net capacity, as gross is less relevant (includes capacity used by power plant istself 

sum(conventional_plants$DE$technology, na.rm = T)

conventional_plants[["DE"]] %>% 
  mutate(age = 2015-commissioned) %>% 
  select(
    name_bnetza,
    technology,
    age,
    capacity_net_bnetza,
    efficiency_estimate,
    type,
    status) %>% 
  filter(
    age >= 0,
    status == "operating") %>% 
  group_by(technology) %>% count()
  

##3Name, Technology, Location, Age, Owner, Capacity, Efficiency

# renewables are probably to be taken from the actual plants

# Conventional FR, NL ----------------------------------------------------------------------

raw_data[["opsd_conventional_power_plants_EU"]] <- read_csv(
  file = "opsd-conventional_power_plants-2018-12-20/conventional_power_plants_EU.csv", 
  col_types = cols(
    chp = col_character(),
    type = col_character(),
    additional_info = col_character()))

plants[["opsd_conventional_power_plants_EU"]] <- raw_data[["opsd_conventional_power_plants_EU"]] %>% 
  select(
    Name = name,
    country,
    Capacity = capacity, 
    commissioned,
    energy_source_level_1,
    energy_source_level_2,
    Technology = energy_source
  ) %>% 
  filter(
    country %in% c("NL", "FR", "BE", "LU"),
    commissioned < 2015 | is.na(commissioned)
    ) %>% 
  mutate(
    Technology = normalise_technology_names(Technology, from = "opsd_stats_name"),
    Location = fct_recode(country, nlNode = "NL", nlNode = "FR"),
    Age = 2015-commissioned
  ) %>% 
  filter(    
    Technology %in% c("Biomass CHP", "Coal PSC", "Hydroelectric", "OCGT", "CCGT","Nuclear PGT", "Fuel oil PGT")) %>%
  left_join(typical_age_for_plants) %>% 
  left_join(typical_efficiencies_for_renewables)

# add Age and Owners randomly

n[["opsd_conventional_power_plants_EU"]] <- nrow(plants[["opsd_conventional_power_plants_EU"]])
plants[["opsd_conventional_power_plants_EU"]]$RandomAges <- sample(1:30, n$opsd_conventional_power_plants_EU , replace = T) # TODO: actually use typical ages
random_owners <- sample_n(all_owners_df, size = n$opsd_conventional_power_plants_EU, replace = TRUE) %>% pull(Owner)

# Conventional plants NL and FR
plants[["opsd_conventional_power_plants_EU"]] <- plants[["opsd_conventional_power_plants_EU"]] %>% 
  mutate(Final_age = ifelse(is.na(Age), RandomAges, Age)) %>%
  add_column(Owner = random_owners) %>% 
  select(Name, Technology, Location, Age = Final_age, Owner, Capacity, Efficiency)
# Name, Technology, Location, Age, Owner, Capacity, Efficiency

#NL CCGT is missing. Why is now all OCGT?

conventional_plants_EU_Veryfinal %>% 
  write_csv(path = "output/conventional_nl_ft")




# Conventional BE ---------------------------------------------------------



## Belgium, o
# Data from Elia.be <https://www.elia.be/en/grid-data/power-generation/generating-facilities>
# No data about commission, unfortunately

raw_data <- list()

raw_data[["elia_conventional_power_plants_BE"]] <- read_xls(path = "other sources/elia-be-ProductionParkOverview-Belgium-2020_edited.xls", range = "B2:M112")

raw_data[["elia_conventional_power_plants_BE"]] %>% 
  group_by(`Fuel for publication`,`Plant Type`) %>% 
  count()

plants[["elia_conventional_power_plants_BE"]] <- raw_data[["elia_conventional_power_plants_BE"]] %>% 
  select(
    name = `Generation plant`, 
    technology = `Fuel for publication`, 
    capacity = `Technical Nominal Power (MW)`) %>% 
  mutate(technology = normalise_technology_names(technology, from = "elia_be"))
         
# Name, Technology, Location, Age, Owner, Capacity, Efficiency


  
plants[["elia_conventional_power_plants_BE"]]  %>% 
  group_by(technology) %>% 
  summarise(sum(capacity))



# WRI ---------------------------------------------------------------------

plants_wri <- read_csv(file = "globalpowerplantdatabasev120/global_power_plant_database.csv")

plants_wri %>% 
  filter(country %in% c("DEU", "NLD", "BEL", "LUX", "FRA")) %>% 
  group_by(country, primary_fuel) %>% View()


# und jetzt? Ziel umwandlung in right format

plants_wri %>% 
  select(country, name, capacity_mw, Technology = primary_fuel, commissioning_year) %>% 
  filter(
    country %in% c("DEU", "NLD", "BEL", "LUX", "FRA"),
    commissioning_year < 2015) %>% 
  mutate(
    Technology = fct_recode(Technology, !!!recode_technologies_from_wri_to_emlab)
  ) %>% 
  filter(Technology == "Onshore wind PGT") %>% View()

# what to di
# this is a bad dataset that does not differentiate between powe plants


# translation tables



