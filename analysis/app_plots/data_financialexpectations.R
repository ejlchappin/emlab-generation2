
# Load data ---------------------------------------------------------------
raw_financialexpectations_results <- read_emlab_results(
  files_to_analyse$reporters, "FinancialExpectation.csv", 
  custom_col_types = cols(.default = "n", producer = "c", market = "c", technology = "c", plant = "c", node = "c"))

# set some filters
tick_min <- min(raw_financialexpectations_results$tick)
tick_max <- max(raw_financialexpectations_results$tick)

# columns to include in every data obtained by (get_data_by_prefix)
meta_cols <- c("iteration", "tick", "market", "producer", "technology", "node", "plant", "investmentRound")

# Expected ROE ----------------------------------------------------

if(process_data("expected_ROEs")){
  
  data[["expected_ROEs"]] <- raw_financialexpectations_results %>% 
    get_vars_from_multiple_columns(prefix = "ROE",vars = "type", value = "ROE")
    #get_var_from_single_column(prefix = NULL, value = "ROE")
  
  show_filters[["expected_ROEs"]] <- c("producer", "technology")
  
  
  plots[["expected_ROEs"]] <- function(data, input, average = TRUE){
    
    data <- data %>% 
      filter(
        producer %in% input$producers_checked,
        technology %in% input$technologies_checked) %>% 
      filter(iteration == 1) %>% # TODO
      ggplot(mapping = aes(x = ROE)) +
        geom_histogram(mapping = aes(fill = producer, lty = technology, color = type) , lwd = 1) +
        facet_grid(tick ~ market, labeller = label_both) +
    
        scale_x_continuous(labels = scales::percent) +
        #scale_x_log10(labels = scales::percent) +
        scale_fill_custom("producer_colors") +
        labs_default(
          y = glue("Number of occurences"),
          x = "Expected ROE",
          title = get_title_of_selected_plot(input),
          subtitle = "Showing only iteration 1 (TODO)",
          fill = "Energy producer"
          )
  }
  
}

# Expected ROE per round ----------------------------------------------------
  
if(process_data("expected_ROEs_per_round")){
      
  
  data[["expected_ROEs_per_round"]] <- data[["expected_ROEs"]]
  
  
  show_filters[["expected_ROEs_per_round"]] <- c("producer", "technology", "tick_filter")
  
  
  plots[["expected_ROEs_per_round"]] <- function(data, input, average = TRUE){
    
    #browser()
    
    data <- data %>% 
      filter(
        producer %in% input$producers_checked,
        technology %in% input$technologies_checked,
        tick == 4) %>% 
      filter(iteration == 1) %>% # TODO
      mutate(
        round = as_factor(investmentRound)
      ) %>% 
      group_by(round, market, technology) %>% 
        ggplot(mapping = aes(x = ROE)) +
        geom_histogram(mapping = aes(fill = technology)) +
        facet_grid(investmentRound ~ market) +
      
      #scale_x_continuous(labels = scales::percent) +
      scale_x_log10(labels = scales::percent) +
      scale_fill_custom("technology_colors") +
      labs_default(
        y = glue("Number of occurences"),
        x = "Expected ROE",
        title = get_title_of_selected_plot(input),
        subtitle = "Showing only iteration 1 (TODO)",
        fill = "Energy producer"
      )
  }
  
}

# Expected expected_ROEs_mapped_vs_modelled ----------------------------------------------------

if(process_data("expected_ROEs_mapped_vs_modelled")){
    
  data[["expected_ROEs_mapped_vs_modelled"]] <- data[["expected_ROEs"]]
  
  
  show_filters[["expected_ROEs_mapped_vs_modelled"]] <- c("producer", "technology", "tick_filter", "iterations", "hide_iterations_range")
  
  base_breaks <- function(n = 10){
    function(x) {
      axisTicks(log10(range(x, na.rm = TRUE)), log = TRUE, n = n)
    }
  }
  
  plots[["expected_ROEs_mapped_vs_modelled"]] <- function(data, input, average = FALSE){
    
  
    data <- data %>% 
      filter(
        producer %in% input$producers_checked,
        technology %in% input$technologies_checked,
        iteration == input$single_iteration
        ) %>% 
      mutate(
        type = fct_rev(type)
      ) %>% 
      ggplot(mapping = aes(y = ROE, x = tick)) +
        geom_point(mapping = aes(colour = technology)) +
        facet_grid(market ~ type, labeller = label_both) +
        scale_y_continuous(labels = scales::percent) +
        # 
        # scale_y_log10(
        #   labels = scales::percent,
        #   #limits = c(0.1/100,1e3/100)
        #   ) +
        labs_default(
          y = glue("Expected ROE"),
          x = "Tick (Year)",
          title = get_title_of_selected_plot(input),
          subtitle = glue("Showing iteration {input$single_iteration}"),
          fill = "Technology"
        )
  }
  
}



# LCOEs ----------------------------------------------------

if(process_data("expected_lcoes")){
  
  data[["expected_lcoes"]] <- raw_financialexpectations_results %>% 
    get_var_from_single_column(prefix = NULL, value = "lcoe")
  
  #-130405515  €/MWh
  
  
  show_filters[["expected_lcoes"]] <- c("producer", "technology")
  
  
  plots[["expected_lcoes"]] <- function(data, input, average = TRUE){
    
    data <- data %>% 
      filter(
        producer %in% input$producers_checked,
        technology %in% input$technologies_checked) %>% 
      filter(iteration == 1) %>% # TODO
      ggplot(mapping = aes(x = lcoe)) +
      geom_histogram(mapping = aes(fill = producer, lty = technology) , lwd = 1) +
      facet_grid(tick ~ market, labeller = label_both) +
      
      scale_x_continuous(labels = scales::percent) +
      #scale_x_log10(labels = scales::percent) +
      scale_fill_custom("producer_colors") +
      labs_default(
        y = glue("Number of occurences"),
        x = "Expected ROE",
        title = get_title_of_selected_plot(input),
        subtitle = "Showing only iteration 1 (TODO)",
        fill = "Energy producer"
      )
  }
  
}