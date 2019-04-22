package Model;

import org.json.JSONObject;

public interface AccountListener {
    void onLoginStart();
    void onLoginSuccess(JSONObject jsonObject , String cookie);
    void onRegisterSuccess(JSONObject jsonObject);
}
