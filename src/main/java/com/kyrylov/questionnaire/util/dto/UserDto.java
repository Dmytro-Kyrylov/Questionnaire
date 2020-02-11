package com.kyrylov.questionnaire.util.dto;

import com.kyrylov.questionnaire.persistence.domain.entities.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO class for user entity
 * {@link com.kyrylov.questionnaire.persistence.domain.entities.User}
 *
 * @author Dmitrii
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserDto implements IDto, Cloneable {

    private static final long serialVersionUID = -8742176469880025594L;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String password;
    private Boolean active;
    private String activationKey;

    public UserDto(User user) {
        this(user.getFirstName(), user.getLastName(), user.getEmail(), user.getPhone(),
                user.getPassword(), user.getActive(), user.getActivationKey());
    }

    @Override
    public UserDto clone() throws CloneNotSupportedException {
        return (UserDto) super.clone();
    }
}
