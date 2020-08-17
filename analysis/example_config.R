
# Please remove example_ and rename to config_(yourname). Your own config files are currently ignored in .gitignore

# Directories and Files -------------------------------------------------------------

# Set the file path to where your emlab result files are stored.
# Use / throught and at the end
emlab_results_directory <- "/Users/path/"

# Colors ------------------------------------------------------------------

# Name of ColorBrewer Palette for colors of these filters. 
# Alternatively uncomment to use custom color settings (or if more than 12 categories)
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



# Filters -----------------------------------------------------------------

# If you want a preselection of the filters, uncomment and add the names 
# of technologies, producers, etc.  (defined in data_main.R, etc.)
# Leave commented if all should be selected by default

#selected_producers <- c("Energy Producer NL A","Energy Producer DE A")
#selected_technologies <- c("example")
#selected_fuels <- c("example")
#selected_segments <- c("example")

# Misc --------------------------------------------------------------------

app_title <- "EMLab2"

# Analysing logs can take some time. Also logs need to be saved in the right format
# see option in EMLab for that
analyse_log <- TRUE
save_log_tempfile <- TRUE

# Experimental
use_plotly <- FALSE
