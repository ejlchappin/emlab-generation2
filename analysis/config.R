
config_params <- list()
config_params[["emlab_results_directory"]] <- "../results/"
config_params[["emlab_results_meta_directory"]] <- "../custom_data"

# Analyse logs takes loading time
config_params[["analyse_log"]] <- TRUE
config_params[["save_log_tempfile"]] <- TRUE

config_params[["app_title"]] <- "EMLab-Generation2"

config_params[["data_to_load"]] <- c("main", "marketinformation")
#config_params[["data_to_load"]] <- c("main", "financialexpectations", "marketinformation")
#config_params[["data_to_load"]] <- c("main", "financialexpectations")

#config_params[["data_to_process"]]  <- c(")
config_params[["data_to_process"]] <- c("operational_capacities", "generation_total", "pipeline_capacities", "cash_by_producers", "cashflows", "nr_powerplants", "fuel_prices", "fuel_volumes", "CO2_prices", "CO2_volumes", "average_electricity_prices", "average_market_volumes", "segment_hours", "segment_volume", "segment_load", "segment_prices")


# set to Name of ColorBrewer Palette or  (see ?brewer)
# manually define below (and delete variable technology_color_palette)
technology_color_palette <- "Set3"
#producer_color_palette <- "Paired"
fuel_color_palette <- "Dark2"
#segment_color_palette <- "YlGnBu"


# nice_colors = brewer.pal(n = 11, name = "Set3")
# custom_technology_colors <- c(
#  "Coal PSC" = nice_colors[1],        
#  "Lignite PSC" = nice_colors[2],
#  "Biomass CHP" = nice_colors[3],
#  "CCGT" = nice_colors[4],
#  "OCGT" = nice_colors[5],
#  "Fuel oil PGT" = nice_colors[6],
#  "Nuclear PGT" = nice_colors[7],
#  "Photovoltaic PGT" = nice_colors[8],
#  "Hydroelectric" = nice_colors[9],
#  "Onshore wind PGT" = nice_colors[10],
#  "Offshore wind PGT" = nice_colors[11]
# )

# fuel_colors


# filters

#selected_producers <- c("Pref Investor Medium NL","Energy Producer NL B")
#selected_producers <- c("Pref Investor Small","Pref Investor Medium","Pref Investor Large", "Pref Investor Very Large")
selected_cashflows <- c("FEED_IN_PREMIUM", "TENDER_SUBSIDY", "DOWNPAYMENT", "LOAN", "ELECTRICITY_SPOT", "CO2TAX", "FIXEDOMCOST", "COMMODITY", "CO2AUCTION")
