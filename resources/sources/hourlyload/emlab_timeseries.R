# This script generates the hourly load profiles for PV, wind onshore and offshore
# by Marc Melliger

source("config.R")
library(lubridate)

outputFile = paste0(outputPath, "renewablesNinja2015Profiles.csv")

countriesToAnalyse <- c("DE","FR", "BE","NL","LU")
# 2015 as it is the last year with 8760 hours and full data. Also load profiles in model are from 2015
dateTimeStart <- as.POSIXct("2015-01-01 00:00:00", tz = "CET")
dateTimeEnd <-  as.POSIXct("2015-12-31 23:00:00", tz = "CET")


# Renewables Ninja Data ---------------------------------------------------
# Download current data from
# https://www.renewables.ninja/downloads
# See license

# MERRA-2 is better suited for long-term stability and overall consistency, 
# SARAH for higher precision on hourly to daily time scales (but it suffers from some missing data)
timeSeriesNinjaPV <- read_csv(file = "input/ninja_europe_pv_v1/ninja_pv_europe_v1.1_merra2.csv")
timeSeriesNinjaWind <- read_csv(file = "input/ninja_europe_wind_v1/ninja_wind_europe_v1.1_current_on-offshore.csv")


# Wrangling ---------------------------------------------------------------

myTimeSeriesNinjaPV <- timeSeriesNinjaPV %>%
  gather(key = "region", value = "profile", -time) %>%
  filter(
    region %in% countriesToAnalyse,
    between(time, dateTimeStart, dateTimeEnd)) %>% 
  add_column(technology = "PV")

myTimeSeriesNinjaWind <- timeSeriesNinjaWind %>% 
  gather(key = "region_technology", value = "profile", -time) %>%
  separate(region_technology, into = c("region", "technology")) %>% 
  filter(
    region %in% countriesToAnalyse,
    between(time, dateTimeStart, dateTimeEnd))
 
myTimeSeriesNinja <- bind_rows(myTimeSeriesNinjaPV, myTimeSeriesNinjaWind) %>% 
  mutate(
    hours = as.double(time - dateTimeStart) / 3600 + 1)

# Test and output ---------------------------------------------------------

warning("Timezone should be set correct, e.g. here all CET, and so first value should be 00:00 CET")
with_tz(head(myTimeSeriesNinjaPV$time),"CET")

# Overview plot:
# myTimeSeriesNinja %>% 
#   ggplot(mapping = aes(x = hours, y = profile)) +
#   geom_line() +
#   facet_grid(technology ~ region)

# Output
myTimeSeriesNinja %>% 
  unite(technology, region, sep = "_", col = "variable") %>% 
  select(variable, profile, hours) %>% 
  spread(key = hours, value = profile) %>%
  rename(lengthInHours = variable) %>% 
  write_csv(path = outputFile)
  
