package com.medilabo.microfront.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.HiddenHttpMethodFilter;

/**
 * Web configuration class for setting up additional web-related beans.
 * This class provides the configuration for enabling HTTP method overrides
 * using the {@link HiddenHttpMethodFilter}.
 */
@Configuration
public class WebConfig {

    /**
     * Creates and configures a {@link HiddenHttpMethodFilter} bean.
     * This filter allows forms to use HTTP methods other than GET and POST (e.g., PUT or DELETE)
     * by using a hidden input field named "_method" in the form data. It enables support for
     * RESTful operations in HTML forms.
     *
     * @return a configured {@link HiddenHttpMethodFilter} instance
     */
    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }

}
