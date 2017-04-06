package mx.infotec.smartcity.backend.controller.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import mx.infotec.smartcity.backend.model.TokenRecovery;
import mx.infotec.smartcity.backend.model.TokenRequest;
import mx.infotec.smartcity.backend.service.AdminUtilsService;
import mx.infotec.smartcity.backend.service.UserService;
import mx.infotec.smartcity.backend.service.exception.ServiceException;
import mx.infotec.smartcity.backend.service.keystone.pojo.createUser.CreateUser;
import mx.infotec.smartcity.backend.service.keystone.pojo.createUser.User_;
import mx.infotec.smartcity.backend.service.keystone.pojo.user.User;
import mx.infotec.smartcity.backend.service.recovery.TokenRecoveryService;
import mx.infotec.smartcity.backend.utils.Constants;
import mx.infotec.smartcity.backend.utils.TemplatesEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class RegisterController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterController.class);

  @Autowired
  private UserService          keystoneUserService;

  @Autowired
  private TokenRecoveryService recoveryService;

  @Autowired
  private AdminUtilsService    adminUtils;
  
  @RequestMapping(value = "/register", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<?> userRegistration(@RequestBody TokenRequest request) {
    try {
      User_ user = new User_();
      user.setPassword(new String(request.getPassword()));
      user.setName(request.getUsername());
      user.setEnabled(false);
      CreateUser createUser = new CreateUser(user);
      if (!keystoneUserService.isRegisteredUser(user.getName())
          && keystoneUserService.createUserAndSendMail(createUser, TemplatesEnum.MAIL_SAMPLE2)) {

        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Success");
      }
      return ResponseEntity.status(HttpStatus.CONFLICT).body("Registered User");
    } catch (ServiceException e) {
        LOGGER.error(null, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
    }
  }

  @RequestMapping(value = "/register/validation/{token}", method = RequestMethod.GET)
  public ResponseEntity<?> validateRegistration(@PathVariable("token") String token) {
    try {

      if (recoveryService.validateTokenRecovery(token)) {
        TokenRecovery recovery = recoveryService.getTokenById(token);
        String adminToken = adminUtils.getAdmintoken();
        User user = keystoneUserService.getUserByName(recovery.getEmail(), adminToken);
        user.setEnabled(true);
        CreateUser createUser = new CreateUser();
        User_ userToUpdate = new User_();
        userToUpdate.setEnabled(true);
        userToUpdate.setId(user.getId());
        createUser.setUser(userToUpdate);
        keystoneUserService.updateUser(user.getId(), adminToken, createUser);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Success");

      }
      return ResponseEntity.status(HttpStatus.CONFLICT).body("Registered User");
    } catch (ServiceException e) {
        LOGGER.error(null, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
    }
  }
  
  @RequestMapping(value = "/register/update-password", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<?> updatePassword(@RequestHeader(name = Constants.AUTH_TOKEN_HEADER) String token,
      @RequestBody mx.infotec.smartcity.backend.service.keystone.pojo.changePassword.User_ user) {
    try {
        if (keystoneUserService.changePassword(user, token)) {
          return ResponseEntity.status(HttpStatus.ACCEPTED).body("Success");
        } else {
          return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bad password"); //Nunca llega a este paso
        }
    } catch (ServiceException e) {
        LOGGER.error(null, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
    }
  }

}
