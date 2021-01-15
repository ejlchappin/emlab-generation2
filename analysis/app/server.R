server <- function(input, output) {
  
  
  observeEvent(input$toggle_shared_options, {
    toggle("shared_filter_panel")
  })
  
  unit_factor <- reactive({
    available_units %>%
      filter(prefix == input$unit_prefix) %>%
      pull(factor)
  })
  assign("unit_factor", unit_factor, envir = .GlobalEnv)
  
  all_plots <- names(plots)
  
  # for each plot in plots[] produce plots of the namescheme plot_(data_name)_(average/by_iterations)
  map(all_plots, function(plot_name){
    
    # get average of all iterations plot
  
    observe({
      output$selected_single_plot <- renderPlot(
        expr = {get_plot_filtered(input$single_plot_selected, input, input$iteration_average)}, 
        height = input$selected_single_plot_height)
    })
    
    output$selected_single_plot_title <- renderText({
      input$single_plot_selected %>% 
        variable_name_to_title()
    })
    
    # # get by iterations plot
    # output[[paste("plot", plot_name, "by_iterations", sep = "_")]] <- renderPlot({
    #   get_plot_filtered(plot_name, input, average = FALSE)
    # })
  })
  
  ## Log files and value boxes
  
  if(config_params[["analyse_log"]]){
    log_table <- DT::datatable(emlab_log,  filter = list(position = 'top', clear = FALSE), options = list(scrollX = T))
  } else {
    log_table <- tibble(info = "Logs analysis not activated in config.R.")
 }
  
  output$dt_log_table = DT::renderDataTable({
    log_table
  })
  
  toggle_filters <- function(filter_name, selected_plot){
    if(selected_plot %in% names(show_filters)){
      (filter_name %in% show_filters[[selected_plot]])
    } else {
      # If not defined show anyway
      TRUE
    }
  }
  
  ## Custom filters
  
  ### Filter for market
  output$show_filter_market <- reactive({
    toggle_filters("market",input$single_plot_selected)
  })
  outputOptions(output, "show_filter_market", suspendWhenHidden = FALSE)  
  
  ### Filter for technology
  output$show_filter_technology <- reactive({
    toggle_filters("technology",input$single_plot_selected)
  })
  outputOptions(output, "show_filter_technology", suspendWhenHidden = FALSE)  
  
  ### Filter for producer
  output$show_filter_producer <- reactive({
    toggle_filters("producer",input$single_plot_selected)
  })
  outputOptions(output, "show_filter_producer", suspendWhenHidden = FALSE)  
  
  ### Filter for cashflow
  output$show_filter_cashflow <- reactive({
    toggle_filters("cashflow",input$single_plot_selected)
  })
  outputOptions(output, "show_filter_cashflow", suspendWhenHidden = FALSE)
  
  ### Filter for fuel
  output$show_filter_fuel <- reactive({
    toggle_filters("fuel",input$single_plot_selected)
  })
  outputOptions(output, "show_filter_fuel", suspendWhenHidden = FALSE)  
  
  ### Filter for segment
  output$show_filter_segment <- reactive({
    toggle_filters("segment",input$single_plot_selected)
  })
  outputOptions(output, "show_filter_segment", suspendWhenHidden = FALSE)  
  
  ### Filter for tick_expected
  output$show_filter_tick_expected <- reactive({
    toggle_filters("tick_expected",input$single_plot_selected)
  })
  outputOptions(output, "show_filter_tick_expected", suspendWhenHidden = FALSE)  
  
  ### Filter for tick
  output$show_filter_tick <- reactive({
    toggle_filters("tick_filter",input$single_plot_selected)
  })
  outputOptions(output, "show_filter_tick", suspendWhenHidden = FALSE)  
  
  ### Filter for single iteration
  output$show_filter_single_iteration <- reactive({
    toggle_filters("iterations",input$single_plot_selected)
  })
  outputOptions(output, "show_filter_single_iteration", suspendWhenHidden = FALSE)  
  
  ### Filter for range
  output$hide_filter_iteration_range <- reactive({
    toggle_filters("hide_iterations_range",input$single_plot_selected)
  })
  
  outputOptions(output, "hide_filter_iteration_range", suspendWhenHidden = FALSE)  
  
  
  
  # Logic for saving data
  
  scenario_descriptions_title <- reactiveVal(
    ifelse(identical(scenario_descriptions_initial_name, character(0)), prefix, scenario_descriptions_initial_name)
  )
      
  output$scenario_descriptions_title <- renderText({
    paste("Loaded Scenario:", scenario_descriptions_title())
  })

  
  observeEvent(input$submit, {

    save_to_description_file(
      file = description_file,
      prefix = prefix,
      name = input[["file_scenario_name"]],
      caption = input[["file_scenario_caption"]])
    
   scenario_descriptions_title(input[["file_scenario_name"]])
    
    
  })
  
  
  output$current_scenario_title <- renderText({
    paste("Loaded scenario:", if_else(scenario_descriptions_current_name == "", prefix, scenario_descriptions_current_name))
  })
  
  


  
  
  
  
}
