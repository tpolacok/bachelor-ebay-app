package tomaspolacok.bachelor.application.helper;

import javax.validation.constraints.NotEmpty;

import lombok.Data;

@Data
public class Email {
	
    @NotEmpty(message = "*Please provide email")
    private String email;
}
