package tomaspolacok.bachelor.application.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private SecurityHandler successHandler;

    @Value("${spring.queries.users-query}")
    private String usersQuery;

    @Value("${spring.queries.roles-query}")
    private String rolesQuery;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        	auth
                .jdbcAuthentication()
                .usersByUsernameQuery(usersQuery)
                .authoritiesByUsernameQuery("select u.email, 'USER' from app_user u where u.email=?")
                .dataSource(dataSource)
                .passwordEncoder(bCryptPasswordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

    	http
	        .authorizeRequests()
	        .antMatchers("/").permitAll()
	        .antMatchers("/login").permitAll()
	        .antMatchers("/privacy").permitAll()
	        .antMatchers("/registration").permitAll()
	        .antMatchers("/activate/**").permitAll()
	        .antMatchers("/user/password/**").permitAll()
	        .antMatchers("/search").hasAuthority("USER").anyRequest()
	        .authenticated().and().csrf().disable().formLogin()
	        .loginPage("/login").failureUrl("/login?error=true")
	        .defaultSuccessUrl("/search")
	        .usernameParameter("email")
	        .passwordParameter("password")
	        .successHandler(successHandler)
	        .and().logout()
	        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
	        .logoutSuccessUrl("/");
    	http
    		.headers()
    		.frameOptions()
    		.disable();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        	web
            	.ignoring()
                .antMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/images/**");
    }

}
