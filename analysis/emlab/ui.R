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
        "Plots", tabName = "single_plots", icon = icon("lightbulb")
      ),
      menuItem(
        "Dashboard", tabName = "dashboard", icon = icon("dashboard")),
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
              
              column(width = 6,
                selectInput(inputId = "single_plot_selected", label = "Choose the plot to display", choices = names(plots))
              ),
              hr(),
              
              
                
              fluidRow(
                column(width = 9,
                       box(title = textOutput("selected_single_plot_title"), status = "primary", width = 12, solidHeader = TRUE,
                           plotOutput("selected_single_plot")#,
                           #plotlyOutput("selected_single_plotly")
                           
                           )
                ),
                
                column(width = 3,
                  box(title = "Iterations", width = 12, solidHeader = TRUE, collapsible = TRUE,collapsed = TRUE,
                      checkboxInput("iteration_average", "Show average", TRUE),
                      sliderInput(
                        "iterations",
                        label = "Range",
                        min = iteration_min, max = iteration_max,
                        value = c(iteration_min, iteration_max))
                  ),
                  
                  # Filters
                  
                  box(title = "Filter by Technologies", width = 12, collapsible = TRUE, collapsed = TRUE, solidHeader = FALSE,
                    checkboxGroupInput("technologies_checked", label = "",
                                       choices = all_technologies,
                                       selected = all_technologies)),
                  
                  box(title = "Filter by Producers", width = 12, collapsible = TRUE, collapsed = TRUE, solidHeader = FALSE,
                      checkboxGroupInput("producers_checked", label = "",
                                         choices = all_producers,
                                         selected = all_producers)),
                  
                  box(title = "Filter by Fuels", width = 12, collapsible = TRUE, collapsed = TRUE, solidHeader = FALSE,
                      checkboxGroupInput("fuels_checked", label = "",
                                         choices = all_fuels,
                                         selected = all_fuels)),
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
                                         selected = all_segments))
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