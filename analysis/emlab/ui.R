library(shinydashboard)

ui <- dashboardPage(
  dashboardHeader(
    title = if_else(exists("app_title"), app_title, "EMLab2"),
    dropdownMenu(type = "notifications",icon = icon("info-circle"),
                 notificationItem(
                   text = paste("File:", prefix),
                   icon("file")
                 )
    )
  ),
  dashboardSidebar(
    sidebarMenu(
      
     
      menuItem(
        "Single plots", tabName = "single_plots", icon = icon("lightbulb")
      ),
      menuItem(
        "Custom pages", tabName = "dashboard", icon = icon("dashboard")),
      menuItem(
        "Logs", tabName = "logs", icon = icon("file-alt")),
      menuItem(
        "Data", tabName = "data", icon = icon("list-ol")),
      
      radioButtons("unit_prefix", "Global unit:",all_unit_prefixes, inline = TRUE)

    )
  ),
  dashboardBody(
    
    tabItems(
      tabItem(tabName = "dashboard",
              h2("Dashboard tab content"),
              p("The plan is to enable user to customise this dashboard with content"),
              fluidRow(
                box(title = "Box title", "Box content"),
                box(status = "warning", "Box content")
              )
      ),
      tabItem(tabName = "logs",
              h2("Logfile"),
              DT::dataTableOutput("dt_log_table")
      ),
      
      tabItem(tabName = "single_plots",
              
              h2("Single plots"),
              hr(),
              

              fluidRow(
                column(width = 9,
                       box(title = textOutput("selected_single_plot_title"), status = "primary", width = 12, height = "100%", solidHeader = TRUE,
                           plotOutput("selected_single_plot", height = "100%")#,
                           #plotlyOutput("selected_single_plotly")
                           
                           )
                ),
                
                column(width = 3,
                       selectInput(
                         inputId = "single_plot_selected", 
                         label = "Choose the plot to display", 
                         choices = single_plot_select_names(names(plots))
                       ),
                       
                   sliderInput(
                     "selected_single_plot_height",
                     label = "Plot height",
                     min = 200, max = 2000, step = 100,
                     value = 500),
                   
                   hr(),
                   tags$label(tags = "control-label", "Filters"),
                   tags$br(),
                         
                    box(title = "Iterations", width = 12, solidHeader = TRUE, collapsible = TRUE,collapsed = TRUE,
                        checkboxInput("iteration_average", "Show average", TRUE),
                        sliderInput(
                          "iterations",
                          label = "Range",
                          min = iteration_min, max = iteration_max,
                          value = c(iteration_min, iteration_max))
                    ),
                    
                    # Filters
                    
                    conditionalPanel(
                      condition = "output.show_filter_technology == true",
                      box(title = "Filter by Technologies", width = 12, collapsible = TRUE, collapsed = TRUE, solidHeader = FALSE,
                          checkboxGroupInput("technologies_checked", label = "",
                                             choices = all_technologies,
                                             selected = selected_technologies))
                    ),
                    
                    conditionalPanel(
                      condition = "output.show_filter_producer == true",
                      box(title = "Filter by Producers", width = 12, collapsible = TRUE, collapsed = TRUE, solidHeader = FALSE,
                          checkboxGroupInput("producers_checked", label = "",
                                             choices = all_producers,
                                             selected = selected_producers))
                    ),
                    
                    conditionalPanel(
                      condition = "output.show_filter_fuel == true",
                      box(title = "Filter by Fuels", width = 12, collapsible = TRUE, collapsed = TRUE, solidHeader = FALSE,
                          checkboxGroupInput("fuels_checked", label = "",
                                             choices = all_fuels,
                                             selected = selected_fuels))
                    ),
                    
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
                    ),
                    
                    conditionalPanel(
                      condition = "output.show_filter_tick_expected == true",
                      box(title = "Tick for Expectations", width = 12, collapsible = TRUE, collapsed = TRUE, solidHeader = FALSE,
                          sliderInput(
                            "tick_expected",
                            label = "Tick",
                            min = tick_expected_min, max = tick_expected_max,
                            value = c(tick_expected_min, tick_expected_max))))
                    )

            )
              
      ),
      
      tabItem(tabName = "data",
              h2("Data"),
              box(title = "Box title", status = "warning", "TODO: Show data")
      
      )
    )
  )
)