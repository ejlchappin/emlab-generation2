# Analysis script for EMLab generation



## Overview

This R shiny app provides an UI to analyse EMLab Generation output. 

With it is possible to:

- Analyse runs from EMLab with a graphical and interactive interface
- Search through the log files
- Generate custom reports for further use in publications

## Installation

To run

## First steps

1. Create your own config file from the config_example.R by renaming it to config.R

config file

To run the shiny app, open app.R and execute all code. By default, the app opens the most recent simulation run. You can customise this behaviour by setting id_to_load in app.R (or alternatively config.R).

Parsing the result files may take a while (we work on making this quicker).



### add filters
You can add new filter to the ui to better analyse your files. For this, you need to make changes to the ui, the server and add the data logic.
Here is the example of adding a filter for markets

1. Add new ui element. In app/ui_filter.R copy and customise one of the existing filters. You need to customise the condition, e.g. output.show_filter_market, and the checkbox content to populate with variables that store the filter values (e.g. DutchMarket).

```
  ui_filters$market <- function(){
    conditionalPanel(
      condition = "output.show_filter_market == true",
      box(title = "Filter by Markets", width = 12, collapsible = TRUE, collapsed = TRUE, solidHeader = FALSE,
          checkboxGroupInput("markets_checked", label = "",
                             choices = all_markets,
                             selected = selected_markets))
    )
  }
```
  

2. Add new ui element to the sidebar. Add the new funtion in the ui_filtersidebar function of top of app/ui_filter

```
    ui_filters$single_iterations(),
    ui_filters$market(),
    ui_filters$technology(),
```




3. In app/server.R look for the Custom filters comment and copy and customise one of the existing codes. Make sure the names match with what's in ui_filters.R

```
  ### Filter for market
  output$show_filter_market <- reactive({
    toggle_filters("market",input$single_plot_selected)
  })
  outputOptions(output, "show_filter_market", suspendWhenHidden = FALSE)
  
```


4. Add values for checkboxed or similar to a file in custom_plots. Copy the code from data_main at the bottom. There are example of the data handling.

```
all_markets <- get_sinlge_variable(data$operational_capacities_by_technology, market)
if(!exists("selected_markets")){  selected_markets <- all_markets }
if(exists("custom_market_colors") | exists("market_color_palette")){
  market_colors <- set_colors(all_market, "custom_market_colors", "market_color_palette")
}
```


5. Add the filters to the plots, e.g. show_filters[["generation_total"]] <- c("technology", "market")
```
show_filters[["generation_total"]] <- c("technology", "market")
```
and in the plot function:
```

  data <- data %>%
    filter(technology %in% input$technologies_checked) %>% 
    filter(market %in% input$markets_checked) 
    
```


