package isetjb.soa.tpmicro.bffservice;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sert la console GraphiQL auto-hébergée (assets locaux) sur /graphiql.
 * L'auto-configuration Spring est désactivée dans application.properties
 * car sa page dépend de unpkg.com, qui renvoie 404 sur graphiql.min.js.
 */
@Configuration
public class GraphiQLPageConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/graphiql").setViewName("forward:/graphiql/index.html");
    }
}
