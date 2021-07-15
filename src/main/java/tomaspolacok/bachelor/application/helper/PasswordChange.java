package tomaspolacok.bachelor.application.helper;

import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;

import lombok.Data;

@Data
public class PasswordChange {

    @NotEmpty(message = "*Please provide your password")
    private String passwordOld;
	
	@Length(min = 5, message = "*Your new password must have at least 5 characters")
    @NotEmpty(message = "*Please provide new password")
    private String passwordNew;
	
	@Length(min = 5, message = "*Your new password must have at least 5 characters")
    @NotEmpty(message = "*Please provide new password")
    private String passwordNewSecond;
}
