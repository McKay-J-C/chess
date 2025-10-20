package service;


import dataaccess.MemUserDAO;
import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;
import request.*;
import response.*;
import static service.UserService.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceTests {

    @Test
    @Order(1)
    @DisplayName("Successful Registration")
    public void successfulRegistration() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "Bob", "goCougs27", "mjc2021@byu.edu");
        RegisterResponse foundResponse =  register(registerRequest);
        UserData foundUser = MemUserDAO.getUser("Bob");
        UserData testUser = new UserData("Bob", "goCougs27", "mjc2021@byu.edu");
        Assertions.assertEquals(testUser, foundUser);

        AuthData testResponse = new AuthData("", "Bob");
        Assertions.assertEquals(testResponse.username(), foundResponse.username());
    }

    @Test
    @Order(2)
    @DisplayName("Taken Username Error Registration")
    public void usernameTakenRegistration() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "Bob", "goCougs27", "mjc2021@byu.edu");
        register(registerRequest);
        RegisterRequest newRegisterRequest = new RegisterRequest(
                "Bob", "hello", "goBYU@byu.edu");
        Assertions.assertThrows(DataAccessException.AlreadyTakenException.class, () -> register(newRegisterRequest));
    }
}
