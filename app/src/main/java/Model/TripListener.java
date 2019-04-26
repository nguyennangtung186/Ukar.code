package Model;

import org.json.JSONObject;

public interface TripListener {
    void onStartSuccess();
    void onFindReponse(JSONObject jsonObject);
    void onCancelFindReponse(JSONObject jsonObject);
    void onPartnerInfoReponse(JSONObject jsonObject);
    void onAcceptReponse(JSONObject jsonObject);
    void onFinishReponse(JSONObject jsonObject);
    void onCancelWaitReponse(JSONObject jsonObject);
    void onCancelReponse(JSONObject jsonObject);
}
