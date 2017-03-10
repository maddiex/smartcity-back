package mx.infotec.smartcity.backend.service.keystone.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import mx.infotec.smartcity.backend.service.keystone.KeystoneLoginServiceImpl;

/**
 *
 * @author Erik Valdivieso
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Response implements Serializable {

    private static final long serialVersionUID = 1L;

    private Token token;

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }
}