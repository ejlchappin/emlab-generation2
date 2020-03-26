
# Please remove example_ and rename to config_(yourname). Your own config files are currently ignored in .gitignore

# Directories and Files -------------------------------------------------------------

# Set the file path to where your emlab result files are stored.
# Use / throught and at the end
emlab_results_directory <- "/Users/path/"

# Comment to always load latest reporters and logs
# Uncomment and specify to load specific reporters and logs
#id_to_load <- "123456789"

# Colors ------------------------------------------------------------------

# Wet to Name of ColorBrewer Palette or 
technology_color_palette <- "Set3"
producer_color_palette <- "Dark2"
fuel_color_palette <- "Dark2"

# #manually define (and delete variable technology_color_palette)
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
# # same for:
# # custom_producer_colors
# # custom_fuel_colors


# Misc --------------------------------------------------------------------

app_title <- "EMLab2"

# Analysing logs can take a long time!
analyse_log <- FALSE

