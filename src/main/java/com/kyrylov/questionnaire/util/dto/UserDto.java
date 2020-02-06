package com.kyrylov.questionnaire.util.dto;

import com.kyrylov.questionnaire.persistence.domain.entities.User;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class UserDto implements Cloneable {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String password;
    private boolean active;
    private String activationKey;

    public UserDto(User user) {
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.password = user.getPassword();
        this.active = user.getActive() != null ? user.getActive() : false;
        this.activationKey = user.getActivationKey();
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public UserDto clone() {
        UserDto userDto = new UserDto();
        userDto.setFirstName(this.getFirstName());
        userDto.setLastName(this.getLastName());
        userDto.setEmail(this.getEmail());
        userDto.setPassword(this.getPassword());
        userDto.setPhone(this.getPhone());
        userDto.setActive(this.isActive());
        userDto.setActivationKey(this.getActivationKey());
        return userDto;
    }
}
