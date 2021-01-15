
ui_filtersidebar <- function(){
  
  column(width = 3,
    ui_filters$plotselection(),
    ui_filters$plotheight(),
    hr(),
    tags$label(tags = "control-label", "Filters"),
    tags$br(),
    
    # Filters
    ui_filters$iteration_range(),
    ui_filters$single_iterations(),
    ui_filters$market(),
    ui_filters$technology(),
    ui_filters$producer(),
    ui_filters$cashflow(),
    ui_filters$fuel(),
    ui_filters$segment(),
    # ui_filters$tick_expected(),
    # ui_filters$tick()
  )
  
}

ui_filters <- list()

# Standard filters ----------------------------------------------------------

ui_filters$iteration_range <- function(){
  
  conditionalPanel(
    condition = "output.hide_filter_iteration_range == false",
    
    box(title = "Iterations", width = 12, solidHeader = TRUE, 
        collapsible = TRUE, collapsed = TRUE,
        checkboxInput("iteration_average", "Show average", TRUE),
        sliderInput(
          "iterations",
          label = "Range",
          min = iteration_min, max = iteration_max,
          value = c(iteration_min, iteration_max))
    )
  )
  
}

ui_filters$single_iterations <- function(){
  
  conditionalPanel(
    condition = "output.show_filter_single_iteration == true",
    
    box(title = "Single Iteration", width = 12, solidHeader = TRUE, 
        collapsible = TRUE, collapsed = TRUE,
        sliderInput(
          "single_iteration", # TODO need to adjust iterations processing if sinlge value back.
          label = "Iteration",
          min = iteration_min, max = iteration_max,
          value = c(iteration_min))
    )
  )
}

ui_filters$market <- function(){
  conditionalPanel(
    condition = "output.show_filter_market == true",
    box(title = "Filter by Markets", width = 12, collapsible = TRUE, collapsed = TRUE, solidHeader = FALSE,
        checkboxGroupInput("markets_checked", label = "",
                           choices = all_markets,
                           selected = selected_markets))
  )
}  

ui_filters$technology <- function(){
  conditionalPanel(
    condition = "output.show_filter_technology == true",
    box(title = "Filter by Technologies", width = 12, collapsible = TRUE, collapsed = TRUE, solidHeader = FALSE,
        checkboxGroupInput("technologies_checked", label = "",
                           choices = all_technologies,
                           selected = selected_technologies))
  )
  
}

ui_filters$producer <- function(){
  conditionalPanel(
    condition = "output.show_filter_producer == true",
    box(title = "Filter by Producers", width = 12, collapsible = TRUE, collapsed = TRUE, solidHeader = FALSE,
        checkboxGroupInput("producers_checked", label = "",
                           choices = all_producers,
                           selected = selected_producers))
  )
}  


ui_filters$cashflow <- function(){
  conditionalPanel(
    condition = "output.show_filter_cashflow == true",
    box(title = "Filter by Cashflow", width = 12, collapsible = TRUE, collapsed = TRUE, solidHeader = FALSE,
        checkboxGroupInput("cashflows_checked", label = "",
                           choices = all_cashflows,
                           selected = selected_cashflows))
  )
}  

ui_filters$fuel <- function(){
  conditionalPanel(
    condition = "output.show_filter_fuel == true",
    box(title = "Filter by Fuels", width = 12, collapsible = TRUE, collapsed = TRUE, solidHeader = FALSE,
        checkboxGroupInput("fuels_checked", label = "",
                           choices = all_fuels,
                           selected = selected_fuels))
  )
}  

ui_filters$segment <- function(){
  conditionalPanel(
    condition = "output.show_filter_segment == true",
    box(title = "Filter by Segment", width = 12, collapsible = TRUE, collapsed = TRUE, solidHeader = FALSE,
        
        checkboxInput(
          inputId = "all_in_one_plot",
          label = "Segments in one plot",
          value = TRUE),
        checkboxInput(
          inputId = "flip_tick_segment",
          label = "Flip tick and segment",
          value = TRUE),
        checkboxGroupInput("segments_checked", label = "",
                           choices = all_segments,
                           selected = selected_segments))
  )
}  

ui_filters$tick_expected <- function(){
  conditionalPanel(
    condition = "output.show_filter_tick_expected == true",
    box(title = "Tick for Expectations", width = 12, collapsible = TRUE, collapsed = TRUE, solidHeader = FALSE,
        sliderInput(
          "tick_expected",
          label = "Tick",
          min = tick_expected_min, max = tick_expected_max,
          value = c(tick_expected_min, tick_expected_max))
    ))
}  

ui_filters$tick <- function(){
  
  conditionalPanel(
    #condition = "output.show_filter_tick == true",
    box(title = "Tick", width = 12, collapsible = TRUE, collapsed = TRUE, solidHeader = FALSE,
        sliderInput(
          "tick_checked",
          label = "Tick",
          min = tick_min, max = tick_max,
          value = tick_min)))
}

# Plot related controls ---------------------------------------------------

ui_filters$plotselection <- function(){
  
  selectInput(
    inputId = "single_plot_selected", 
    label = "Choose the plot to display", 
    choices = single_plot_select_names(names(plots))
  )
  
}

ui_filters$plotheight <- function(){
  sliderInput(
    "selected_single_plot_height",
    label = "Plot height",
    min = 200, max = 5000, step = 100,
    value = 500)
}



